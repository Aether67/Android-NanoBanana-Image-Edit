package com.yunho.nanobanana

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yunho.nanobanana.performance.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Enhanced NanoBanana service with improved error handling and Gemini 2.5 API integration
 * Provides robust image editing capabilities with retry mechanism and comprehensive logging
 * 
 * Performance features:
 * - LRU image caching for faster repeated requests
 * - Exponential backoff retry with circuit breaker pattern
 * - Resource-aware quality adjustment
 * - Performance telemetry and metrics
 * - Task prioritization for optimal responsiveness
 */
class NanoBananaService(
    private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
        
    private val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    var apiKey by mutableStateOf("")
    
    // Performance components
    private val imageCache = ImageCache.getInstance()
    private val metrics = PerformanceMetrics.getInstance()
    private val retryPolicy = RetryPolicy()
    private val resourceMonitor = ResourceMonitor(context)

    init {
        apiKey = sharedPreferences.getString("api_key", "") ?: ""
    }

    /**
     * Saves the API key to persistent storage
     */
    fun onSaveKey() {
        sharedPreferences.edit().apply {
            putString("api_key", apiKey)
            apply()
        }
        Log.d(TAG, "API key saved successfully")
    }

    /**
     * Edits images using the Gemini 2.5 Flash model with enhanced performance features
     * - Checks cache first for instant results
     * - Uses resource-aware quality settings
     * - Implements retry with exponential backoff and circuit breaker
     * - Tracks performance metrics
     * 
     * @param prompt The text prompt for image transformation
     * @param bitmaps List of input images to transform
     * @return The generated bitmap or throws exception on failure
     */
    suspend fun editImage(
        prompt: String,
        bitmaps: List<Bitmap>
    ): Bitmap? = withContext(DispatcherProvider.mediumPriority) {
        if (apiKey.isBlank()) {
            Log.e(TAG, "API key is missing")
            throw IllegalStateException("API key is required")
        }
        
        // Check cache first
        val cachedResult = imageCache.get(prompt, bitmaps)
        if (cachedResult != null) {
            metrics.recordCacheHit()
            Log.d(TAG, "Returning cached result")
            return@withContext cachedResult
        }
        metrics.recordCacheMiss()
        
        // Check resource constraints
        val resourceStatus = resourceMonitor.getResourceStatus()
        if (!resourceStatus.hasNetwork) {
            throw IllegalStateException("No network connection available")
        }
        
        if (resourceStatus.isLowMemory) {
            Log.w(TAG, "Low memory detected, using reduced quality")
        }
        
        // Use retry policy with exponential backoff
        val startTime = System.currentTimeMillis()
        metrics.recordApiCall()
        
        val result = retryPolicy.executeWithRetry(
            maxAttempts = 3,
            initialDelayMs = 1000,
            maxDelayMs = 10000,
            backoffMultiplier = 2.0
        ) { attempt ->
            try {
                editImageWithPrompt(
                    prompt = prompt,
                    imageBase64List = bitmaps.map { bitmapToBase64(it, resourceStatus.recommendedQuality) },
                    attempt = attempt
                )
            } catch (e: Exception) {
                Log.e(TAG, "Attempt $attempt failed: ${e.message}", e)
                null
            }
        }
        
        val elapsed = System.currentTimeMillis() - startTime
        
        result.fold(
            onSuccess = { bitmap ->
                metrics.recordApiSuccess(elapsed)
                // Cache the result
                imageCache.put(prompt, bitmaps, bitmap)
                bitmap
            },
            onFailure = { exception ->
                val errorType = when {
                    exception is RetryPolicy.CircuitBreakerException -> "CircuitBreaker"
                    exception.message?.contains("timeout") == true -> "Timeout"
                    exception.message?.contains("network") == true -> "Network"
                    exception.message?.contains("401") == true -> "Unauthorized"
                    exception.message?.contains("429") == true -> "RateLimit"
                    else -> "Unknown"
                }
                metrics.recordApiFailure(errorType, exception.message ?: "Unknown error")
                throw exception
            }
        )
    }

    /**
     * Sends request to Gemini 2.5 Flash API for image generation
     * @param prompt The transformation prompt
     * @param imageBase64List Base64 encoded images
     * @param attempt Current attempt number for logging
     * @return Generated bitmap or null if failed
     */
    private suspend fun editImageWithPrompt(
        prompt: String,
        imageBase64List: List<String>,
        attempt: Int = 1
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent"
            val media = "application/json; charset=utf-8".toMediaType()

            // Build request body with proper JSON structure for Gemini 2.5
            val bodyJson = buildString {
                append("{")
                append("\"contents\": [{")
                append("\"parts\":[")
                append("{ \"text\": \"$prompt. Generate a high-quality, artifact-free image with proper resolution and clarity.\" }")

                imageBase64List.forEach { imageBase64 ->
                    if (imageBase64.isNotEmpty()) {
                        append(", { \"inline_data\": { \"mime_type\": \"image/jpeg\", \"data\": \"$imageBase64\" } }")
                    }
                }

                append("]")
                append("}],")
                // Add generation config for better quality
                append("\"generationConfig\": {")
                append("\"temperature\": 0.4,")
                append("\"topK\": 32,")
                append("\"topP\": 1")
                append("}")
                append("}")
            }

            val body = bodyJson.toRequestBody(media)

            val req = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            try {
                Log.d(TAG, "Sending request to Gemini 2.5 Flash API (attempt $attempt)")
                
                client.newCall(req).execute().use { response ->
                    val rawText = response.body?.string().orEmpty()

                    if (!response.isSuccessful) {
                        Log.e(TAG, "HTTP ${response.code}: $rawText")
                        
                        // Parse error message for better user feedback
                        try {
                            val errorJson = JSONObject(rawText)
                            val errorMessage = errorJson.optJSONObject("error")?.optString("message")
                            if (errorMessage != null) {
                                Log.e(TAG, "API Error: $errorMessage")
                                throw Exception("API Error (${response.code}): $errorMessage")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Could not parse error response", e)
                        }
                        
                        throw Exception("HTTP ${response.code}: Request failed")
                    }

                    Log.d(TAG, "Response received successfully")

                    try {
                        val jsonResponse = JSONObject(rawText)
                        val candidatesArray = jsonResponse.optJSONArray("candidates")

                        if (candidatesArray != null && candidatesArray.length() > 0) {
                            val candidate = candidatesArray.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val partsArray = content?.optJSONArray("parts")

                            if (partsArray != null) {
                                for (i in 0 until partsArray.length()) {
                                    val part = partsArray.getJSONObject(i)
                                    val inlineData = part.optJSONObject("inlineData") ?: part.optJSONObject("inline_data")

                                    if (inlineData != null) {
                                        val base64Image = inlineData.optString("data")

                                        if (base64Image.isNotEmpty()) {
                                            Log.d(TAG, "Decoding generated image")
                                            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                            
                                            if (bitmap != null) {
                                                Log.d(TAG, "Image decoded successfully: ${bitmap.width}x${bitmap.height}")
                                                return@withContext bitmap
                                            } else {
                                                Log.e(TAG, "Failed to decode bitmap from base64")
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "No candidates found in response")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse image from response", e)
                        throw e
                    }

                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Request failed", e)
                throw e
            }
        }
    }

    /**
     * Converts bitmap to Base64 encoded JPEG with optimized quality
     * @param bitmap The bitmap to encode
     * @param quality JPEG quality (0-100), defaults to 95
     * @return Base64 encoded string
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 95): String {
        val outputStream = ByteArrayOutputStream()
        
        // Use resource-aware quality setting
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        
        val byteArray = outputStream.toByteArray()
        
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * Gets performance metrics summary
     */
    fun getMetrics(): PerformanceMetrics.PerformanceSummary {
        return metrics.getSummary()
    }
    
    /**
     * Gets resource status
     */
    fun getResourceStatus(): ResourceMonitor.ResourceStatus {
        return resourceMonitor.getResourceStatus()
    }
    
    /**
     * Logs current performance metrics
     */
    fun logPerformanceMetrics() {
        metrics.logSummary()
    }
    
    /**
     * Clears the image cache
     */
    fun clearCache() {
        imageCache.clear()
    }

    companion object {
        private const val TAG = "NanoBananaService"
        
        @Composable
        fun rememberNanoBananaService(context: Context) = remember { NanoBananaService(context) }
    }
}
