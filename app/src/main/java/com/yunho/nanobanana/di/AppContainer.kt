package com.yunho.nanobanana.di

import android.content.Context
import com.yunho.nanobanana.data.datasource.AIDataSource
import com.yunho.nanobanana.data.datasource.GeminiAIDataSource
import com.yunho.nanobanana.data.datasource.SettingsDataSource
import com.yunho.nanobanana.data.repository.AIRepositoryImpl
import com.yunho.nanobanana.data.repository.SettingsRepositoryImpl
import com.yunho.nanobanana.domain.repository.AIRepository
import com.yunho.nanobanana.domain.repository.SettingsRepository
import com.yunho.nanobanana.domain.usecase.GenerateAIContentUseCase
import com.yunho.nanobanana.domain.usecase.ManageAIParametersUseCase
import com.yunho.nanobanana.domain.usecase.ManageApiKeyUseCase
import com.yunho.nanobanana.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Manual dependency injection container
 * Manages creation and lifecycle of dependencies
 * 
 * For a larger app, consider using Hilt or Koin
 */
class AppContainer(private val context: Context) {
    
    // Data sources
    private val settingsDataSource: SettingsDataSource by lazy {
        SettingsDataSource(context)
    }
    
    private fun createAIDataSource(): AIDataSource {
        // Get API key from settings
        val apiKey = runBlocking {
            settingsDataSource.getApiKeyFlow().first()
        }
        return GeminiAIDataSource(apiKey)
    }
    
    // Repositories
    private val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(settingsDataSource)
    }
    
    private val aiRepository: AIRepository by lazy {
        AIRepositoryImpl(createAIDataSource())
    }
    
    // Use cases
    private val generateAIContentUseCase: GenerateAIContentUseCase by lazy {
        GenerateAIContentUseCase(aiRepository)
    }
    
    private val manageApiKeyUseCase: ManageApiKeyUseCase by lazy {
        ManageApiKeyUseCase(settingsRepository)
    }
    
    private val manageParametersUseCase: ManageAIParametersUseCase by lazy {
        ManageAIParametersUseCase(settingsRepository)
    }
    
    // ViewModels
    fun createMainViewModel(): MainViewModel {
        return MainViewModel(
            generateContentUseCase = generateAIContentUseCase,
            manageApiKeyUseCase = manageApiKeyUseCase,
            manageParametersUseCase = manageParametersUseCase
        )
    }
}
