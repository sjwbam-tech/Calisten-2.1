package com.example.domain.engine

import com.example.data.local.entity.ExerciseEntity

data class RecommendationProposal(
    val exerciseToReplace: ExerciseEntity?,
    val proposedExercise: ExerciseEntity,
    val reason: String,
    val patternMatch: String,
    val shoulderLoadImpact: String,
    val volumeImpactExplanation: String
)

object ExerciseRecommendationEngine {

    /**
     * Evaluates available exercises against current routine exercises, profile limitations,
     * equipment, and goals. Prefers replacing redundant movements to prevent excessive volume.
     */
    fun findRecommendation(
        currentExercises: List<ExerciseEntity>,
        allExercises: List<ExerciseEntity>,
        availableEquipment: List<String>,
        experienceLevel: String,
        recentReadiness: String = "GREEN", // GREEN, YELLOW, RED
        shoulderConcern: Boolean = false
    ): List<RecommendationProposal> {
        val proposals = mutableListOf<RecommendationProposal>()

        val currentIds = currentExercises.map { it.id }.toSet()
        val currentPatterns = currentExercises.groupBy { it.movementPattern }

        for (candidate in allExercises) {
            if (candidate.id in currentIds) continue

            // Check difficulty match
            if (experienceLevel == "BEGINNER" && candidate.difficulty in listOf("ADVANCED", "ELITE")) {
                continue
            }

            // Shoulder load check
            if (shoulderConcern && candidate.shoulderLoad == "HIGH") {
                continue
            }

            // Recovery check: if RED readiness, avoid high shoulder/high difficulty
            if (recentReadiness == "RED" && candidate.shoulderLoad != "LOW") {
                continue
            }

            // Check if user already has exercises with the same pattern (e.g. HORIZONTAL_PUSH)
            val redundantWith = currentPatterns[candidate.movementPattern]?.firstOrNull()

            if (redundantWith != null) {
                // If candidate is a direct progression or healthier alternative
                val isProgression = redundantWith.progressionExerciseId == candidate.id
                val isRegression = redundantWith.regressionExerciseId == candidate.id
                val isAlternative = redundantWith.alternativeExerciseIdsJson.contains(candidate.id)

                if (isProgression || isRegression || isAlternative) {
                    val reason = when {
                        isProgression -> "ارتقا به حرکت سطح بالاتر به جای افزایش حجم تکرارها در حرکت فعلی"
                        isRegression -> "تعدیل ایمن حرکت برای تمرکز بر فرم صحیح و کاهش فشار مفصلی"
                        else -> "جایگزینی حرکت تکراری با الگوی بیومکانیکی تازه بدون افزایش بی‌رویه حجم هفتگی"
                    }

                    proposals.add(
                        RecommendationProposal(
                            exerciseToReplace = redundantWith,
                            proposedExercise = candidate,
                            reason = reason,
                            patternMatch = candidate.movementPattern,
                            shoulderLoadImpact = "فشار شانه حرکت پیشنهادی: ${candidate.shoulderLoad}",
                            volumeImpactExplanation = "جایگزین ${redundantWith.persianName} شده تا توازن ست‌های هفتگی حفظ شود."
                        )
                    )
                }
            } else if (currentExercises.size < 6) {
                // New movement pattern needed
                proposals.add(
                    RecommendationProposal(
                        exerciseToReplace = null,
                        proposedExercise = candidate,
                        reason = "تکمیل الگوی حرکتی ${candidate.movementPattern} جهت ایجاد بالانس عضلانی کامل",
                        patternMatch = candidate.movementPattern,
                        shoulderLoadImpact = "فشار شانه: ${candidate.shoulderLoad}",
                        volumeImpactExplanation = "افزودن ۳ ست استاندارد جهت پر کردن خلا الگوی حرکتی"
                    )
                )
            }
        }

        return proposals.distinctBy { it.proposedExercise.id }
    }
}
