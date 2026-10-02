package com.example.domain.engine

import com.example.data.local.entity.AdaptationDecisionEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.PainEntryEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProgramEntity
import com.example.data.local.entity.ProgramVersionEntity
import com.example.data.local.entity.RecoveryEntryEntity
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.SkillProgressEntity
import com.example.data.local.entity.UserCapabilityEntity
import com.example.data.local.entity.WorkoutExerciseEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.domain.equipment.AdaptiveEquipmentEngine
import com.example.domain.equipment.UserEquipmentProfile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class GeneratedProgramPackage(
    val program: ProgramEntity,
    val initialVersion: ProgramVersionEntity,
    val sessions: List<WorkoutSessionEntity>,
    val workoutExercises: List<WorkoutExerciseEntity>,
    val decisions: List<AdaptationDecisionEntity>
)

data class ProgramGenerationParams(
    val profile: ProfileEntity,
    val capability: UserCapabilityEntity,
    val allExercises: List<ExerciseEntity>,
    val assessment: AssessmentEntity? = null,
    val recentSetLogs: List<SetLogEntity> = emptyList(),
    val recentRecovery: List<RecoveryEntryEntity> = emptyList(),
    val recentPain: List<PainEntryEntity> = emptyList(),
    val skillProgress: List<SkillProgressEntity> = emptyList(),
    val targetDaysPerWeek: Int? = null,
    val sessionDurationMinutes: Int? = null,
    val forcedSplit: String? = null,
    val userEquipmentProfile: UserEquipmentProfile? = null
)

object DataDrivenProgramGenerator {

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
     * Backward-compatible overload
     */
    fun generateProgram(
        profile: ProfileEntity,
        capability: UserCapabilityEntity,
        allExercises: List<ExerciseEntity>,
        targetDaysPerWeek: Int? = null,
        forcedSplit: String? = null
    ): GeneratedProgramPackage {
        return generateProgram(
            ProgramGenerationParams(
                profile = profile,
                capability = capability,
                allExercises = allExercises,
                targetDaysPerWeek = targetDaysPerWeek,
                forcedSplit = forcedSplit
            )
        )
    }

    /**
     * Intelligent, personalized program generation based on goals, experience, days, duration,
     * equipment, current performance, workout history, recovery, and skills.
     */
    fun generateProgram(params: ProgramGenerationParams): GeneratedProgramPackage {
        val profile = params.profile
        val capability = params.capability
        val allExercises = params.allExercises
        val assessment = params.assessment
        val recentSetLogs = params.recentSetLogs
        val recentRecovery = params.recentRecovery
        val recentPain = params.recentPain

        val programId = UUID.randomUUID().toString()
        val days = (params.targetDaysPerWeek ?: profile.trainingDaysPerWeek).coerceIn(2, 6)
        val level = profile.experienceLevel.uppercase()
        val durationMin = (params.sessionDurationMinutes ?: 60).coerceIn(30, 90)

        val userEquipment = params.userEquipmentProfile ?: run {
            val list = parseJsonStringList(profile.equipmentJson).map { it.lowercase() }
            UserEquipmentProfile(
                hasBodyweightOnly = list.isEmpty(),
                hasPullupBar = list.contains("pullup_bar"),
                hasParallettes = list.contains("parallettes"),
                hasRings = list.contains("rings"),
                hasResistanceBands = list.contains("resistance_bands"),
                hasSingleDumbbell = list.contains("dumbbell_single"),
                hasPairDumbbells = list.contains("dumbbell_pair"),
                hasAdjustableDumbbells = list.contains("dumbbell_adjustable"),
                hasBarbell = list.contains("barbell"),
                hasWeightVest = list.contains("weight_vest"),
                hasBench = list.contains("bench")
            )
        }

        val equipment = userEquipment.toEquipmentIdList().map { it.lowercase() }
        val goals = parseJsonStringList(profile.goalsJson).ifEmpty { listOf("STRENGTH") }

        val primaryGoal = goals.firstOrNull() ?: "STRENGTH"
        val hasPullupBar = userEquipment.hasPullupBar || userEquipment.hasRings
        val hasParallettes = userEquipment.hasParallettes

        val decisions = mutableListOf<AdaptationDecisionEntity>()
        val exerciseMap = allExercises.associateBy { it.id }

        val equipmentSummary = when {
            userEquipment.hasBodyweightOnly -> "بدون تجهیزات (تمرکز کامل بر وزن بدن)"
            userEquipment.toEquipmentIdList().isEmpty() -> "بدون تجهیزات (تمرکز کامل بر وزن بدن)"
            else -> userEquipment.toEquipmentIdList().joinToString("، ") { eqId ->
                when (eqId) {
                    "dumbbell_single" -> "دمبل تک (${userEquipment.singleDumbbellWeightKg}kg)"
                    "dumbbell_pair" -> "جفت دمبل (${userEquipment.pairDumbbellWeightKg}kg)"
                    "dumbbell_adjustable" -> "دمبل متغیر (${userEquipment.adjustableMinWeightKg}-${userEquipment.adjustableMaxWeightKg}kg)"
                    "barbell" -> "هالتر (${userEquipment.barbellTotalWeightKg}kg)"
                    "resistance_bands" -> "کش‌های مقاومتی"
                    "pullup_bar" -> "میله بارفیکس"
                    "parallettes" -> "پارالل"
                    "rings" -> "حلقه‌های ژیمناستیک"
                    "weight_vest" -> "جلیقه وزنی (${userEquipment.weightVestWeightKg}kg)"
                    "bench" -> "نیمکت"
                    else -> eqId
                }
            }
        }

        decisions.add(
            AdaptationDecisionEntity(
                id = UUID.randomUUID().toString(),
                profileId = profile.id,
                programId = programId,
                decisionType = "EQUIPMENT_CONFIGURATION",
                decision = "شخصی‌سازی کامل برنامه بر اساس تجهیزات فعال کاربر: $equipmentSummary",
                reason = "اولویت‌دهی به تجهیزات در دسترس، حذف تمرینات نیازمند تجهیزات ناموجود و تعدیل بیومکانیکی تکرارها و تمپو با اوزان مشخص‌شده.",
                programVersion = 1
            )
        )

        // -------------------------------------------------------------
        // Step 1: Health, Pain & Recovery Assessment
        // -------------------------------------------------------------
        val activePainShoulder = recentPain.any {
            (it.location.contains("شانه") || it.location.contains("کتف")) &&
                    (it.severity >= 3 || it.status == "RED" || it.status == "YELLOW")
        }
        val activePainWrist = recentPain.any {
            it.location.contains("مچ") && (it.severity >= 3 || it.status == "RED" || it.status == "YELLOW")
        }
        val activePainKnee = recentPain.any {
            it.location.contains("زانو") && (it.severity >= 3 || it.status == "RED" || it.status == "YELLOW")
        }

        val avgSleep = if (recentRecovery.isNotEmpty()) {
            recentRecovery.take(5).map { it.sleepDurationHours }.average().toFloat()
        } else 7.5f

        val avgFatigue = if (recentRecovery.isNotEmpty()) {
            recentRecovery.take(5).map { it.fatigueLevel }.average().toInt()
        } else 2

        val isFatigued = avgSleep < 6.0f || avgFatigue >= 4

        if (activePainShoulder) {
            decisions.add(
                AdaptationDecisionEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profile.id,
                    programId = programId,
                    decisionType = "PAIN_AVOIDANCE",
                    decision = "حذف حرکات پرفشار شانه (دیپ عمیق و بارفیکس سنگین) و جایگزینی با حرکات ایمن",
                    reason = "ثبت ناراحتی در ناحیه شانه/کتف؛ کاهش بار تجمعی کپسول قدامی و تمرکز بر ثبات اسکاپولا.",
                    programVersion = 1
                )
            )
        }

        if (activePainWrist) {
            decisions.add(
                AdaptationDecisionEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profile.id,
                    programId = programId,
                    decisionType = "PAIN_AVOIDANCE",
                    decision = "تعدیل بار مچ دست و اولویت‌دهی به دستگیره خنثی یا پلانک ساعد",
                    reason = "ثبت ناراحتی در مفصل مچ دست؛ اجتناب از هایپراکستنشن بدون تکیه‌گاه خنثی.",
                    programVersion = 1
                )
            )
        }

        if (activePainKnee) {
            decisions.add(
                AdaptationDecisionEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profile.id,
                    programId = programId,
                    decisionType = "PAIN_AVOIDANCE",
                    decision = "حذف اسکات تک‌پا و جایگزینی با اسکات کنترل‌شده دو پا",
                    reason = "ثبت ناراحتی در زانو؛ کنترل بار تاندون پاتلار و حذف نیروهای برشی شدید.",
                    programVersion = 1
                )
            )
        }

        if (isFatigued) {
            decisions.add(
                AdaptationDecisionEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profile.id,
                    programId = programId,
                    decisionType = "RECOVERY_MODULATION",
                    decision = "تعدیل حجم کل جلسات (کاهش ست‌ها به میزان ۲۰ الی ۲۵ درصد)",
                    reason = "میانگین خواب کمتر از ۶ ساعت یا خستگی عمومی بالا در روزهای اخیر ثبت شده است.",
                    programVersion = 1
                )
            )
        }

        if (!hasPullupBar) {
            decisions.add(
                AdaptationDecisionEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profile.id,
                    programId = programId,
                    decisionType = "EQUIPMENT_ADAPTATION",
                    decision = "جایگزینی حرکات بارفیکس با تمرینات کشش افقی و میان‌تنه زمینی",
                    reason = "عدم ثبت میله بارفیکس در تجهیزات کاربر؛ طراحی تمرین متکی بر اینورتد رو و پلانک.",
                    programVersion = 1
                )
            )
        }

        // -------------------------------------------------------------
        // Step 2: Recent Workout History & Recovery Conflict Detection
        // -------------------------------------------------------------
        val completedLogs = recentSetLogs.filter { it.status == "COMPLETED" }
        val avgRecentRpe = if (completedLogs.isNotEmpty()) completedLogs.map { it.rpe }.average() else 7.5
        val recentFailureOrPain = recentSetLogs.any { it.status == "PAIN_STOP" || it.painSeverity >= 3 }

        val performanceDelta = when {
            recentFailureOrPain -> -1
            completedLogs.size >= 4 && avgRecentRpe <= 7.0 -> 1
            completedLogs.size >= 4 && avgRecentRpe >= 8.8 -> -1
            else -> 0
        }

        if (performanceDelta == 1) {
            decisions.add(
                AdaptationDecisionEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profile.id,
                    programId = programId,
                    decisionType = "PROGRESSIVE_OVERLOAD",
                    decision = "اعمال ارتقای تک‌متغیره در تکرارها و زمان مکث",
                    reason = "تکمیل موفقیت‌آمیز ست‌های قبلی با RPE کنترل‌شده (میانگین ${String.format(Locale.US, "%.1f", avgRecentRpe)}).",
                    programVersion = 1
                )
            )
        } else if (performanceDelta == -1) {
            decisions.add(
                AdaptationDecisionEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profile.id,
                    programId = programId,
                    decisionType = "REGRESSION_MODULATION",
                    decision = "کاهش هدف تکرارها جهت تثبیت فرم و بازیابی انرژی",
                    reason = if (recentFailureOrPain) "ثبت شاخص درد یا توقف ست در تمرینات اخیر." else "فشار درک‌شده بالا (RPE ${String.format(Locale.US, "%.1f", avgRecentRpe)}) در ست‌های اخیر.",
                    programVersion = 1
                )
            )
        }

        // Detect if yesterday user trained a specific muscle group heavily
        val lastLoggedExerciseIds = recentSetLogs.takeLast(8).map { it.exerciseId }
        val recentPushFatigue = lastLoggedExerciseIds.any { it.contains("push") || it.contains("dip") }
        val recentPullFatigue = lastLoggedExerciseIds.any { it.contains("pull") || it.contains("row") || it.contains("lever") }
        val recentLegFatigue = lastLoggedExerciseIds.any { it.contains("squat") || it.contains("lunge") }

        // -------------------------------------------------------------
        // Step 3: Skill-Oriented Training & Prerequisite Verification
        // -------------------------------------------------------------
        val selectedSkills = goals.mapNotNull { SupportedSkill.fromIdOrName(it) }.distinct()
        val skillEvaluations = SkillProgressionRegistry.evaluateAllSkills(
            skills = selectedSkills,
            capability = capability,
            assessment = assessment,
            availableEquipment = equipment,
            allExercises = allExercises,
            skillProgress = params.skillProgress
        )

        skillEvaluations.forEach { eval ->
            decisions.add(
                AdaptationDecisionEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profile.id,
                    programId = programId,
                    decisionType = if (eval.meetsPrerequisites) "SKILL_STAGE_ASSIGNED" else "PREREQUISITE_EVALUATION",
                    decision = if (eval.meetsPrerequisites) {
                        "تجویز مرحله ${eval.stageNameFa} مهارت ${eval.skill.titleFa} با حرکت ${exerciseMap[eval.chosenExerciseId]?.persianName ?: eval.chosenExerciseId}"
                    } else {
                        "تجویز پیش‌نیازهای پایه ${eval.skill.titleFa} با حرکت ${exerciseMap[eval.chosenExerciseId]?.persianName ?: eval.chosenExerciseId}"
                    },
                    reason = eval.rationaleFa,
                    programVersion = 1
                )
            )
        }

        // -------------------------------------------------------------
        // Step 4: Split Type & Weekly Structure
        // -------------------------------------------------------------
        val splitType = params.forcedSplit ?: when (days) {
            2 -> "FULL_BODY_2DAY"
            3 -> "FULL_BODY_3DAY"
            4 -> "UPPER_LOWER"
            5 -> "PUSH_PULL_LEGS_UPPER_SKILL"
            6 -> "PUSH_PULL_LEGS_CYCLE"
            else -> "FULL_BODY_3DAY"
        }

        if (recentPushFatigue || recentPullFatigue || recentLegFatigue) {
            decisions.add(
                AdaptationDecisionEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profile.id,
                    programId = programId,
                    decisionType = "RECOVERY_CONFLICT_AVOIDED",
                    decision = "تطبیق چیدمان جلسه آغازین با عضلات ریکاوری‌شده",
                    reason = "تشخیص بارگیری اخیر در زنجیره‌های عضلانی؛ اولویت‌دهی به زنجیره متضاد در اولین جلسه.",
                    programVersion = 1
                )
            )
        }

        decisions.add(
            AdaptationDecisionEntity(
                id = UUID.randomUUID().toString(),
                profileId = profile.id,
                programId = programId,
                decisionType = "SPLIT_ALLOCATION",
                decision = "تخصیص ساختار هفتگی $splitType برای $days روز تمرین و مدت جلسه $durationMin دقیقه",
                reason = "مدیریت بهینه فاصله استراحت ۴۸ تا ۷۲ ساعته بین زنجیره‌های عضلانی هم‌راستا.",
                programVersion = 1
            )
        )

        // -------------------------------------------------------------
        // Step 5: Exercise Pool Selection based on Capability & Equipment
        // -------------------------------------------------------------
        val pushupsMax = assessment?.pushupsMax ?: 10
        val pullupsMax = assessment?.pullupsMax ?: 2

        // Push Movement Hierarchy (Equipment-aware)
        val pushPrimaryCandidate = when {
            activePainShoulder -> exerciseMap["pushup_knee"] ?: exerciseMap["pushup_std"]
            activePainWrist && hasParallettes -> exerciseMap["dips_parallel"] ?: exerciseMap["pushup_knee"]
            activePainWrist -> exerciseMap["pushup_knee"]
            userEquipment.hasWeightVest && pushupsMax >= 18 -> exerciseMap["vest_pushup"] ?: exerciseMap["pushup_diamond"]
            userEquipment.hasBarbell && level != "BEGINNER" -> exerciseMap["bb_floor_press"] ?: exerciseMap["bb_overhead_press"]
            (userEquipment.hasPairDumbbells || userEquipment.hasAdjustableDumbbells) && level != "BEGINNER" -> exerciseMap["db_pair_floor_press"] ?: exerciseMap["db_pair_overhead_press"]
            userEquipment.hasSingleDumbbell -> exerciseMap["db_single_floor_press"] ?: exerciseMap["pushup_diamond"]
            userEquipment.hasResistanceBands && pushupsMax >= 15 -> exerciseMap["band_resisted_pushup"]
            capability.pushingStrength >= 70f && hasParallettes -> exerciseMap["dips_parallel"]
            capability.pushingStrength >= 55f || pushupsMax >= 18 -> exerciseMap["pushup_diamond"]
            capability.pushingStrength >= 30f || pushupsMax >= 6 -> exerciseMap["pushup_std"]
            else -> exerciseMap["pushup_knee"]
        } ?: allExercises.first { it.category == "PUSH" }

        val pushPrimary = AdaptiveEquipmentEngine.substituteIfUnavailable(pushPrimaryCandidate, userEquipment, allExercises)

        val pushSecondaryCandidate = when {
            activePainShoulder -> exerciseMap["pushup_knee"] ?: pushPrimary
            userEquipment.hasResistanceBands && !pushPrimary.id.contains("band") -> exerciseMap["band_resisted_pushup"] ?: exerciseMap["pushup_std"]
            userEquipment.hasSingleDumbbell && pushPrimary.id != "db_single_overhead_press" -> exerciseMap["db_single_overhead_press"] ?: exerciseMap["pushup_std"]
            userEquipment.hasPairDumbbells && pushPrimary.id != "db_pair_overhead_press" -> exerciseMap["db_pair_overhead_press"] ?: exerciseMap["pushup_std"]
            pushPrimary.id == "dips_parallel" -> exerciseMap["pushup_diamond"] ?: exerciseMap["pushup_std"]
            pushPrimary.id == "pushup_diamond" -> exerciseMap["pushup_std"]
            else -> exerciseMap["dips_bench"] ?: exerciseMap["pushup_knee"] ?: pushPrimary
        } ?: pushPrimary

        val pushSecondary = AdaptiveEquipmentEngine.substituteIfUnavailable(pushSecondaryCandidate, userEquipment, allExercises)

        // Pull Movement Hierarchy (Equipment-aware)
        val pullPrimaryCandidate = when {
            activePainShoulder -> exerciseMap["inverted_row"]
            userEquipment.hasWeightVest && pullupsMax >= 8 && hasPullupBar -> exerciseMap["vest_pullup"] ?: exerciseMap["pullup_std"]
            userEquipment.hasBarbell && level != "BEGINNER" -> exerciseMap["bb_bent_over_row"] ?: exerciseMap["inverted_row"]
            (userEquipment.hasPairDumbbells || userEquipment.hasAdjustableDumbbells) && !hasPullupBar -> exerciseMap["db_pair_bent_over_row"] ?: exerciseMap["inverted_row"]
            userEquipment.hasSingleDumbbell && !hasPullupBar -> exerciseMap["db_single_row"] ?: exerciseMap["inverted_row"]
            hasPullupBar && (capability.pullingStrength >= 65f || pullupsMax >= 8) -> exerciseMap["pullup_std"]
            hasPullupBar && (capability.pullingStrength >= 40f || pullupsMax >= 4) -> exerciseMap["pullup_chinup"]
            userEquipment.hasResistanceBands && hasPullupBar && pullupsMax < 4 -> exerciseMap["band_assisted_pullup"] ?: exerciseMap["inverted_row"]
            else -> exerciseMap["inverted_row"]
        } ?: allExercises.first { it.category == "PULL" }

        val pullPrimary = AdaptiveEquipmentEngine.substituteIfUnavailable(pullPrimaryCandidate, userEquipment, allExercises)

        val pullSecondaryCandidate = when {
            activePainShoulder -> exerciseMap["inverted_row"]
            userEquipment.hasResistanceBands -> exerciseMap["band_face_pull"] ?: exerciseMap["band_pull_apart"] ?: exerciseMap["inverted_row"]
            userEquipment.hasSingleDumbbell && pullPrimary.id != "db_single_row" -> exerciseMap["db_single_row"] ?: exerciseMap["inverted_row"]
            userEquipment.hasPairDumbbells && pullPrimary.id != "db_pair_bent_over_row" -> exerciseMap["db_pair_bent_over_row"] ?: exerciseMap["inverted_row"]
            !hasPullupBar -> exerciseMap["inverted_row"]
            pullPrimary.id == "pullup_std" -> exerciseMap["pullup_chinup"] ?: exerciseMap["inverted_row"]
            pullPrimary.id == "pullup_chinup" -> exerciseMap["inverted_row"]
            else -> exerciseMap["dead_hang"] ?: exerciseMap["inverted_row"]
        } ?: pullPrimary

        val pullSecondary = AdaptiveEquipmentEngine.substituteIfUnavailable(pullSecondaryCandidate, userEquipment, allExercises)

        // Leg Movement Hierarchy (Equipment-aware)
        val legPrimaryCandidate = when {
            activePainKnee -> exerciseMap["squat_bodyweight"]
            userEquipment.hasBarbell && level != "BEGINNER" -> exerciseMap["bb_squat"] ?: exerciseMap["bb_deadlift"] ?: exerciseMap["squat_bulgarian"]
            (userEquipment.hasPairDumbbells || userEquipment.hasAdjustableDumbbells) && level != "BEGINNER" -> exerciseMap["db_pair_squat"] ?: exerciseMap["db_pair_romanian_deadlift"]
            userEquipment.hasSingleDumbbell -> exerciseMap["db_goblet_squat"] ?: exerciseMap["db_single_rdl"]
            userEquipment.hasWeightVest -> exerciseMap["vest_squat"] ?: exerciseMap["squat_bulgarian"]
            userEquipment.hasResistanceBands -> exerciseMap["band_squat"] ?: exerciseMap["squat_bulgarian"]
            capability.legStrength >= 75f && level == "ADVANCED" -> exerciseMap["squat_pistol"] ?: exerciseMap["squat_bulgarian"]
            capability.legStrength >= 45f -> exerciseMap["squat_bulgarian"]
            else -> exerciseMap["squat_bodyweight"]
        } ?: allExercises.first { it.category == "LEGS" }

        val legPrimary = AdaptiveEquipmentEngine.substituteIfUnavailable(legPrimaryCandidate, userEquipment, allExercises)

        val legSecondaryCandidate = when {
            activePainKnee -> exerciseMap["squat_bodyweight"]
            userEquipment.hasBarbell && legPrimary.id != "bb_deadlift" -> exerciseMap["bb_deadlift"] ?: exerciseMap["squat_bulgarian"]
            (userEquipment.hasPairDumbbells || userEquipment.hasAdjustableDumbbells) && legPrimary.id != "db_pair_romanian_deadlift" -> exerciseMap["db_pair_romanian_deadlift"] ?: exerciseMap["squat_bulgarian"]
            userEquipment.hasSingleDumbbell && legPrimary.id != "db_single_rdl" -> exerciseMap["db_single_rdl"] ?: exerciseMap["squat_bulgarian"]
            legPrimary.id == "squat_pistol" -> exerciseMap["squat_bulgarian"]
            legPrimary.id == "squat_bulgarian" -> exerciseMap["squat_bodyweight"]
            else -> exerciseMap["squat_bulgarian"] ?: legPrimary
        } ?: legPrimary

        val legSecondary = AdaptiveEquipmentEngine.substituteIfUnavailable(legSecondaryCandidate, userEquipment, allExercises)

        // Core Movement Hierarchy (Equipment-aware)
        val corePrimaryCandidate = when {
            capability.coreStrength >= 65f && hasPullupBar -> exerciseMap["hanging_leg_raise"] ?: exerciseMap["hollow_body"]
            capability.coreStrength >= 40f -> exerciseMap["hollow_body"] ?: exerciseMap["plank_std"]
            else -> exerciseMap["plank_std"]
        } ?: allExercises.first { it.category == "CORE" }

        val corePrimary = AdaptiveEquipmentEngine.substituteIfUnavailable(corePrimaryCandidate, userEquipment, allExercises)

        val coreSecondaryCandidate = when {
            corePrimary.id == "hanging_leg_raise" -> exerciseMap["hollow_body"]
            corePrimary.id == "hollow_body" -> exerciseMap["plank_std"]
            else -> exerciseMap["hollow_body"] ?: corePrimary
        } ?: corePrimary

        val coreSecondary = AdaptiveEquipmentEngine.substituteIfUnavailable(coreSecondaryCandidate, userEquipment, allExercises)

        // -------------------------------------------------------------
        // Step 6: Reps, Sets, and Rest based on Goal & Duration
        // -------------------------------------------------------------
        val baseSets = when {
            isFatigued -> 2
            level == "BEGINNER" -> if (durationMin <= 40) 2 else 3
            level == "ADVANCED" -> if (durationMin <= 40) 3 else 4
            durationMin <= 35 -> 2
            durationMin <= 50 -> 3
            durationMin >= 75 -> 4
            else -> 3
        }

        val (targetRepsNormal, targetRpeNormal, restNormalSec) = when (primaryGoal) {
            "STRENGTH" -> Triple(5, 8, 120)
            "HYPERTROPHY" -> Triple(10, 7, 90)
            "ENDURANCE" -> Triple(18, 6, 60)
            else -> Triple(8, 7, 90)
        }

        // -------------------------------------------------------------
        // Step 7: Staggered Scheduling & Balanced Sessions
        // -------------------------------------------------------------
        val sessions = mutableListOf<WorkoutSessionEntity>()
        val workoutExercises = mutableListOf<WorkoutExerciseEntity>()

        val scheduledDates = calculateScheduledDates(Date(), days, splitType)

        for (i in 1..days) {
            val sId = UUID.randomUUID().toString()
            val sessionDate = scheduledDates.getOrElse(i - 1) { scheduledDates.last() }

            // Dynamic session titles taking recent fatigue into account
            val sTitle = when (splitType) {
                "FULL_BODY_2DAY" -> {
                    if (recentPushFatigue) {
                        if (i == 1) "جلسه ۱: فول بادی کششی و تعادلی (Full Body Pull Focus)" else "جلسه ۲: فول بادی فشاری و مهارتی (Full Body Push Focus)"
                    } else {
                        if (i == 1) "جلسه ۱: فول بادی فشاری و مهارتی (Full Body Push Focus)" else "جلسه ۲: فول بادی کششی و تعادلی (Full Body Pull Focus)"
                    }
                }
                "FULL_BODY_3DAY" -> when (i) {
                    1 -> if (recentPushFatigue) "جلسه ۱: فول بادی محور کششی (Pull Focus)" else "جلسه ۱: فول بادی محور فشاری (Push Focus)"
                    2 -> if (recentPushFatigue) "جلسه ۲: فول بادی محور فشاری (Push Focus)" else "جلسه ۲: فول بادی محور کششی (Pull Focus)"
                    else -> "جلسه ۳: فول بادی تعادل الگوها و مهارت (Balance & Core)"
                }
                "UPPER_LOWER" -> {
                    if (recentPushFatigue || recentPullFatigue) {
                        when (i) {
                            1 -> "جلسه ۱: پایین‌تنه و هسته بدن (Lower & Core A)"
                            2 -> "جلسه ۲: بالاتنه فشاری و مهارتی (Upper A)"
                            3 -> "جلسه ۳: پایین‌تنه قدرتی و شکم (Lower & Core B)"
                            else -> "جلسه ۴: بالاتنه کششی و پایداری (Upper B)"
                        }
                    } else {
                        when (i) {
                            1 -> "جلسه ۱: بالاتنه فشاری و مهارتی (Upper A)"
                            2 -> "جلسه ۲: پایین‌تنه و هسته بدن (Lower & Core A)"
                            3 -> "جلسه ۳: بالاتنه کششی و پایداری (Upper B)"
                            else -> "جلسه ۴: پایین‌تنه قدرتی و شکم (Lower & Core B)"
                        }
                    }
                }
                "PUSH_PULL_LEGS_UPPER_SKILL" -> {
                    if (recentPushFatigue) {
                        when (i) {
                            1 -> "جلسه ۱: عضلات کششی (Pull)"
                            2 -> "جلسه ۲: عضلات فشاری (Push)"
                            3 -> "جلسه ۳: پاها و میان‌تنه (Legs & Core)"
                            4 -> "جلسه ۴: بالاتنه تلفیقی (Upper Body)"
                            else -> "جلسه ۵: مهارت تخصصی و زنجیره حرکتی (Skill & Core)"
                        }
                    } else {
                        when (i) {
                            1 -> "جلسه ۱: عضلات فشاری (Push)"
                            2 -> "جلسه ۲: عضلات کششی (Pull)"
                            3 -> "جلسه ۳: پاها و میان‌تنه (Legs & Core)"
                            4 -> "جلسه ۴: بالاتنه تلفیقی (Upper Body)"
                            else -> "جلسه ۵: مهارت تخصصی و زنجیره حرکتی (Skill & Core)"
                        }
                    }
                }
                "PUSH_PULL_LEGS_CYCLE" -> {
                    val mod = (i - 1) % 3
                    val startWithPull = recentPushFatigue
                    val sessionName = if (startWithPull) {
                        when (mod) {
                            0 -> "کششی تخصصی (Pull)"
                            1 -> "پاها و عضلات شکم (Legs)"
                            else -> "فشاری تخصصی (Push)"
                        }
                    } else {
                        when (mod) {
                            0 -> "فشاری تخصصی (Push)"
                            1 -> "کششی تخصصی (Pull)"
                            else -> "پاها و عضلات شکم (Legs)"
                        }
                    }
                    "جلسه $i: $sessionName"
                }
                else -> "جلسه $i: تمرین کالیستنیکس"
            }

            sessions.add(
                WorkoutSessionEntity(
                    id = sId,
                    profileId = profile.id,
                    programId = programId,
                    title = sTitle,
                    scheduledDate = sessionDate,
                    status = "PLANNED"
                )
            )

            var orderIndex = 1

            // ---------------------------------------------------------
            // Priority 1: Skill Block Placement (Fresh Neuromuscular Training)
            // ---------------------------------------------------------
            val applicableSkill = skillEvaluations.firstOrNull { eval ->
                val isPushSkill = eval.skill == SupportedSkill.PLANCHE || eval.skill == SupportedSkill.HANDSTAND
                val isPullSkill = eval.skill == SupportedSkill.FRONT_LEVER || eval.skill == SupportedSkill.BACK_LEVER || eval.skill == SupportedSkill.MUSCLE_UP
                val isCoreSkill = eval.skill == SupportedSkill.LSIT || eval.skill == SupportedSkill.VSIT || eval.skill == SupportedSkill.HUMAN_FLAG

                when {
                    splitType.contains("FULL_BODY") -> {
                        val skillIndex = (i - 1) % skillEvaluations.size
                        eval == skillEvaluations[skillIndex]
                    }
                    sTitle.contains("Push") && isPushSkill -> true
                    sTitle.contains("Pull") && isPullSkill -> true
                    (sTitle.contains("Legs") || sTitle.contains("Core")) && isCoreSkill -> true
                    sTitle.contains("Upper") && (isPushSkill || isPullSkill) -> true
                    sTitle.contains("Skill") -> true
                    else -> false
                }
            }

            if (applicableSkill != null) {
                val skillEx = exerciseMap[applicableSkill.chosenExerciseId] ?: pushPrimary
                val targetReps = if (applicableSkill.targetHoldSec > 0) 0 else (applicableSkill.targetReps + performanceDelta).coerceAtLeast(3)
                val targetHold = if (applicableSkill.targetHoldSec > 0) (applicableSkill.targetHoldSec + performanceDelta * 2).coerceAtLeast(5) else 0

                workoutExercises.add(
                    WorkoutExerciseEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = sId,
                        exerciseId = skillEx.id,
                        orderIndex = orderIndex++,
                        targetSets = if (isFatigued) 2 else 3,
                        targetReps = targetReps,
                        targetHoldSec = targetHold,
                        targetRpe = applicableSkill.targetRpe,
                        restSec = 120,
                        notes = "تمرکز عصبی بر اجرای دقیق ${applicableSkill.skill.titleFa} (${applicableSkill.stageNameFa}) پیش از خستگی عضلانی"
                    )
                )
            }

            // ---------------------------------------------------------
            // Priority 2: Primary & Secondary Compound Movements
            // ---------------------------------------------------------
            val sessionExercisesToAdd = mutableListOf<ExerciseEntity>()

            when {
                sTitle.contains("Push") && !sTitle.contains("Full Body") -> {
                    sessionExercisesToAdd.add(pushPrimary)
                    sessionExercisesToAdd.add(pushSecondary)
                    if (durationMin >= 45) sessionExercisesToAdd.add(corePrimary)
                    if (durationMin >= 60) sessionExercisesToAdd.add(exerciseMap["dips_bench"] ?: pushPrimary)
                }
                sTitle.contains("Pull") && !sTitle.contains("Full Body") -> {
                    sessionExercisesToAdd.add(pullPrimary)
                    sessionExercisesToAdd.add(pullSecondary)
                    if (durationMin >= 45) sessionExercisesToAdd.add(coreSecondary)
                    if (durationMin >= 60 && hasPullupBar) sessionExercisesToAdd.add(exerciseMap["dead_hang"] ?: pullPrimary)
                }
                sTitle.contains("Legs") || sTitle.contains("Lower") -> {
                    sessionExercisesToAdd.add(legPrimary)
                    sessionExercisesToAdd.add(legSecondary)
                    sessionExercisesToAdd.add(corePrimary)
                    if (durationMin >= 60) sessionExercisesToAdd.add(coreSecondary)
                }
                sTitle.contains("Upper") -> {
                    sessionExercisesToAdd.add(pushPrimary)
                    sessionExercisesToAdd.add(pullPrimary)
                    if (durationMin >= 45) sessionExercisesToAdd.add(pushSecondary)
                    if (durationMin >= 60) sessionExercisesToAdd.add(pullSecondary)
                }
                else -> {
                    // Full Body Session: Balanced Push, Pull, Leg, Core
                    val isAltSession = i % 2 == 0
                    sessionExercisesToAdd.add(if (isAltSession) pushSecondary else pushPrimary)
                    sessionExercisesToAdd.add(if (isAltSession) pullPrimary else pullSecondary)
                    sessionExercisesToAdd.add(if (isAltSession) legSecondary else legPrimary)
                    if (durationMin >= 45) {
                        sessionExercisesToAdd.add(if (isAltSession) coreSecondary else corePrimary)
                    }
                    if (durationMin >= 60) {
                        val extra = if (hasPullupBar && !sessionExercisesToAdd.any { it.id == "dead_hang" }) {
                            exerciseMap["dead_hang"] ?: coreSecondary
                        } else {
                            if (isAltSession) pushPrimary else pullPrimary
                        }
                        if (!sessionExercisesToAdd.contains(extra)) {
                            sessionExercisesToAdd.add(extra)
                        }
                    }
                }
            }

            // Truncate based on time capacity
            val maxAllowedExercises = when {
                durationMin <= 35 -> 3
                durationMin <= 50 -> 4
                durationMin <= 70 -> 5
                else -> 6
            }

            val finalExerciseList = sessionExercisesToAdd
                .distinctBy { it.id }
                .filter { it.id != applicableSkill?.chosenExerciseId }
                .take(maxAllowedExercises - (if (orderIndex > 1) 1 else 0))

            finalExerciseList.forEach { ex ->
                val prescription = AdaptiveEquipmentEngine.adaptPrescription(
                    targetExercise = ex,
                    equipment = userEquipment,
                    userLevel = level,
                    userWeightKg = profile.weightKg,
                    capability = capability,
                    goal = primaryGoal,
                    recentRpeAvg = avgRecentRpe.toFloat(),
                    allExercises = allExercises
                )

                if (prescription.isAdaptedForWeight && prescription.adaptationNote != null && decisions.none { it.exerciseId == prescription.exercise.id && it.decisionType == "WEIGHT_ADAPTATION" }) {
                    decisions.add(
                        AdaptationDecisionEntity(
                            id = UUID.randomUUID().toString(),
                            profileId = profile.id,
                            programId = programId,
                            exerciseId = prescription.exercise.id,
                            decisionType = "WEIGHT_ADAPTATION",
                            decision = "تعدیل بار حرکت ${prescription.exercise.persianName} با ${prescription.requiredEquipmentFa}",
                            reason = prescription.adaptationNote,
                            programVersion = 1
                        )
                    )
                }

                val isIsometric = prescription.exercise.minHoldSec > 0 || prescription.exercise.id in listOf("plank_std", "hollow_body", "dead_hang", "lsit", "front_lever_tuck", "handstand")
                val reps = if (isIsometric) 0 else (prescription.targetReps + performanceDelta).coerceAtLeast(4)
                val hold = if (isIsometric) {
                    val baseHold = when (prescription.exercise.id) {
                        "plank_std" -> if (primaryGoal == "ENDURANCE") 45 else 30
                        "hollow_body" -> if (primaryGoal == "ENDURANCE") 30 else 20
                        "dead_hang" -> if (primaryGoal == "ENDURANCE") 40 else 25
                        "lsit" -> 12
                        "front_lever_tuck" -> 10
                        "handstand" -> 20
                        else -> 15
                    }
                    (baseHold + performanceDelta * 3).coerceAtLeast(6)
                } else prescription.targetHoldSec

                workoutExercises.add(
                    WorkoutExerciseEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = sId,
                        exerciseId = prescription.exercise.id,
                        orderIndex = orderIndex++,
                        targetSets = if (isFatigued) 2 else prescription.targetSets,
                        targetReps = reps,
                        targetHoldSec = hold,
                        targetRpe = prescription.targetRpe,
                        restSec = if (isIsometric) 60 else prescription.restSec,
                        notes = prescription.formattedNotes
                    )
                )
            }
        }

        // -------------------------------------------------------------
        // Step 8: Program & Version Entities
        // -------------------------------------------------------------
        val skillSummary = if (skillEvaluations.isNotEmpty()) {
            "اهداف مهارتی: " + skillEvaluations.joinToString("، ") { it.skill.titleFa } + ". "
        } else ""

        val programTitle = "برنامه تخصصی کالیستنیکس - $splitType"
        val programDesc = "طراحی‌شده بر مبنای توانایی: فشار ${capability.pushingStrength.toInt()}٪، کشش ${capability.pullingStrength.toInt()}٪، پا ${capability.legStrength.toInt()}٪. $skillSummary" +
                "زمان هر جلسه: $durationMin دقیقه."

        val program = ProgramEntity(
            id = programId,
            profileId = profile.id,
            title = programTitle,
            description = programDesc,
            level = level,
            daysPerWeek = days,
            status = "ACTIVE",
            currentVersionNumber = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val version = ProgramVersionEntity(
            id = UUID.randomUUID().toString(),
            programId = programId,
            versionNumber = 1,
            reason = "ایجاد ساختار تطبیقی هوشمند بر مبنای ظرفیت واقعی، اهداف مهارتی و پایش ریکاوری",
            changesSummary = "توزیع $days روزه با تفکیک $splitType، پایش سلامت مفاصل و توازن عضلانی",
            programStructureJson = "{\"split\": \"$splitType\", \"days\": $days, \"duration\": $durationMin, \"primaryGoal\": \"$primaryGoal\"}",
            previousVersionNumber = null
        )

        return GeneratedProgramPackage(
            program = program,
            initialVersion = version,
            sessions = sessions,
            workoutExercises = workoutExercises,
            decisions = decisions
        )
    }

    private fun calculateScheduledDates(startDate: Date, daysCount: Int, splitType: String): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = startDate

        val dayOffsets = when (daysCount) {
            2 -> listOf(0, 3) // e.g., Mon, Thu (72h recovery between full body)
            3 -> listOf(0, 2, 4) // e.g., Mon, Wed, Fri (48h recovery)
            4 -> listOf(0, 1, 3, 4) // Upper/Lower with midweek rest
            5 -> listOf(0, 1, 2, 4, 5) // 3 on, 1 off, 2 on, 1 off
            6 -> listOf(0, 1, 2, 3, 4, 5) // PPL 2x
            else -> (0 until daysCount).map { it * 2 }
        }

        return dayOffsets.take(daysCount).map { offset ->
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, offset)
            sdf.format(c.time)
        }
    }
}
