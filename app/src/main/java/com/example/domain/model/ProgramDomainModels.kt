package com.example.domain.model

import com.example.data.local.model.GoalPriority
import com.example.data.local.model.MovementFamily
import com.example.data.local.model.MovementSubPattern

data class GoalPriorityProfile(
    val primaryGoal: String,
    val secondaryGoal: String? = null,
    val maintenanceGoals: List<String> = emptyList()
)

data class WeeklyScheduleConstraints(
    val availableDaysCount: Int, // 2 to 6
    val preferredDurationMinutes: Int = 60,
    val availableEquipment: List<String> = emptyList(),
    val environment: String = "HOME"
)

data class MovementFamilyVolume(
    val family: MovementFamily,
    val totalSets: Int,
    val estimatedRpeAverage: Float,
    val shoulderLoadImpactScore: Float
)

data class WeeklyVolumeAnalysis(
    val pushingSets: Int,
    val pullingSets: Int,
    val legSets: Int,
    val coreSets: Int,
    val skillPracticeSets: Int,
    val pushPullRatio: Float,
    val highShoulderLoadSets: Int,
    val balanceStatus: VolumeBalanceStatus
)

enum class VolumeBalanceStatus(val messageFa: String) {
    BALANCED("توازن بهینه بین الگوهای حرکتی و عضلات آنتاگونیست"),
    PUSH_DOMINANT("غلبه غیرایمن الگوی فشاری نسبت به کششی؛ نیاز به افزایش بارفیکس یا رو"),
    PULL_DOMINANT("غلبه الگوی کششی نسبت به فشاری"),
    HIGH_SHOULDER_FATIGUE("بار تجمعی شانه بیش از آستانه ایمنی تاندون‌ها"),
    LOW_STIMULUS("حجم کلی کمتر از حداقل مؤثر هفتگی")
}

data class TimeConstrainedSessionPlan(
    val originalSessionTitle: String,
    val targetDurationMinutes: Int,
    val retainedExerciseIds: List<String>,
    val trimmedExerciseIds: List<String>,
    val reasonExplanation: String
)

data class DailyAdjustmentResult(
    val isAdjusted: Boolean,
    val originalSessionId: String,
    val adjustmentType: String, // VOLUME_REDUCED, INTENSITY_REDUCED, DELOAD_SWAP, SHORTENED_TIME, UNCHANGED
    val adjustedExerciseTargets: Map<String, Pair<Int, Int>>, // exerciseId -> Pair(sets, reps)
    val substituteExercises: Map<String, String>, // originalId -> newId
    val explanationFa: String
)

data class ProgramValidationReport(
    val isValid: Boolean,
    val warnings: List<String>,
    val errors: List<String>,
    val pushPullRatio: Float,
    val estimatedDurationMin: Int
)
