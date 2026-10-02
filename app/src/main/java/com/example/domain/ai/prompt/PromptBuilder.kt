package com.example.domain.ai.prompt

import com.example.domain.ai.model.AIInferenceRequest
import com.example.domain.ai.model.AITaskType

/**
 * Builds compact, structured prompts optimized for on-device local models.
 * Incorporates calisthenics domain knowledge, safety rules, and strict JSON output schemas.
 */
object PromptBuilder {

    private const val SYSTEM_PROMPT = """
You are Calisten AI, an expert offline calisthenics and biomechanics coach.
Rules:
1. Always output ONLY valid JSON without markdown wrapping or commentary.
2. Adhere strictly to the requested JSON schema.
3. Prioritize joint health, shoulder preservation, and progressive overload.
4. If pain or red readiness is present, decrease volume or substitute with safer biomechanical angles.
"""

    /**
     * Constructs the full formatted prompt string for an inference request.
     */
    fun buildPrompt(request: AIInferenceRequest): String {
        val taskInstruction = when (request.taskType) {
            AITaskType.WORKOUT_ADVICE -> buildWorkoutAdviceInstruction(request)
            AITaskType.PLATEAU_ANALYSIS -> buildPlateauAnalysisInstruction(request)
            AITaskType.EXERCISE_SUBSTITUTION -> buildExerciseSubstitutionInstruction(request)
            AITaskType.PROGRESSION_ADVICE -> buildProgressionAdviceInstruction(request)
            AITaskType.RECOVERY_INSIGHT -> buildRecoveryInsightInstruction(request)
        }

        val contextSection = buildContextSection(request)

        return """
<|im_start|>system
$SYSTEM_PROMPT
<|im_end|>
<|im_start|>user
TASK: ${request.taskType.name}
CONTEXT:
$contextSection

INSTRUCTIONS:
$taskInstruction
<|im_end|>
<|im_start|>assistant
""".trimIndent()
    }

    private fun buildContextSection(request: AIInferenceRequest): String {
        val sb = StringBuilder()
        sb.append("- User Profile: ${request.userProfileSummary}\n")
        sb.append("- Experience Level: ${request.experienceLevel}\n")
        sb.append("- Readiness State: ${request.readinessScore}\n")
        sb.append("- Target Goal: ${request.targetGoal}\n")

        if (request.availableEquipment.isNotEmpty()) {
            sb.append("- Equipment: ${request.availableEquipment.joinToString(", ")}\n")
        } else {
            sb.append("- Equipment: None (Pure Bodyweight)\n")
        }

        if (request.painAreas.isNotEmpty()) {
            sb.append("- ACTIVE PAIN/DISCOMFORT: ${request.painAreas.joinToString(", ")} (CRITICAL: Protect these joints)\n")
        }

        if (request.currentExerciseNames.isNotEmpty()) {
            sb.append("- Current Exercises: ${request.currentExerciseNames.joinToString(", ")}\n")
        }

        if (request.specificQuestionOrContext.isNotBlank()) {
            sb.append("- Note/Query: ${request.specificQuestionOrContext}\n")
        }

        return sb.toString().trim()
    }

    private fun buildWorkoutAdviceInstruction(request: AIInferenceRequest): String {
        return """
Provide workout session advice in this exact JSON schema:
{
  "summary": "Short summary of workout adjustments",
  "primaryFocus": "Biomechanical focus",
  "recommendedSetsDelta": 0,
  "recommendedRpeTarget": 7.5,
  "exerciseModifications": [
    {
      "exerciseName": "Exercise name",
      "targetReps": 10,
      "targetSets": 3,
      "rpeTarget": 7.5,
      "modificationType": "MAINTAIN",
      "reason": "Why this modification is prescribed"
    }
  ],
  "recoveryRecommendation": "Sleep or recovery tip",
  "safetyWarnings": ["Safety warning if pain or excessive fatigue"]
}
"""
    }

    private fun buildPlateauAnalysisInstruction(request: AIInferenceRequest): String {
        return """
Provide plateau breakthrough analysis in this exact JSON schema:
{
  "exerciseId": "id_or_name",
  "isPlateauConfirmed": true,
  "primaryCause": "Detailed biomechanical or neurological cause",
  "recommendedAction": "Actionable breakthrough protocol",
  "deloadRecommended": false,
  "suggestedVariations": ["Variation 1", "Variation 2"],
  "biomechanicalCues": ["Cue 1", "Cue 2"]
}
"""
    }

    private fun buildExerciseSubstitutionInstruction(request: AIInferenceRequest): String {
        return """
Provide exercise substitution in this exact JSON schema:
{
  "originalExercise": "Original exercise name",
  "substitutedExercise": "Substituted exercise name",
  "matchingMovementPattern": "e.g. HORIZONTAL_PUSH, VERTICAL_PULL",
  "shoulderLoadImpact": "REDUCED",
  "justification": "Why this substitution satisfies biomechanics",
  "safetyCues": ["Cue 1", "Cue 2"]
}
"""
    }

    private fun buildProgressionAdviceInstruction(request: AIInferenceRequest): String {
        return """
Analyze progression readiness in this exact JSON schema:
{
  "summary": "Progression evaluation summary",
  "primaryFocus": "Technique stability before load increase",
  "recommendedSetsDelta": 0,
  "recommendedRpeTarget": 8.0,
  "exerciseModifications": [],
  "recoveryRecommendation": "Adequate rest between sessions",
  "safetyWarnings": []
}
"""
    }

    private fun buildRecoveryInsightInstruction(request: AIInferenceRequest): String {
        return """
Provide recovery guidance in this exact JSON schema:
{
  "summary": "Recovery analysis",
  "primaryFocus": "Systemic restoration",
  "recommendedSetsDelta": -1,
  "recommendedRpeTarget": 6.0,
  "exerciseModifications": [],
  "recoveryRecommendation": "Sleep and mobility focus",
  "safetyWarnings": ["Avoid high intensity training today"]
}
"""
    }
}
