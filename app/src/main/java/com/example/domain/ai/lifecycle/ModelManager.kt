package com.example.domain.ai.lifecycle

import com.example.domain.ai.model.AIModelInfo
import com.example.domain.ai.model.ModelState
import com.example.domain.ai.runtime.InferenceRuntime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manages the local model lifecycle:
 * - Checks model availability on disk/assets
 * - Manages loading states (Uninitialized -> Checking -> Loading -> Loaded / Unavailable / Error)
 * - Safely unloads models to free device memory
 * - Implements graceful failure and switches to fallback runtime
 */
class ModelManager(
    private val expectedModelPath: String? = null,
    private val inferenceRuntime: InferenceRuntime,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _modelState = MutableStateFlow<ModelState>(ModelState.Uninitialized)
    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    private var activeModelInfo: AIModelInfo? = null

    /**
     * Checks whether an offline model artifact exists locally on disk or assets.
     * Guaranteed to NOT make any network requests.
     */
    suspend fun checkAvailability(): Boolean = withContext(dispatcher) {
        _modelState.value = ModelState.Checking

        if (expectedModelPath.isNullOrBlank()) {
            _modelState.value = ModelState.Unavailable(
                "هیچ مدل باینری محلی نصب نشده است (حالت آفلاین پشتیبان فعال شد)."
            )
            return@withContext false
        }

        val file = File(expectedModelPath)
        if (file.exists() && file.isFile && file.length() > 0) {
            return@withContext true
        } else {
            _modelState.value = ModelState.Unavailable(
                "فایل مدل محلی در مسیر مشخص‌شده یافت نشد: $expectedModelPath"
            )
            return@withContext false
        }
    }

    /**
     * Loads the model into memory. If the physical model is absent or fails to load,
     * catches exceptions gracefully and marks state as Unavailable/Error without crashing.
     */
    suspend fun loadModel(): Result<AIModelInfo> = withContext(dispatcher) {
        try {
            _modelState.value = ModelState.Loading

            val isAvailable = checkAvailability()
            if (!isAvailable) {
                // Graceful fallback mode
                val fallbackInfo = AIModelInfo(
                    modelId = "fallback-heuristic-v1",
                    modelName = "Biomechanical Offline Heuristic Engine",
                    version = "1.0.0",
                    isFallbackOnly = true
                )
                activeModelInfo = fallbackInfo
                inferenceRuntime.initialize(null)
                // We keep modelState as Unavailable so callers know no heavy model is loaded,
                // but engine can use fallback runtime safely.
                return@withContext Result.success(fallbackInfo)
            }

            val file = File(expectedModelPath!!)
            val initResult = inferenceRuntime.initialize(file.absolutePath)

            if (initResult.isSuccess) {
                val info = AIModelInfo(
                    modelId = file.nameWithoutExtension,
                    modelName = file.name,
                    version = "1.0",
                    filePath = file.absolutePath,
                    fileSizeBytes = file.length(),
                    isFallbackOnly = false
                )
                activeModelInfo = info
                _modelState.value = ModelState.Loaded(info)
                Result.success(info)
            } else {
                val err = initResult.exceptionOrNull() ?: RuntimeException("Unknown runtime initialization error")
                _modelState.value = ModelState.Error(err)
                Result.failure(err)
            }
        } catch (e: Throwable) {
            _modelState.value = ModelState.Error(e)
            Result.failure(e)
        }
    }

    /**
     * Unloads the model from memory and releases runtime buffers.
     */
    suspend fun unloadModel() = withContext(dispatcher) {
        try {
            inferenceRuntime.release()
            activeModelInfo = null
            _modelState.value = ModelState.Unloaded
        } catch (e: Exception) {
            _modelState.value = ModelState.Error(e)
        }
    }

    fun isModelLoaded(): Boolean {
        return _modelState.value is ModelState.Loaded
    }

    fun isFallbackActive(): Boolean {
        return activeModelInfo?.isFallbackOnly == true || _modelState.value is ModelState.Unavailable
    }

    fun getActiveModelInfo(): AIModelInfo? = activeModelInfo
}
