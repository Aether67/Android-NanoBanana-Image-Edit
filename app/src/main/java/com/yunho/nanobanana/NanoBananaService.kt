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
 */
class NanoBananaService(
    context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
        
    private val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    var apiKey by mutableStateOf("")

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
     * Edits images using the Gemini 2.5 Flash model with retry mechanism
     * @param prompt The text prompt for image transformation
     * @param bitmaps List of input images to transform
     * @return The generated bitmap or null if the operation fails
     */
    suspend fun editImage(
        prompt: String,
        bitmaps: List<Bitmap>
    ): Bitmap? {
        if (apiKey.isBlank()) {
            Log.e(TAG, "API key is missing")
            return null
        }

        val imageBase64List = bitmaps.map { bitmapToBase64(it) }
        
        // Retry mechanism for better reliability
        var attempt = 0
        val maxAttempts = 3
        
        while (attempt < maxAttempts) {
            try {
                val result = editImageWithPrompt(prompt, imageBase64List)
                if (result != null) {
                    Log.d(TAG, "Image generation successful on attempt ${attempt + 1}")
                    return result
                }
            } catch (e: Exception) {
                Log.e(TAG, "Attempt ${attempt + 1} failed: ${e.message}", e)
            }
            
            attempt++
            if (attempt < maxAttempts) {
                Log.d(TAG, "Retrying in ${attempt * 2} seconds...")
                delay(attempt * 2000L) // Exponential backoff
            }
        }
        
        Log.e(TAG, "All $maxAttempts attempts failed")
        return null
    }

    /**
     * Sends request to Gemini 2.5 Flash API for image generation
     * @param prompt The transformation prompt
     * @param imageBase64List Base64 encoded images
     * @return Generated bitmap or null if failed
     */
    private suspend fun editImageWithPrompt(
        prompt: String,
        imageBase64List: List<String>
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
                Log.d(TAG, "Sending request to Gemini 2.5 Flash API")
                
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
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Could not parse error response", e)
                        }
                        
                        return@withContext null
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
                    }

                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Request failed", e)
                return@withContext null
            }
        }
    }

    /**
     * Converts bitmap to Base64 encoded JPEG with optimized quality
     * @param bitmap The bitmap to encode
     * @return Base64 encoded string
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        
        // Optimize image quality for better processing
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        
        val byteArray = outputStream.toByteArray()
        
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    companion object {
        private const val TAG = "NanoBananaService"
        
        @Composable
        fun rememberNanoBananaService(context: Context) = remember { NanoBananaService(context) }
    }
}
