package com.yunho.nanobanana.ai

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

/**
 * Enhanced AI Service - Extended capabilities for text and combined generation
 * 
 * Features:
 * - Multi-modal output (image, text, combined)
 * - Quality validation and auto-regeneration
 * - Reasoning extraction from AI responses
 * - Text coherence validation
 * - Image artifact detection (basic)
 */
class EnhancedAIService(
    private val apiKey: String,
    private val promptManager: PromptManager
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    // Advanced output validator for comprehensive quality checks
    private val outputValidator = OutputValidator()
    
    /**
     * Generates AI output based on configured mode
     * 
     * @param basePrompt The user's base prompt
     * @param bitmaps Optional input images
     * @param mode Output mode (image, text, or combined)
     * @return AIGenerationResult containing image and/or text
     */
    suspend fun generateWithMode(
        basePrompt: String,
        bitmaps: List<Bitmap> = emptyList(),
        mode: AIOutputMode = promptManager.outputMode
    ): AIGenerationResult {
        if (apiKey.isBlank()) {
            Log.e(TAG, "API key is missing")
            return AIGenerationResult.Error("API key is required")
        }
        
        // Generate enhanced prompt
        val enhancedPrompt = promptManager.generatePrompt(basePrompt, mode, includeReasoning = true)
        
        // Retry mechanism
        var attempt = 0
        val maxAttempts = 3
        
        while (attempt < maxAttempts) {
            try {
                val result = performGeneration(enhancedPrompt, bitmaps, mode)
                
                // Advanced validation with detailed feedback
                val validationResult = when (result) {
                    is AIGenerationResult.Success -> {
                        outputValidator.validate(result.image, result.text, mode)
                    }
                    else -> null
                }
                
                if (validationResult != null) {
                    Log.d(TAG, "Validation result (attempt ${attempt + 1}): ${validationResult.getDetailedFeedback()}")
                    
                    if (validationResult.passed) {
                        Log.d(TAG, "Generation successful: ${validationResult.getUserMessage()}")
                        return result
                    } else if (!validationResult.shouldRetry) {
                        // Validation failed but shouldn't retry (e.g., only minor issues)
                        Log.w(TAG, "Accepting result despite validation issues: ${validationResult.getUserMessage()}")
                        return result
                    } else {
                        Log.w(TAG, "Quality validation failed: ${validationResult.getUserMessage()}")
                    }
                } else {
                    // Result is an error
                    Log.w(TAG, "Generation failed on attempt ${attempt + 1}")
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
        return AIGenerationResult.Error("Failed after $maxAttempts attempts")
    }
    
    /**
     * Performs the actual AI generation request
     */
    private suspend fun performGeneration(
        prompt: String,
        bitmaps: List<Bitmap>,
        mode: AIOutputMode
    ): AIGenerationResult {
        return withContext(Dispatchers.IO) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent"
            val media = "application/json; charset=utf-8".toMediaType()
            
            // Build request body
            val bodyJson = buildRequestBody(prompt, bitmaps, mode)
            val body = bodyJson.toRequestBody(media)
            
            val req = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()
            
            try {
                Log.d(TAG, "Sending request to Gemini API (mode: $mode)")
                
                client.newCall(req).execute().use { response ->
                    val rawText = response.body?.string().orEmpty()
                    
                    if (!response.isSuccessful) {
                        Log.e(TAG, "HTTP ${response.code}: $rawText")
                        return@withContext AIGenerationResult.Error("HTTP ${response.code}")
                    }
                    
                    Log.d(TAG, "Response received successfully")
                    parseResponse(rawText, mode)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Request failed", e)
                AIGenerationResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Builds request body based on output mode
     */
    private fun buildRequestBody(
        prompt: String,
        bitmaps: List<Bitmap>,
        mode: AIOutputMode
    ): String {
        return JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        // Add text prompt
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                        
                        // Add images if provided
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
            
            // Add generation config
            put("generationConfig", JSONObject().apply {
                put("temperature", promptManager.creativityLevel.toDouble())
                put("topK", 32)
                put("topP", 1.0)
                put("candidateCount", 1)
            })
        }.toString()
    }
    
    /**
     * Parses API response based on expected mode
     */
    private fun parseResponse(rawText: String, mode: AIOutputMode): AIGenerationResult {
        try {
            val jsonResponse = JSONObject(rawText)
            val candidatesArray = jsonResponse.optJSONArray("candidates")
            
            if (candidatesArray == null || candidatesArray.length() == 0) {
                return AIGenerationResult.Error("No candidates in response")
            }
            
            val candidate = candidatesArray.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val partsArray = content?.optJSONArray("parts")
            
            if (partsArray == null) {
                return AIGenerationResult.Error("No parts in response")
            }
            
            var generatedImage: Bitmap? = null
            var generatedText = StringBuilder()
            
            // Extract image and text from parts
            for (i in 0 until partsArray.length()) {
                val part = partsArray.getJSONObject(i)
                
                // Check for image data
                val inlineData = part.optJSONObject("inlineData") ?: part.optJSONObject("inline_data")
                if (inlineData != null && mode != AIOutputMode.TEXT_ONLY) {
                    val base64Image = inlineData.optString("data")
                    if (base64Image.isNotEmpty()) {
                        generatedImage = decodeBase64ToBitmap(base64Image)
                    }
                }
                
                // Check for text data
                val textPart = part.optString("text")
                if (textPart.isNotEmpty() && mode != AIOutputMode.IMAGE_ONLY) {
                    generatedText.append(textPart)
                }
            }
            
            // Validate based on mode
            return when (mode) {
                AIOutputMode.IMAGE_ONLY -> {
                    if (generatedImage != null) {
                        AIGenerationResult.Success(image = generatedImage)
                    } else {
                        AIGenerationResult.Error("No image generated")
                    }
                }
                AIOutputMode.TEXT_ONLY -> {
                    val text = generatedText.toString()
                    if (text.isNotEmpty()) {
                        AIGenerationResult.Success(text = text)
                    } else {
                        AIGenerationResult.Error("No text generated")
                    }
                }
                AIOutputMode.COMBINED -> {
                    val text = generatedText.toString()
                    if (generatedImage != null || text.isNotEmpty()) {
                        AIGenerationResult.Success(
                            image = generatedImage,
                            text = text.takeIf { it.isNotEmpty() }
                        )
                    } else {
                        AIGenerationResult.Error("No output generated")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response", e)
            return AIGenerationResult.Error("Parse error: ${e.message}")
        }
    }
    
    /**
     * Converts bitmap to Base64
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * Decodes Base64 to Bitmap
     */
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
        private const val TAG = "EnhancedAIService"
    }
}

/**
 * Result wrapper for AI generation
 */
sealed class AIGenerationResult {
    data class Success(
        val image: Bitmap? = null,
        val text: String? = null
    ) : AIGenerationResult()
    
    data class Error(val message: String) : AIGenerationResult()
}
