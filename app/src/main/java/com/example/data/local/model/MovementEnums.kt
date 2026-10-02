package com.example.data.local.model

enum class MovementFamily(val titleFa: String, val titleEn: String) {
    PUSH("فشاری", "Push"),
    PULL("کششی", "Pull"),
    LEGS("پاها", "Legs"),
    CORE("هسته بدن و میان‌تنه", "Core"),
    SKILLS("مهارت‌های تخصصی", "Skills"),
    MOBILITY("تحرک‌پذیری و آماده‌سازی", "Mobility")
}

enum class MovementSubPattern(val titleFa: String, val family: MovementFamily) {
    // PUSH
    HORIZONTAL_PUSH("فشاری افقی (شنا)", MovementFamily.PUSH),
    VERTICAL_PUSH("فشاری عمودی (پایک / هنداستند)", MovementFamily.PUSH),
    DIP_PATTERN("الگوی پارالل / دیپ", MovementFamily.PUSH),
    PSEUDO_PLANCHE("فشار مایکل پلانش", MovementFamily.PUSH),
    HANDSTAND_PUSH("پرس عمودی تعادلی", MovementFamily.PUSH),
    PLANCHE_PRESSING("پرس ایزومتریک قدامی پلانش", MovementFamily.PUSH),

    // PULL
    VERTICAL_PULL("کششی عمودی (بارفیکس)", MovementFamily.PULL),
    HORIZONTAL_PULL("کششی افقی (استرالیایی / رو)", MovementFamily.PULL),
    HIGH_PULL("کششی انفجاری بالا", MovementFamily.PULL),
    MUSCLE_UP_TRANSITION("انتقال و ترنزیشن ماسل‌آپ", MovementFamily.PULL),
    FRONT_LEVER_PULL("کشش اهرمی فرانت لور", MovementFamily.PULL),
    BACK_LEVER_PULL("اکستنشن شانه‌ای بک لور", MovementFamily.PULL),

    // LEGS
    SQUAT("اسکات دوپا", MovementFamily.LEGS),
    SPLIT_SQUAT("اسکات تک‌پا / اسپلیت", MovementFamily.LEGS),
    LUNGE("لانج و گام‌برداری", MovementFamily.LEGS),
    HINGE("لولای ران و زنجیره پشتی", MovementFamily.LEGS),
    POSTERIOR_CHAIN("زنجیره خلفی و همسترینگ", MovementFamily.LEGS),
    CALF("ساق پا", MovementFamily.LEGS),
    UNILATERAL_LEG("تمرین تک‌پای تعادلی (پیستول)", MovementFamily.LEGS),

    // CORE
    ANTI_EXTENSION("ضد اکستنشن (پلانک / رول‌اوت)", MovementFamily.CORE),
    ANTI_ROTATION("ضد چرخش (پالوف / ساید)", MovementFamily.CORE),
    ANTI_LATERAL_FLEXION("ضد خمش جانبی (ساید پلانک)", MovementFamily.CORE),
    COMPRESSION("تراکم و فشرده‌سازی شکم (ال‌سیت)", MovementFamily.CORE),
    FLEXION("خمش قدامی کنترل‌شده", MovementFamily.CORE),
    HOLLOW("وضعیت گود توخالی (Hollow Body)", MovementFamily.CORE),
    ARCH("وضعیت کمان معکوس (Arch Body)", MovementFamily.CORE),
    HANGING_CORE("میان‌تنه آویزان از میله", MovementFamily.CORE),

    // SKILLS
    HANDSTAND("تعادل روی دست (هنداستند)", MovementFamily.SKILLS),
    LSIT("ال‌سیت (L-Sit)", MovementFamily.SKILLS),
    VSIT("وی‌سیت (V-Sit)", MovementFamily.SKILLS),
    PLANCHE("پلانش (Planche)", MovementFamily.SKILLS),
    FRONT_LEVER("فرانت لور (Front Lever)", MovementFamily.SKILLS),
    BACK_LEVER("بک لور (Back Lever)", MovementFamily.SKILLS),
    MUSCLE_UP("ماسل‌آپ (Muscle-Up)", MovementFamily.SKILLS),
    HUMAN_FLAG("پرچم انسانی (Human Flag)", MovementFamily.SKILLS),

    // MOBILITY
    WRIST_PREPARATION("آماده‌سازی و تحرک مچ دست", MovementFamily.MOBILITY),
    SHOULDER_MOBILITY("تحرک و چرخش مفصل شانه", MovementFamily.MOBILITY),
    SCAPULAR_CONTROL("کنترل و ثبات اسکاپولا (کتف)", MovementFamily.MOBILITY),
    THORACIC_MOBILITY("تحرک ستون فقرات سینه‌ای", MovementFamily.MOBILITY),
    HIP_MOBILITY("تحرک و انعطاف لگن", MovementFamily.MOBILITY),
    ANKLE_MOBILITY("تحرک مچ پا", MovementFamily.MOBILITY)
}

enum class ExerciseType {
    STRENGTH,
    SKILL,
    HYPERTROPHY,
    PREPARATION,
    ISOMETRIC,
    DYNAMIC
}

enum class SkillOrStrength {
    SKILL,
    STRENGTH,
    HYBRID
}

enum class RelationshipType(val titleFa: String) {
    REGRESSION_OF("رگرسیون و حالت ساده‌ترِ"),
    PROGRESSION_OF("پروگرسیون و ارتقای پیشرفته‌ترِ"),
    ALTERNATIVE_TO("جایگزین معادل بیومکانیکی برای"),
    SIMILAR_TO("مشابه از نظر درگیری عضلانی با"),
    PREPARES_FOR("پیش‌نیاز آمادگی بافتی برای"),
    PREREQUISITE_FOR("پیش‌نیاز قدرتی/مهارتی برای"),
    COMPLEMENTS("مکمل و تعادل‌بخش به"),
    OVERLAPS_WITH("هم‌پوشانی و تداخل بار تمرینی با"),
    REPLACES("جایگزین مستقیم در صورت نبود امکانات"),
    REQUIRES_MORE_THAN("نیاز به قدرت بیشتر از"),
    REQUIRES_LESS_THAN("نیاز به قدرت کمتر از")
}

enum class FailureClassification(val titleFa: String, val isSevere: Boolean) {
    RECOVERY_LIMITED("محدودیت ناشی از ریکاوری و خواب ناکافی", false),
    TECHNIQUE_LIMITED("محدودیت ناشی از افت تکنیک و کنترل مفصلی", false),
    EQUIPMENT_LIMITED("محدودیت یا تغییر در شرایط ابزار و تجهیزات", false),
    TIME_LIMITED("محدودیت زمانی یا تعجیل در استراحت بین ست‌ها", false),
    PROGRAM_LOAD_LIMITED("بار تجمعی بیش از حد برنامه تمرینی", true),
    PAIN_LIMITED("توقف به علت علائم درد یا استرس غیرطبیعی بافت", true),
    NORMAL_VARIATION("نوسان طبیعی عملکرد روزانه", false),
    INSUFFICIENT_DATA("داده ناکافی برای طبقه‌بندی دقیق", false)
}

enum class GoalPriority {
    PRIMARY,
    SECONDARY,
    MAINTENANCE
}

enum class ProgramPhaseType(val titleFa: String, val durationWeeks: Int) {
    FOUNDATION("آمادگی ساختاری، مفصلی و تاندونی (Foundation)", 6),
    STRENGTH("توسعه قدرت پایه و حجم هایپرتروفی (Strength)", 8),
    SKILL_DEVELOPMENT("توسعه و انتقال مهارت‌های پیشرفته (Skill Dev)", 6),
    SKILL_INTENSIFICATION("تثبیت و اوج‌گیری مهارت‌ها (Skill Peak)", 4),
    CAPACITY("توسعه ظرفیت کار و تاب‌آوری (Work Capacity)", 4),
    CONSOLIDATION("تثبیت دستاوردها و دی‌لود محافظه‌کارانه (Consolidation)", 2),
    RECOVERY_TRANSITION("ریکاوری فعال و انتقال دوره‌ای (Transition)", 2)
}

enum class EvidenceLevel(val titleFa: String) {
    ESTABLISHED_EVIDENCE("شواهد تثبیت‌شده علمی (متاآنالیز و کارآزمایی بالینی)"),
    EMERGING_EVIDENCE("شواهد نوین پژوهشی (مطالعات تجربی معتبر)"),
    COACHING_CONVENTION("اصول بیومکانیک و سنت‌های تمرینی معتبر کالیستنیکس"),
    APP_HEURISTIC("قاعده محاسباتی الگوریتم کالیستن")
}
