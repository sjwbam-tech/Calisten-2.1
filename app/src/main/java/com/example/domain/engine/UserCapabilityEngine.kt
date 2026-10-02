package com.example.domain.engine

import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.PainEntryEntity
import com.example.data.local.entity.RecoveryEntryEntity
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.SkillProgressEntity
import com.example.data.local.entity.UserCapabilityEntity
import java.util.UUID

object UserCapabilityEngine {

    fun calculateCapability(
        profileId: String,
        assessment: AssessmentEntity?,
        recentSetLogs: List<SetLogEntity>,
        skillProgress: List<SkillProgressEntity>,
        recentRecovery: List<RecoveryEntryEntity>,
        recentPain: List<PainEntryEntity>,
        currentCapability: UserCapabilityEntity? = null
    ): UserCapabilityEntity {
        // Pushing strength calculation (baseline from pushups + log history)
        val pushupBase = assessment?.pushupsMax ?: 10
        val loggedPushReps = recentSetLogs.filter { it.exerciseId.contains("push") || it.exerciseId.contains("dip") }
            .map { it.reps }.maxOrNull() ?: pushupBase
        val pushScore = ((loggedPushReps.coerceIn(0, 40) / 40f) * 100f).coerceIn(10f, 100f)

        // Pulling strength calculation (baseline from pullups + log history)
        val pullupBase = assessment?.pullupsMax ?: 2
        val loggedPullReps = recentSetLogs.filter { it.exerciseId.contains("pull") || it.exerciseId.contains("row") }
            .map { it.reps }.maxOrNull() ?: pullupBase
        val pullScore = ((loggedPullReps.coerceIn(0, 25) / 25f) * 100f).coerceIn(5f, 100f)

        // Leg strength
        val squatBase = assessment?.squatsMax ?: 15
        val legScore = ((squatBase.coerceIn(0, 50) / 50f) * 100f).coerceIn(15f, 100f)

        // Core strength
        val plankBase = assessment?.plankSec ?: 30
        val coreScore = ((plankBase.coerceIn(0, 120) / 120f) * 100f).coerceIn(10f, 100f)

        // Grip capacity
        val hangBase = assessment?.deadHangSec ?: 30
        val gripScore = ((hangBase.coerceIn(0, 90) / 90f) * 100f).coerceIn(10f, 100f)

        // Scapular control: boosted if pull-ups or rows are executed with good RPE
        val scapularScore = (pullScore * 0.7f + pushScore * 0.3f).coerceIn(10f, 100f)

        // Shoulder tolerance: decreased if shoulder pain is logged
        val hasShoulderPain = recentPain.any { it.location.contains("کتف") || it.location.contains("شانه") }
        val shoulderTolerance = if (hasShoulderPain) 30f else (pushScore * 0.8f + 20f).coerceIn(20f, 100f)

        // Wrist tolerance: decreased if wrist pain is logged
        val hasWristPain = recentPain.any { it.location.contains("مچ") }
        val wristTolerance = if (hasWristPain) 30f else 75f

        // Mobility score
        val mobilityScore = 65f

        // Skill proficiency
        val skillPoints = skillProgress.sumOf { it.currentStepIndex + 1 } * 10f
        val skillProficiency = skillPoints.coerceIn(10f, 100f)

        // Work capacity based on consistency & session volume
        val setLogCount = recentSetLogs.size
        val workCapacity = (50f + setLogCount * 1.5f).coerceIn(30f, 95f)

        // Recovery capacity based on average sleep & fatigue
        val avgSleep = if (recentRecovery.isNotEmpty()) recentRecovery.map { it.sleepDurationHours }.average().toFloat() else 7.5f
        val recoveryCapacity = ((avgSleep / 8.0f) * 80f).coerceIn(30f, 100f)

        return UserCapabilityEntity(
            id = currentCapability?.id ?: UUID.randomUUID().toString(),
            profileId = profileId,
            pushingStrength = pushScore,
            pullingStrength = pullScore,
            legStrength = legScore,
            coreStrength = coreScore,
            gripCapacity = gripScore,
            scapularControl = scapularScore,
            shoulderTolerance = shoulderTolerance,
            wristTolerance = wristTolerance,
            mobilityScore = mobilityScore,
            skillProficiency = skillProficiency,
            workCapacity = workCapacity,
            recoveryCapacity = recoveryCapacity,
            trainingAgeMonths = currentCapability?.trainingAgeMonths ?: 6,
            consistencyScore = if (recentSetLogs.isNotEmpty()) 85f else 50f,
            techniqueConfidence = if (recentSetLogs.any { it.status == "COMPLETED" }) 80f else 60f,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
