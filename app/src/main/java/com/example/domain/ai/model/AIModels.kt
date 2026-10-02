package com.example.domain.ai.model

/**
 * Lifecycle state of the local offline AI model.
 */
sealed class ModelState {
    object Uninitialized : ModelState()
    object Checking : ModelState()
    data class Unavailable(val reason: String) : ModelState()
    object Loading : ModelState()
    data class Loaded(val modelInfo: AIModelInfo) : ModelState()
    data class Error(val throwable: Throwable) : ModelState()
    object Unloaded : ModelState()
}

/**
 * Metadata descriptor for an offline AI model artifact.
 */
data class AIModelInfo(
    val modelId: String,
    val modelName: String,
    val version: String,
    val parameterCount: String = "Unknown",
    val quantization: String = "INT4",
    val contextWindowTokens: Int = 2048,
    val filePath: String? = null,
    val fileSizeBytes: Long = 0L,
    val isFallbackOnly: Boolean = false
)

/**
 * Input request passed to the Offline AI Engine.
 */
data class AIInferenceRequest(
    val taskType: AITaskType,
    val userProfileSummary: String,
    val experienceLevel: String = "INTERMEDIATE",
    val availableEquipment: List<String> = emptyList(),
    val painAreas: List<String> = emptyList(),
    val readinessScore: String = "GREEN", // GREEN, YELLOW, RED
    val currentExerciseNames: List<String> = emptyList(),
    val targetGoal: String = "STRENGTH",
    val specificQuestionOrContext: String = "",
    val maxTokens: Int = 512,
    val temperature: Float = 0.2f
)

enum class AITaskType {
    WORKOUT_ADVICE,
    PLATEAU_ANALYSIS,
    EXERCISE_SUBSTITUTION,
    PROGRESSION_ADVICE,
    RECOVERY_INSIGHT
}

/**
 * Unified response from the Offline AI Engine.
 */
data class AIInferenceResponse(
    val requestId: String,
    val taskType: AITaskType,
    val structuredData: AIStructuredOutput,
    val formattedResponsePersian: String,
    val formattedResponseEnglish: String,
    val isFallback: Boolean,
    val inferenceTimeMs: Long,
    val modelVersionUsed: String
)

/**
 * Base marker interface for all validated structured model outputs.
 */
sealed interface AIStructuredOutput

/**
 * Structured advice for workout session adjustments.
 */
data class AIWorkoutAdvice(
    val summary: String,
    val primaryFocus: String,
    val recommendedSetsDelta: Int = 0,
    val recommendedRpeTarget: Double = 7.5,
    val exerciseModifications: List<AIExerciseModification> = emptyList(),
    val recoveryRecommendation: String = "",
    val safetyWarnings: List<String> = emptyList()
) : AIStructuredOutput

data class AIExerciseModification(
    val exerciseName: String,
    val targetReps: Int,
    val targetSets: Int,
    val rpeTarget: Double,
    val modificationType: String, // "MAINTAIN", "PROGRESS", "REGRESS", "SUBSTITUTE", "DELOAD"
    val reason: String
)

/**
 * Structured advice for overcoming a detected movement plateau.
 */
data class AIPlateauAdvice(
    val exerciseId: String,
    val isPlateauConfirmed: Boolean,
    val primaryCause: String,
    val recommendedAction: String,
    val deloadRecommended: Boolean,
    val suggestedVariations: List<String> = emptyList(),
    val biomechanicalCues: List<String> = emptyList()
) : AIStructuredOutput

/**
 * Structured advice for exercise substitution.
 */
data class AIExerciseSubstitutionAdvice(
    val originalExercise: String,
    val substitutedExercise: String,
    val matchingMovementPattern: String,
    val shoulderLoadImpact: String, // "REDUCED", "EQUAL", "INCREASED"
    val justification: String,
    val safetyCues: List<String> = emptyList()
) : AIStructuredOutput

/**
 * Fallback generic structured output when specific schema cannot be matched.
 */
data class AIGenericAdvice(
    val title: String,
    val keyPoints: List<String>,
    val safetyNotice: String,
    val isHeuristicGenerated: Boolean = true
) : AIStructuredOutput

/**
 * Overall operational status of the Offline AI Engine.
 */
data class AIEngineStatus(
    val modelState: ModelState,
    val isOperational: Boolean,
    val isUsingFallback: Boolean,
    val activeRuntimeName: String,
    val contextWindowTokens: Int,
    val offlineVerified: Boolean = true
)
