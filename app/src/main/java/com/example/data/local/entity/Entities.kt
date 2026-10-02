package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val heightCm: Float,
    val weightKg: Float,
    val gender: String, // "MALE", "FEMALE", "PREFER_NOT_TO_SAY"
    val waistCm: Float? = null,
    val activityLevel: String = "MODERATE", // SEDENTARY, LIGHT, MODERATE, VERY_ACTIVE
    val experienceLevel: String = "BEGINNER", // BEGINNER, INTERMEDIATE, ADVANCED
    val goalsJson: String = "[]", // List<String>
    val equipmentJson: String = "[]", // List<String>
    val environment: String = "HOME", // HOME, PARK, GYM
    val trainingDaysPerWeek: Int = 3,
    val preferredTimeOfDay: String = "EVENING",
    val limitationsNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "assessments",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class AssessmentEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val pushupsMax: Int = 0,
    val pullupsMax: Int = 0,
    val squatsMax: Int = 0,
    val plankSec: Int = 0,
    val deadHangSec: Int = 0,
    val cardioTolerance: String = "MEDIUM",
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val persianName: String,
    val englishName: String,
    val category: String, // PUSH, PULL, LEGS, CORE, SHOULDERS, ARMS, GRIP, MOBILITY, CARDIO, JUMP_ROPE, SKILLS
    val musclesJson: String = "[]",
    val movementPattern: String = "HORIZONTAL_PUSH", // HORIZONTAL_PUSH, VERTICAL_PULL, etc.
    val movementSubPattern: String = "HORIZONTAL_PUSH",
    val movementFamily: String = "PUSH",
    val progressionFamily: String = "",
    val exerciseType: String = "STRENGTH",
    val skillOrStrength: String = "STRENGTH",
    val primaryMusclesJson: String = "[]",
    val secondaryMusclesJson: String = "[]",
    val jointActionsJson: String = "[]",
    val difficultyTier: Int = 1, // 1 to 10
    val difficulty: String = "BEGINNER", // BEGINNER, INTERMEDIATE, ADVANCED, ELITE
    val technicalDifficulty: Int = 1, // 1 to 5
    val strengthDemand: Int = 1, // 1 to 5
    val stabilityDemand: Int = 1, // 1 to 5
    val mobilityDemand: Int = 1, // 1 to 5
    val coordinationDemand: Int = 1, // 1 to 5
    val recoveryCost: Int = 1, // 1 to 5
    val equipmentRequiredJson: String = "[]", // List of equipment IDs
    val equipmentOptionalJson: String = "[]",
    val environmentRequirements: String = "HOME_OR_PARK",
    val prerequisitesJson: String = "[]",
    val regressionsJson: String = "[]",
    val progressionsJson: String = "[]",
    val alternativesJson: String = "[]",
    val complementaryExercisesJson: String = "[]",
    val similarExercisesJson: String = "[]",
    val contraindicatedWhen: String = "",
    val startingPosition: String = "",
    val instructionsJson: String = "[]",
    val commonMistakesJson: String = "[]",
    val correctionsJson: String = "[]",
    val breathingNotes: String = "",
    val safetyCriteria: String = "",
    val shoulderLoad: String = "LOW", // LOW, MEDIUM, HIGH
    val tempo: String = "2-0-1-0",
    val restRecommendationSec: Int = 90,
    val minReps: Int = 5,
    val maxReps: Int = 12,
    val minHoldSec: Int = 0,
    val maxHoldSec: Int = 0,
    val targetRpeMin: Int = 6,
    val targetRpeMax: Int = 8,
    val targetRirMin: Int = 1,
    val targetRirMax: Int = 3,
    val suitableGoalsJson: String = "[]",
    val unsuitableGoalsJson: String = "[]",
    val skillTreeRelationsJson: String = "[]",
    val unilateralOrBilateral: String = "BILATERAL",
    val skillLevel: String = "BEGINNER",
    val beginnerAccessible: Boolean = true,
    val advancedOnly: Boolean = false,
    val requiresSpotter: Boolean = false,
    val requiresSpecialEquipment: Boolean = false,
    val createdBy: String = "SYSTEM",
    val source: String = "EXERCISE_SCIENCE_EVIDENCE",
    val sourceVersion: String = "v1.0",
    val active: Boolean = true,
    val regressionExerciseId: String? = null,
    val progressionExerciseId: String? = null,
    val alternativeExerciseIdsJson: String = "[]",
    val isCustom: Boolean = false,
    val profileId: String? = null // For user custom exercises
)

@Entity(
    tableName = "exercise_relationships",
    indices = [Index("sourceExerciseId"), Index("targetExerciseId")]
)
data class ExerciseRelationshipEntity(
    @PrimaryKey val id: String,
    val sourceExerciseId: String,
    val targetExerciseId: String,
    val relationshipType: String,
    val impactWeight: Float = 1.0f,
    val reason: String = ""
)

@Entity(
    tableName = "user_capabilities",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class UserCapabilityEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val pushingStrength: Float = 50f,
    val pullingStrength: Float = 50f,
    val legStrength: Float = 50f,
    val coreStrength: Float = 50f,
    val gripCapacity: Float = 50f,
    val scapularControl: Float = 50f,
    val shoulderTolerance: Float = 50f,
    val wristTolerance: Float = 50f,
    val mobilityScore: Float = 50f,
    val skillProficiency: Float = 30f,
    val workCapacity: Float = 50f,
    val recoveryCapacity: Float = 70f,
    val trainingAgeMonths: Int = 3,
    val consistencyScore: Float = 80f,
    val techniqueConfidence: Float = 70f,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "adaptation_decisions",
    indices = [Index("profileId"), Index("programId")]
)
data class AdaptationDecisionEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val programId: String,
    val sessionId: String? = null,
    val exerciseId: String? = null,
    val decisionType: String,
    val decision: String,
    val reason: String,
    val inputDataJson: String = "{}",
    val timestamp: Long = System.currentTimeMillis(),
    val programVersion: Int = 1
)

@Entity(
    tableName = "programs",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class ProgramEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val title: String,
    val description: String = "",
    val level: String = "BEGINNER",
    val daysPerWeek: Int = 3,
    val status: String = "ACTIVE", // ACTIVE, ARCHIVED
    val currentVersionNumber: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "program_versions",
    foreignKeys = [
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("programId")]
)
data class ProgramVersionEntity(
    @PrimaryKey val id: String,
    val programId: String,
    val versionNumber: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val reason: String = "نسخه اولیه برنامه",
    val changesSummary: String = "ساخت ساختار تمرینی بر اساس ارزیابی و اهداف",
    val programStructureJson: String = "{}", // Detailed serialized routines
    val previousVersionNumber: Int? = null
)

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("programId")]
)
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val programId: String? = null,
    val title: String,
    val scheduledDate: String, // YYYY-MM-DD
    val completedDate: String? = null,
    val status: String = "PLANNED", // PLANNED, IN_PROGRESS, COMPLETED, SKIPPED
    val totalDurationSec: Int = 0,
    val overallRpe: Int? = null, // 1 to 10
    val painFlag: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class WorkoutExerciseEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int = 3,
    val targetReps: Int = 8,
    val targetHoldSec: Int = 0,
    val targetRpe: Int = 7,
    val restSec: Int = 90,
    val notes: String = ""
)

@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutExerciseId"), Index("profileId"), Index("exerciseId")]
)
data class SetLogEntity(
    @PrimaryKey val id: String,
    val workoutExerciseId: String,
    val sessionId: String,
    val profileId: String,
    val exerciseId: String,
    val setNumber: Int,
    val reps: Int,
    val holdSeconds: Int = 0,
    val addedWeightKg: Float = 0f,
    val rpe: Int = 7,
    val rir: Int = 2,
    val status: String = "COMPLETED", // COMPLETED, PARTIAL, FAILED, PAIN_STOP
    val painSeverity: Int = 0, // 0 to 10
    val painLocation: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val id: String,
    val persianName: String,
    val englishName: String,
    val description: String,
    val category: String, // PUSH_STATIC, PULL_STATIC, DYNAMIC, BALANCE
    val progressionStepsJson: String, // Ordered JSON array of milestone step names & targets
    val prerequisitesJson: String = "[]",
    val accessoryExerciseIdsJson: String = "[]"
)

@Entity(
    tableName = "skill_progress",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("skillId")]
)
data class SkillProgressEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val skillId: String,
    val currentStepIndex: Int = 0,
    val bestHoldSec: Int = 0,
    val bestReps: Int = 0,
    val qualityScore: Int = 8, // 1 to 10
    val lastTrainedDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "metrics",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class MetricEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val metricType: String, // WEIGHT, WAIST, PUSHUPS, PULLUPS, SQUATS, PLANK, DEADHANG, CARDIO, ROPE
    val value: Float,
    val unit: String,
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class PREntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val exerciseOrSkillId: String,
    val title: String,
    val prType: String, // MAX_REPS, MAX_WEIGHT, MAX_DURATION, SKILL_STEP
    val value: Float,
    val unit: String,
    val date: Long = System.currentTimeMillis(),
    val conditionsNote: String = ""
)

@Entity(
    tableName = "nutrition_entries",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("date")]
)
data class NutritionEntryEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val date: String, // YYYY-MM-DD
    val mealType: String, // BREAKFAST, LUNCH, DINNER, SNACK, WATER
    val foodName: String,
    val calories: Int = 0,
    val proteinG: Float = 0f,
    val carbsG: Float = 0f,
    val fatG: Float = 0f,
    val waterMl: Int = 0,
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recovery_entries",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("date")]
)
data class RecoveryEntryEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val date: String, // YYYY-MM-DD
    val sleepDurationHours: Float,
    val sleepQuality: Int, // 1 to 5
    val energyLevel: Int, // 1 to 5
    val fatigueLevel: Int, // 1 to 5
    val muscleSoreness: Int, // 1 to 5
    val stressLevel: Int, // 1 to 5
    val readiness: String, // GREEN, YELLOW, RED
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_checkins",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("date")]
)
data class DailyCheckInEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val date: String, // YYYY-MM-DD
    val sleepHours: Float,
    val energy: Int,
    val fatigue: Int,
    val soreness: Int,
    val painLevel: Int = 0,
    val readiness: String, // GREEN, YELLOW, RED
    val habitsCompletedJson: String = "[]",
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "pain_entries",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("date")]
)
data class PainEntryEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val date: String,
    val location: String, // کتف، آرنج، مچ دست، زانو، کمر، مچ پا
    val severity: Int, // 1-10
    val timing: String, // قبل تمرین، حین ست، بعد تمرین، روز بعد
    val exerciseId: String? = null,
    val duringWorkout: Boolean = false,
    val afterWorkout: Boolean = false,
    val nextDay: Boolean = false,
    val effectOnForm: Boolean = false,
    val status: String = "GREEN", // GREEN, YELLOW, RED
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "science_sources")
data class ScienceSourceEntity(
    @PrimaryKey val id: String,
    val packVersion: String = "v1.0",
    val topic: String, // هیپرتروفی، بارگذاری تاندون، ریکاوری و خواب، پروتئین، قدرت کالیستنیکس
    val title: String,
    val authorOrg: String,
    val publicationYear: Int,
    val reviewDate: String,
    val evidenceLevel: String, // متاآنالیز، کارآزمایی بالینی، مرور سیستماتیک، راهنمای بالینی
    val keyTakeawaysJson: String,
    val practicalApplication: String,
    val limitations: String
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
