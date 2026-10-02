package com.example.domain.ai.runtime

import com.example.domain.ai.model.AIInferenceRequest
import com.example.domain.ai.model.AITaskType

/**
 * Low-level input passed directly to the inference backend.
 */
data class RawInferenceInput(
    val prompt: String,
    val tokenIds: List<Int>,
    val maxTokens: Int = 512,
    val temperature: Float = 0.2f,
    val stopTokens: List<String> = listOf("<|im_end|>", "```\n")
)

/**
 * Output returned directly by an inference runtime.
 */
data class RawInferenceOutput(
    val rawText: String,
    val tokenIds: List<Int> = emptyList(),
    val promptTokensCount: Int,
    val generatedTokensCount: Int,
    val executionTimeMs: Long,
    val isFallbackGenerated: Boolean = false
)

/**
 * Abstraction for on-device local model inference runtimes.
 * Allows replacing backend with LiteRT / TFLite / ONNX / MediaPipe GenAI / GGUF in the future
 * without changing the domain logic.
 */
interface InferenceRuntime {
    /**
     * Human-readable runtime name (e.g. "FallbackHeuristicRuntime", "LiteRT-Gemma-2B", "ONNX-MiniLM")
     */
    val runtimeName: String

    /**
     * Checks if this runtime can be initialized on current device/environment.
     */
    fun isSupported(): Boolean

    /**
     * Initializes the backend runtime with an optional model file path.
     */
    suspend fun initialize(modelFilePath: String?): Result<Unit>

    /**
     * Runs forward inference on the provided input.
     * Guaranteed to run off the main thread.
     */
    suspend fun runInference(input: RawInferenceInput): RawInferenceOutput

    /**
     * Frees native resources, buffers, or sessions.
     */
    suspend fun release()
}

/**
 * Safe, reliable in-memory fallback runtime.
 * Activates when no heavy model weights are bundled or available on disk.
 * Generates valid structured JSON responses adhering to biomechanical and calisthenics principles.
 */
class FallbackInferenceRuntime : InferenceRuntime {

    override val runtimeName: String = "OfflineBiomechanicalFallbackRuntime"
    private var initialized: Boolean = false

    override fun isSupported(): Boolean = true

    override suspend fun initialize(modelFilePath: String?): Result<Unit> {
        initialized = true
        return Result.success(Unit)
    }

    override suspend fun runInference(input: RawInferenceInput): RawInferenceOutput {
        val startTime = System.currentTimeMillis()

        // Generate structured JSON matching the requested task
        val generatedJson = when {
            input.prompt.contains("WORKOUT_ADVICE") -> generateWorkoutAdviceJson(input.prompt)
            input.prompt.contains("PLATEAU_ANALYSIS") -> generatePlateauJson(input.prompt)
            input.prompt.contains("EXERCISE_SUBSTITUTION") -> generateSubstitutionJson(input.prompt)
            else -> generateGenericAdviceJson(input.prompt)
        }

        val elapsed = System.currentTimeMillis() - startTime

        return RawInferenceOutput(
            rawText = generatedJson,
            promptTokensCount = input.tokenIds.size,
            generatedTokensCount = generatedJson.length / 4,
            executionTimeMs = elapsed,
            isFallbackGenerated = true
        )
    }

    override suspend fun release() {
        initialized = false
    }

    private fun generateWorkoutAdviceJson(prompt: String): String {
        val hasShoulderPain = prompt.contains("شانه", ignoreCase = true) || prompt.contains("shoulder", ignoreCase = true)
        val isRedReadiness = prompt.contains("RED", ignoreCase = true)

        val summary = if (isRedReadiness) {
            "شاخص آمادگی امروز پایین گزارش شده است. حجم تمرین برای بازیابی سیستم عصبی ۳۰٪ کاهش داده شد."
        } else if (hasShoulderPain) {
            "هشدار درد مفصل شانه فعال است. حرکات پرفشار شانه با گزینه‌های پایدار جایگزین شدند."
        } else {
            "برنامه تمرینی بر اساس اصل اضافه بار تدریجی و حفظ توان انفجاری تنظیم گردید."
        }

        val rpeTarget = if (isRedReadiness) 6.0 else 7.5

        return """
        {
          "summary": "$summary",
          "primaryFocus": "کنترل فرم بیومکانیکی و پایداری مفاصل",
          "recommendedSetsDelta": ${if (isRedReadiness) -1 else 0},
          "recommendedRpeTarget": $rpeTarget,
          "exerciseModifications": [
            {
              "exerciseName": "شنا استاندارد",
              "targetReps": 10,
              "targetSets": 3,
              "rpeTarget": $rpeTarget,
              "modificationType": "${if (isRedReadiness) "DELOAD" else "MAINTAIN"}",
              "reason": "تمرکز بر دامنه کامل حرکتی و ثبات عضلات سراتوس قدامی"
            }
          ],
          "recoveryRecommendation": "حداقل ۸ ساعت خواب و تغذیه غنی از پروتئین جهت تسریع سنتز پروتئین عضلانی.",
          "safetyWarnings": [
            "در صورت احساس تیر کشیدن در شانه یا مچ دست فوراً ست را متوقف نمایید."
          ]
        }
        """.trimIndent()
    }

    private fun generatePlateauJson(prompt: String): String {
        return """
        {
          "exerciseId": "stagnant_exercise",
          "isPlateauConfirmed": true,
          "primaryCause": "عدم تطابق عصبی-عضلانی و فرسودگی ناشی از تکرار مداوم در یک دامنه حرکتی ثابت",
          "recommendedAction": "تغییر تمپوی اجرای حرکت به صورت ۲-۲-۱ و اعمال دی‌لود یک‌هفته‌ای",
          "deloadRecommended": true,
          "suggestedVariations": [
            "مکث ۲ ثانیه‌ای در عمیق‌ترین نقطه انقباض (Pause Reps)",
            "استفاده از مقاومت متغیر با کش یا زاویه لور متفاوت"
          ],
          "biomechanicalCues": [
            "افزایش زمان زیر بار (TUT)",
            "انقباض ارادی کامل در بخش کانسنتریک حرکت"
          ]
        }
        """.trimIndent()
    }

    private fun generateSubstitutionJson(prompt: String): String {
        return """
        {
          "originalExercise": "دیپ پارالل",
          "substitutedExercise": "شنا روی پایه‌های موازی با زانو خم",
          "matchingMovementPattern": "VERTICAL_PUSH",
          "shoulderLoadImpact": "REDUCED",
          "justification": "کاهش اکستنشن بیش از حد مفصل گلنوهومرال و حفظ ایمنی کپسول قدامی شانه",
          "safetyCues": [
            "زاویه آرنج‌ها بیش از ۹۰ درجه نشود",
            "عضلات لت و شکم حین فرود کاملاً سفت نگه داشته شوند"
          ]
        }
        """.trimIndent()
    }

    private fun generateGenericAdviceJson(prompt: String): String {
        return """
        {
          "title": "راهنمای تطبیقی کالیستنیکس",
          "keyPoints": [
            "حفظ انقباض هسته بدن (Hollow Body) در تمامی الگوهای فشاری و کششی",
            "رعایت حداقل ۴۸ ساعت استراحت میان تمرینات شدید یک گروه عضلانی"
          ],
          "safetyNotice": "کالیستنیکس بر پایه تسلط بر وزن بدن است؛ هرگز تکنیک را فدای تعداد تکرار نکنید.",
          "isHeuristicGenerated": true
        }
        """.trimIndent()
    }
}
