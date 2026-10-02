package com.example.domain.engine

import com.example.data.local.SeedData
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.PainEntryEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.RecoveryEntryEntity
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.UserCapabilityEntity
import com.example.data.local.entity.WorkoutExerciseEntity
import com.example.data.local.entity.WorkoutSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class PlanningEngineUnitTest {

    private fun createBaseProfile(
        experienceLevel: String = "INTERMEDIATE",
        trainingDays: Int = 3,
        equipment: List<String> = listOf("pullup_bar", "parallettes"),
        goals: List<String> = listOf("STRENGTH")
    ): ProfileEntity {
        val equipJson = "[" + equipment.joinToString(",") { "\"$it\"" } + "]"
        val goalsJson = "[" + goals.joinToString(",") { "\"$it\"" } + "]"
        return ProfileEntity(
            id = "test_profile_1",
            name = "ورزشکار تست",
            age = 25,
            heightCm = 178f,
            weightKg = 74f,
            gender = "MALE",
            experienceLevel = experienceLevel,
            trainingDaysPerWeek = trainingDays,
            equipmentJson = equipJson,
            goalsJson = goalsJson
        )
    }

    private fun createBaseCapability(
        push: Float = 60f,
        pull: Float = 60f,
        legs: Float = 60f,
        core: Float = 60f,
        grip: Float = 60f,
        shoulderTolerance: Float = 75f,
        wristTolerance: Float = 75f
    ): UserCapabilityEntity {
        return UserCapabilityEntity(
            id = "test_cap_1",
            profileId = "test_profile_1",
            pushingStrength = push,
            pullingStrength = pull,
            legStrength = legs,
            coreStrength = core,
            gripCapacity = grip,
            scapularControl = 65f,
            shoulderTolerance = shoulderTolerance,
            wristTolerance = wristTolerance,
            mobilityScore = 70f,
            skillProficiency = 50f,
            workCapacity = 60f,
            recoveryCapacity = 75f,
            trainingAgeMonths = 12,
            consistencyScore = 80f,
            techniqueConfidence = 80f,
            lastUpdated = System.currentTimeMillis()
        )
    }

    // =========================================================================
    // 1. DIFFERENT GOALS
    // =========================================================================

    @Test
    fun `test program generation adjusts volume and rest for STRENGTH goal`() {
        val profile = createBaseProfile(goals = listOf("STRENGTH"))
        val capability = createBaseCapability()
        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = SeedData.exercises
            )
        )

        val repExercise = pkg.workoutExercises.first { it.targetReps > 0 }
        assertEquals("Target reps for STRENGTH should be 5", 5, repExercise.targetReps)
        assertEquals("Target RPE for STRENGTH should be 8", 8, repExercise.targetRpe)
        assertEquals("Rest for STRENGTH should be 120s", 120, repExercise.restSec)
    }

    @Test
    fun `test program generation adjusts volume and rest for HYPERTROPHY goal`() {
        val profile = createBaseProfile(goals = listOf("HYPERTROPHY"))
        val capability = createBaseCapability()
        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = SeedData.exercises
            )
        )

        val repExercise = pkg.workoutExercises.first { it.targetReps > 0 }
        assertEquals("Target reps for HYPERTROPHY should be 10", 10, repExercise.targetReps)
        assertEquals("Rest for HYPERTROPHY should be 90s", 90, repExercise.restSec)
    }

    @Test
    fun `test program generation adjusts volume and rest for ENDURANCE goal`() {
        val profile = createBaseProfile(goals = listOf("ENDURANCE"))
        val capability = createBaseCapability()
        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = SeedData.exercises
            )
        )

        val repExercise = pkg.workoutExercises.first { it.targetReps > 0 }
        assertEquals("Target reps for ENDURANCE should be 18", 18, repExercise.targetReps)
        assertEquals("Rest for ENDURANCE should be 60s", 60, repExercise.restSec)
    }

    // =========================================================================
    // 2. DIFFERENT EXPERIENCE LEVELS
    // =========================================================================

    @Test
    fun `test BEGINNER receives foundational exercises and lower volume`() {
        val profile = createBaseProfile(experienceLevel = "BEGINNER", trainingDays = 3)
        val capability = createBaseCapability(push = 25f, pull = 25f, legs = 25f)
        val assessment = AssessmentEntity(
            id = "a1", profileId = profile.id, pushupsMax = 5, pullupsMax = 1, squatsMax = 12, plankSec = 25
        )

        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = SeedData.exercises,
                assessment = assessment,
                sessionDurationMinutes = 45
            )
        )

        val assignedIds = pkg.workoutExercises.map { it.exerciseId }.toSet()
        // Beginner with low capability should get knee pushup, inverted row, bodyweight squat, plank
        assertTrue("Beginner should receive pushup_knee", assignedIds.contains("pushup_knee"))
        assertTrue("Beginner should receive inverted_row", assignedIds.contains("inverted_row"))
        assertTrue("Beginner should receive squat_bodyweight", assignedIds.contains("squat_bodyweight"))
        // Should NOT receive advanced pistol squat or dips parallel
        assertFalse("Beginner should not receive squat_pistol", assignedIds.contains("squat_pistol"))
        assertFalse("Beginner should not receive dips_parallel", assignedIds.contains("dips_parallel"))
    }

    @Test
    fun `test ADVANCED receives high difficulty exercises and higher set volume`() {
        val profile = createBaseProfile(experienceLevel = "ADVANCED", trainingDays = 3)
        val capability = createBaseCapability(push = 85f, pull = 85f, legs = 85f)
        val assessment = AssessmentEntity(
            id = "a1", profileId = profile.id, pushupsMax = 35, pullupsMax = 18, squatsMax = 40, plankSec = 90
        )

        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = SeedData.exercises,
                assessment = assessment,
                sessionDurationMinutes = 60
            )
        )

        val assignedIds = pkg.workoutExercises.map { it.exerciseId }.toSet()
        assertTrue("Advanced should receive dips_parallel", assignedIds.contains("dips_parallel"))
        assertTrue("Advanced should receive pullup_std", assignedIds.contains("pullup_std"))
        assertTrue("Advanced should receive squat_pistol", assignedIds.contains("squat_pistol"))

        // Set volume should be 4 sets for primary exercises
        val pushExercises = pkg.workoutExercises.filter { it.exerciseId == "dips_parallel" }
        assertTrue(pushExercises.any { it.targetSets >= 4 })
    }

    // =========================================================================
    // 3. DIFFERENT EQUIPMENT CONSTRAINTS
    // =========================================================================

    @Test
    fun `test program generation substitutes exercises when pullup bar is missing`() {
        val profile = createBaseProfile(equipment = emptyList()) // No pullup bar!
        val capability = createBaseCapability(pull = 70f)

        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = SeedData.exercises
            )
        )

        val assignedIds = pkg.workoutExercises.map { it.exerciseId }.toSet()
        assertFalse("Cannot assign pullup_std without pullup bar", assignedIds.contains("pullup_std"))
        assertFalse("Cannot assign dead_hang without pullup bar", assignedIds.contains("dead_hang"))
        assertFalse("Cannot assign hanging_leg_raise without pullup bar", assignedIds.contains("hanging_leg_raise"))
        assertTrue("Should substitute inverted_row for pulling", assignedIds.contains("inverted_row"))

        val equipDecision = pkg.decisions.firstOrNull { it.decisionType == "EQUIPMENT_ADAPTATION" }
        assertNotNull("Should emit an EQUIPMENT_ADAPTATION decision", equipDecision)
    }

    @Test
    fun `test program generation substitutes dips when parallettes are missing`() {
        val profile = createBaseProfile(equipment = listOf("pullup_bar")) // Has bar, no parallettes
        val capability = createBaseCapability(push = 75f)

        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = SeedData.exercises
            )
        )

        val assignedIds = pkg.workoutExercises.map { it.exerciseId }.toSet()
        assertFalse("Cannot assign dips_parallel without parallettes", assignedIds.contains("dips_parallel"))
        assertTrue("Should assign diamond pushup or bench dip", assignedIds.contains("pushup_diamond") || assignedIds.contains("dips_bench"))
    }

    // =========================================================================
    // 4. DIFFERENT TRAINING FREQUENCIES & SCHEDULE SPACING
    // =========================================================================

    @Test
    fun `test 2-day frequency generates FULL_BODY_2DAY with 72h spacing`() {
        val profile = createBaseProfile(trainingDays = 2)
        val capability = createBaseCapability()

        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(profile = profile, capability = capability, allExercises = SeedData.exercises)
        )

        assertEquals(2, pkg.sessions.size)
        assertTrue(pkg.initialVersion.changesSummary.contains("FULL_BODY_2DAY"))
        assertNotEquals("Dates must not be identical", pkg.sessions[0].scheduledDate, pkg.sessions[1].scheduledDate)
    }

    @Test
    fun `test 4-day frequency generates UPPER_LOWER split with balanced recovery`() {
        val profile = createBaseProfile(trainingDays = 4)
        val capability = createBaseCapability()

        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(profile = profile, capability = capability, allExercises = SeedData.exercises)
        )

        assertEquals(4, pkg.sessions.size)
        assertTrue(pkg.initialVersion.changesSummary.contains("UPPER_LOWER"))
        // Check session titles reflect Upper and Lower
        assertTrue(pkg.sessions[0].title.contains("Upper") || pkg.sessions[0].title.contains("بالاتنه"))
        assertTrue(pkg.sessions[1].title.contains("Lower") || pkg.sessions[1].title.contains("پایین‌تنه"))
    }

    // =========================================================================
    // 5. SKILL PREREQUISITES (ALL 8 SKILLS)
    // =========================================================================

    @Test
    fun `test Front Lever prerequisites - foundation when weak vs tuck when strong`() {
        val weakCap = createBaseCapability(pull = 30f)
        val weakAssessment = AssessmentEntity(id = "a1", profileId = "p1", pullupsMax = 3, deadHangSec = 15)
        val weakEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.FRONT_LEVER,
            capability = weakCap,
            assessment = weakAssessment,
            availableEquipment = listOf("pullup_bar"),
            allExercises = SeedData.exercises
        )
        assertFalse("Weak athlete should not meet Front Lever prerequisites", weakEval.meetsPrerequisites)
        assertEquals("Should assign inverted_row as foundation", "inverted_row", weakEval.chosenExerciseId)

        val strongCap = createBaseCapability(pull = 75f)
        val strongAssessment = AssessmentEntity(id = "a2", profileId = "p1", pullupsMax = 12, deadHangSec = 45, plankSec = 45)
        val strongEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.FRONT_LEVER,
            capability = strongCap,
            assessment = strongAssessment,
            availableEquipment = listOf("pullup_bar"),
            allExercises = SeedData.exercises
        )
        assertTrue("Strong athlete should meet Front Lever prerequisites", strongEval.meetsPrerequisites)
        assertEquals("Should assign front_lever_tuck", "front_lever_tuck", strongEval.chosenExerciseId)
        assertTrue("Target hold should be >= 10s", strongEval.targetHoldSec >= 10)
    }

    @Test
    fun `test Planche prerequisites - knee pushup on wrist pain vs diamond or dip when ready`() {
        val injuredCap = createBaseCapability(push = 70f, wristTolerance = 30f) // Wrist pain
        val injuredEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.PLANCHE,
            capability = injuredCap,
            assessment = AssessmentEntity(id = "a1", profileId = "p1", pushupsMax = 25),
            availableEquipment = listOf("parallettes"),
            allExercises = SeedData.exercises
        )
        assertFalse("Low wrist tolerance should block Planche prerequisites", injuredEval.meetsPrerequisites)
        assertEquals("pushup_knee", injuredEval.chosenExerciseId)

        val readyCap = createBaseCapability(push = 75f, wristTolerance = 70f)
        val readyEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.PLANCHE,
            capability = readyCap,
            assessment = AssessmentEntity(id = "a2", profileId = "p1", pushupsMax = 25, plankSec = 50),
            availableEquipment = listOf("parallettes"),
            allExercises = SeedData.exercises
        )
        assertTrue("Ready athlete should meet Planche prerequisites", readyEval.meetsPrerequisites)
        assertEquals("dips_parallel", readyEval.chosenExerciseId)
    }

    @Test
    fun `test Muscle-Up prerequisites - vertical pull foundation vs explosive pullup`() {
        val weakEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.MUSCLE_UP,
            capability = createBaseCapability(pull = 35f),
            assessment = AssessmentEntity(id = "a1", profileId = "p1", pullupsMax = 4),
            availableEquipment = listOf("pullup_bar"),
            allExercises = SeedData.exercises
        )
        assertFalse(weakEval.meetsPrerequisites)
        assertEquals("pullup_chinup", weakEval.chosenExerciseId)

        val readyEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.MUSCLE_UP,
            capability = createBaseCapability(pull = 70f, push = 65f),
            assessment = AssessmentEntity(id = "a2", profileId = "p1", pullupsMax = 12),
            availableEquipment = listOf("pullup_bar"),
            allExercises = SeedData.exercises
        )
        assertTrue(readyEval.meetsPrerequisites)
        assertEquals("pullup_std", readyEval.chosenExerciseId)
    }

    @Test
    fun `test Handstand prerequisites - plank foundation vs handstand hold`() {
        val weakEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.HANDSTAND,
            capability = createBaseCapability(wristTolerance = 30f),
            assessment = AssessmentEntity(id = "a1", profileId = "p1", pushupsMax = 5, plankSec = 20),
            availableEquipment = listOf("wall"),
            allExercises = SeedData.exercises
        )
        assertFalse(weakEval.meetsPrerequisites)
        assertEquals("plank_std", weakEval.chosenExerciseId)

        val readyEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.HANDSTAND,
            capability = createBaseCapability(wristTolerance = 60f, shoulderTolerance = 60f),
            assessment = AssessmentEntity(id = "a2", profileId = "p1", pushupsMax = 15, plankSec = 50),
            availableEquipment = listOf("wall"),
            allExercises = SeedData.exercises
        )
        assertTrue(readyEval.meetsPrerequisites)
        assertEquals("handstand", readyEval.chosenExerciseId)
    }

    @Test
    fun `test L-Sit and V-Sit prerequisites evaluate properly`() {
        val lsitEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.LSIT,
            capability = createBaseCapability(core = 50f, push = 45f),
            assessment = AssessmentEntity(id = "a1", profileId = "p1", plankSec = 40),
            availableEquipment = listOf("parallettes"),
            allExercises = SeedData.exercises
        )
        assertTrue("Meets L-Sit prerequisites", lsitEval.meetsPrerequisites)
        assertEquals("lsit", lsitEval.chosenExerciseId)

        val vsitWeakEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.VSIT,
            capability = createBaseCapability(core = 40f),
            assessment = AssessmentEntity(id = "a2", profileId = "p1", plankSec = 35),
            availableEquipment = listOf("parallettes", "pullup_bar"),
            allExercises = SeedData.exercises
        )
        assertFalse("Does not meet V-Sit prerequisites yet", vsitWeakEval.meetsPrerequisites)

        val vsitReadyEval = SkillProgressionRegistry.evaluateSkill(
            skill = SupportedSkill.VSIT,
            capability = createBaseCapability(core = 75f, push = 60f),
            assessment = AssessmentEntity(id = "a3", profileId = "p1", plankSec = 60),
            availableEquipment = listOf("parallettes"),
            allExercises = SeedData.exercises
        )
        assertTrue("Meets V-Sit prerequisites", vsitReadyEval.meetsPrerequisites)
        assertEquals("lsit", vsitReadyEval.chosenExerciseId)
    }

    // =========================================================================
    // 6. PROGRESSION AFTER SUCCESSFUL WORKOUTS
    // =========================================================================

    @Test
    fun `test ProgressionEngine advances repetition exercise by exactly 1 rep`() {
        val exercise = SeedData.exercises.first { it.id == "pushup_std" }
        val successfulLogs = listOf(
            SetLogEntity(id = "1", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = exercise.id, setNumber = 1, reps = 8, rpe = 7, status = "COMPLETED"),
            SetLogEntity(id = "2", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = exercise.id, setNumber = 2, reps = 8, rpe = 7, status = "COMPLETED"),
            SetLogEntity(id = "3", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = exercise.id, setNumber = 3, reps = 8, rpe = 7, status = "COMPLETED")
        )

        val decision = ProgressionEngine.evaluateProgression(
            exercise = exercise,
            currentSets = 3,
            currentReps = 8,
            currentHoldSec = 0,
            recentSetLogs = successfulLogs,
            recentSleepAvg = 8f,
            recentFatigueAvg = 1
        )

        assertEquals(DecisionType.ADVANCE_ONE_VARIABLE, decision.decisionType)
        assertEquals("Target reps should advance from 8 to 9", 9, decision.newTargetReps)
    }

    @Test
    fun `test ProgressionEngine advances isometric exercise by 3 seconds hold`() {
        val exercise = SeedData.exercises.first { it.id == "plank_std" }
        val successfulLogs = listOf(
            SetLogEntity(id = "1", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = exercise.id, setNumber = 1, reps = 0, holdSeconds = 30, rpe = 7, status = "COMPLETED"),
            SetLogEntity(id = "2", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = exercise.id, setNumber = 2, reps = 0, holdSeconds = 30, rpe = 7, status = "COMPLETED")
        )

        val decision = ProgressionEngine.evaluateProgression(
            exercise = exercise,
            currentSets = 2,
            currentReps = 0,
            currentHoldSec = 30,
            recentSetLogs = successfulLogs,
            recentSleepAvg = 7.5f,
            recentFatigueAvg = 2
        )

        assertEquals(DecisionType.ADVANCE_ONE_VARIABLE, decision.decisionType)
        assertEquals("Target hold should advance from 30 to 33", 33, decision.newTargetHoldSec)
    }

    // =========================================================================
    // 7. REGRESSION AFTER POOR PERFORMANCE & PAIN
    // =========================================================================

    @Test
    fun `test ProgressionEngine regresses immediately on pain stop`() {
        val exercise = SeedData.exercises.first { it.id == "pushup_std" }
        val painLogs = listOf(
            SetLogEntity(id = "1", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = exercise.id, setNumber = 1, reps = 6, rpe = 9, status = "PAIN_STOP", painSeverity = 5)
        )

        val decision = ProgressionEngine.evaluateProgression(
            exercise = exercise,
            currentSets = 3,
            currentReps = 8,
            currentHoldSec = 0,
            recentSetLogs = painLogs
        )

        assertEquals(DecisionType.DELOAD_OR_REGRESS, decision.decisionType)
        assertTrue("Target reps should decrease on pain", decision.newTargetReps < 8)
    }

    @Test
    fun `test ProgressionEngine regresses on severe performance drop`() {
        val exercise = SeedData.exercises.first { it.id == "pushup_std" }
        // User attempted 8 reps but only completed 3 reps each set
        val failedLogs = listOf(
            SetLogEntity(id = "1", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = exercise.id, setNumber = 1, reps = 3, rpe = 10, status = "COMPLETED"),
            SetLogEntity(id = "2", workoutExerciseId = "w1", sessionId = "s1", profileId = "p1", exerciseId = exercise.id, setNumber = 2, reps = 2, rpe = 10, status = "COMPLETED")
        )

        val decision = ProgressionEngine.evaluateProgression(
            exercise = exercise,
            currentSets = 3,
            currentReps = 8,
            currentHoldSec = 0,
            recentSetLogs = failedLogs
        )

        assertEquals(DecisionType.DELOAD_OR_REGRESS, decision.decisionType)
        assertTrue("Target reps should be regressed due to severe drop", decision.newTargetReps < 8)
    }

    // =========================================================================
    // 8. RECOVERY CONFLICTS & PAIN AVOIDANCE
    // =========================================================================

    @Test
    fun `test shoulder pain eliminates high shoulder load exercises`() {
        val profile = createBaseProfile(experienceLevel = "ADVANCED")
        val capability = createBaseCapability(push = 80f, pull = 80f)
        val painList = listOf(
            PainEntryEntity(id = "p1", profileId = profile.id, date = "2026-09-29", location = "شانه راست", severity = 4, timing = "حین ست", status = "RED")
        )

        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = SeedData.exercises,
                recentPain = painList
            )
        )

        val assignedIds = pkg.workoutExercises.map { it.exerciseId }.toSet()
        assertFalse("Shoulder pain must eliminate dips_parallel", assignedIds.contains("dips_parallel"))
        assertFalse("Shoulder pain must eliminate pullup_std", assignedIds.contains("pullup_std"))
        assertTrue("Should substitute safe knee pushup", assignedIds.contains("pushup_knee"))
        assertTrue("Should substitute inverted row", assignedIds.contains("inverted_row"))

        val painDecision = pkg.decisions.firstOrNull { it.decisionType == "PAIN_AVOIDANCE" }
        assertNotNull("Should emit a PAIN_AVOIDANCE decision", painDecision)
    }

    @Test
    fun `test high fatigue and low sleep modulates total session sets`() {
        val profile = createBaseProfile()
        val capability = createBaseCapability()
        val recoveryList = listOf(
            RecoveryEntryEntity(
                id = "r1",
                profileId = profile.id,
                date = "2026-09-28",
                sleepDurationHours = 4.5f,
                sleepQuality = 2,
                energyLevel = 2,
                fatigueLevel = 5,
                muscleSoreness = 4,
                stressLevel = 3,
                readiness = "RED"
            )
        )

        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = SeedData.exercises,
                recentRecovery = recoveryList
            )
        )

        // Base sets should be capped at 2 due to fatigue
        pkg.workoutExercises.forEach {
            assertTrue("Fatigued athlete sets must be capped at 2", it.targetSets <= 2)
        }
        val recoveryDecision = pkg.decisions.firstOrNull { it.decisionType == "RECOVERY_MODULATION" }
        assertNotNull("Should emit a RECOVERY_MODULATION decision", recoveryDecision)
    }

    @Test
    fun `test previous day push fatigue switches session 1 to pull or lower body`() {
        val profile = createBaseProfile(trainingDays = 4) // Upper/Lower
        val capability = createBaseCapability()
        // Logs from yesterday were heavy pushups
        val recentPushLogs = listOf(
            SetLogEntity(id = "1", workoutExerciseId = "w1", sessionId = "s_old", profileId = profile.id, exerciseId = "pushup_std", setNumber = 1, reps = 10, rpe = 8, status = "COMPLETED"),
            SetLogEntity(id = "2", workoutExerciseId = "w1", sessionId = "s_old", profileId = profile.id, exerciseId = "dips_parallel", setNumber = 1, reps = 8, rpe = 8, status = "COMPLETED")
        )

        val pkg = DataDrivenProgramGenerator.generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = SeedData.exercises,
                recentSetLogs = recentPushLogs
            )
        )

        // Session 1 must not be Upper A; it should be Lower/Core to avoid conflicting push recovery
        val s1Title = pkg.sessions[0].title
        assertTrue("Session 1 should be Lower/Core when Upper/Push was fatigued yesterday", s1Title.contains("Lower") || s1Title.contains("پایین‌تنه"))

        val conflictDecision = pkg.decisions.firstOrNull { it.decisionType == "RECOVERY_CONFLICT_AVOIDED" }
        assertNotNull("Should emit RECOVERY_CONFLICT_AVOIDED decision", conflictDecision)
    }

    // =========================================================================
    // 9. PLAN MODIFICATION ENGINE (ADD, REMOVE, DIFFICULTY ADJUST)
    // =========================================================================

    @Test
    fun `test PlanModificationEngine adds exercise with correct order index and sets`() {
        val session = WorkoutSessionEntity(id = "s1", profileId = "p1", title = "تست", scheduledDate = "2026-09-29")
        val existing = listOf(
            WorkoutExerciseEntity(id = "w1", sessionId = "s1", exerciseId = "pushup_std", orderIndex = 1, targetSets = 3, targetReps = 8, restSec = 90)
        )
        val newEx = SeedData.exercises.first { it.id == "squat_bodyweight" }

        val result = PlanModificationEngine.addExerciseToSession(session, existing, newEx)
        assertNotNull(result.updatedExercise)
        assertEquals(2, result.updatedExercise?.orderIndex)
        assertEquals(2, result.reindexedSessionExercises.size)
        assertEquals("squat_bodyweight", result.updatedExercise?.exerciseId)
        assertNotNull(result.decision)
    }

    @Test
    fun `test PlanModificationEngine removes exercise and reindexes cleanly`() {
        val session = WorkoutSessionEntity(id = "s1", profileId = "p1", title = "تست", scheduledDate = "2026-09-29")
        val existing = listOf(
            WorkoutExerciseEntity(id = "w1", sessionId = "s1", exerciseId = "pushup_std", orderIndex = 1, targetSets = 3, targetReps = 8, restSec = 90),
            WorkoutExerciseEntity(id = "w2", sessionId = "s1", exerciseId = "pullup_std", orderIndex = 2, targetSets = 3, targetReps = 6, restSec = 120),
            WorkoutExerciseEntity(id = "w3", sessionId = "s1", exerciseId = "plank_std", orderIndex = 3, targetSets = 3, targetReps = 0, targetHoldSec = 30, restSec = 60)
        )

        val result = PlanModificationEngine.removeExerciseFromSession(session, existing, "w2")
        assertEquals(2, result.reindexedSessionExercises.size)
        assertEquals("w1", result.reindexedSessionExercises[0].id)
        assertEquals(1, result.reindexedSessionExercises[0].orderIndex)
        assertEquals("w3", result.reindexedSessionExercises[1].id)
        assertEquals("w3 must be re-indexed to 2", 2, result.reindexedSessionExercises[1].orderIndex)
    }

    @Test
    fun `test PlanModificationEngine adjustDifficulty increases to harder progression`() {
        val session = WorkoutSessionEntity(id = "s1", profileId = "p1", title = "تست", scheduledDate = "2026-09-29")
        val currentWE = WorkoutExerciseEntity(id = "w1", sessionId = "s1", exerciseId = "pushup_std", orderIndex = 1, targetSets = 3, targetReps = 10, restSec = 90)
        val currentDetail = SeedData.exercises.first { it.id == "pushup_std" }

        val result = PlanModificationEngine.adjustDifficulty(
            session = session,
            currentWorkoutExercise = currentWE,
            currentExerciseDetail = currentDetail,
            direction = DifficultyDirection.INCREASE,
            allExercises = SeedData.exercises
        )

        assertNotNull(result.updatedExercise)
        assertEquals("pushup_diamond", result.updatedExercise?.exerciseId)
    }

    @Test
    fun `test PlanModificationEngine adjustDifficulty decreases to easier regression`() {
        val session = WorkoutSessionEntity(id = "s1", profileId = "p1", title = "تست", scheduledDate = "2026-09-29")
        val currentWE = WorkoutExerciseEntity(id = "w1", sessionId = "s1", exerciseId = "pushup_diamond", orderIndex = 1, targetSets = 3, targetReps = 8, restSec = 90)
        val currentDetail = SeedData.exercises.first { it.id == "pushup_diamond" }

        val result = PlanModificationEngine.adjustDifficulty(
            session = session,
            currentWorkoutExercise = currentWE,
            currentExerciseDetail = currentDetail,
            direction = DifficultyDirection.DECREASE,
            allExercises = SeedData.exercises
        )

        assertNotNull(result.updatedExercise)
        assertEquals("pushup_std", result.updatedExercise?.exerciseId)
    }
}
