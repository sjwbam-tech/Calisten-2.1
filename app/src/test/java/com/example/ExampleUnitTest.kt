package com.example

import com.example.data.local.SeedData
import com.example.data.local.entity.SetLogEntity
import com.example.domain.engine.DecisionType
import com.example.domain.engine.ExerciseRecommendationEngine
import com.example.domain.engine.PlateauEngine
import com.example.domain.engine.ProgressionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testProgressionEngine_advancesOneVariableOnSuccess() {
        val pushupEx = SeedData.exercises.first { it.id == "pushup_std" }
        val logs = listOf(
            SetLogEntity(
                id = "1", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = pushupEx.id,
                setNumber = 1, reps = 8, rpe = 7, status = "COMPLETED"
            ),
            SetLogEntity(
                id = "2", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = pushupEx.id,
                setNumber = 2, reps = 8, rpe = 7, status = "COMPLETED"
            ),
            SetLogEntity(
                id = "3", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = pushupEx.id,
                setNumber = 3, reps = 8, rpe = 7, status = "COMPLETED"
            )
        )

        val decision = ProgressionEngine.evaluateProgression(
            exercise = pushupEx,
            currentSets = 3,
            currentReps = 8,
            currentHoldSec = 0,
            recentSetLogs = logs,
            recentSleepAvg = 8f,
            recentFatigueAvg = 1
        )

        assertEquals(DecisionType.ADVANCE_ONE_VARIABLE, decision.decisionType)
        assertEquals(9, decision.newTargetReps) // Advanced exactly one rep!
        assertEquals(3, decision.newTargetSets) // Sets preserved
    }

    @Test
    fun testProgressionEngine_regressesConservativelyOnPain() {
        val pushupEx = SeedData.exercises.first { it.id == "pushup_std" }
        val logsWithPain = listOf(
            SetLogEntity(
                id = "1", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = pushupEx.id,
                setNumber = 1, reps = 8, rpe = 7, status = "PAIN_STOP", painSeverity = 5
            )
        )

        val decision = ProgressionEngine.evaluateProgression(
            exercise = pushupEx,
            currentSets = 3,
            currentReps = 8,
            currentHoldSec = 0,
            recentSetLogs = logsWithPain
        )

        assertEquals(DecisionType.DELOAD_OR_REGRESS, decision.decisionType)
        assertTrue(decision.newTargetReps < 8)
    }

    @Test
    fun testPlateauEngine_exactInsufficientDataMessage() {
        val result = PlateauEngine.analyzePlateau(
            exerciseId = "pushup_std",
            sessionsLogs = mapOf("sess1" to emptyList())
        )

        assertFalse(result.hasEnoughData)
        assertEquals(PlateauEngine.INSUFFICIENT_DATA_MESSAGE, result.message)
    }

    @Test
    fun testExerciseRecommendation_suggestsReplacementOverAddingVolume() {
        val currentExs = listOf(SeedData.exercises.first { it.id == "pushup_std" })
        val recommendations = ExerciseRecommendationEngine.findRecommendation(
            currentExercises = currentExs,
            allExercises = SeedData.exercises,
            availableEquipment = listOf("pullup_bar"),
            experienceLevel = "BEGINNER"
        )

        assertTrue(recommendations.isNotEmpty())
        // Should find recommendation replacing or complementing
        val diamondRec = recommendations.firstOrNull { it.proposedExercise.id == "pushup_diamond" }
        if (diamondRec != null) {
            assertEquals("pushup_std", diamondRec.exerciseToReplace?.id)
        }
    }
}
