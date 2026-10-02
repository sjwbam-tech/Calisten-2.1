package com.example.domain.ai.validation

import com.example.domain.ai.model.AIExerciseModification
import com.example.domain.ai.model.AIExerciseSubstitutionAdvice
import com.example.domain.ai.model.AIPlateauAdvice
import com.example.domain.ai.model.AIStructuredOutput
import com.example.domain.ai.model.AIWorkoutAdvice

sealed class ValidationResult {
    data class Valid(val sanitizedOutput: AIStructuredOutput) : ValidationResult()
    data class Invalid(val reasons: List<String>, val fallbackOutput: AIStructuredOutput) : ValidationResult()
}

/**
 * Biomechanical and safety validation layer for offline AI generated advice.
 * Guards against hallucinated or dangerous workout parameters (e.g. 500 reps, RPE 25, high shoulder load during active injury).
 */
object ValidationLayer {

    const val MIN_REPS = 1
    const val MAX_REPS = 60
    const val MIN_SETS = 1
    const val MAX_SETS = 8
    const val MIN_RPE = 1.0
    const val MAX_RPE = 10.0

    /**
     * Validates and sanitizes structured advice against user constraints and safety standards.
     */
    fun validate(
        output: AIStructuredOutput,
        userHasActiveShoulderPain: Boolean = false,
        availableEquipment: List<String> = emptyList()
    ): ValidationResult {
        return when (output) {
            is AIWorkoutAdvice -> validateWorkoutAdvice(output, userHasActiveShoulderPain)
            is AIPlateauAdvice -> validatePlateauAdvice(output)
            is AIExerciseSubstitutionAdvice -> validateSubstitutionAdvice(output, userHasActiveShoulderPain, availableEquipment)
            else -> ValidationResult.Valid(output)
        }
    }

    private fun validateWorkoutAdvice(
        advice: AIWorkoutAdvice,
        hasShoulderPain: Boolean
    ): ValidationResult {
        val violations = mutableListOf<String>()

        val clampedRpe = advice.recommendedRpeTarget.coerceIn(MIN_RPE, MAX_RPE)
        if (advice.recommendedRpeTarget < MIN_RPE || advice.recommendedRpeTarget > MAX_RPE) {
            violations.add("مقدار RPE پیشنهادی (${advice.recommendedRpeTarget}) خارج از محدوده مجاز (۱ تا ۱۰) بود.")
        }

        val sanitizedMods = advice.exerciseModifications.map { mod ->
            var reps = mod.targetReps
            var sets = mod.targetSets
            var rpe = mod.rpeTarget

            if (reps < MIN_REPS || reps > MAX_REPS) {
                violations.add("تعداد تکرار (${reps}) برای حرکت ${mod.exerciseName} غیرمنطقی بود و تصحیح شد.")
                reps = reps.coerceIn(MIN_REPS, MAX_REPS)
            }

            if (sets < MIN_SETS || sets > MAX_SETS) {
                violations.add("تعداد ست (${sets}) برای حرکت ${mod.exerciseName} غیرمنطقی بود و تصحیح شد.")
                sets = sets.coerceIn(MIN_SETS, MAX_SETS)
            }

            if (rpe < MIN_RPE || rpe > MAX_RPE) {
                rpe = rpe.coerceIn(MIN_RPE, MAX_RPE)
            }

            mod.copy(
                targetReps = reps,
                targetSets = sets,
                rpeTarget = rpe
            )
        }

        val extraWarnings = advice.safetyWarnings.toMutableList()
        if (hasShoulderPain && extraWarnings.none { it.contains("شانه", ignoreCase = true) }) {
            extraWarnings.add("هشدار ایمنی: با توجه به درد ثبت‌شده شانه، از قفل کردن مفاصل در انتهای حرکت خودداری فرمایید.")
        }

        val sanitizedAdvice = advice.copy(
            recommendedRpeTarget = clampedRpe,
            exerciseModifications = sanitizedMods,
            safetyWarnings = extraWarnings
        )

        return if (violations.isEmpty()) {
            ValidationResult.Valid(sanitizedAdvice)
        } else {
            ValidationResult.Invalid(violations, sanitizedAdvice)
        }
    }

    private fun validatePlateauAdvice(advice: AIPlateauAdvice): ValidationResult {
        val violations = mutableListOf<String>()

        if (advice.primaryCause.isBlank()) {
            violations.add("علت پلاتو مشخص نشده است.")
        }
        if (advice.recommendedAction.isBlank()) {
            violations.add("راهکار رفع پلاتو خالی است.")
        }

        return if (violations.isEmpty()) {
            ValidationResult.Valid(advice)
        } else {
            val fallback = advice.copy(
                primaryCause = if (advice.primaryCause.isBlank()) "سازگاری بیش از حد با الگوی حرکتی فعلی" else advice.primaryCause,
                recommendedAction = if (advice.recommendedAction.isBlank()) "اعمال تغییر در متغیر زمان زیر فشار (TUT) یا دی‌لود" else advice.recommendedAction
            )
            ValidationResult.Invalid(violations, fallback)
        }
    }

    private fun validateSubstitutionAdvice(
        advice: AIExerciseSubstitutionAdvice,
        hasShoulderPain: Boolean,
        availableEquipment: List<String>
    ): ValidationResult {
        val violations = mutableListOf<String>()

        if (hasShoulderPain && advice.shoulderLoadImpact == "INCREASED") {
            violations.add("حرکت جایگزین بار شانه را در زمان درد افزایش می‌دهد.")
        }

        val safeAdvice = if (hasShoulderPain && advice.shoulderLoadImpact == "INCREASED") {
            advice.copy(
                shoulderLoadImpact = "REDUCED",
                justification = advice.justification + " (تعدیل شده به علت علائم درد شانه)"
            )
        } else {
            advice
        }

        return if (violations.isEmpty()) {
            ValidationResult.Valid(safeAdvice)
        } else {
            ValidationResult.Invalid(violations, safeAdvice)
        }
    }
}
