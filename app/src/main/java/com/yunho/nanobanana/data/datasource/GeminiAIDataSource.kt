package com.yunho.nanobanana.data.datasource

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Implementation of AIDataSource using Gemini API
 * Handles all HTTP communication with the AI service
 */
class GeminiAIDataSource @Inject constructor(
    private val apiKey: String
) : AIDataSource {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    override suspend fun generateImage(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float
    ): Bitmap? {
        val enhancedPrompt = "$prompt. Generate a high-quality, artifact-free image with proper resolution and clarity."
        return performGenerationWithRetry(enhancedPrompt, bitmaps, temperature, extractImage = true).first
    }
    
    override suspend fun generateText(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float
    ): String? {
        val enhancedPrompt = "$prompt. Provide a detailed explanation with reasoning."
        return performGenerationWithRetry(enhancedPrompt, bitmaps, temperature, extractImage = false).second
    }
    
    override suspend fun generateCombined(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float
    ): Pair<Bitmap?, String?> {
        val enhancedPrompt = """
            Task: $prompt
            
            Image Generation:
            Generate a high-quality, artifact-free image with proper resolution and clarity.
            
            Text Analysis:
            Provide a clear explanation of the generated image, including:
            - What changes were made and why
            - Key visual elements and their significance
            - Artistic or technical considerations
        """.trimIndent()
        
        return performGenerationWithRetry(enhancedPrompt, bitmaps, temperature, extractImage = true)
    }
    
    /**
     * Performs generation with retry mechanism
     */
    private suspend fun performGenerationWithRetry(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float,
        extractImage: Boolean
    ): Pair<Bitmap?, String?> {
        var attempt = 0
        val maxAttempts = 3
        
        while (attempt < maxAttempts) {
            try {
                val result = performGeneration(prompt, bitmaps, temperature, extractImage)
                if (result.first != null || result.second != null) {
                    Log.d(TAG, "Generation successful on attempt ${attempt + 1}")
                    return result
                }
            } catch (e: Exception) {
                Log.e(TAG, "Attempt ${attempt + 1} failed: ${e.message}", e)
            }
            
            attempt++
            if (attempt < maxAttempts) {
                Log.d(TAG, "Retrying in ${attempt * 2} seconds...")
                delay(attempt * 2000L)
            }
        }
        
        Log.e(TAG, "All $maxAttempts attempts failed")
        return Pair(null, null)
    }
    
    /**
     * Performs a single generation attempt
     */
    private suspend fun performGeneration(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float,
        extractImage: Boolean
    ): Pair<Bitmap?, String?> {
        return withContext(Dispatchers.IO) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent"
            val media = "application/json; charset=utf-8".toMediaType()
            
            val bodyJson = buildRequestBody(prompt, bitmaps, temperature)
            val body = bodyJson.toRequestBody(media)
            
            val req = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()
            
            try {
                Log.d(TAG, "Sending request to Gemini API")
                
                client.newCall(req).execute().use { response ->
                    val rawText = response.body?.string().orEmpty()
                    
                    if (!response.isSuccessful) {
                        Log.e(TAG, "HTTP ${response.code}: $rawText")
                        return@withContext Pair(null, null)
                    }
                    
                    Log.d(TAG, "Response received successfully")
                    parseResponse(rawText, extractImage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Request failed", e)
                Pair(null, null)
            }
        }
    }
    
    /**
     * Builds JSON request body
     */
    private fun buildRequestBody(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float
    ): String {
        return JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                        
                        bitmaps.forEach { bitmap ->
                            val base64 = bitmapToBase64(bitmap)
                            if (base64.isNotEmpty()) {
                                put(JSONObject().apply {
                                    put("inline_data", JSONObject().apply {
                                        put("mime_type", "image/jpeg")
                                        put("data", base64)
                                    })
                                })
                            }
                        }
                    })
                })
            })
            
            put("generationConfig", JSONObject().apply {
                put("temperature", temperature.toDouble())
                put("topK", 32)
                put("topP", 1.0)
                put("candidateCount", 1)
            })
        }.toString()
    }
    
    /**
     * Parses API response
     */
    private fun parseResponse(rawText: String, extractImage: Boolean): Pair<Bitmap?, String?> {
        try {
            val jsonResponse = JSONObject(rawText)
            val candidatesArray = jsonResponse.optJSONArray("candidates")
            
            if (candidatesArray == null || candidatesArray.length() == 0) {
                return Pair(null, null)
            }
            
            val candidate = candidatesArray.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val partsArray = content?.optJSONArray("parts")
            
            if (partsArray == null) {
                return Pair(null, null)
            }
            
            var image: Bitmap? = null
            val textBuilder = StringBuilder()
            
            for (i in 0 until partsArray.length()) {
                val part = partsArray.getJSONObject(i)
                
                if (extractImage) {
                    val inlineData = part.optJSONObject("inlineData") ?: part.optJSONObject("inline_data")
                    if (inlineData != null) {
                        val base64Image = inlineData.optString("data")
                        if (base64Image.isNotEmpty()) {
                            image = decodeBase64ToBitmap(base64Image)
                        }
                    }
                }
                
                val textPart = part.optString("text")
                if (textPart.isNotEmpty()) {
                    textBuilder.append(textPart)
                }
            }
            
            val text = textBuilder.toString().takeIf { it.isNotEmpty() }
            return Pair(image, text)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response", e)
            return Pair(null, null)
        }
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    private fun decodeBase64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap", e)
            null
        }
    }
    
    companion object {
        private const val TAG = "GeminiAIDataSource"
    }
}
