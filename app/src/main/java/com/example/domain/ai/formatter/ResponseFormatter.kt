package com.example.domain.ai.formatter

import com.example.domain.ai.model.AIGenericAdvice
import com.example.domain.ai.model.AIPlateauAdvice
import com.example.domain.ai.model.AIStructuredOutput
import com.example.domain.ai.model.AIWorkoutAdvice
import com.example.domain.ai.model.AIExerciseSubstitutionAdvice

data class FormattedPresentation(
    val persianText: String,
    val englishText: String,
    val primaryBadgeText: String,
    val isWarningPresent: Boolean
)

/**
 * Formats structured AI domain models into clean, bilingual, user-facing presentations.
 */
object ResponseFormatter {

    fun format(output: AIStructuredOutput): FormattedPresentation {
        return when (output) {
            is AIWorkoutAdvice -> formatWorkoutAdvice(output)
            is AIPlateauAdvice -> formatPlateauAdvice(output)
            is AIExerciseSubstitutionAdvice -> formatSubstitutionAdvice(output)
            is AIGenericAdvice -> formatGenericAdvice(output)
        }
    }

    private fun formatWorkoutAdvice(advice: AIWorkoutAdvice): FormattedPresentation {
        val fa = StringBuilder()
        fa.append("📋 **خلاصه تحلیل جلسه:**\n")
        fa.append(advice.summary).append("\n\n")

        fa.append("🎯 **تمرکز بیومکانیکی:** ").append(advice.primaryFocus).append("\n")
        fa.append("⚡ **شاخص شدت هدف (RPE):** ").append(advice.recommendedRpeTarget).append("\n\n")

        if (advice.exerciseModifications.isNotEmpty()) {
            fa.append("🏋️ **تنظیم حرکات این جلسه:**\n")
            advice.exerciseModifications.forEach { mod ->
                val typeLabel = when (mod.modificationType) {
                    "DELOAD" -> "دی‌لود (تسکین فشار)"
                    "PROGRESS" -> "افزایش سطح"
                    "REGRESS" -> "تعدیل سطح"
                    "SUBSTITUTE" -> "جایگزین"
                    else -> "تثبیت"
                }
                fa.append("• ${mod.exerciseName}: ${mod.targetSets} ست × ${mod.targetReps} تکرار (RPE ${mod.rpeTarget}) — [$typeLabel]\n")
                if (mod.reason.isNotBlank()) {
                    fa.append("  ↳ علت: ${mod.reason}\n")
                }
            }
            fa.append("\n")
        }

        if (advice.recoveryRecommendation.isNotBlank()) {
            fa.append("💤 **توصیه ریکاوری:** ").append(advice.recoveryRecommendation).append("\n\n")
        }

        if (advice.safetyWarnings.isNotEmpty()) {
            fa.append("⚠️ **هشدارهای ایمنی و مفاصل:**\n")
            advice.safetyWarnings.forEach { fa.append("• ").append(it).append("\n") }
        }

        // English counterpart
        val en = StringBuilder()
        en.append("Session Analysis: ").append(advice.summary).append("\n")
        en.append("Focus: ").append(advice.primaryFocus).append(" | Target RPE: ").append(advice.recommendedRpeTarget).append("\n")
        if (advice.recoveryRecommendation.isNotBlank()) {
            en.append("Recovery: ").append(advice.recoveryRecommendation).append("\n")
        }

        return FormattedPresentation(
            persianText = fa.toString().trim(),
            englishText = en.toString().trim(),
            primaryBadgeText = "RPE ${advice.recommendedRpeTarget}",
            isWarningPresent = advice.safetyWarnings.isNotEmpty()
        )
    }

    private fun formatPlateauAdvice(advice: AIPlateauAdvice): FormattedPresentation {
        val fa = StringBuilder()
        fa.append("🔍 **تشخیص وضعیت پلاتو:**\n")
        fa.append(if (advice.isPlateauConfirmed) "توقف پیشرفت (پلاتو) تایید گردید." else "روند پیشرفت عادی است.").append("\n\n")

        fa.append("🧠 **ریشه فیزیولوژیکی:** ").append(advice.primaryCause).append("\n\n")
        fa.append("🛠️ **راهکار عملی:** ").append(advice.recommendedAction).append("\n\n")

        if (advice.suggestedVariations.isNotEmpty()) {
            fa.append("🔄 **تغییرات بیومکانیکی پیشنهادی:**\n")
            advice.suggestedVariations.forEach { fa.append("• ").append(it).append("\n") }
            fa.append("\n")
        }

        if (advice.biomechanicalCues.isNotEmpty()) {
            fa.append("💡 **نکات حرکتی (Cues):**\n")
            advice.biomechanicalCues.forEach { fa.append("• ").append(it).append("\n") }
        }

        val en = "Plateau Analysis: ${advice.primaryCause}. Suggested: ${advice.recommendedAction}"

        return FormattedPresentation(
            persianText = fa.toString().trim(),
            englishText = en,
            primaryBadgeText = if (advice.deloadRecommended) "نیاز به دی‌لود" else "تغییر متغیر",
            isWarningPresent = advice.deloadRecommended
        )
    }

    private fun formatSubstitutionAdvice(advice: AIExerciseSubstitutionAdvice): FormattedPresentation {
        val fa = StringBuilder()
        fa.append("🔄 **پیشنهاد جایگزینی حرکت:**\n")
        fa.append("حرکت مبدا: **${advice.originalExercise}** ➔ جایگزین: **${advice.substitutedExercise}**\n\n")
        fa.append("📐 **الگوی حرکتی:** ${advice.matchingMovementPattern}\n")
        fa.append("🛡️ **تاثیر بر فشار مفصل شانه:** ${advice.shoulderLoadImpact}\n\n")
        fa.append("📋 **توجیه بیومکانیکی:** ${advice.justification}\n")

        if (advice.safetyCues.isNotEmpty()) {
            fa.append("\n⚠️ **نکات ایمنی اجرا:**\n")
            advice.safetyCues.forEach { fa.append("• ").append(it).append("\n") }
        }

        val en = "Substituted ${advice.originalExercise} with ${advice.substitutedExercise} (${advice.shoulderLoadImpact})"

        return FormattedPresentation(
            persianText = fa.toString().trim(),
            englishText = en,
            primaryBadgeText = advice.shoulderLoadImpact,
            isWarningPresent = advice.shoulderLoadImpact == "INCREASED"
        )
    }

    private fun formatGenericAdvice(advice: AIGenericAdvice): FormattedPresentation {
        val fa = StringBuilder()
        fa.append("📌 **${advice.title}**\n\n")
        advice.keyPoints.forEach { fa.append("• ").append(it).append("\n") }
        if (advice.safetyNotice.isNotBlank()) {
            fa.append("\n🛡️ ").append(advice.safetyNotice)
        }

        return FormattedPresentation(
            persianText = fa.toString().trim(),
            englishText = advice.title,
            primaryBadgeText = "راهنمای تطبیقی",
            isWarningPresent = false
        )
    }
}
