package com.example.domain.equipment

import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.UserCapabilityEntity
import java.util.Locale

data class AdaptedExercisePrescription(
    val exercise: ExerciseEntity,
    val targetSets: Int,
    val targetReps: Int,
    val targetHoldSec: Int = 0,
    val targetRpe: Int = 7,
    val restSec: Int = 90,
    val tempo: String = "2-0-1-0",
    val requiredEquipmentFa: String,
    val equipmentRequiredId: String,
    val adaptationNote: String? = null,
    val isAdaptedForWeight: Boolean = false,
    val weightUsedKg: Float? = null
) {
    /**
     * Formats comprehensive note stored in WorkoutExerciseEntity.notes for seamless UI display.
     */
    val formattedNotes: String
        get() {
            val sb = StringBuilder()
            sb.append("تجهیزات: ").append(requiredEquipmentFa)
            if (adaptationNote != null) {
                sb.append(" | ").append(adaptationNote)
            }
            return sb.toString()
        }
}

/**
 * Intelligent Adaptive Equipment Engine (Offline, Deterministic, Biomechanically Sound).
 *
 * Responsibilities:
 * 1. Filter out exercises requiring equipment the user does not own.
 * 2. Select appropriate substitutions preserving muscle groups and training intent.
 * 3. Adapt repetitions, tempo, sets, rest, and leverage according to available weight in kg.
 * 4. Ensure safety: avoid excessive loading on beginners; ensure bodyweight alternatives are always ready.
 */
object AdaptiveEquipmentEngine {

    private fun parseJsonStringList(jsonStr: String): List<String> {
        if (jsonStr.isBlank() || jsonStr.trim() == "[]") return emptyList()
        return try {
            val cleaned = jsonStr.trim().removeSurrounding("[", "]")
            if (cleaned.isBlank()) return emptyList()
            cleaned.split(",").map {
                it.trim().removeSurrounding("\"").removeSurrounding("'").trim()
            }.filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Determines whether an exercise can be executed given the user's equipment profile.
     */
    fun canPerform(exercise: ExerciseEntity, equipment: UserEquipmentProfile): Boolean {
        val required = parseJsonStringList(exercise.equipmentRequiredJson)
        return equipment.hasEquipmentFor(required)
    }

    /**
     * Filters a list of exercises to only those feasible with user's equipment.
     */
    fun filterFeasibleExercises(
        exercises: List<ExerciseEntity>,
        equipment: UserEquipmentProfile
    ): List<ExerciseEntity> {
        return exercises.filter { canPerform(it, equipment) }
    }

    /**
     * Replaces an unavailable exercise with the best matching alternative that the user CAN perform.
     * Preserves movementPattern, category, and muscle groups, falling back to bodyweight if necessary.
     */
    fun substituteIfUnavailable(
        exercise: ExerciseEntity,
        equipment: UserEquipmentProfile,
        allExercises: List<ExerciseEntity>
    ): ExerciseEntity {
        if (canPerform(exercise, equipment)) {
            return exercise
        }

        val exerciseMap = allExercises.associateBy { it.id }

        // 1. Try explicit alternatives
        val alternatives = parseJsonStringList(exercise.alternativeExerciseIdsJson)
        for (altId in alternatives) {
            val candidate = exerciseMap[altId]
            if (candidate != null && canPerform(candidate, equipment)) {
                return candidate
            }
        }

        // 2. Try regression
        val regressionId = exercise.regressionExerciseId
        if (regressionId != null) {
            val candidate = exerciseMap[regressionId]
            if (candidate != null && canPerform(candidate, equipment)) {
                return candidate
            }
        }

        // 3. Match by movementPattern from feasible exercises
        val samePattern = allExercises.filter {
            it.movementPattern == exercise.movementPattern && canPerform(it, equipment)
        }
        if (samePattern.isNotEmpty()) {
            // Pick closest in difficultyTier
            return samePattern.minByOrNull { kotlin.math.abs(it.difficultyTier - exercise.difficultyTier) } ?: samePattern.first()
        }

        // 4. Match by category from feasible exercises
        val sameCategory = allExercises.filter {
            it.category == exercise.category && canPerform(it, equipment)
        }
        if (sameCategory.isNotEmpty()) {
            return sameCategory.minByOrNull { kotlin.math.abs(it.difficultyTier - exercise.difficultyTier) } ?: sameCategory.first()
        }

        // 5. Ultimate fallback: pure bodyweight pushup or squat
        return allExercises.firstOrNull { it.id == "pushup_std" || it.id == "squat_bodyweight" } ?: exercise
    }

    /**
     * Human-readable Persian label for required equipment and exact weight.
     */
    fun getEquipmentDisplayLabel(
        exercise: ExerciseEntity,
        equipment: UserEquipmentProfile
    ): Pair<String, String> {
        val required = parseJsonStringList(exercise.equipmentRequiredJson)
        if (required.isEmpty()) {
            return "وزن بدن (بدون وسیله)" to "bodyweight"
        }

        when {
            required.contains("dumbbell_single") -> {
                val weight = equipment.getEffectiveWeightKg("dumbbell_single") ?: equipment.singleDumbbellWeightKg
                return "دمبل تک (${formatWeight(weight)} کیلوگرم)" to "dumbbell_single"
            }
            required.contains("dumbbell_pair") -> {
                val weight = equipment.getEffectiveWeightKg("dumbbell_pair") ?: equipment.pairDumbbellWeightKg
                return "جفت دمبل (هر کدام ${formatWeight(weight)} کیلوگرم)" to "dumbbell_pair"
            }
            required.contains("barbell") -> {
                val weight = equipment.getEffectiveWeightKg("barbell") ?: equipment.barbellTotalWeightKg
                return "هالتر (${formatWeight(weight)} کیلوگرم)" to "barbell"
            }
            required.contains("weight_vest") -> {
                val weight = equipment.getEffectiveWeightKg("weight_vest") ?: equipment.weightVestWeightKg
                val hasBar = required.contains("pullup_bar")
                val prefix = if (hasBar) "میله بارفیکس + " else ""
                return "${prefix}جلیقه وزنی (${formatWeight(weight)} کیلوگرم)" to "weight_vest"
            }
            required.contains("resistance_bands") -> {
                val hasBar = required.contains("pullup_bar")
                val hasParallettes = required.contains("parallettes")
                val prefix = when {
                    hasBar -> "میله بارفیکس + "
                    hasParallettes -> "پارالل + "
                    else -> ""
                }
                return "${prefix}کش مقاومتی" to "resistance_bands"
            }
            required.contains("pullup_bar") -> return "میله بارفیکس" to "pullup_bar"
            required.contains("parallettes") -> return "پارالل / پارالت" to "parallettes"
            required.contains("rings") -> return "حلقه ژیمناستیک" to "rings"
            required.contains("bench") || required.contains("chair") -> return "نیمکت / تکیه‌گاه" to "bench"
            required.contains("wall") -> return "دیوار تکیه‌گاه" to "wall"
            else -> return "تجهیزات اختصاصی" to "other"
        }
    }

    private fun formatWeight(weight: Float): String {
        return if (weight % 1.0f == 0.0f) {
            String.format(Locale.US, "%.0f", weight)
        } else {
            String.format(Locale.US, "%.1f", weight)
        }
    }

    /**
     * Adapts volume, repetitions, rest periods, tempo, and exercise selection
     * based on user's exact available weight, experience level, capability, and training goal.
     */
    fun adaptPrescription(
        targetExercise: ExerciseEntity,
        equipment: UserEquipmentProfile,
        userLevel: String = "INTERMEDIATE",
        userWeightKg: Float = 75f,
        capability: UserCapabilityEntity? = null,
        goal: String = "STRENGTH",
        recentRpeAvg: Float = 7.5f,
        allExercises: List<ExerciseEntity> = emptyList()
    ): AdaptedExercisePrescription {
        // 1. Ensure exercise is feasible; if not, substitute it
        val finalExercise = if (!canPerform(targetExercise, equipment) && allExercises.isNotEmpty()) {
            substituteIfUnavailable(targetExercise, equipment, allExercises)
        } else {
            targetExercise
        }

        val (equipmentLabel, equipmentId) = getEquipmentDisplayLabel(finalExercise, equipment)
        val requiredList = parseJsonStringList(finalExercise.equipmentRequiredJson)

        val isBeginner = userLevel.uppercase() == "BEGINNER"
        val isAdvanced = userLevel.uppercase() == "ADVANCED"

        // Baseline targets from exercise
        var baseSets = if (isAdvanced) 4 else 3
        var baseReps = finalExercise.maxReps.coerceAtLeast(8)
        var baseHoldSec = finalExercise.maxHoldSec
        var baseRestSec = finalExercise.restRecommendationSec
        var baseTempo = finalExercise.tempo.ifBlank { "2-0-1-0" }
        var targetRpe = 7

        var isAdapted = false
        var adaptationNote: String? = null
        var effectiveWeightKg: Float? = null

        // Check if movement uses weights
        val isWeightedExercise = requiredList.any { it in listOf("dumbbell_single", "dumbbell_pair", "barbell", "weight_vest") }

        if (isWeightedExercise) {
            effectiveWeightKg = equipment.getEffectiveWeightKg(equipmentId) ?: 10f
            val weight = effectiveWeightKg
            val weightRatio = weight / userWeightKg.coerceAtLeast(40f)

            // Pattern-specific capability
            val patternCapability = when (finalExercise.category) {
                "PUSH" -> capability?.pushingStrength ?: 50f
                "PULL" -> capability?.pullingStrength ?: 50f
                "LEGS" -> capability?.legStrength ?: 50f
                "SHOULDERS" -> capability?.pushingStrength ?: 40f
                else -> 50f
            }

            // A. Insufficient Weight Check for Leg Compound movements
            // Example: 3kg dumbbell for squat with an intermediate trainee
            val isLegCompound = finalExercise.category == "LEGS" && finalExercise.movementPattern == "SQUAT"
            val isInsuffientForBilateral = isLegCompound && weight < 8f && !isBeginner && finalExercise.unilateralOrBilateral == "BILATERAL"

            if (isInsuffientForBilateral) {
                // Adapt to unilateral tempo or higher TUT
                baseReps = 16
                baseTempo = "3-1-2-0"
                baseRestSec = 60
                isAdapted = true
                adaptationNote = "تعدیل وزنه: به دلیل سبک بودن وزنه نسبت به توان پاها، تکرارها به ۱۶ افزایش یافته و تمپوی ۳-۱-۲-۰ برای حفظ زمان تحت تنش اعمال گردید."
            }
            // B. Heavy Weight Handling
            else if (weightRatio > 0.35f || (equipmentId == "dumbbell_single" && weight >= 20f) || (equipmentId == "barbell" && weight >= 60f)) {
                if (isBeginner && patternCapability <= 45f) {
                    // Safety protection: Weight is excessive for a beginner
                    baseReps = 8
                    targetRpe = 6
                    baseRestSec = 100
                    baseTempo = "3-0-1-0"
                    isAdapted = true
                    adaptationNote = "تعدیل ایمنی: با توجه به سطح مبتدی، جهت پیشگیری از آسیب تاندون‌ها بار با RPE محافظه‌کارانه ۶ و دامنه تکرار امن تنظیم شد."
                } else {
                    // Valid heavy strength stimulus
                    baseSets = if (isAdvanced) 4 else 3
                    baseReps = 5
                    targetRpe = 8
                    baseRestSec = 120
                    baseTempo = "2-1-1-0"
                    isAdapted = true
                    adaptationNote = "تعدیل وزنه: تمرکز بر توان و قدرت بیشینه با ۵ تکرار، استراحت ۱۲۰ ثانیه و RPE 8 متناسب با وزنه سنگین."
                }
            }
            // C. Light Weight Handling
            else if (weightRatio < 0.15f || (equipmentId == "dumbbell_single" && weight <= 7f)) {
                baseReps = (baseReps + 4).coerceAtMost(18)
                baseTempo = "3-1-2-0"
                baseRestSec = 60
                targetRpe = 7
                isAdapted = true
                adaptationNote = "تعدیل وزنه: افزایش تکرارها به $baseReps و تمپوی کنترل‌شده ۳-۱-۲-۰ جهت ایجاد هایپرتروفی موثر با وزنه سبک."
            }
            // D. Moderate Weight Handling
            else {
                baseReps = 10
                baseRestSec = 90
                baseTempo = "2-1-1-0"
                targetRpe = 7
                isAdapted = true
                adaptationNote = "تعدیل وزنه: بارگذاری استاندارد ۱۰ تکرار با تمرکز بر دامنه کامل حرکتی و ثبات مفاصل."
            }
        } else if (requiredList.contains("resistance_bands")) {
            if (finalExercise.id.contains("assisted")) {
                baseReps = 8
                targetRpe = 7
                baseRestSec = 90
                isAdapted = true
                adaptationNote = "تعدیل با کش کمکی: کاهش بار موثر جهت تکمیل دامنه حرکتی کامل و ایمنی کپسول شانه."
            } else {
                baseReps = 12
                baseRestSec = 60
                isAdapted = true
                adaptationNote = "تعدیل با مقاومت کش: ایجاد بار متغیر صعودی در انتهای دامنه حرکتی برای فعال‌سازی حداکثری عضلات تثبیت‌کننده."
            }
        } else {
            // Pure bodyweight exercise
            if (equipment.isPureBodyweight) {
                // Calisthenics leverage adjustment
                if (isAdvanced && baseReps < 15 && baseHoldSec == 0) {
                    baseReps = (baseReps + 2).coerceAtMost(16)
                }
            }
        }

        // Final Goal Tuning
        when (goal.uppercase()) {
            "STRENGTH" -> {
                if (baseHoldSec == 0 && !isAdapted) {
                    baseReps = 5
                }
                if (!isAdapted) {
                    targetRpe = 8
                    baseRestSec = 120
                }
            }
            "HYPERTROPHY" -> {
                if (baseHoldSec == 0 && !isAdapted) {
                    baseReps = 10
                }
                if (!isAdapted) {
                    targetRpe = 7
                    baseRestSec = 90
                }
            }
            "ENDURANCE" -> {
                if (baseHoldSec == 0 && !isAdapted) {
                    baseReps = 18
                } else if (!isAdapted) {
                    baseHoldSec = baseHoldSec.coerceAtLeast(35)
                }
                if (!isAdapted) {
                    targetRpe = 7
                    baseRestSec = 60
                }
            }
        }

        return AdaptedExercisePrescription(
            exercise = finalExercise,
            targetSets = baseSets,
            targetReps = baseReps,
            targetHoldSec = baseHoldSec,
            targetRpe = targetRpe,
            restSec = baseRestSec,
            tempo = baseTempo,
            requiredEquipmentFa = equipmentLabel,
            equipmentRequiredId = equipmentId,
            adaptationNote = adaptationNote,
            isAdaptedForWeight = isAdapted,
            weightUsedKg = effectiveWeightKg
        )
    }
}
