package com.example.domain.engine

import com.example.data.local.entity.SetLogEntity

data class PlateauAnalysisResult(
    val hasEnoughData: Boolean,
    val isPlateauDetected: Boolean,
    val message: String,
    val suggestedAction: String? = null,
    val alternativeExerciseId: String? = null,
    val recommendedReason: String? = null
)

object PlateauEngine {

    const val INSUFFICIENT_DATA_MESSAGE = "برای تشخیص پلاتو هنوز داده کافی ثبت نشده است."

    /**
     * Requires at least 3 distinct recorded sessions of the exercise to diagnose plateau.
     * Evaluates whether maximum reps/volume have stalled or declined over >= 3 sessions.
     */
    fun analyzePlateau(
        exerciseId: String,
        sessionsLogs: Map<String, List<SetLogEntity>>, // Map of sessionId/date to set logs
        sleepQualityAvg: Float = 3.5f,
        recentPainLogged: Boolean = false
    ): PlateauAnalysisResult {
        if (sessionsLogs.size < 3) {
            return PlateauAnalysisResult(
                hasEnoughData = false,
                isPlateauDetected = false,
                message = INSUFFICIENT_DATA_MESSAGE
            )
        }

        // Extract max reps and total volume per session sorted by timestamp
        val sessionSummaries = sessionsLogs.values.map { logs ->
            val maxReps = logs.maxOfOrNull { it.reps } ?: 0
            val maxHold = logs.maxOfOrNull { it.holdSeconds } ?: 0
            val completedSets = logs.count { it.status == "COMPLETED" }
            val avgRpe = if (logs.isNotEmpty()) logs.map { it.rpe }.average() else 7.0
            Triple(maxReps, maxHold, avgRpe)
        }

        val lastThree = sessionSummaries.takeLast(3)
        val repValues = lastThree.map { it.first }
        val isStagnant = repValues.distinct().size == 1 || (repValues[2] <= repValues[0] && repValues[1] <= repValues[0])
        val isHighStruggle = lastThree.all { it.third >= 8.5 }

        if (isStagnant && isHighStruggle) {
            val reason = if (recentPainLogged) {
                "توقف پیشرفت ناشی از علائم درد یا بار نامناسب مفاصل است. تنظیم فشار و تغییر زاویه یا جایگزینی حرکت توصیه می‌شود."
            } else if (sleepQualityAvg < 3.0f) {
                "عامل احتمالی استپ عضلانی: افت ریکاوری و خواب ناکافی در روزهای گذشته. کاهش حجم موقت (دی‌لود) پیشنهاد می‌گردد."
            } else {
                "رکورد تکرارها در ۳ جلسه متوالی ثابت مانده و RPE بیش از ۸.۵ است. نیاز به ایجاد تنوع بیومکانیکی یا متد استراحت متغیر."
            }

            val action = "پیشنهاد ایجاد نسخه تطبیقی جدید در برنامه تمرینی: تغییر متغیر حرکتی یا اعمال دی‌لود یک‌هفته‌ای."

            return PlateauAnalysisResult(
                hasEnoughData = true,
                isPlateauDetected = true,
                message = "پلاتو (استپ تمرینی) در این حرکت شناسایی شد.",
                suggestedAction = action,
                recommendedReason = reason
            )
        }

        return PlateauAnalysisResult(
            hasEnoughData = true,
            isPlateauDetected = false,
            message = "روند سازگاری و اضافه بار حرکتی در محدوده طبیعی است؛ نیازی به تغییر ساختار برنامه نیست."
        )
    }
}
