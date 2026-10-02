package com.example.domain.ai.engine

import com.example.domain.ai.formatter.ResponseFormatter
import com.example.domain.ai.lifecycle.ModelManager
import com.example.domain.ai.model.AIEngineStatus
import com.example.domain.ai.model.AIInferenceRequest
import com.example.domain.ai.model.AIInferenceResponse
import com.example.domain.ai.model.AITaskType
import com.example.domain.ai.model.ModelState
import com.example.domain.ai.parser.ParseResult
import com.example.domain.ai.parser.StructuredOutputParser
import com.example.domain.ai.prompt.PromptBuilder
import com.example.domain.ai.runtime.FallbackInferenceRuntime
import com.example.domain.ai.runtime.InferenceRuntime
import com.example.domain.ai.runtime.RawInferenceInput
import com.example.domain.ai.tokenizer.OfflineRuleBasedTokenizer
import com.example.domain.ai.tokenizer.Tokenizer
import com.example.domain.ai.validation.ValidationLayer
import com.example.domain.ai.validation.ValidationResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Unified interface for the local offline AI engine.
 */
interface OfflineAIEngine {
    val modelManager: ModelManager
    val inferenceRuntime: InferenceRuntime
    val tokenizer: Tokenizer

    suspend fun executeInference(request: AIInferenceRequest): AIInferenceResponse
    suspend fun getEngineStatus(): AIEngineStatus
}

/**
 * Production implementation of OfflineAIEngine.
 * - 100% offline: Never initiates or requires any network connections.
 * - Executes all inference pipeline stages on a background coroutine dispatcher.
 * - Handles missing models seamlessly with deterministic biomechanical fallbacks.
 * - Ready to swap backend runtimes (e.g. MediaPipe GenAI, LiteRT, ONNX) without refactoring caller layers.
 */
class DefaultOfflineAIEngine(
    override val inferenceRuntime: InferenceRuntime = FallbackInferenceRuntime(),
    override val tokenizer: Tokenizer = OfflineRuleBasedTokenizer(),
    expectedModelPath: String? = null,
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default
) : OfflineAIEngine {

    override val modelManager = ModelManager(
        expectedModelPath = expectedModelPath,
        inferenceRuntime = inferenceRuntime,
        dispatcher = backgroundDispatcher
    )

    /**
     * Executes the end-to-end local offline inference pipeline on the background dispatcher:
     * 1. Check/load model state (or engage safe fallback)
     * 2. Build structured prompt (PromptBuilder)
     * 3. Tokenize input (Tokenizer)
     * 4. Execute on-device forward inference (InferenceRuntime)
     * 5. Parse output into strongly-typed DTO (StructuredOutputParser)
     * 6. Biomechanical validation & sanitization (ValidationLayer)
     * 7. Format user presentation in Persian/English (ResponseFormatter)
     */
    override suspend fun executeInference(request: AIInferenceRequest): AIInferenceResponse =
        withContext(backgroundDispatcher) {
            val startTime = System.currentTimeMillis()
            val requestId = UUID.randomUUID().toString()

            // Step 1: Ensure runtime is ready or fallback engaged
            if (!modelManager.isModelLoaded() && !modelManager.isFallbackActive()) {
                modelManager.loadModel()
            }

            // Step 2: Build domain-specific prompt
            val prompt = PromptBuilder.buildPrompt(request)

            // Step 3: Tokenize
            val tokenIds = tokenizer.encode(prompt)

            // Step 4: Run inference (off main thread)
            val rawOutput = inferenceRuntime.runInference(
                RawInferenceInput(
                    prompt = prompt,
                    tokenIds = tokenIds,
                    maxTokens = request.maxTokens,
                    temperature = request.temperature
                )
            )

            // Step 5: Parse structured output
            val parseResult = StructuredOutputParser.parse(rawOutput.rawText, request.taskType)

            val rawStructured = when (parseResult) {
                is ParseResult.Success -> parseResult.output
                is ParseResult.Error -> {
                    // Parser recovery: re-run through fallback heuristic parser
                    val fallbackRaw = FallbackInferenceRuntime().runInference(
                        RawInferenceInput(prompt = request.taskType.name, tokenIds = emptyList())
                    )
                    (StructuredOutputParser.parse(fallbackRaw.rawText, request.taskType) as? ParseResult.Success)?.output
                        ?: throw IllegalStateException("Fallback parsing failed: ${parseResult.message}")
                }
            }

            // Step 6: Validate and sanitize against safety rules
            val hasShoulderPain = request.painAreas.any { it.contains("شانه", ignoreCase = true) || it.contains("shoulder", ignoreCase = true) }
            val validation = ValidationLayer.validate(
                output = rawStructured,
                userHasActiveShoulderPain = hasShoulderPain,
                availableEquipment = request.availableEquipment
            )

            val validatedOutput = when (validation) {
                is ValidationResult.Valid -> validation.sanitizedOutput
                is ValidationResult.Invalid -> validation.fallbackOutput
            }

            // Step 7: Localized Presentation Formatting
            val presentation = ResponseFormatter.format(validatedOutput)

            val elapsed = System.currentTimeMillis() - startTime

            AIInferenceResponse(
                requestId = requestId,
                taskType = request.taskType,
                structuredData = validatedOutput,
                formattedResponsePersian = presentation.persianText,
                formattedResponseEnglish = presentation.englishText,
                isFallback = rawOutput.isFallbackGenerated || modelManager.isFallbackActive(),
                inferenceTimeMs = elapsed,
                modelVersionUsed = modelManager.getActiveModelInfo()?.version ?: "fallback-1.0"
            )
        }

    override suspend fun getEngineStatus(): AIEngineStatus = withContext(backgroundDispatcher) {
        val state = modelManager.modelState.value
        val isFallback = modelManager.isFallbackActive() || state is ModelState.Unavailable
        val activeInfo = modelManager.getActiveModelInfo()

        AIEngineStatus(
            modelState = state,
            isOperational = true,
            isUsingFallback = isFallback,
            activeRuntimeName = inferenceRuntime.runtimeName,
            contextWindowTokens = activeInfo?.contextWindowTokens ?: 2048,
            offlineVerified = true
        )
    }
}
