package com.example.domain.engine

import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.SkillProgressEntity
import com.example.data.local.entity.UserCapabilityEntity

enum class SupportedSkill(
    val id: String,
    val titleFa: String,
    val titleEn: String,
    val defaultLeadExerciseId: String
) {
    FRONT_LEVER("skill_front_lever", "فرانت لور (Front Lever)", "Front Lever", "front_lever_tuck"),
    PLANCHE("skill_planche", "پلانچ (Planche)", "Planche", "pushup_diamond"),
    MUSCLE_UP("skill_muscleup", "ماسل‌آپ (Muscle-Up)", "Muscle-Up", "pullup_std"),
    HUMAN_FLAG("skill_human_flag", "پرچم انسانی (Human Flag)", "Human Flag", "inverted_row"),
    BACK_LEVER("skill_back_lever", "بک لور (Back Lever)", "Back Lever", "dead_hang"),
    HANDSTAND("skill_handstand", "هنداستند (Handstand)", "Handstand", "handstand"),
    LSIT("skill_lsit", "ال‌سیت (L-Sit)", "L-Sit", "lsit"),
    VSIT("skill_vsit", "وی‌سیت (V-Sit)", "V-Sit", "lsit");

    companion object {
        fun fromIdOrName(query: String): SupportedSkill? {
            val q = query.trim().uppercase()
            val normalized = query.trim().replace("‌", " ").replace("-", " ")
            return entries.firstOrNull {
                it.id.equals(query, ignoreCase = true) ||
                        it.name == q ||
                        q.contains(it.titleEn.uppercase()) ||
                        query.contains(it.titleFa) ||
                        normalized.contains(it.titleFa.replace("‌", " ")) ||
                        when (it) {
                            FRONT_LEVER -> normalized.contains("فرانت لور") || q.contains("FRONT LEVER") || q.contains("FRONT_LEVER")
                            PLANCHE -> normalized.contains("پلانچ") || q.contains("PLANCHE")
                            MUSCLE_UP -> normalized.contains("ماسل آپ") || normalized.contains("ماسل اپ") || q.contains("MUSCLE UP") || q.contains("MUSCLEUP")
                            HUMAN_FLAG -> normalized.contains("پرچم انسانی") || normalized.contains("پرچم") || q.contains("HUMAN FLAG") || q.contains("HUMAN_FLAG")
                            BACK_LEVER -> normalized.contains("بک لور") || q.contains("BACK LEVER") || q.contains("BACK_LEVER")
                            HANDSTAND -> normalized.contains("هنداستند") || normalized.contains("بالانس") || q.contains("HANDSTAND")
                            LSIT -> normalized.contains("ال سیت") || q.contains("LSIT") || q.contains("L SIT") || q.contains("L_SIT")
                            VSIT -> normalized.contains("وی سیت") || q.contains("VSIT") || q.contains("V SIT") || q.contains("V_SIT")
                        }
            }
        }
    }
}

data class SkillReadinessEvaluation(
    val skill: SupportedSkill,
    val meetsPrerequisites: Boolean,
    val stageNameFa: String = "",
    val stageLevel: Int = 0,
    val chosenExerciseId: String,
    val rationaleFa: String,
    val missingPrerequisites: List<String>,
    val targetHoldSec: Int = 0,
    val targetReps: Int = 0,
    val targetRpe: Int = 7
)

object SkillProgressionRegistry {

    fun evaluateAllSkills(
        skills: List<SupportedSkill>,
        capability: UserCapabilityEntity,
        assessment: AssessmentEntity?,
        availableEquipment: List<String>,
        allExercises: List<ExerciseEntity>,
        skillProgress: List<SkillProgressEntity> = emptyList()
    ): List<SkillReadinessEvaluation> {
        return skills.distinct().map { skill ->
            evaluateSkill(skill, capability, assessment, availableEquipment, allExercises, skillProgress)
        }
    }

    fun evaluateSkill(
        skill: SupportedSkill,
        capability: UserCapabilityEntity,
        assessment: AssessmentEntity?,
        availableEquipment: List<String>,
        allExercises: List<ExerciseEntity>,
        skillProgress: List<SkillProgressEntity> = emptyList()
    ): SkillReadinessEvaluation {
        val hasPullupBar = availableEquipment.contains("pullup_bar")
        val hasParallettes = availableEquipment.contains("parallettes") || availableEquipment.contains("dip_station")

        val pullupsMax = assessment?.pullupsMax ?: ((capability.pullingStrength / 100f) * 20f).toInt()
        val pushupsMax = assessment?.pushupsMax ?: ((capability.pushingStrength / 100f) * 35f).toInt()
        val plankSec = assessment?.plankSec ?: ((capability.coreStrength / 100f) * 90f).toInt()
        val deadHangSec = assessment?.deadHangSec ?: ((capability.gripCapacity / 100f) * 60f).toInt()

        val exerciseMap = allExercises.associateBy { it.id }

        return when (skill) {
            SupportedSkill.FRONT_LEVER -> {
                val missing = mutableListOf<String>()
                if (!hasPullupBar) missing.add("میله بارفیکس جهت اجرای مهارت ضروری است")
                if (pullupsMax < 8) missing.add("حداقل ۸ بارفیکس استاندارد کامل (ثبت فعلی: $pullupsMax)")
                if (deadHangSec < 30) missing.add("حداقل ۳۰ ثانیه آویزان ماندن اکتیو (ثبت فعلی: $deadHangSec ثانیه)")
                if (plankSec < 30) missing.add("حداقل ۳۰ ثانیه هالو بادی یا پلانک")

                val isReady = missing.isEmpty()
                val (stageLevel, stageName, chosenId, hold, reps) = when {
                    !hasPullupBar -> {
                        Tuple5(0, "پایه‌سازی کشش افقی بدون میله", "inverted_row", 0, 8)
                    }
                    isReady && capability.pullingStrength >= 75f -> {
                        Tuple5(2, "تاک فرانت لور پیشرفته", "front_lever_tuck", 12, 0)
                    }
                    isReady -> {
                        Tuple5(1, "تاک فرانت لور ایزومتریک", "front_lever_tuck", 10, 0)
                    }
                    pullupsMax >= 5 -> {
                        Tuple5(0, "تقویت بارفیکس عمودی و اسکاپولار", "pullup_std", 0, 6)
                    }
                    else -> {
                        Tuple5(0, "تقویت کشش افقی و ثبات تیغه‌های کتف", "inverted_row", 0, 8)
                    }
                }

                SkillReadinessEvaluation(
                    skill = skill,
                    meetsPrerequisites = isReady,
                    stageNameFa = stageName,
                    stageLevel = stageLevel,
                    chosenExerciseId = chosenId,
                    rationaleFa = if (isReady) {
                        "شما تمام پیش‌نیازهای قدرتی و تاندونی ورود به فرانت لور را دارا هستید. تمرین با $stageName آغاز می‌شود."
                    } else {
                        "به دلیل عدم تکمیل پیش‌نیازها (${missing.joinToString("، ")}), ابتدا $stageName با حرکت ${exerciseMap[chosenId]?.persianName ?: chosenId} تجویز شد."
                    },
                    missingPrerequisites = missing,
                    targetHoldSec = hold,
                    targetReps = reps,
                    targetRpe = if (isReady) 8 else 7
                )
            }

            SupportedSkill.PLANCHE -> {
                val missing = mutableListOf<String>()
                if (pushupsMax < 20) missing.add("حداقل ۲۰ شنای استاندارد بدون افت فرم (ثبت فعلی: $pushupsMax)")
                if (capability.pushingStrength < 60f) missing.add("قدرت فشاری کافی جهت مهار اهرم بدن (حداقل ۶۰٪)")
                if (capability.wristTolerance < 40f) missing.add("آمادگی و انعطاف بافت تاندونی مچ دست")
                if (plankSec < 40) missing.add("حداقل ۴۰ ثانیه پلانک استاندارد")

                val isReady = missing.isEmpty()
                val (stageLevel, stageName, chosenId, hold, reps) = when {
                    capability.wristTolerance < 40f -> {
                        Tuple5(0, "آماده‌سازی بدون درد مچ و تقویت کم‌فشار", "pushup_knee", 0, 10)
                    }
                    isReady && hasParallettes -> {
                        Tuple5(1, "دیپ پارالل و پروتراکشن شانه روی پارالت", "dips_parallel", 0, 8)
                    }
                    isReady -> {
                        Tuple5(1, "شنای الماسی و تمرکز بر پلانچ لین", "pushup_diamond", 0, 8)
                    }
                    capability.pushingStrength >= 50f -> {
                        Tuple5(0, "تقویت سه‌سر بازو و قدام دلتوئید", "pushup_diamond", 0, 8)
                    }
                    capability.pushingStrength >= 30f -> {
                        Tuple5(0, "تثبیت پایه شنای کلاسیک", "pushup_std", 0, 10)
                    }
                    else -> {
                        Tuple5(0, "پایه‌سازی مقدماتی عضلات فشاری", "pushup_knee", 0, 12)
                    }
                }

                SkillReadinessEvaluation(
                    skill = skill,
                    meetsPrerequisites = isReady,
                    stageNameFa = stageName,
                    stageLevel = stageLevel,
                    chosenExerciseId = chosenId,
                    rationaleFa = if (isReady) {
                        "آمادگی عضلانی و تاندونی مناسب برای بارگذاری اهرمی پلانچ؛ تمرکز بر زاویه قدامی شانه و زاویه مچ با $stageName."
                    } else {
                        "پلانچ فشار فوق‌العاده‌ای به تاندون دوسربازو و مفصل مچ وارد می‌کند. تا تثبیت کامل پیش‌نیازها (${missing.joinToString("، ")}), حرکت ${exerciseMap[chosenId]?.persianName ?: chosenId} تجویز شد."
                    },
                    missingPrerequisites = missing,
                    targetHoldSec = hold,
                    targetReps = reps,
                    targetRpe = if (isReady) 8 else 7
                )
            }

            SupportedSkill.MUSCLE_UP -> {
                val missing = mutableListOf<String>()
                if (!hasPullupBar) missing.add("میله بارفیکس جهت ترنزیشن و صعود الزامی است")
                if (pullupsMax < 10) missing.add("حداقل ۱۰ بارفیکس کامل و بدون ضربه (ثبت فعلی: $pullupsMax)")
                if (capability.pushingStrength < 55f) missing.add("حداقل ۸ دیپ پارالل با دامنه کامل")
                if (capability.shoulderTolerance < 50f) missing.add("تحمل کشش و چرخش داخلی شانه در نقطه ترنزیشن")

                val isReady = missing.isEmpty()
                val (stageLevel, stageName, chosenId, hold, reps) = when {
                    !hasPullupBar -> {
                        Tuple5(0, "تقویت عضلات پشت بدون میله", "inverted_row", 0, 10)
                    }
                    isReady -> {
                        Tuple5(1, "بارفیکس انفجاری تا خط سینه (Chest-to-Bar)", "pullup_std", 0, 5)
                    }
                    pullupsMax >= 6 -> {
                        Tuple5(0, "افزایش حجم بارفیکس و قدرت کشش عمودی", "pullup_std", 0, 7)
                    }
                    else -> {
                        Tuple5(0, "تقویت بارفیکس مچ برعکس (چین‌آپ)", "pullup_chinup", 0, 6)
                    }
                }

                SkillReadinessEvaluation(
                    skill = skill,
                    meetsPrerequisites = isReady,
                    stageNameFa = stageName,
                    stageLevel = stageLevel,
                    chosenExerciseId = chosenId,
                    rationaleFa = if (isReady) {
                        "شما قدرت کافی بارفیکس و دیپ را دارید؛ تمرین روی بارفیکس انفجاری تا خط سینه جهت ترنزیشن ماسل‌آپ تنظیم شد."
                    } else {
                        "برای انتقال ایمن از فاز کشش به دیپ روی میله، نیاز به حجم بارفیکس بالاتر دارید (${missing.joinToString("، ")}). حرکات سازگارکننده فعال شدند."
                    },
                    missingPrerequisites = missing,
                    targetHoldSec = hold,
                    targetReps = reps,
                    targetRpe = if (isReady) 8 else 7
                )
            }

            SupportedSkill.HUMAN_FLAG -> {
                val missing = mutableListOf<String>()
                if (!hasPullupBar && !availableEquipment.contains("rings")) missing.add("میله عمودی، ستون یا بارفیکس مستحکم")
                if (pullupsMax < 8) missing.add("حداقل ۸ بارفیکس استاندارد (ثبت فعلی: $pullupsMax)")
                if (plankSec < 45) missing.add("حداقل ۴۵ ثانیه پایداری پلانک برای عضلات مورب شکم")
                if (capability.shoulderTolerance < 45f) missing.add("ثبات و تحمل کمربند شانه‌ای دست تکیه‌گاه")

                val isReady = missing.isEmpty()
                val (stageLevel, stageName, chosenId, hold, reps) = when {
                    isReady -> {
                        Tuple5(1, "کشش افقی نامتقارن و تقویت زنجیره جانبی", "inverted_row", 0, 8)
                    }
                    plankSec < 40 -> {
                        Tuple5(0, "تقویت ثبات ضد چرخش و پلانک خط مستقیم", "plank_std", 35, 0)
                    }
                    pullupsMax >= 6 -> {
                        Tuple5(0, "تقویت پشتی بزرگ و قدرت کششی", "pullup_std", 0, 7)
                    }
                    else -> {
                        Tuple5(0, "کشش افقی ردیفی و اسکاپولار", "inverted_row", 0, 8)
                    }
                }

                SkillReadinessEvaluation(
                    skill = skill,
                    meetsPrerequisites = isReady,
                    stageNameFa = stageName,
                    stageLevel = stageLevel,
                    chosenExerciseId = chosenId,
                    rationaleFa = if (isReady) {
                        "پیش‌نیازهای اولیه قدرت جانبی و کششی فراهم است؛ تمرینات تقویت زنجیره جانبی و کشش نامتقارن فعال شد."
                    } else {
                        "پرچم انسانی نیازمند قدرت فوق‌العاده عضلات مورب شکم و کتف است (${missing.joinToString("، ")}). تمرکز روی پیش‌نیازهای پایه قرار گرفت."
                    },
                    missingPrerequisites = missing,
                    targetHoldSec = hold,
                    targetReps = reps,
                    targetRpe = 7
                )
            }

            SupportedSkill.BACK_LEVER -> {
                val missing = mutableListOf<String>()
                if (!hasPullupBar) missing.add("میله بارفیکس یا حلقه برای معلق ماندن")
                if (pullupsMax < 8) missing.add("حداقل ۸ بارفیکس تمیز (ثبت فعلی: $pullupsMax)")
                if (deadHangSec < 30) missing.add("حداقل ۳۰ ثانیه آویزان ماندن اکتیو")
                if (capability.shoulderTolerance < 50f) missing.add("انعطاف و تاب‌آوری شانه در اکستنشن کامل (حداقل ۵۰٪)")

                val isReady = missing.isEmpty()
                val (stageLevel, stageName, chosenId, hold, reps) = when {
                    !hasPullupBar -> {
                        Tuple5(0, "تقویت عضلات پشت با اینورتد رو", "inverted_row", 0, 8)
                    }
                    isReady -> {
                        Tuple5(1, "آویزان معکوس و آماده‌سازی ژرمن هنگ", "dead_hang", 30, 0)
                    }
                    capability.shoulderTolerance < 45f -> {
                        Tuple5(0, "آویزان اکتیو جهت تطبیق کپسول شانه و موبیلیتی", "dead_hang", 25, 0)
                    }
                    else -> {
                        Tuple5(0, "افزایش حجم بارفیکس و قدرت اکستنشن", "pullup_std", 0, 7)
                    }
                }

                SkillReadinessEvaluation(
                    skill = skill,
                    meetsPrerequisites = isReady,
                    stageNameFa = stageName,
                    stageLevel = stageLevel,
                    chosenExerciseId = chosenId,
                    rationaleFa = if (isReady) {
                        "شانه در وضعیت امن قرار دارد؛ آماده‌سازی تاندون دیستال جلو بازو و وضعیت آویزان ژرمن هنگ با $stageName."
                    } else {
                        "بک لور فشار کششی بسیار بالایی به کپسول قدامی شانه وارد می‌کند (${missing.joinToString("، ")}). نیاز به ارتقای بارفیکس و تحرک شانه."
                    },
                    missingPrerequisites = missing,
                    targetHoldSec = hold,
                    targetReps = reps,
                    targetRpe = 7
                )
            }

            SupportedSkill.HANDSTAND -> {
                val missing = mutableListOf<String>()
                if (plankSec < 40) missing.add("حداقل ۴۰ ثانیه پلانک جهت ثبات خط بدن (ثبت فعلی: $plankSec ثانیه)")
                if (pushupsMax < 10) missing.add("حداقل ۱۰ شنای استاندارد (ثبت فعلی: $pushupsMax)")
                if (capability.wristTolerance < 40f) missing.add("تحمل و ثبات بدون درد مچ دست در اکستنشن ۹۰ درجه")
                if (capability.shoulderTolerance < 45f) missing.add("تحرک و بالا کشیدن کامل کتف (اسکاپولار الویشن)")

                val isReady = missing.isEmpty()
                val (stageLevel, stageName, chosenId, hold, reps) = when {
                    capability.wristTolerance < 40f -> {
                        Tuple5(0, "پلانک روی ساعد برای خط مستقیم بدون بار روی مچ", "plank_std", 35, 0)
                    }
                    isReady -> {
                        Tuple5(1, "هنداستند رو به دیوار و تمرین خط مستقیم بدن", "handstand", 25, 0)
                    }
                    pushupsMax >= 10 -> {
                        Tuple5(0, "شنای الماسی جهت تقویت سه‌سر بازو و خط شانه", "pushup_diamond", 0, 8)
                    }
                    else -> {
                        Tuple5(0, "پلانک استاندارد و ثبات میان‌تنه", "plank_std", 30, 0)
                    }
                }

                SkillReadinessEvaluation(
                    skill = skill,
                    meetsPrerequisites = isReady,
                    stageNameFa = stageName,
                    stageLevel = stageLevel,
                    chosenExerciseId = chosenId,
                    rationaleFa = if (isReady) {
                        "آمادگی عصبی و کمربند شانه‌ای جهت تمرین تعادل ایستادن روی دست‌ها با $stageName."
                    } else {
                        "ثبات مچ دست و عضلات دندانه‌ای قدامی برای نگه‌داشتن وزن بدن باید تقویت گردد (${missing.joinToString("، ")})."
                    },
                    missingPrerequisites = missing,
                    targetHoldSec = hold,
                    targetReps = reps,
                    targetRpe = 7
                )
            }

            SupportedSkill.LSIT -> {
                val missing = mutableListOf<String>()
                if (plankSec < 30) missing.add("حداقل ۳۰ ثانیه پلانک یا هالو بادی (ثبت فعلی: $plankSec ثانیه)")
                if (capability.pushingStrength < 35f) missing.add("قدرت ساپورت هلد روی دست‌ها (دپریشن کتف)")
                if (capability.coreStrength < 35f) missing.add("تراکم عضلات راست شکمی")

                val isReady = missing.isEmpty()
                val (stageLevel, stageName, chosenId, hold, reps) = when {
                    isReady -> {
                        Tuple5(1, "ال‌سیت تاک با مکث ایزومتریک", "lsit", 12, 0)
                    }
                    plankSec >= 20 -> {
                        Tuple5(0, "هالو بادی هلد و فشرده‌سازی کمر به زمین", "hollow_body", 22, 0)
                    }
                    else -> {
                        Tuple5(0, "پلانک استاندارد و تقویت عضلات عمقی شکم", "plank_std", 25, 0)
                    }
                }

                SkillReadinessEvaluation(
                    skill = skill,
                    meetsPrerequisites = isReady,
                    stageNameFa = stageName,
                    stageLevel = stageLevel,
                    chosenExerciseId = chosenId,
                    rationaleFa = if (isReady) {
                        "پایداری میان‌تنه و انعطاف زنجیره خلفی مناسب است؛ تمرین تاک ال‌سیت با مکث کنترل‌شده."
                    } else {
                        "فشرده‌سازی عضلات شکم و پائین‌کشیدن کتف با تمرینات هالو بادی و پلانک تقویت می‌شود (${missing.joinToString("، ")})."
                    },
                    missingPrerequisites = missing,
                    targetHoldSec = hold,
                    targetReps = reps,
                    targetRpe = 7
                )
            }

            SupportedSkill.VSIT -> {
                val missing = mutableListOf<String>()
                if (plankSec < 45) missing.add("حداقل ۴۵ ثانیه پلانک یا هالو بادی")
                if (capability.coreStrength < 60f) missing.add("تسلط کامل بر ال‌سیت حداقل ۱۵ ثانیه")
                if (capability.pushingStrength < 45f) missing.add("قدرت ساپورت هلد قوی برای بالا بردن پاها")

                val isReady = missing.isEmpty()
                val (stageLevel, stageName, chosenId, hold, reps) = when {
                    isReady -> {
                        Tuple5(1, "ال‌سیت مرتفع به سمت زاویه ۴۵ درجه (وی‌سیت)", "lsit", 15, 0)
                    }
                    hasPullupBar && capability.coreStrength >= 45f -> {
                        Tuple5(0, "بالا آوردن پا در حالت آویزان (Hanging Leg Raise)", "hanging_leg_raise", 0, 8)
                    }
                    capability.coreStrength >= 40f -> {
                        Tuple5(0, "تثبیت ال‌سیت پایه و افزایش زمان مکث", "lsit", 10, 0)
                    }
                    else -> {
                        Tuple5(0, "هالو بادی هلد جهت تقویت فشرده‌سازی شکمی", "hollow_body", 25, 0)
                    }
                }

                SkillReadinessEvaluation(
                    skill = skill,
                    meetsPrerequisites = isReady,
                    stageNameFa = stageName,
                    stageLevel = stageLevel,
                    chosenExerciseId = chosenId,
                    rationaleFa = if (isReady) {
                        "قدرت تراکم شکم و ثبات شانه بالا است؛ تلاش برای افزایش زاویه پاها فراتر از افقی (وی‌سیت)."
                    } else {
                        "وی‌سیت به قدرت شدید عضلات ایلیوپسواس و راست رانی نیاز دارد (${missing.joinToString("، ")}). تمرین پایه در دستور کار قرار گرفت."
                    },
                    missingPrerequisites = missing,
                    targetHoldSec = hold,
                    targetReps = reps,
                    targetRpe = 7
                )
            }
        }
    }

    private data class Tuple5<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )
}
