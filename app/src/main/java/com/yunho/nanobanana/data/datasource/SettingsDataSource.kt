package com.yunho.nanobanana.data.datasource

import android.content.Context
import android.content.SharedPreferences
import com.yunho.nanobanana.domain.model.AIParameters
import com.yunho.nanobanana.domain.model.AIOutputStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Data source for settings persistence
 * Uses SharedPreferences with Flow for reactive updates
 */
class SettingsDataSource @Inject constructor(
    context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("nanobanana_settings", Context.MODE_PRIVATE)
    
    private val _apiKeyFlow = MutableStateFlow(getStoredApiKey())
    private val _parametersFlow = MutableStateFlow(getStoredParameters())
    
    fun getApiKeyFlow(): Flow<String> = _apiKeyFlow.asStateFlow()
    
    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply()
        _apiKeyFlow.value = apiKey
    }
    
    fun getParametersFlow(): Flow<AIParameters> = _parametersFlow.asStateFlow()
    
    fun saveParameters(parameters: AIParameters) {
        sharedPreferences.edit().apply {
            putFloat(KEY_CREATIVITY, parameters.creativityLevel)
            putInt(KEY_DETAIL, parameters.detailLevel)
            putInt(KEY_REASONING, parameters.reasoningDepth)
            putString(KEY_STYLE, parameters.outputStyle.name)
            apply()
        }
        _parametersFlow.value = parameters
    }
    
    private fun getStoredApiKey(): String {
        return sharedPreferences.getString(KEY_API_KEY, "") ?: ""
    }
    
    private fun getStoredParameters(): AIParameters {
        return AIParameters(
            creativityLevel = sharedPreferences.getFloat(KEY_CREATIVITY, 0.7f),
            detailLevel = sharedPreferences.getInt(KEY_DETAIL, 3),
            reasoningDepth = sharedPreferences.getInt(KEY_REASONING, 2),
            outputStyle = AIOutputStyle.valueOf(
                sharedPreferences.getString(KEY_STYLE, AIOutputStyle.BALANCED.name) 
                    ?: AIOutputStyle.BALANCED.name
            )
        )
    }
    
    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_CREATIVITY = "creativity_level"
        private const val KEY_DETAIL = "detail_level"
        private const val KEY_REASONING = "reasoning_depth"
        private const val KEY_STYLE = "output_style"
    }
}
