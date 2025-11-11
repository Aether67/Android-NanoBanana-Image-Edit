package com.yunho.nanobanana.domain.repository

import com.yunho.nanobanana.domain.model.AIParameters
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for application settings
 */
interface SettingsRepository {
    /**
     * Get API key as Flow for reactive updates
     */
    fun getApiKey(): Flow<String>
    
    /**
     * Save API key
     */
    suspend fun saveApiKey(apiKey: String)
    
    /**
     * Get AI parameters
     */
    fun getAIParameters(): Flow<AIParameters>
    
    /**
     * Save AI parameters
     */
    suspend fun saveAIParameters(parameters: AIParameters)
}
