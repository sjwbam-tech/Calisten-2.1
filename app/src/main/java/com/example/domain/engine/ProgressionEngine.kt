package com.example.domain.engine

import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.SetLogEntity

data class ProgressionDecision(
    val exerciseId: String,
    val baselineDescription: String,
    val actualPerformance: String,
    val decisionType: DecisionType, // ADVANCE_ONE_VARIABLE, REPEAT_TARGET, DELOAD_OR_REGRESS, FORM_CHECK
    val newTargetSets: Int,
    val newTargetReps: Int,
    val newTargetHoldSec: Int,
    val reasonExplanation: String
)

enum class DecisionType {
    ADVANCE_ONE_VARIABLE,
    REPEAT_TARGET,
    DELOAD_OR_REGRESS,
    FORM_CHECK
}

object ProgressionEngine {

    /**
     * Deterministic Double Progression Rule:
     * - Evaluates recent completed sets for this exercise.
     * - Only changes ONE variable at a time (e.g., reps +1, or hold +2s).
     * - Never makes aggressive jumps from a single good session.
     * - If pain occurred, immediately suggests regression/deload.
     */
    fun evaluateProgression(
        exercise: ExerciseEntity,
        currentSets: Int,
        currentReps: Int,
        currentHoldSec: Int,
        recentSetLogs: List<SetLogEntity>,
        recentSleepAvg: Float = 7.5f,
        recentFatigueAvg: Int = 2
    ): ProgressionDecision {
        if (recentSetLogs.isEmpty()) {
            return ProgressionDecision(
                exerciseId = exercise.id,
                baselineDescription = "$currentSets ست × $currentReps تکرار",
                actualPerformance = "بدون سابقه ثبت‌شده",
                decisionType = DecisionType.REPEAT_TARGET,
                newTargetSets = currentSets,
                newTargetReps = currentReps,
                newTargetHoldSec = currentHoldSec,
                reasonExplanation = "هدف تمرینی پایه حفظ شد تا داده عملکردی واقعی ثبت شود."
            )
        }

        // Check for pain in recent sets
        val hadPain = recentSetLogs.any { it.status == "PAIN_STOP" || it.painSeverity >= 4 }
        if (hadPain) {
            val regressedReps = (currentReps - 2).coerceAtLeast(4)
            return ProgressionDecision(
                exerciseId = exercise.id,
                baselineDescription = "$currentSets ست × $currentReps تکرار",
                actualPerformance = "ثبت ناراحتی یا درد در ست‌های اخیر",
                decisionType = DecisionType.DELOAD_OR_REGRESS,
                newTargetSets = currentSets,
                newTargetReps = regressedReps,
                newTargetHoldSec = (currentHoldSec - 5).coerceAtLeast(0),
                reasonExplanation = "به علت ثبت شاخص درد، حجم به طور محافظه‌کارانه کاهش یافت تا بافت همبند بازسازی شود."
            )
        }

        val completedSets = recentSetLogs.filter { it.status == "COMPLETED" }
        val avgRpe = if (completedSets.isNotEmpty()) completedSets.map { it.rpe }.average() else 8.0
        val allTargetMet = completedSets.size >= currentSets && completedSets.all { it.reps >= currentReps }

        val actualSummary = completedSets.joinToString(" / ") { "${it.reps} تکرار (RPE ${it.rpe})" }

        // If user hit all targets with solid form & manageable RPE (<= 8.0) and good recovery
        if (allTargetMet && avgRpe <= 8.0 && recentSleepAvg >= 6.5f && recentFatigueAvg <= 3) {
            if (currentHoldSec > 0) {
                // Isometric exercise: advance hold time by 2-3 seconds
                val newHold = currentHoldSec + 3
                return ProgressionDecision(
                    exerciseId = exercise.id,
                    baselineDescription = "$currentSets ست × $currentHoldSec ثانیه مکث",
                    actualPerformance = actualSummary,
                    decisionType = DecisionType.ADVANCE_ONE_VARIABLE,
                    newTargetSets = currentSets,
                    newTargetReps = currentReps,
                    newTargetHoldSec = newHold,
                    reasonExplanation = "تکمیل تمام ست‌ها با کنترل عالی و RPE پایدار؛ افزایش ۳ ثانیه به زمان مکث (اضافه‌بار تک‌متغیره)."
                )
            } else {
                // Repetition exercise: advance by 1 rep (e.g., 3x8 -> 3x9)
                val newReps = currentReps + 1
                return ProgressionDecision(
                    exerciseId = exercise.id,
                    baselineDescription = "$currentSets ست × $currentReps تکرار",
                    actualPerformance = actualSummary,
                    decisionType = DecisionType.ADVANCE_ONE_VARIABLE,
                    newTargetSets = currentSets,
                    newTargetReps = newReps,
                    newTargetHoldSec = 0,
                    reasonExplanation = "تکمیل تمام ست‌ها با دامنه کامل و RPE کنترل‌شده (میانگین ${String.format("%.1f", avgRpe)})؛ افزایش ۱ تکرار به هدف هر ست."
                )
            }
        }

        // Check for severe performance drop or chronic failure
        val isSevereStruggle = (completedSets.isNotEmpty() && completedSets.all { it.reps <= (currentReps * 0.7f).toInt().coerceAtLeast(1) }) ||
                (avgRpe >= 9.5 && completedSets.any { it.reps < currentReps })

        if (isSevereStruggle) {
            val regressedReps = if (currentHoldSec > 0) currentReps else (currentReps - 1).coerceAtLeast(4)
            val regressedHold = if (currentHoldSec > 0) (currentHoldSec - 4).coerceAtLeast(5) else 0
            return ProgressionDecision(
                exerciseId = exercise.id,
                baselineDescription = if (currentHoldSec > 0) "$currentSets ست × $currentHoldSec ثانیه مکث" else "$currentSets ست × $currentReps تکرار",
                actualPerformance = actualSummary.ifEmpty { "افت عملکرد در ست‌ها" },
                decisionType = DecisionType.DELOAD_OR_REGRESS,
                newTargetSets = currentSets,
                newTargetReps = regressedReps,
                newTargetHoldSec = regressedHold,
                reasonExplanation = "افت عملکرد در ست‌های اخیر یا فشار بسیار بالا (RPE ${String.format(java.util.Locale.US, "%.1f", avgRpe)})؛ تعدیل محافظه‌کارانه بار جهت بازیابی توان و جلوگیری از آسیب."
            )
        }

        // If failed to meet target or high RPE, keep current target
        val reason = if (recentSleepAvg < 6.5f || recentFatigueAvg >= 4) {
            "شاخص ریکاوری یا خواب در روزهای اخیر نامساعد بوده است؛ تثبیت هدف قبلی جهت جلوگیری از تمرین‌زدگی."
        } else if (avgRpe >= 9.0) {
            "شدت درک‌شده ست‌ها بالا بوده (RPE ${String.format(java.util.Locale.US, "%.1f", avgRpe)})؛ تثبیت هدف فعلی تا تسلط کامل بر فرم حرکت."
        } else {
            "تکرارهای هدف در تمام ست‌ها به حد نصاب تثبیت نرسیده است؛ تکرار هدف فعلی توصیه می‌شود."
        }

        return ProgressionDecision(
            exerciseId = exercise.id,
            baselineDescription = "$currentSets ست × $currentReps تکرار",
            actualPerformance = actualSummary.ifEmpty { "بخشی از ست‌ها تکمیل شد" },
            decisionType = DecisionType.REPEAT_TARGET,
            newTargetSets = currentSets,
            newTargetReps = currentReps,
            newTargetHoldSec = currentHoldSec,
            reasonExplanation = reason
        )
    }
}
