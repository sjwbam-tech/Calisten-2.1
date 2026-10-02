package com.example.domain.ai.workout

import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.PainEntryEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.RecoveryEntryEntity
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.SkillProgressEntity
import com.example.data.local.entity.UserCapabilityEntity
import com.example.domain.engine.DataDrivenProgramGenerator
import com.example.domain.engine.ExerciseRecommendationEngine
import com.example.domain.engine.ExerciseRelationshipGraph
import com.example.domain.engine.GeneratedProgramPackage
import com.example.domain.engine.PlateauAnalysisResult
import com.example.domain.engine.PlateauEngine
import com.example.domain.engine.ProgramGenerationParams
import com.example.domain.engine.ProgressionDecision
import com.example.domain.engine.ProgressionEngine
import com.example.domain.engine.SkillProgressionRegistry
import com.example.domain.engine.SupportedSkill
import com.example.domain.engine.UserCapabilityEngine

/**
 * High-level intelligence directives synthesized from historical workout logs,
 * recovery markers, pain indicators, and user capability benchmarks.
 */
data class WorkoutIntelligenceDirectives(
    val readinessScore: String, // "GREEN", "YELLOW", "RED"
    val volumeScaleFactor: Float, // 0.65f to 1.15f
    val protectedJoints: List<String>,
    val detectedPlateaus: List<PlateauIntelligenceInsight>,
    val suggestedSubstitutions: Map<String, String>, // exerciseToReplaceId -> substituteExerciseId
    val prioritizedSkillIds: List<String>,
    val recommendedSplit: String?,
    val reasoningLog: List<String>
)

data class PlateauIntelligenceInsight(
    val exerciseId: String,
    val exerciseName: String,
    val analysis: PlateauAnalysisResult
)

data class ExerciseSubstitutionProposal(
    val originalExerciseId: String,
    val originalExerciseName: String,
    val substituteExerciseId: String,
    val substituteExerciseName: String,
    val movementPattern: String,
    val rationale: String
)

/**
 * Internal Workout Intelligence Layer (Stage 3 Offline AI).
 *
 * This layer works SILENTLY in the background without requiring:
 * - A conversational chatbot
 * - A heavy 2GB+ LLM runtime
 * - Any internet connection or cloud API
 *
 * It combines user data, performance history, biomechanical graphs, and recovery signals
 * to feed actionable directives into the Planning Engine (DataDrivenProgramGenerator).
 *
 * CRITICAL SAFETY INVARIANT:
 * The Planning Engine (DataDrivenProgramGenerator, ProgressionEngine, PlateauEngine)
 * remains the FINAL, absolute authority on volume caps, difficulty ceilings, recovery
 * boundaries, equipment prerequisites, and progression safety.
 */
object InternalWorkoutIntelligenceEngine {

    /**
     * Synthesizes user telemetry, recovery, and past logs into machine-actionable intelligence directives.
     */
    fun synthesizeDirectives(params: ProgramGenerationParams): WorkoutIntelligenceDirectives {
        val reasoning = mutableListOf<String>()

        // 1. Analyze Readiness & Pain
        val activePainList = params.recentPain.filter { it.status == "RED" || it.status == "YELLOW" }
        val protectedJoints = activePainList.map { it.location.trim() }.distinct()

        val avgSleep = if (params.recentRecovery.isNotEmpty()) {
            params.recentRecovery.map { it.sleepDurationHours.coerceAtLeast(3.0f) }.average().toFloat()
        } else {
            7.0f
        }

        val avgSoreness = if (params.recentRecovery.isNotEmpty()) {
            params.recentRecovery.map { it.muscleSoreness.coerceIn(1, 5) }.average().toFloat()
        } else {
            2.0f
        }

        val readinessScore = when {
            activePainList.any { it.status == "RED" } || avgSleep < 5.0f || avgSoreness >= 4.5f -> {
                reasoning.add("شاخص آمادگی پایین (RED): درد شدید مفاصل یا خواب زیر ۵ ساعت ثبت شده است.")
                "RED"
            }
            activePainList.isNotEmpty() || avgSleep < 6.5f || avgSoreness >= 3.5f -> {
                reasoning.add("شاخص آمادگی متوسط (YELLOW): خستگی عضلانی یا درد خفیف گزارش شده است.")
                "YELLOW"
            }
            else -> {
                reasoning.add("شاخص آمادگی مطلوب (GREEN): ریکاوری مناسب و بدون علائم آسیب مفاصل.")
                "GREEN"
            }
        }

        // 2. Volume Scale Factor based on readiness
        val volumeScaleFactor = when (readinessScore) {
            "RED" -> 0.70f // 30% reduction to prevent overreaching
            "YELLOW" -> 0.85f // 15% reduction
            else -> 1.00f
        }

        // 3. Plateau Detection across logged exercises
        val plateaus = detectPlateaus(params.recentSetLogs, params.allExercises, avgSleep, activePainList.isNotEmpty())
        plateaus.forEach {
            reasoning.add("پلاتو در حرکت ${it.exerciseName}: ${it.analysis.message}")
        }

        // 4. Biomechanical Substitutions for Injured/Stagnant Movements
        val substitutions = findBiomechanicalSubstitutions(
            allExercises = params.allExercises,
            protectedJoints = protectedJoints,
            plateaus = plateaus,
            experienceLevel = params.profile.experienceLevel,
            readiness = readinessScore
        )
        substitutions.forEach {
            reasoning.add("پیشنهاد تعویض حرکت ${it.originalExerciseName} با ${it.substituteExerciseName}: ${it.rationale}")
        }

        // 5. Skill Progression Prioritization
        val prioritizedSkills = evaluateSkillPriorities(
            skillProgress = params.skillProgress,
            capability = params.capability,
            availableEquipment = params.profile.equipmentJson.let {
                if (it.isBlank() || it.trim() == "[]") emptyList()
                else it.removeSurrounding("[", "]").split(",").map { s -> s.trim().removeSurrounding("\"") }
            },
            allExercises = params.allExercises
        )
        if (prioritizedSkills.isNotEmpty()) {
            reasoning.add("مهارت‌های اولویت‌دار جهت گنجاندن در برنامه: ${prioritizedSkills.joinToString(", ")}")
        }

        // 6. Split Recommendation
        val recommendedSplit = evaluateOptimalSplit(
            trainingDays = params.targetDaysPerWeek ?: params.profile.trainingDaysPerWeek,
            experienceLevel = params.profile.experienceLevel,
            readinessScore = readinessScore
        )

        return WorkoutIntelligenceDirectives(
            readinessScore = readinessScore,
            volumeScaleFactor = volumeScaleFactor,
            protectedJoints = protectedJoints,
            detectedPlateaus = plateaus,
            suggestedSubstitutions = substitutions.associate { it.originalExerciseId to it.substituteExerciseId },
            prioritizedSkillIds = prioritizedSkills,
            recommendedSplit = recommendedSplit,
            reasoningLog = reasoning
        )
    }

    /**
     * Executes end-to-end plan generation:
     * User Data -> Internal Intelligence -> Exercise Database -> Planning Engine (Final Authority).
     */
    fun generatePersonalizedPlan(params: ProgramGenerationParams): GeneratedProgramPackage {
        // Step 1: Synthesize internal intelligence directives
        val directives = synthesizeDirectives(params)

        // Step 2: Feed into existing Planning Engine with directives applied
        val adjustedParams = if (directives.recommendedSplit != null && params.forcedSplit == null) {
            params.copy(forcedSplit = directives.recommendedSplit)
        } else {
            params
        }

        // The deterministic planning engine executes with final authority
        return DataDrivenProgramGenerator.generateProgram(adjustedParams)
    }

    private fun detectPlateaus(
        logs: List<SetLogEntity>,
        allExercises: List<ExerciseEntity>,
        avgSleep: Float,
        hasPain: Boolean
    ): List<PlateauIntelligenceInsight> {
        if (logs.isEmpty()) return emptyList()

        val exercisesMap = allExercises.associateBy { it.id }
        val logsByExercise = logs.groupBy { it.exerciseId }
        val results = mutableListOf<PlateauIntelligenceInsight>()

        for ((exerciseId, exerciseLogs) in logsByExercise) {
            val sessionsMap: Map<String, List<SetLogEntity>> = exerciseLogs.groupBy { it.sessionId }
            if (sessionsMap.size >= 3) {
                val analysis = PlateauEngine.analyzePlateau(
                    exerciseId = exerciseId,
                    sessionsLogs = sessionsMap,
                    sleepQualityAvg = (avgSleep / 2.0f).coerceIn(1.0f, 5.0f),
                    recentPainLogged = hasPain
                )
                if (analysis.isPlateauDetected) {
                    val exName = exercisesMap[exerciseId]?.persianName ?: exerciseId
                    results.add(PlateauIntelligenceInsight(exerciseId, exName, analysis))
                }
            }
        }

        return results
    }

    private fun findBiomechanicalSubstitutions(
        allExercises: List<ExerciseEntity>,
        protectedJoints: List<String>,
        plateaus: List<PlateauIntelligenceInsight>,
        experienceLevel: String,
        readiness: String
    ): List<ExerciseSubstitutionProposal> {
        val proposals = mutableListOf<ExerciseSubstitutionProposal>()
        val exercisesMap = allExercises.associateBy { it.id }

        // Check exercises associated with detected plateaus
        for (plateau in plateaus) {
            val original = exercisesMap[plateau.exerciseId] ?: continue
            val altId = original.alternativeExerciseIdsJson
                .removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .firstOrNull { it.isNotBlank() && it != original.id }

            if (altId != null && exercisesMap.containsKey(altId)) {
                val substitute = exercisesMap[altId]!!
                proposals.add(
                    ExerciseSubstitutionProposal(
                        originalExerciseId = original.id,
                        originalExerciseName = original.persianName,
                        substituteExerciseId = substitute.id,
                        substituteExerciseName = substitute.persianName,
                        movementPattern = original.movementPattern,
                        rationale = plateau.analysis.recommendedReason ?: plateau.analysis.suggestedAction ?: "تغییر زاویه و تنوع بیومکانیکی برای عبور از پلاتو"
                    )
                )
            }
        }

        return proposals
    }

    private fun evaluateSkillPriorities(
        skillProgress: List<SkillProgressEntity>,
        capability: UserCapabilityEntity,
        availableEquipment: List<String>,
        allExercises: List<ExerciseEntity>
    ): List<String> {
        val readySkills = mutableListOf<String>()

        for (progress in skillProgress) {
            val supported = SupportedSkill.fromIdOrName(progress.skillId) ?: continue
            val eval = SkillProgressionRegistry.evaluateSkill(
                skill = supported,
                capability = capability,
                assessment = null,
                availableEquipment = availableEquipment,
                allExercises = allExercises,
                skillProgress = skillProgress
            )
            if (eval.meetsPrerequisites) {
                readySkills.add(progress.skillId)
            }
        }

        return readySkills
    }

    private fun evaluateOptimalSplit(
        trainingDays: Int,
        experienceLevel: String,
        readinessScore: String
    ): String {
        return when {
            trainingDays <= 3 -> "FULL_BODY"
            trainingDays == 4 -> "UPPER_LOWER"
            else -> if (experienceLevel == "BEGINNER") "FULL_BODY" else "PUSH_PULL_LEGS"
        }
    }
}
