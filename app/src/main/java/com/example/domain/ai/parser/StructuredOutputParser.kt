package com.example.domain.ai.parser

import com.example.domain.ai.model.AIGenericAdvice
import com.example.domain.ai.model.AIPlateauAdvice
import com.example.domain.ai.model.AIStructuredOutput
import com.example.domain.ai.model.AITaskType
import com.example.domain.ai.model.AIWorkoutAdvice
import com.example.domain.ai.model.AIExerciseModification
import com.example.domain.ai.model.AIExerciseSubstitutionAdvice
import org.json.JSONArray
import org.json.JSONObject

sealed class ParseResult {
    data class Success(val output: AIStructuredOutput) : ParseResult()
    data class Error(val message: String, val rawOutput: String, val cause: Throwable? = null) : ParseResult()
}

/**
 * Robust JSON and structured output parser for local on-device SLMs.
 * Resilient against markdown wrappers, leading/trailing conversational text, and whitespace variations.
 */
object StructuredOutputParser {

    /**
     * Extracts and parses model text into the expected typed domain structure.
     */
    fun parse(rawText: String, taskType: AITaskType): ParseResult {
        if (rawText.isBlank()) {
            return ParseResult.Error(
                message = "خروجی مدل خالی است.",
                rawOutput = rawText
            )
        }

        val jsonStr = extractJsonString(rawText)
        if (jsonStr.isNullOrBlank()) {
            return ParseResult.Error(
                message = "هیچ شیء ساختاریافته معتبری در خروجی یافت نشد.",
                rawOutput = rawText
            )
        }

        return try {
            val jsonObject = JSONObject(jsonStr)
            when (taskType) {
                AITaskType.WORKOUT_ADVICE, AITaskType.PROGRESSION_ADVICE, AITaskType.RECOVERY_INSIGHT -> {
                    parseWorkoutAdvice(jsonObject)
                }
                AITaskType.PLATEAU_ANALYSIS -> {
                    parsePlateauAdvice(jsonObject)
                }
                AITaskType.EXERCISE_SUBSTITUTION -> {
                    parseSubstitutionAdvice(jsonObject)
                }
            }
        } catch (e: Throwable) {
            ParseResult.Error(
                message = "خطا در تفسیر داده‌های خروجی: ${e.message}",
                rawOutput = rawText,
                cause = e
            )
        }
    }

    /**
     * Finds and extracts JSON content between markdown backticks or outermost curly brackets.
     */
    fun extractJsonString(rawText: String): String? {
        val trimmed = rawText.trim()

        // Case 1: Markdown code block ```json ... ```
        val codeBlockRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
        val match = codeBlockRegex.find(trimmed)
        if (match != null) {
            val candidate = match.groupValues[1].trim()
            if (candidate.startsWith("{") && candidate.endsWith("}")) {
                return candidate
            }
        }

        // Case 2: Scan for outermost matching curly braces '{' ... '}'
        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1).trim()
        }

        return null
    }

    private fun parseWorkoutAdvice(json: JSONObject): ParseResult {
        val summary = json.optString("summary", "توصیه برنامه تمرینی آفلاین")
        val primaryFocus = json.optString("primaryFocus", "فرم حرکت و پایداری")
        val setsDelta = json.optInt("recommendedSetsDelta", 0)
        val rpeTarget = json.optDouble("recommendedRpeTarget", 7.5)
        val recoveryRecommendation = json.optString("recoveryRecommendation", "استراحت و تغذیه کافی")

        val modifications = mutableListOf<AIExerciseModification>()
        val modsArray = json.optJSONArray("exerciseModifications")
        if (modsArray != null) {
            for (i in 0 until modsArray.length()) {
                val item = modsArray.optJSONObject(i) ?: continue
                modifications.add(
                    AIExerciseModification(
                        exerciseName = item.optString("exerciseName", "حرکت بدون نام"),
                        targetReps = item.optInt("targetReps", 10),
                        targetSets = item.optInt("targetSets", 3),
                        rpeTarget = item.optDouble("rpeTarget", 7.5),
                        modificationType = item.optString("modificationType", "MAINTAIN"),
                        reason = item.optString("reason", "")
                    )
                )
            }
        }

        val safetyWarnings = jsonArrayToStringList(json.optJSONArray("safetyWarnings"))

        return ParseResult.Success(
            AIWorkoutAdvice(
                summary = summary,
                primaryFocus = primaryFocus,
                recommendedSetsDelta = setsDelta,
                recommendedRpeTarget = rpeTarget,
                exerciseModifications = modifications,
                recoveryRecommendation = recoveryRecommendation,
                safetyWarnings = safetyWarnings
            )
        )
    }

    private fun parsePlateauAdvice(json: JSONObject): ParseResult {
        val exerciseId = json.optString("exerciseId", "unknown_exercise")
        val isConfirmed = json.optBoolean("isPlateauConfirmed", true)
        val cause = json.optString("primaryCause", "توقف تطابق عصبی یا فرسودگی سیستم عصبی")
        val action = json.optString("recommendedAction", "اعمال تنوع بیومکانیکی یا تغییر زاویه")
        val deload = json.optBoolean("deloadRecommended", false)
        val variations = jsonArrayToStringList(json.optJSONArray("suggestedVariations"))
        val cues = jsonArrayToStringList(json.optJSONArray("biomechanicalCues"))

        return ParseResult.Success(
            AIPlateauAdvice(
                exerciseId = exerciseId,
                isPlateauConfirmed = isConfirmed,
                primaryCause = cause,
                recommendedAction = action,
                deloadRecommended = deload,
                suggestedVariations = variations,
                biomechanicalCues = cues
            )
        )
    }

    private fun parseSubstitutionAdvice(json: JSONObject): ParseResult {
        val original = json.optString("originalExercise", "حرکت پایه")
        val substituted = json.optString("substitutedExercise", "حرکت جایگزین")
        val pattern = json.optString("matchingMovementPattern", "UNKNOWN")
        val shoulderLoad = json.optString("shoulderLoadImpact", "EQUAL")
        val justification = json.optString("justification", "حرکت هم‌راستا با ایمنی مفصل")
        val safetyCues = jsonArrayToStringList(json.optJSONArray("safetyCues"))

        return ParseResult.Success(
            AIExerciseSubstitutionAdvice(
                originalExercise = original,
                substitutedExercise = substituted,
                matchingMovementPattern = pattern,
                shoulderLoadImpact = shoulderLoad,
                justification = justification,
                safetyCues = safetyCues
            )
        )
    }

    private fun jsonArrayToStringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            val str = array.optString(i)
            if (str.isNotBlank()) list.add(str)
        }
        return list
    }
}
