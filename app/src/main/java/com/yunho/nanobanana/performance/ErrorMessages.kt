package com.yunho.nanobanana.performance

/**
 * Error message provider with user-friendly messages for different failure scenarios
 */
object ErrorMessages {
    
    /**
     * Gets user-friendly error message based on error type
     */
    fun getErrorMessage(exception: Exception): String {
        return when {
            exception is RetryPolicy.CircuitBreakerException -> {
                "⚠️ Service temporarily unavailable\n\nThe AI service has encountered multiple failures. Please wait a moment and try again.\n\nTip: Check your internet connection and API key."
            }
            
            exception.message?.contains("timeout", ignoreCase = true) == true -> {
                "⏱️ Request timed out\n\nThe AI service is taking longer than expected. This might be due to:\n• Slow internet connection\n• Server being busy\n• Large image size\n\nTip: Try with a smaller image or wait a moment."
            }
            
            exception.message?.contains("network", ignoreCase = true) == true ||
            exception.message?.contains("unable to resolve host", ignoreCase = true) == true -> {
                "🌐 Network error\n\nCouldn't connect to the AI service. Please check:\n• Your internet connection\n• WiFi or mobile data is enabled\n• You're not behind a restrictive firewall\n\nTip: Try switching between WiFi and mobile data."
            }
            
            exception.message?.contains("API key", ignoreCase = true) == true ||
            exception.message?.contains("unauthorized", ignoreCase = true) == true ||
            exception.message?.contains("401", ignoreCase = true) == true -> {
                "🔑 Invalid API key\n\nYour Google AI API key appears to be invalid or expired. Please:\n• Verify your API key is correct\n• Check it hasn't been revoked\n• Get a new key from Google AI Studio\n\nTip: Copy-paste carefully to avoid extra spaces."
            }
            
            exception.message?.contains("quota", ignoreCase = true) == true ||
            exception.message?.contains("rate limit", ignoreCase = true) == true ||
            exception.message?.contains("429", ignoreCase = true) == true -> {
                "📊 Rate limit exceeded\n\nYou've reached the API usage limit. This could mean:\n• Too many requests in a short time\n• Daily quota exceeded\n• Free tier limit reached\n\nTip: Wait a few minutes and try again, or upgrade your API plan."
            }
            
            exception.message?.contains("500", ignoreCase = true) == true ||
            exception.message?.contains("503", ignoreCase = true) == true -> {
                "🔧 Server error\n\nThe AI service is experiencing issues. This is temporary and should resolve soon.\n\nTip: Wait a moment and try again."
            }
            
            exception.message?.contains("memory", ignoreCase = true) == true ||
            exception is OutOfMemoryError -> {
                "💾 Memory limit reached\n\nYour device doesn't have enough memory for this operation.\n\nTip: Close other apps or try with a smaller image."
            }
            
            else -> {
                "❌ Generation failed\n\n${exception.message ?: "An unexpected error occurred"}\n\nTip: Please check your API key and internet connection, then try again."
            }
        }
    }
    
    /**
     * Gets short error title for notifications
     */
    fun getErrorTitle(exception: Exception): String {
        return when {
            exception is RetryPolicy.CircuitBreakerException -> "Service Unavailable"
            exception.message?.contains("timeout", ignoreCase = true) == true -> "Request Timed Out"
            exception.message?.contains("network", ignoreCase = true) == true -> "Network Error"
            exception.message?.contains("API key", ignoreCase = true) == true -> "Invalid API Key"
            exception.message?.contains("quota", ignoreCase = true) == true -> "Rate Limit Exceeded"
            exception.message?.contains("500", ignoreCase = true) == true -> "Server Error"
            exception.message?.contains("memory", ignoreCase = true) == true -> "Out of Memory"
            else -> "Generation Failed"
        }
    }
    
    /**
     * Gets degraded mode message
     */
    fun getDegradedModeMessage(reason: DegradationReason): String {
        return when (reason) {
            DegradationReason.LOW_MEMORY -> {
                "ℹ️ Running in reduced quality mode due to low memory.\n\nImages will be processed at lower resolution to prevent crashes."
            }
            DegradationReason.POOR_CONNECTION -> {
                "ℹ️ Poor network connection detected.\n\nProcessing may take longer than usual. Consider connecting to WiFi for better performance."
            }
            DegradationReason.METERED_CONNECTION -> {
                "ℹ️ Using mobile data.\n\nBe aware that image generation will consume your data plan. Switch to WiFi if available."
            }
            DegradationReason.HIGH_LOAD -> {
                "ℹ️ System under high load.\n\nSome features may be temporarily limited to ensure smooth operation."
            }
        }
    }
    
    enum class DegradationReason {
        LOW_MEMORY,
        POOR_CONNECTION,
        METERED_CONNECTION,
        HIGH_LOAD
    }
}
