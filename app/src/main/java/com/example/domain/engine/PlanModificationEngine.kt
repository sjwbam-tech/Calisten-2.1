package com.example.domain.engine

import com.example.data.local.entity.AdaptationDecisionEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.WorkoutExerciseEntity
import com.example.data.local.entity.WorkoutSessionEntity
import java.util.UUID

enum class DifficultyDirection {
    INCREASE, // Progression
    DECREASE  // Regression
}

data class ExerciseModificationResult(
    val updatedExercise: WorkoutExerciseEntity?,
    val reindexedSessionExercises: List<WorkoutExerciseEntity>,
    val changeExplanation: String,
    val decision: AdaptationDecisionEntity?
)

object PlanModificationEngine {

    /**
     * Adds an exercise to a session while maintaining valid orderIndex and balanced target reps.
     */
    fun addExerciseToSession(
        session: WorkoutSessionEntity,
        existingExercises: List<WorkoutExerciseEntity>,
        newExercise: ExerciseEntity,
        targetSets: Int = 3,
        targetRpe: Int = 7
    ): ExerciseModificationResult {
        val nextOrder = (existingExercises.maxOfOrNull { it.orderIndex } ?: 0) + 1

        val isIsometric = newExercise.minHoldSec > 0 ||
                newExercise.id in listOf("plank_std", "hollow_body", "dead_hang", "lsit", "front_lever_tuck")

        val targetHold = if (isIsometric) {
            when (newExercise.id) {
                "plank_std" -> 30
                "hollow_body" -> 20
                "dead_hang" -> 25
                "lsit" -> 12
                "front_lever_tuck" -> 10
                else -> 15
            }
        } else 0

        val targetReps = if (isIsometric) 0 else {
            when (newExercise.difficulty) {
                "BEGINNER" -> 10
                "INTERMEDIATE" -> 8
                "ADVANCED" -> 6
                else -> 8
            }
        }

        val addedEntity = WorkoutExerciseEntity(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            exerciseId = newExercise.id,
            orderIndex = nextOrder,
            targetSets = targetSets,
            targetReps = targetReps,
            targetHoldSec = targetHold,
            targetRpe = targetRpe,
            restSec = if (isIsometric) 60 else 90,
            notes = "حرکت افزوده شده به جلسه تمرین با کنترل فرم"
        )

        val updatedList = existingExercises + addedEntity

        val decision = AdaptationDecisionEntity(
            id = UUID.randomUUID().toString(),
            profileId = session.profileId,
            programId = session.programId ?: "",
            sessionId = session.id,
            decisionType = "EXERCISE_ADDED",
            decision = "افزودن ${newExercise.persianName} به جلسه تمرینی",
            reason = "افزودن حرکت با موقعیت ردیف $nextOrder و توازن حجم هفتگی.",
            programVersion = 1
        )

        return ExerciseModificationResult(
            updatedExercise = addedEntity,
            reindexedSessionExercises = updatedList,
            changeExplanation = "حرکت ${newExercise.persianName} با موفقیت به انتهای جلسه اضافه شد.",
            decision = decision
        )
    }

    /**
     * Removes an exercise from a session and smoothly re-indexes the remaining exercises.
     */
    fun removeExerciseFromSession(
        session: WorkoutSessionEntity,
        existingExercises: List<WorkoutExerciseEntity>,
        workoutExerciseIdToRemove: String
    ): ExerciseModificationResult {
        val remaining = existingExercises
            .filter { it.id != workoutExerciseIdToRemove }
            .sortedBy { it.orderIndex }
            .mapIndexed { idx, we -> we.copy(orderIndex = idx + 1) }

        val removedEx = existingExercises.firstOrNull { it.id == workoutExerciseIdToRemove }

        val decision = AdaptationDecisionEntity(
            id = UUID.randomUUID().toString(),
            profileId = session.profileId,
            programId = session.programId ?: "",
            sessionId = session.id,
            decisionType = "EXERCISE_REMOVED",
            decision = "حذف حرکت از جلسه تمرینی و بازشماری ردیف‌ها",
            reason = "حذف حرکت با شناسه ${removedEx?.exerciseId} جهت متناسب‌سازی زمان و حجم جلسه.",
            programVersion = 1
        )

        return ExerciseModificationResult(
            updatedExercise = null,
            reindexedSessionExercises = remaining,
            changeExplanation = "حرکت حذف شد و ترتیب سایر حرکات بازتنظیم گردید.",
            decision = decision
        )
    }

    /**
     * Adjusts the difficulty of an exercise in a session (progression or regression swap).
     */
    fun adjustDifficulty(
        session: WorkoutSessionEntity,
        currentWorkoutExercise: WorkoutExerciseEntity,
        currentExerciseDetail: ExerciseEntity,
        direction: DifficultyDirection,
        allExercises: List<ExerciseEntity>,
        relationshipGraph: ExerciseRelationshipGraph? = null
    ): ExerciseModificationResult {
        val exerciseMap = allExercises.associateBy { it.id }

        val fallbackRegressionId = when (currentExerciseDetail.id) {
            "pullup_std" -> "pullup_chinup"
            "pullup_chinup" -> "inverted_row"
            "front_lever_tuck" -> "pullup_std"
            "dips_parallel" -> "dips_bench"
            "pushup_diamond" -> "pushup_std"
            "pushup_std" -> "pushup_knee"
            "squat_pistol" -> "squat_bulgarian"
            "squat_bulgarian" -> "squat_bodyweight"
            "hanging_leg_raise" -> "hollow_body"
            "lsit" -> "hollow_body"
            "hollow_body" -> "plank_std"
            "handstand" -> "pushup_diamond"
            else -> null
        }

        val fallbackProgressionId = when (currentExerciseDetail.id) {
            "pushup_knee" -> "pushup_std"
            "pushup_std" -> "pushup_diamond"
            "pushup_diamond" -> "dips_parallel"
            "dips_bench" -> "dips_parallel"
            "inverted_row" -> "pullup_chinup"
            "dead_hang" -> "pullup_chinup"
            "pullup_chinup" -> "pullup_std"
            "pullup_std" -> "front_lever_tuck"
            "squat_bodyweight" -> "squat_bulgarian"
            "squat_bulgarian" -> "squat_pistol"
            "plank_std" -> "hollow_body"
            "hollow_body" -> "hanging_leg_raise"
            "hanging_leg_raise" -> "lsit"
            else -> null
        }

        val targetExercise: ExerciseEntity? = when (direction) {
            DifficultyDirection.INCREASE -> {
                // Look for progression
                val directProg = currentExerciseDetail.progressionExerciseId?.let { exerciseMap[it] }
                directProg ?: relationshipGraph?.getProgressions(currentExerciseDetail.id)?.firstOrNull()
                ?: fallbackProgressionId?.let { exerciseMap[it] }
            }
            DifficultyDirection.DECREASE -> {
                // Look for regression
                val directReg = currentExerciseDetail.regressionExerciseId?.let { exerciseMap[it] }
                directReg ?: relationshipGraph?.getRegressions(currentExerciseDetail.id)?.firstOrNull()
                ?: fallbackRegressionId?.let { exerciseMap[it] }
            }
        }

        if (targetExercise == null) {
            // No direct progression/regression in database: adjust reps/hold dynamically!
            val updated = when (direction) {
                DifficultyDirection.INCREASE -> {
                    if (currentWorkoutExercise.targetHoldSec > 0) {
                        currentWorkoutExercise.copy(targetHoldSec = currentWorkoutExercise.targetHoldSec + 5)
                    } else {
                        currentWorkoutExercise.copy(targetReps = currentWorkoutExercise.targetReps + 2)
                    }
                }
                DifficultyDirection.DECREASE -> {
                    if (currentWorkoutExercise.targetHoldSec > 0) {
                        currentWorkoutExercise.copy(targetHoldSec = (currentWorkoutExercise.targetHoldSec - 5).coerceAtLeast(5))
                    } else {
                        currentWorkoutExercise.copy(targetReps = (currentWorkoutExercise.targetReps - 2).coerceAtLeast(4))
                    }
                }
            }

            val decision = AdaptationDecisionEntity(
                id = UUID.randomUUID().toString(),
                profileId = session.profileId,
                programId = session.programId ?: "",
                sessionId = session.id,
                decisionType = if (direction == DifficultyDirection.INCREASE) "DIFFICULTY_INCREASED" else "DIFFICULTY_DECREASED",
                decision = if (direction == DifficultyDirection.INCREASE) "افزایش شدت تکرارها در ${currentExerciseDetail.persianName}" else "کاهش حجم تکرارها در ${currentExerciseDetail.persianName}",
                reason = "تنظیم بار تمرینی بر مبنای درخواست کاربر جهت بهینه‌سازی بارگذاری.",
                programVersion = 1
            )

            return ExerciseModificationResult(
                updatedExercise = updated,
                reindexedSessionExercises = listOf(updated),
                changeExplanation = "تکرارها/زمان مکث متناسب با سطح انتخابی تغییر یافت.",
                decision = decision
            )
        }

        // Swapping with higher or lower variation
        val isNewIsometric = targetExercise.minHoldSec > 0 ||
                targetExercise.id in listOf("plank_std", "hollow_body", "dead_hang", "lsit", "front_lever_tuck")

        val newReps = if (isNewIsometric) 0 else {
            if (direction == DifficultyDirection.INCREASE) {
                (currentWorkoutExercise.targetReps - 2).coerceAtLeast(5)
            } else {
                (currentWorkoutExercise.targetReps + 2).coerceAtMost(15)
            }
        }

        val newHold = if (isNewIsometric) {
            if (direction == DifficultyDirection.INCREASE) 10 else 25
        } else 0

        val updated = currentWorkoutExercise.copy(
            exerciseId = targetExercise.id,
            targetReps = newReps,
            targetHoldSec = newHold,
            notes = "تعدیل سطح دشواری به ${targetExercise.persianName} بنا به انتخاب کاربر"
        )

        val decision = AdaptationDecisionEntity(
            id = UUID.randomUUID().toString(),
            profileId = session.profileId,
            programId = session.programId ?: "",
            sessionId = session.id,
            decisionType = if (direction == DifficultyDirection.INCREASE) "PROGRESSION_SWAP" else "REGRESSION_SWAP",
            decision = "جایگزینی ${currentExerciseDetail.persianName} با ${targetExercise.persianName}",
            reason = if (direction == DifficultyDirection.INCREASE) "ارتقا به حرکت دشوارتر برای چالش بیشتر" else "تعدیل به حرکت ساده‌تر جهت حفظ کیفیت و ایمنی فرم",
            programVersion = 1
        )

        return ExerciseModificationResult(
            updatedExercise = updated,
            reindexedSessionExercises = listOf(updated),
            changeExplanation = "حرکت به ${targetExercise.persianName} تغییر یافت.",
            decision = decision
        )
    }

    /**
     * Adapts upcoming planned exercises based on recent workout history.
     */
    fun adaptUpcomingExercises(
        plannedExercises: List<WorkoutExerciseEntity>,
        recentSetLogs: List<SetLogEntity>,
        allExercises: List<ExerciseEntity>
    ): List<Pair<WorkoutExerciseEntity, AdaptationDecisionEntity?>> {
        val exerciseMap = allExercises.associateBy { it.id }

        return plannedExercises.map { plannedWE ->
            val logsForEx = recentSetLogs.filter { it.exerciseId == plannedWE.exerciseId && it.status == "COMPLETED" }
            if (logsForEx.size >= plannedWE.targetSets) {
                val avgRpe = logsForEx.map { it.rpe }.average()
                val allCompleted = logsForEx.all { it.reps >= plannedWE.targetReps }

                if (allCompleted && avgRpe <= 7.2) {
                    // Successful: advance one rep or +3s hold
                    val newWE = if (plannedWE.targetHoldSec > 0) {
                        plannedWE.copy(targetHoldSec = plannedWE.targetHoldSec + 3)
                    } else {
                        plannedWE.copy(targetReps = plannedWE.targetReps + 1)
                    }

                    val exName = exerciseMap[plannedWE.exerciseId]?.persianName ?: plannedWE.exerciseId
                    val decision = AdaptationDecisionEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = "",
                        programId = "",
                        sessionId = plannedWE.sessionId,
                        decisionType = "PROGRESSIVE_OVERLOAD",
                        decision = "افزایش هوشمند بار در $exName برای جلسات آینده",
                        reason = "تکمیل موفق تمام ست‌ها با RPE میانگین ${String.format("%.1f", avgRpe)}; اعمال اضافه‌بار تدریجی.",
                        programVersion = 1
                    )
                    Pair(newWE, decision)
                } else if (avgRpe >= 9.0 || recentSetLogs.any { it.exerciseId == plannedWE.exerciseId && it.painSeverity >= 3 }) {
                    // Struggling or pain: regress slightly
                    val newWE = if (plannedWE.targetHoldSec > 0) {
                        plannedWE.copy(targetHoldSec = (plannedWE.targetHoldSec - 4).coerceAtLeast(5))
                    } else {
                        plannedWE.copy(targetReps = (plannedWE.targetReps - 1).coerceAtLeast(4))
                    }

                    val exName = exerciseMap[plannedWE.exerciseId]?.persianName ?: plannedWE.exerciseId
                    val decision = AdaptationDecisionEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = "",
                        programId = "",
                        sessionId = plannedWE.sessionId,
                        decisionType = "LOAD_REDUCTION",
                        decision = "تثبیت و کاهش محافظه‌کارانه بار در $exName",
                        reason = "ثبت خستگی بالا (RPE ${String.format("%.1f", avgRpe)}) جهت ریکاوری و تحکیم فرم حرکتی.",
                        programVersion = 1
                    )
                    Pair(newWE, decision)
                } else {
                    Pair(plannedWE, null)
                }
            } else {
                Pair(plannedWE, null)
            }
        }
    }
}
