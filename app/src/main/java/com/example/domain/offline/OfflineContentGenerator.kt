package com.example.domain.offline

import com.example.data.local.SeedData
import com.example.domain.engine.SupportedSkill
import com.example.domain.offline.model.OfflinePackageFileEntry
import com.example.domain.offline.model.OfflinePackageManifest
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Builds the actual offline package data streams, computes exact byte sizes,
 * and generates cryptographic SHA-256 hashes for manifest verification.
 * NO HARDCODED OR FAKE SIZES ARE USED.
 */
object OfflineContentGenerator {

    const val CURRENT_PACKAGE_VERSION = "kalisten-offline-v1.0"
    const val CURRENT_SCHEMA_VERSION = "2.0"

    data class GeneratedOfflinePayload(
        val fileName: String,
        val category: String,
        val contentBytes: ByteArray,
        val description: String
    ) {
        val sizeBytes: Long get() = contentBytes.size.toLong()
        val sha256: String get() = computeSha256(contentBytes)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GeneratedOfflinePayload) return false
            return fileName == other.fileName && contentBytes.contentEquals(other.contentBytes)
        }

        override fun hashCode(): Int {
            return fileName.hashCode() * 31 + contentBytes.contentHashCode()
        }
    }

    /**
     * Generates all required payload items for complete offline autonomy.
     */
    fun generateAllPayloads(): List<GeneratedOfflinePayload> {
        val payloads = mutableListOf<GeneratedOfflinePayload>()

        // 1. Exercises Database & Detailed Biomechanical Attributes
        val exercisesJson = buildExercisesJson()
        payloads.add(
            GeneratedOfflinePayload(
                fileName = "exercises_database.json",
                category = "EXERCISES",
                contentBytes = exercisesJson.toByteArray(Charsets.UTF_8),
                description = "پایگاه داده کامل حرکات کالیستنیکس با ضرایب بار مفاصل و الگوهای حرکتی"
            )
        )

        // 2. Skill Progression & Milestone Chains
        val skillsJson = buildSkillsProgressionJson()
        payloads.add(
            GeneratedOfflinePayload(
                fileName = "skills_progression.json",
                category = "SKILLS",
                contentBytes = skillsJson.toByteArray(Charsets.UTF_8),
                description = "زنجیره پیش‌نیازها و گام‌های پیشرفت مهارت‌های شاخص (پلنچ، فرانت لور، ماسل‌آپ و...)"
            )
        )

        // 3. Exercise Relationship Graph & Substitutions
        val relationshipsJson = buildExerciseRelationshipsJson()
        payloads.add(
            GeneratedOfflinePayload(
                fileName = "exercise_relationships.json",
                category = "RELATIONSHIPS",
                contentBytes = relationshipsJson.toByteArray(Charsets.UTF_8),
                description = "گراف اتصالات پیشرفت، رگرسیون و جایگزین‌های بیومکانیکی حرکات"
            )
        )

        // 4. Planning & Programming Engine Rules
        val planningRulesJson = buildPlanningRulesJson()
        payloads.add(
            GeneratedOfflinePayload(
                fileName = "planning_rules.json",
                category = "PLANNING",
                contentBytes = planningRulesJson.toByteArray(Charsets.UTF_8),
                description = "قوانین تفکیک تقسیم تمرین، حجم بهینه و ماتریس پوشش عضلات"
            )
        )

        // 5. Scientific Compendium & Recovery Principles
        val scienceJson = buildScienceCompendiumJson()
        payloads.add(
            GeneratedOfflinePayload(
                fileName = "science_compendium.json",
                category = "SCIENCE",
                contentBytes = scienceJson.toByteArray(Charsets.UTF_8),
                description = "راهنماهای علمی بارگذاری تاندون، ریکاوری سیستم عصبی و تغذیه ورزشی"
            )
        )

        // 6. Visual Biomechanical Cues Manifest
        val cuesJson = buildBiomechanicalCuesJson()
        payloads.add(
            GeneratedOfflinePayload(
                fileName = "biomechanical_cues.json",
                category = "ASSETS",
                contentBytes = cuesJson.toByteArray(Charsets.UTF_8),
                description = "مشخصات فنی و هشدارهای مفاصل شانه، مچ و ستون فقرات برای تمرین بدون مربی"
            )
        )

        return payloads
    }

    /**
     * Builds the authoritative offline manifest from dynamically generated payloads.
     */
    fun createAuthoritativeManifest(payloads: List<GeneratedOfflinePayload>): OfflinePackageManifest {
        val totalBytes = payloads.sumOf { it.sizeBytes }
        // The installed footprint includes raw files plus local index overhead
        val installedBytes = (totalBytes * 1.15).toLong()

        val fileEntries = payloads.map { payload ->
            OfflinePackageFileEntry(
                fileName = payload.fileName,
                category = payload.category,
                sizeBytes = payload.sizeBytes,
                sha256Checksum = payload.sha256,
                description = payload.description
            )
        }

        return OfflinePackageManifest(
            packageVersion = CURRENT_PACKAGE_VERSION,
            schemaVersion = CURRENT_SCHEMA_VERSION,
            createdAtTimestamp = System.currentTimeMillis(),
            totalSizeBytes = totalBytes,
            installedSizeBytes = installedBytes,
            fileEntries = fileEntries,
            compatibilityMinAppVersion = "1.0.0"
        )
    }

    private fun computeSha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun buildExercisesJson(): String {
        val array = JSONArray()
        for (ex in SeedData.exercises) {
            val obj = JSONObject()
            obj.put("id", ex.id)
            obj.put("persianName", ex.persianName)
            obj.put("englishName", ex.englishName)
            obj.put("category", ex.category)
            obj.put("movementPattern", ex.movementPattern)
            obj.put("difficulty", ex.difficulty)
            obj.put("difficultyTier", ex.difficultyTier)
            obj.put("primaryMusclesJson", ex.primaryMusclesJson)
            obj.put("secondaryMusclesJson", ex.secondaryMusclesJson)
            obj.put("shoulderLoad", ex.shoulderLoad)
            obj.put("instructionsJson", ex.instructionsJson)
            obj.put("commonMistakesJson", ex.commonMistakesJson)
            obj.put("equipmentRequiredJson", ex.equipmentRequiredJson)
            array.put(obj)
        }
        return JSONObject().put("version", "2.0").put("count", SeedData.exercises.size).put("exercises", array).toString(2)
    }

    private fun buildSkillsProgressionJson(): String {
        val array = JSONArray()
        for (skill in SupportedSkill.entries) {
            val obj = JSONObject()
            obj.put("skillId", skill.id)
            obj.put("titleFa", skill.titleFa)
            obj.put("titleEn", skill.titleEn)
            obj.put("leadExerciseId", skill.defaultLeadExerciseId)
            array.put(obj)
        }
        return JSONObject().put("version", "2.0").put("skills", array).toString(2)
    }

    private fun buildExerciseRelationshipsJson(): String {
        val obj = JSONObject()
        val relationships = JSONArray()
        for (ex in SeedData.exercises) {
            val rObj = JSONObject()
            rObj.put("exerciseId", ex.id)
            rObj.put("progressionExerciseId", ex.progressionExerciseId)
            rObj.put("regressionExerciseId", ex.regressionExerciseId)
            rObj.put("alternativeExerciseIds", ex.alternativeExerciseIdsJson)
            relationships.put(rObj)
        }
        obj.put("version", "2.0")
        obj.put("relationships", relationships)
        return obj.toString(2)
    }

    private fun buildPlanningRulesJson(): String {
        val obj = JSONObject()
        obj.put("volumeRecoverySafetyMargin", 0.70)
        obj.put("maxSessionDurationMinutes", 75)
        obj.put("defaultRestIntervalSeconds", 90)
        obj.put("rpeTargetRangeMin", 6.5)
        obj.put("rpeTargetRangeMax", 9.0)
        obj.put("allowedSplits", JSONArray(listOf("FULL_BODY", "UPPER_LOWER", "PUSH_PULL_LEGS")))
        return obj.toString(2)
    }

    private fun buildScienceCompendiumJson(): String {
        val array = JSONArray()
        for (s in SeedData.scienceSources) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("title", s.title)
            obj.put("topic", s.topic)
            obj.put("authorOrg", s.authorOrg)
            array.put(obj)
        }
        return JSONObject().put("version", "2.0").put("sources", array).toString(2)
    }

    private fun buildBiomechanicalCuesJson(): String {
        val obj = JSONObject()
        obj.put("shoulderStabilityRule", "همیشه قبل از شروع حرکات فشاری، اسکاپولا را در وضعیت دیپرشن و پروتراکشن پایدار کنید.")
        obj.put("wristMobilityRule", "در بارگذاری‌های با زاویه مچ دست بالا (پلانچ، شنا)، قبل از ست موبیلیتی دینامیک مچ انجام دهید.")
        obj.put("hollowBodyStandard", "در تمامی الگوهای فشاری، لگن در چرخش خلفی (PPT) و هسته بدن سفت باشد.")
        return obj.toString(2)
    }
}
