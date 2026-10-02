package com.example.domain.equipment

import org.json.JSONObject

/**
 * Types of equipment supported by the Calisten Adaptive Engine.
 */
enum class EquipmentType(
    val id: String,
    val titleFa: String,
    val descriptionFa: String,
    val isWeighted: Boolean
) {
    BODYWEIGHT("bodyweight", "وزن بدن (بدون وسیله)", "تمرینات متکی بر جرم بدن و کنترل اهرم‌ها بر روی زمین", false),
    DUMBBELL_SINGLE("dumbbell_single", "یک عدد دمبل", "انجام حرکات یک‌طرفه (Unilateral) و تقویت تقارن و کنترل تنه", true),
    DUMBBELL_PAIR("dumbbell_pair", "یک جفت دمبل", "تمرینات متقارن دوطرفه با بار آزاد و توسعه توازن عضلانی", true),
    DUMBBELL_ADJUSTABLE("dumbbell_adjustable", "دمبل متغیر (قابل تنظیم)", "امکان تغییر وزن گام‌به‌گام برای حرکات مختلف بر اساس توانایی", true),
    BARBELL("barbell", "هالتر و صفحات وزنه", "تمرینات سنگین‌تر و بارگذاری افزایشی سیستمیک برای پا و بالاتنه", true),
    RESISTANCE_BANDS("resistance_bands", "کش‌های مقاومتی (پاوربند / لوپ)", "تعدیل بار حرکات سخت (کمکی) یا ایجاد مقاومت متغیر صعودی", false),
    PULLUP_BAR("pullup_bar", "میله بارفیکس", "حرکات کششی عمودی، بارفیکس و بالا آوردن پاها در وضعیت آویزان", false),
    PARALLETTES("parallettes", "پارالل / پارالت", "دیپ موازی، ال‌سیت و کاهش فشار هایپراکستنشن روی مچ دست", false),
    RINGS("rings", "حلقه‌های ژیمناستیک", "حرکات معلق با بی‌ثباتی ریز و درگیری حداکثری عضلات تثبیت‌کننده", false),
    WEIGHT_VEST("weight_vest", "جلیقه وزنی / کمربند وزنه", "افزایش ایمن شدت حرکات پایه کالیستنیکس بدون برهم خوردن بیومکانیک", true),
    BENCH("bench", "میز یا نیمکت تمرین", "تکیه‌گاه برای حرکات تک‌پا، شیب‌دار و دامنه‌های عمیق‌تر کششی", false);

    companion object {
        fun fromId(id: String): EquipmentType? = values().firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}

/**
 * Comprehensive user equipment profile storing owned gear and exact weight metrics.
 * Persists locally offline in Room database app_settings.
 */
data class UserEquipmentProfile(
    // 1. Bodyweight only flag
    val hasBodyweightOnly: Boolean = false,

    // 2. Single Dumbbell
    val hasSingleDumbbell: Boolean = false,
    val singleDumbbellWeightKg: Float = 10f,

    // 3. Pair of Dumbbells
    val hasPairDumbbells: Boolean = false,
    val pairDumbbellWeightKg: Float = 10f,

    // 4. Adjustable Dumbbells
    val hasAdjustableDumbbells: Boolean = false,
    val adjustableMinWeightKg: Float = 2.5f,
    val adjustableMaxWeightKg: Float = 24f,
    val adjustableIncrementKg: Float = 2.5f,

    // 5. Barbell
    val hasBarbell: Boolean = false,
    val barbellTotalWeightKg: Float = 30f,

    // 6. Resistance Bands
    val hasResistanceBands: Boolean = false,
    val resistanceBandLevel: String = "ALL", // LIGHT, MEDIUM, HEAVY, ALL

    // 7. Pull-up Bar
    val hasPullupBar: Boolean = false,

    // 8. Other Equipment
    val hasParallettes: Boolean = false,
    val hasRings: Boolean = false,
    val hasWeightVest: Boolean = false,
    val weightVestWeightKg: Float = 10f,
    val hasBench: Boolean = false
) {
    /**
     * Returns true if user has selected purely bodyweight, or has no weighted/external equipment.
     */
    val isPureBodyweight: Boolean
        get() = hasBodyweightOnly || (!hasSingleDumbbell && !hasPairDumbbells && !hasAdjustableDumbbells &&
                !hasBarbell && !hasResistanceBands && !hasPullupBar && !hasParallettes && !hasRings &&
                !hasWeightVest && !hasBench)

    /**
     * Converts configuration into a list of equipment IDs supported by the exercise database.
     */
    fun toEquipmentIdList(): List<String> {
        if (hasBodyweightOnly) return emptyList()
        val list = mutableListOf<String>()
        if (hasSingleDumbbell) list.add("dumbbell_single")
        if (hasPairDumbbells) list.add("dumbbell_pair")
        if (hasAdjustableDumbbells) {
            list.add("dumbbell_adjustable")
            list.add("dumbbell_single")
            list.add("dumbbell_pair")
        }
        if (hasBarbell) list.add("barbell")
        if (hasResistanceBands) list.add("resistance_bands")
        if (hasPullupBar) list.add("pullup_bar")
        if (hasParallettes) list.add("parallettes")
        if (hasRings) list.add("rings")
        if (hasWeightVest) list.add("weight_vest")
        if (hasBench) list.add("bench")
        return list.distinct()
    }

    /**
     * Checks if the user owns all or at least one acceptable alternative of the required equipment items.
     * Empty required list means bodyweight only, which is always satisfied.
     */
    fun hasEquipmentFor(requiredEquipmentIds: List<String>): Boolean {
        if (requiredEquipmentIds.isEmpty()) return true
        val householdFixtures = setOf("wall", "floor", "table", "chair", "door")
        if (requiredEquipmentIds.any { it in householdFixtures }) {
            return true
        }
        if (hasBodyweightOnly) {
            return requiredEquipmentIds.all { it in householdFixtures }
        }
        val owned = toEquipmentIdList().toSet()
        return requiredEquipmentIds.any { req ->
            req in owned ||
                    req in householdFixtures ||
                    ((req == "chair" || req == "bench") && (hasBench || "bench" in owned)) ||
                    (req == "dumbbell_single" && (hasAdjustableDumbbells || hasPairDumbbells || hasSingleDumbbell || "dumbbell_single" in owned)) ||
                    (req == "dumbbell_pair" && (hasAdjustableDumbbells || hasPairDumbbells || "dumbbell_pair" in owned)) ||
                    (req == "pullup_bar" && (hasRings || hasPullupBar || "pullup_bar" in owned))
        }
    }

    /**
     * Finds effective available weight for a given equipment type (in kg).
     */
    fun getEffectiveWeightKg(equipmentId: String): Float? {
        return when (equipmentId) {
            "dumbbell_single" -> {
                when {
                    hasSingleDumbbell -> singleDumbbellWeightKg
                    hasAdjustableDumbbells -> adjustableMaxWeightKg
                    hasPairDumbbells -> pairDumbbellWeightKg
                    else -> null
                }
            }
            "dumbbell_pair" -> {
                when {
                    hasPairDumbbells -> pairDumbbellWeightKg
                    hasAdjustableDumbbells -> adjustableMaxWeightKg
                    else -> null
                }
            }
            "barbell" -> if (hasBarbell) barbellTotalWeightKg else null
            "weight_vest" -> if (hasWeightVest) weightVestWeightKg else null
            else -> null
        }
    }

    /**
     * Serializes to JSON string for local Room AppSettingDao persistence.
     */
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("hasBodyweightOnly", hasBodyweightOnly)
        obj.put("hasSingleDumbbell", hasSingleDumbbell)
        obj.put("singleDumbbellWeightKg", singleDumbbellWeightKg.toDouble())
        obj.put("hasPairDumbbells", hasPairDumbbells)
        obj.put("pairDumbbellWeightKg", pairDumbbellWeightKg.toDouble())
        obj.put("hasAdjustableDumbbells", hasAdjustableDumbbells)
        obj.put("adjustableMinWeightKg", adjustableMinWeightKg.toDouble())
        obj.put("adjustableMaxWeightKg", adjustableMaxWeightKg.toDouble())
        obj.put("adjustableIncrementKg", adjustableIncrementKg.toDouble())
        obj.put("hasBarbell", hasBarbell)
        obj.put("barbellTotalWeightKg", barbellTotalWeightKg.toDouble())
        obj.put("hasResistanceBands", hasResistanceBands)
        obj.put("resistanceBandLevel", resistanceBandLevel)
        obj.put("hasPullupBar", hasPullupBar)
        obj.put("hasParallettes", hasParallettes)
        obj.put("hasRings", hasRings)
        obj.put("hasWeightVest", hasWeightVest)
        obj.put("weightVestWeightKg", weightVestWeightKg.toDouble())
        obj.put("hasBench", hasBench)
        return obj.toString()
    }

    companion object {
        fun fromJson(jsonStr: String?): UserEquipmentProfile {
            if (jsonStr.isNullOrBlank()) return UserEquipmentProfile()
            return try {
                val obj = JSONObject(jsonStr)
                UserEquipmentProfile(
                    hasBodyweightOnly = obj.optBoolean("hasBodyweightOnly", false),
                    hasSingleDumbbell = obj.optBoolean("hasSingleDumbbell", false),
                    singleDumbbellWeightKg = obj.optDouble("singleDumbbellWeightKg", 10.0).toFloat(),
                    hasPairDumbbells = obj.optBoolean("hasPairDumbbells", false),
                    pairDumbbellWeightKg = obj.optDouble("pairDumbbellWeightKg", 10.0).toFloat(),
                    hasAdjustableDumbbells = obj.optBoolean("hasAdjustableDumbbells", false),
                    adjustableMinWeightKg = obj.optDouble("adjustableMinWeightKg", 2.5).toFloat(),
                    adjustableMaxWeightKg = obj.optDouble("adjustableMaxWeightKg", 24.0).toFloat(),
                    adjustableIncrementKg = obj.optDouble("adjustableIncrementKg", 2.5).toFloat(),
                    hasBarbell = obj.optBoolean("hasBarbell", false),
                    barbellTotalWeightKg = obj.optDouble("barbellTotalWeightKg", 30.0).toFloat(),
                    hasResistanceBands = obj.optBoolean("hasResistanceBands", false),
                    resistanceBandLevel = obj.optString("resistanceBandLevel", "ALL"),
                    hasPullupBar = obj.optBoolean("hasPullupBar", false),
                    hasParallettes = obj.optBoolean("hasParallettes", false),
                    hasRings = obj.optBoolean("hasRings", false),
                    hasWeightVest = obj.optBoolean("hasWeightVest", false),
                    weightVestWeightKg = obj.optDouble("weightVestWeightKg", 10.0).toFloat(),
                    hasBench = obj.optBoolean("hasBench", false)
                )
            } catch (_: Exception) {
                UserEquipmentProfile()
            }
        }
    }
}
