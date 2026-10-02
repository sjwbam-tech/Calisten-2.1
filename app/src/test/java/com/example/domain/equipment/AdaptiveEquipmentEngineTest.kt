package com.example.domain.equipment

import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.exercises.AdaptiveEquipmentExerciseData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdaptiveEquipmentEngineTest {

    private val allExercises = AdaptiveEquipmentExerciseData.exercises + listOf(
        ExerciseEntity(
            id = "pushup_std",
            persianName = "شنا استاندارد",
            englishName = "Standard Push-Up",
            category = "PUSH",
            movementPattern = "HORIZONTAL_PUSH",
            equipmentRequiredJson = "[]",
            difficulty = "BEGINNER",
            startingPosition = "زمین",
            tempo = "2-0-1-0",
            minReps = 6,
            maxReps = 12,
            restRecommendationSec = 60
        ),
        ExerciseEntity(
            id = "pullup_std",
            persianName = "بارفیکس استاندارد",
            englishName = "Standard Pull-Up",
            category = "PULL",
            movementPattern = "VERTICAL_PULL",
            equipmentRequiredJson = "[\"pullup_bar\"]",
            difficulty = "INTERMEDIATE",
            startingPosition = "آویزان از میله",
            tempo = "2-1-1-0",
            minReps = 4,
            maxReps = 8,
            restRecommendationSec = 90
        ),
        ExerciseEntity(
            id = "squat_bodyweight",
            persianName = "اسکات با وزن بدن",
            englishName = "Bodyweight Squat",
            category = "LEGS",
            movementPattern = "SQUAT",
            equipmentRequiredJson = "[]",
            difficulty = "BEGINNER",
            startingPosition = "ایستاده",
            tempo = "2-0-1-0",
            minReps = 10,
            maxReps = 20,
            restRecommendationSec = 60
        )
    )

    @Test
    fun testEquipmentProfile_SerializationAndDeserialization() {
        val original = UserEquipmentProfile(
            hasBodyweightOnly = false,
            hasSingleDumbbell = true,
            singleDumbbellWeightKg = 12.5f,
            hasPairDumbbells = true,
            pairDumbbellWeightKg = 15f,
            hasAdjustableDumbbells = true,
            adjustableMinWeightKg = 2.5f,
            adjustableMaxWeightKg = 32f,
            adjustableIncrementKg = 2.5f,
            hasBarbell = true,
            barbellTotalWeightKg = 50f,
            hasResistanceBands = true,
            resistanceBandLevel = "ALL",
            hasPullupBar = true,
            hasParallettes = true,
            hasRings = false,
            hasWeightVest = true,
            weightVestWeightKg = 10f,
            hasBench = true
        )

        val json = original.toJson()
        val restored = UserEquipmentProfile.fromJson(json)

        assertEquals(original.hasSingleDumbbell, restored.hasSingleDumbbell)
        assertEquals(original.singleDumbbellWeightKg, restored.singleDumbbellWeightKg, 0.01f)
        assertEquals(original.pairDumbbellWeightKg, restored.pairDumbbellWeightKg, 0.01f)
        assertEquals(original.adjustableMaxWeightKg, restored.adjustableMaxWeightKg, 0.01f)
        assertEquals(original.barbellTotalWeightKg, restored.barbellTotalWeightKg, 0.01f)
        assertEquals(original.hasResistanceBands, restored.hasResistanceBands)
        assertEquals(original.hasPullupBar, restored.hasPullupBar)
        assertEquals(original.hasWeightVest, restored.hasWeightVest)
        assertEquals(original.hasBench, restored.hasBench)
    }

    @Test
    fun testBodyweightOnlyProfile_DisallowsWeightedAndGymEquipment() {
        val bodyweightProfile = UserEquipmentProfile(hasBodyweightOnly = true)

        assertTrue(bodyweightProfile.isPureBodyweight)
        assertTrue(bodyweightProfile.hasEquipmentFor(emptyList()))
        assertTrue(bodyweightProfile.hasEquipmentFor(listOf("floor", "wall")))

        assertFalse(bodyweightProfile.hasEquipmentFor(listOf("dumbbell_single")))
        assertFalse(bodyweightProfile.hasEquipmentFor(listOf("barbell")))
        assertFalse(bodyweightProfile.hasEquipmentFor(listOf("pullup_bar")))
    }

    @Test
    fun testExerciseSubstitution_WhenEquipmentNotOwned() {
        // Trainee has ONLY a single dumbbell (no pull-up bar, no barbell)
        val singleDbProfile = UserEquipmentProfile(
            hasSingleDumbbell = true,
            singleDumbbellWeightKg = 10f,
            hasPullupBar = false
        )

        val pullupExercise = allExercises.first { it.id == "pullup_std" }
        assertFalse(AdaptiveEquipmentEngine.canPerform(pullupExercise, singleDbProfile))

        val substitution = AdaptiveEquipmentEngine.substituteIfUnavailable(
            exercise = pullupExercise,
            equipment = singleDbProfile,
            allExercises = allExercises
        )

        // Must replace with an exercise feasible with single dumbbell or bodyweight
        assertTrue(AdaptiveEquipmentEngine.canPerform(substitution, singleDbProfile))
        assertNotNull(substitution)
    }

    @Test
    fun testWeightBasedIntensityAdjustment_LightWeightIncreasesRepsAndTempo() {
        val lightProfile = UserEquipmentProfile(
            hasSingleDumbbell = true,
            singleDumbbellWeightKg = 5f // Light weight for 75kg trainee
        )

        val gobletSquat = allExercises.firstOrNull { it.id == "db_goblet_squat" } ?: allExercises.first()
        val prescription = AdaptiveEquipmentEngine.adaptPrescription(
            targetExercise = gobletSquat,
            equipment = lightProfile,
            userLevel = "INTERMEDIATE",
            userWeightKg = 75f
        )

        assertTrue(prescription.isAdaptedForWeight)
        assertNotNull(prescription.adaptationNote)
        assertEquals("3-1-2-0", prescription.tempo)
        assertTrue(prescription.targetReps >= 12)
    }

    @Test
    fun testWeightBasedIntensityAdjustment_HeavyWeightUsesStrengthReps() {
        val heavyProfile = UserEquipmentProfile(
            hasSingleDumbbell = true,
            singleDumbbellWeightKg = 30f // Heavy single dumbbell
        )

        val singleRow = allExercises.firstOrNull { it.id == "db_single_row" } ?: allExercises.first()
        val prescription = AdaptiveEquipmentEngine.adaptPrescription(
            targetExercise = singleRow,
            equipment = heavyProfile,
            userLevel = "ADVANCED",
            userWeightKg = 75f
        )

        assertTrue(prescription.isAdaptedForWeight)
        assertEquals(5, prescription.targetReps)
        assertEquals(8, prescription.targetRpe)
        assertEquals(120, prescription.restSec)
    }

    @Test
    fun testBeginnerSafetyProtection_PreventsDangerousLoading() {
        val heavyProfile = UserEquipmentProfile(
            hasSingleDumbbell = true,
            singleDumbbellWeightKg = 30f // Heavy single dumbbell for a beginner
        )

        val pressExercise = allExercises.firstOrNull { it.id == "db_single_overhead_press" } ?: allExercises.first()
        val prescription = AdaptiveEquipmentEngine.adaptPrescription(
            targetExercise = pressExercise,
            equipment = heavyProfile,
            userLevel = "BEGINNER",
            userWeightKg = 70f
        )

        assertTrue(prescription.isAdaptedForWeight)
        // Conservative RPE 6 to protect connective tissue
        assertEquals(6, prescription.targetRpe)
        assertTrue(prescription.adaptationNote?.contains("ایمنی") == true)
    }
}
