package com.yunho.nanobanana.domain.usecase

import com.yunho.nanobanana.domain.model.AIParameters
import com.yunho.nanobanana.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for managing API key
 */
class ManageApiKeyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Get current API key
     */
    fun getApiKey(): Flow<String> = settingsRepository.getApiKey()
    
    /**
     * Save API key
     */
    suspend fun saveApiKey(apiKey: String) {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
        settingsRepository.saveApiKey(apiKey)
    }
}

/**
 * Use case for managing AI parameters
 */
class ManageAIParametersUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Get current AI parameters
     */
    fun getParameters(): Flow<AIParameters> = settingsRepository.getAIParameters()
    
    /**
     * Update AI parameters
     */
    suspend fun updateParameters(parameters: AIParameters) {
        settingsRepository.saveAIParameters(parameters)
    }
}
