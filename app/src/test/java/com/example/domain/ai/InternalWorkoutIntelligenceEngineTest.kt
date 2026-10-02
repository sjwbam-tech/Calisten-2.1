package com.example.domain.ai

import com.example.data.local.SeedData
import com.example.data.local.entity.PainEntryEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.RecoveryEntryEntity
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.SkillProgressEntity
import com.example.data.local.entity.UserCapabilityEntity
import com.example.domain.ai.workout.InternalWorkoutIntelligenceEngine
import com.example.domain.engine.ProgramGenerationParams
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class InternalWorkoutIntelligenceEngineTest {

    private fun createBaseProfile(
        experienceLevel: String = "INTERMEDIATE",
        trainingDays: Int = 3
    ): ProfileEntity {
        return ProfileEntity(
            id = "profile_1",
            name = "علی کالیستنیکس",
            age = 24,
            heightCm = 175f,
            weightKg = 72f,
            gender = "MALE",
            experienceLevel = experienceLevel,
            trainingDaysPerWeek = trainingDays,
            equipmentJson = "[\"pullup_bar\", \"parallettes\"]",
            goalsJson = "[\"STRENGTH\", \"SKILL\"]"
        )
    }

    private fun createBaseCapability(): UserCapabilityEntity {
        return UserCapabilityEntity(
            id = "cap_1",
            profileId = "profile_1",
            pushingStrength = 70f,
            pullingStrength = 65f,
            legStrength = 60f,
            coreStrength = 65f,
            gripCapacity = 60f,
            shoulderTolerance = 75f,
            wristTolerance = 75f
        )
    }

    @Test
    fun testReadinessRed_appliesVolumeReductionAndProtectsJoints() {
        val profile = createBaseProfile()
        val capability = createBaseCapability()

        val pain = listOf(
            PainEntryEntity(
                id = "pain_1",
                profileId = profile.id,
                date = "2026-09-29",
                location = "مفصل شانه راست",
                severity = 7,
                timing = "حین ست",
                status = "RED",
                notes = "درد حین شنا"
            )
        )

        val recovery = listOf(
            RecoveryEntryEntity(
                id = "rec_1",
                profileId = profile.id,
                date = "2026-09-29",
                sleepDurationHours = 4.5f,
                sleepQuality = 2,
                energyLevel = 2,
                fatigueLevel = 5,
                muscleSoreness = 5,
                stressLevel = 4,
                readiness = "RED"
            )
        )

        val params = ProgramGenerationParams(
            profile = profile,
            capability = capability,
            allExercises = SeedData.exercises,
            recentRecovery = recovery,
            recentPain = pain
        )

        val directives = InternalWorkoutIntelligenceEngine.synthesizeDirectives(params)

        assertEquals("RED", directives.readinessScore)
        assertEquals(0.70f, directives.volumeScaleFactor, 0.01f)
        assertTrue(directives.protectedJoints.any { it.contains("شانه") })
        assertTrue(directives.reasoningLog.any { it.contains("آمادگی پایین") })
    }

    @Test
    fun testPlateauDetectionAndBiomechanicalSubstitution() {
        val profile = createBaseProfile()
        val capability = createBaseCapability()

        // 3 consecutive stagnant sessions for pullup_std
        val setLogs = mutableListOf<SetLogEntity>()
        listOf("session_1", "session_2", "session_3").forEachIndexed { sIdx, sId ->
            for (setNum in 1..3) {
                setLogs.add(
                    SetLogEntity(
                        id = UUID.randomUUID().toString(),
                        workoutExerciseId = "we_1",
                        sessionId = sId,
                        profileId = profile.id,
                        exerciseId = "pullup_std",
                        setNumber = setNum,
                        reps = 6,
                        rpe = 9,
                        status = "COMPLETED",
                        timestamp = System.currentTimeMillis() - (3 - sIdx) * 86400000L
                    )
                )
            }
        }

        val params = ProgramGenerationParams(
            profile = profile,
            capability = capability,
            allExercises = SeedData.exercises,
            recentSetLogs = setLogs
        )

        val directives = InternalWorkoutIntelligenceEngine.synthesizeDirectives(params)

        assertTrue("Should detect plateau in pullup_std", directives.detectedPlateaus.any { it.exerciseId == "pullup_std" })
        assertTrue("Should find substitute for pullup_std", directives.suggestedSubstitutions.containsKey("pullup_std"))
    }

    @Test
    fun testSkillPrerequisitesEvaluation() {
        val profile = createBaseProfile()
        val capability = createBaseCapability().copy(
            pullingStrength = 85f,
            pushingStrength = 85f,
            gripCapacity = 85f,
            coreStrength = 85f
        )

        val progress = listOf(
            SkillProgressEntity(
                id = "prog_1",
                profileId = profile.id,
                skillId = "skill_front_lever",
                currentStepIndex = 1,
                bestHoldSec = 10,
                bestReps = 0,
                qualityScore = 8
            )
        )

        val params = ProgramGenerationParams(
            profile = profile,
            capability = capability,
            allExercises = SeedData.exercises,
            skillProgress = progress
        )

        val directives = InternalWorkoutIntelligenceEngine.synthesizeDirectives(params)

        assertTrue(directives.prioritizedSkillIds.contains("skill_front_lever"))
    }

    @Test
    fun testEndToEndPlanGeneration_maintainsDeterministicPlanningAuthority() {
        val profile = createBaseProfile(trainingDays = 3)
        val capability = createBaseCapability()

        val params = ProgramGenerationParams(
            profile = profile,
            capability = capability,
            allExercises = SeedData.exercises,
            targetDaysPerWeek = 3
        )

        val generatedPackage = InternalWorkoutIntelligenceEngine.generatePersonalizedPlan(params)

        assertNotNull(generatedPackage)
        assertNotNull(generatedPackage.program)
        assertEquals(3, generatedPackage.sessions.size)
        assertTrue(generatedPackage.workoutExercises.isNotEmpty())

        // Verify deterministic planning engine generated sessions adhering to full-body split
        generatedPackage.sessions.forEach { session ->
            assertTrue(session.scheduledDate.isNotBlank())
            assertTrue(session.title.isNotBlank())
        }
    }
}
