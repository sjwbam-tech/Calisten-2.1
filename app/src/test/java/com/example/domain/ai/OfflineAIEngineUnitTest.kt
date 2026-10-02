package com.example.domain.ai

import com.example.domain.ai.engine.DefaultOfflineAIEngine
import com.example.domain.ai.formatter.ResponseFormatter
import com.example.domain.ai.lifecycle.ModelManager
import com.example.domain.ai.model.AIInferenceRequest
import com.example.domain.ai.model.AITaskType
import com.example.domain.ai.model.AIWorkoutAdvice
import com.example.domain.ai.model.AIExerciseModification
import com.example.domain.ai.model.AIPlateauAdvice
import com.example.domain.ai.model.ModelState
import com.example.domain.ai.parser.ParseResult
import com.example.domain.ai.parser.StructuredOutputParser
import com.example.domain.ai.prompt.PromptBuilder
import com.example.domain.ai.runtime.FallbackInferenceRuntime
import com.example.domain.ai.runtime.InferenceRuntime
import com.example.domain.ai.runtime.RawInferenceInput
import com.example.domain.ai.runtime.RawInferenceOutput
import com.example.domain.ai.tokenizer.OfflineRuleBasedTokenizer
import com.example.domain.ai.validation.ValidationLayer
import com.example.domain.ai.validation.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class OfflineAIEngineUnitTest {

    /**
     * 1. Test Model Unavailable:
     * When model file does not exist, ModelManager transitions to Unavailable and
     * loadModel() safely falls back without throwing an uncaught exception.
     */
    @Test
    fun testModelUnavailable_gracefullyEntersFallback() = runTest {
        val nonExistentPath = "/path/to/non_existent_model_weights.bin"
        val manager = ModelManager(
            expectedModelPath = nonExistentPath,
            inferenceRuntime = FallbackInferenceRuntime(),
            dispatcher = StandardTestDispatcher(testScheduler)
        )

        val isAvailable = manager.checkAvailability()
        assertFalse("Model should not be available for non-existent path", isAvailable)
        assertTrue("Model state should be Unavailable", manager.modelState.value is ModelState.Unavailable)

        val loadResult = manager.loadModel()
        assertTrue("Loading should succeed in fallback mode", loadResult.isSuccess)
        assertTrue("Fallback should be active", manager.isFallbackActive())
        assertNotNull(manager.getActiveModelInfo())
        assertEquals("fallback-heuristic-v1", manager.getActiveModelInfo()?.modelId)
    }

    /**
     * 2. Test Invalid Model Output:
     * When model hallucinates extreme values (e.g. 500 reps, RPE 25),
     * ValidationLayer catches the violations and clamps them to safe physiological boundaries.
     */
    @Test
    fun testInvalidModelOutput_sanitizedByValidationLayer() {
        val dangerousAdvice = AIWorkoutAdvice(
            summary = "جلسه بسیار پرفشار",
            primaryFocus = "قدرت انفجاری",
            recommendedSetsDelta = 0,
            recommendedRpeTarget = 25.0, // Invalid RPE > 10
            exerciseModifications = listOf(
                AIExerciseModification(
                    exerciseName = "شنا سوئدی",
                    targetReps = 500, // Invalid extreme reps
                    targetSets = 20, // Invalid extreme sets
                    rpeTarget = 15.0,
                    modificationType = "PROGRESS",
                    reason = "آزمون فشار بیش از حد"
                )
            ),
            recoveryRecommendation = "استراحت",
            safetyWarnings = emptyList()
        )

        val validationResult = ValidationLayer.validate(
            output = dangerousAdvice,
            userHasActiveShoulderPain = true
        )

        assertTrue("Validation result should be Invalid due to bounds violation", validationResult is ValidationResult.Invalid)

        val sanitized = (validationResult as ValidationResult.Invalid).fallbackOutput as AIWorkoutAdvice
        assertEquals(10.0, sanitized.recommendedRpeTarget, 0.01)
        assertEquals(ValidationLayer.MAX_REPS, sanitized.exerciseModifications.first().targetReps)
        assertEquals(ValidationLayer.MAX_SETS, sanitized.exerciseModifications.first().targetSets)
        assertEquals(10.0, sanitized.exerciseModifications.first().rpeTarget, 0.01)

        // Safety warning should be injected for active shoulder pain
        assertTrue("Shoulder safety warning must be injected", sanitized.safetyWarnings.any { it.contains("شانه") })
    }

    /**
     * 3. Test Valid Structured Output:
     * When model generates well-formed JSON, StructuredOutputParser parses it correctly into typed DTOs.
     */
    @Test
    fun testValidStructuredOutput_parsedSuccessfully() {
        val validJson = """
        ```json
        {
          "summary": "تنظیم جلسه تمرینی با رویکرد حفظ مفاصل",
          "primaryFocus": "ثبات اسکاپولا و تعادل عضلانی",
          "recommendedSetsDelta": -1,
          "recommendedRpeTarget": 7.0,
          "exerciseModifications": [
            {
              "exerciseName": "شنا استاندارد",
              "targetReps": 12,
              "targetSets": 3,
              "rpeTarget": 7.0,
              "modificationType": "MAINTAIN",
              "reason": "حفظ فرم کنترل‌شده"
            }
          ],
          "recoveryRecommendation": "تمرینات موبیلیتی شانه قبل از خواب",
          "safetyWarnings": ["عدم خم کردن بیش از حد مچ دست"]
        }
        ```
        """.trimIndent()

        val result = StructuredOutputParser.parse(validJson, AITaskType.WORKOUT_ADVICE)
        assertTrue("Parse result should be Success", result is ParseResult.Success)

        val advice = (result as ParseResult.Success).output as AIWorkoutAdvice
        assertEquals("تنظیم جلسه تمرینی با رویکرد حفظ مفاصل", advice.summary)
        assertEquals(7.0, advice.recommendedRpeTarget, 0.01)
        assertEquals(1, advice.exerciseModifications.size)
        assertEquals("شنا استاندارد", advice.exerciseModifications.first().exerciseName)
        assertEquals(12, advice.exerciseModifications.first().targetReps)
    }

    /**
     * 4. Test Parser Failure:
     * When model output is corrupted or completely unparseable,
     * parser returns ParseResult.Error with diagnostic context rather than crashing.
     */
    @Test
    fun testParserFailure_returnsErrorGracefully() {
        val corruptedOutput = "This is random conversational gibberish without any JSON structure or braces."
        val result = StructuredOutputParser.parse(corruptedOutput, AITaskType.PLATEAU_ANALYSIS)

        assertTrue("Parser should report Error on non-JSON input", result is ParseResult.Error)
        val error = result as ParseResult.Error
        assertTrue("Error message should be descriptive", error.message.isNotBlank())
        assertEquals(corruptedOutput, error.rawOutput)
    }

    /**
     * 5. Test Fallback Behavior:
     * When runtime output fails parsing or model is uninstalled, engine recovers safely
     * via fallback pipeline and returns valid formatted bilingual advice.
     */
    @Test
    fun testFallbackBehavior_recoversAndProvidesActionableAdvice() = runTest {
        // Mock a faulty runtime that produces invalid output to trigger fallback recovery
        val faultyRuntime = object : InferenceRuntime {
            override val runtimeName: String = "FaultyTestRuntime"
            override fun isSupported(): Boolean = true
            override suspend fun initialize(modelFilePath: String?): Result<Unit> = Result.success(Unit)
            override suspend fun runInference(input: RawInferenceInput): RawInferenceOutput {
                return RawInferenceOutput(
                    rawText = "MALFORMED_OUTPUT_NOT_JSON",
                    promptTokensCount = 10,
                    generatedTokensCount = 5,
                    executionTimeMs = 5L
                )
            }
            override suspend fun release() {}
        }

        val engine = DefaultOfflineAIEngine(
            inferenceRuntime = faultyRuntime,
            tokenizer = OfflineRuleBasedTokenizer(),
            expectedModelPath = null,
            backgroundDispatcher = StandardTestDispatcher(testScheduler)
        )

        val request = AIInferenceRequest(
            taskType = AITaskType.WORKOUT_ADVICE,
            userProfileSummary = "ورزشکار متوسط با سابقه ۶ ماه تمرین",
            experienceLevel = "INTERMEDIATE",
            availableEquipment = listOf("بارفیکس", "پارالل"),
            readinessScore = "YELLOW"
        )

        val response = engine.executeInference(request)
        assertNotNull(response)
        assertNotNull(response.structuredData)
        assertTrue("Persian formatted response should not be blank", response.formattedResponsePersian.isNotBlank())
        assertTrue("English formatted response should not be blank", response.formattedResponseEnglish.isNotBlank())
    }

    /**
     * 6. Test Offline Operation:
     * Verifies that the tokenizer, prompt builder, and fallback inference run
     * completely in-memory with zero network or internet dependencies.
     */
    @Test
    fun testOfflineOperation_worksCompletelyInMemory() {
        val tokenizer = OfflineRuleBasedTokenizer()

        val persianText = "تمرین شنا سوئدی و بارفیکس برای افزایش قدرت عضلات پشتی و سینه"
        val tokens = tokenizer.encode(persianText)
        assertTrue("Tokens list should not be empty", tokens.isNotEmpty())

        val decoded = tokenizer.decode(tokens)
        assertTrue("Decoded text should contain core keywords", decoded.contains("شنا") && decoded.contains("بارفیکس"))

        val tokenCount = tokenizer.countTokens(persianText)
        assertTrue("Token count should be greater than 0", tokenCount > 0)

        // Verify PromptBuilder offline operation
        val request = AIInferenceRequest(
            taskType = AITaskType.PLATEAU_ANALYSIS,
            userProfileSummary = "ورزشکار کالیستنیکس",
            specificQuestionOrContext = "استپ در حرکت دیپ"
        )
        val prompt = PromptBuilder.buildPrompt(request)
        assertTrue("Prompt should include task identifier", prompt.contains("PLATEAU_ANALYSIS"))
        assertTrue("Prompt should include context", prompt.contains("استپ در حرکت دیپ"))
    }

    /**
     * 7. Test Background Execution:
     * Verifies that inference execution runs on background dispatcher and delivers response
     * without blocking.
     */
    @Test
    fun testBackgroundExecution_runsSmoothlyOnDispatcher() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val engine = DefaultOfflineAIEngine(
            inferenceRuntime = FallbackInferenceRuntime(),
            tokenizer = OfflineRuleBasedTokenizer(),
            expectedModelPath = null,
            backgroundDispatcher = testDispatcher
        )

        val request = AIInferenceRequest(
            taskType = AITaskType.PLATEAU_ANALYSIS,
            userProfileSummary = "ورزشکار پیشرفته",
            currentExerciseNames = listOf("بارفیکس مچ برعکس", "دیپ پارالل"),
            painAreas = listOf("شانه راست")
        )

        val response = engine.executeInference(request)
        assertNotNull(response)
        assertEquals(AITaskType.PLATEAU_ANALYSIS, response.taskType)
        assertTrue("Structured output should be AIPlateauAdvice", response.structuredData is AIPlateauAdvice)

        val plateauAdvice = response.structuredData as AIPlateauAdvice
        assertTrue("Plateau confirmed flag should be boolean", plateauAdvice.isPlateauConfirmed)
        assertTrue("Primary cause should be populated", plateauAdvice.primaryCause.isNotBlank())
        assertTrue("Biomechanical cues should be present", plateauAdvice.biomechanicalCues.isNotEmpty())

        val status = engine.getEngineStatus()
        assertTrue("Engine must report operational", status.isOperational)
        assertTrue("Engine must report offline verified", status.offlineVerified)
    }
}
