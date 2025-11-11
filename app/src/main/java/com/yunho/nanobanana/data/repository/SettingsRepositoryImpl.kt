package com.yunho.nanobanana.data.repository

import com.yunho.nanobanana.data.datasource.SettingsDataSource
import com.yunho.nanobanana.domain.model.AIParameters
import com.yunho.nanobanana.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of SettingsRepository
 * Manages application settings persistence
 */
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataSource: SettingsDataSource
) : SettingsRepository {
    
    override fun getApiKey(): Flow<String> = settingsDataSource.getApiKeyFlow()
    
    override suspend fun saveApiKey(apiKey: String) {
        settingsDataSource.saveApiKey(apiKey)
    }
    
    override fun getAIParameters(): Flow<AIParameters> = settingsDataSource.getParametersFlow()
    
    override suspend fun saveAIParameters(parameters: AIParameters) {
        settingsDataSource.saveParameters(parameters)
    }
}
