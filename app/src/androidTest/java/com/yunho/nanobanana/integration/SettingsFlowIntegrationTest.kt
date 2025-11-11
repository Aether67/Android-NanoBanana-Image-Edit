package com.yunho.nanobanana.integration

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yunho.nanobanana.data.datasource.SettingsDataSource
import com.yunho.nanobanana.data.repository.SettingsRepositoryImpl
import com.yunho.nanobanana.domain.model.AIParameters
import com.yunho.nanobanana.domain.model.AIOutputStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * Integration test for Settings data flow
 * Tests the complete flow from DataSource -> Repository -> Flow emission
 */
@RunWith(AndroidJUnit4::class)
class SettingsFlowIntegrationTest {
    
    private lateinit var context: Context
    private lateinit var dataSource: SettingsDataSource
    private lateinit var repository: SettingsRepositoryImpl
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Clear previous data
        context.getSharedPreferences("nanobanana_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        
        dataSource = SettingsDataSource(context)
        repository = SettingsRepositoryImpl(dataSource)
    }
    
    @Test
    fun apiKey_saveAndRetrieve_flowEmitsCorrectValue() = runTest {
        // Given
        val testApiKey = "test-api-key-12345"
        
        // When
        repository.saveApiKey(testApiKey)
        
        // Then
        val retrievedKey = repository.getApiKey().first()
        assertEquals(testApiKey, retrievedKey)
    }
    
    @Test
    fun aiParameters_saveAndRetrieve_flowEmitsCorrectValue() = runTest {
        // Given
        val testParameters = AIParameters(
            creativityLevel = 0.9f,
            detailLevel = 5,
            reasoningDepth = 3,
            outputStyle = AIOutputStyle.CREATIVE
        )
        
        // When
        repository.saveAIParameters(testParameters)
        
        // Then
        val retrievedParams = repository.getAIParameters().first()
        assertEquals(testParameters, retrievedParams)
    }
    
    @Test
    fun multipleUpdates_flowEmitsLatestValue() = runTest {
        // Given
        val key1 = "key-1"
        val key2 = "key-2"
        val key3 = "key-3"
        
        // When
        repository.saveApiKey(key1)
        repository.saveApiKey(key2)
        repository.saveApiKey(key3)
        
        // Then - should get the latest value
        val retrievedKey = repository.getApiKey().first()
        assertEquals(key3, retrievedKey)
    }
    
    @Test
    fun emptyApiKey_returnsEmptyString() = runTest {
        // When - no API key saved
        val retrievedKey = repository.getApiKey().first()
        
        // Then
        assertEquals("", retrievedKey)
    }
    
    @Test
    fun defaultParameters_returnsDefaultValues() = runTest {
        // When - no parameters saved
        val retrievedParams = repository.getAIParameters().first()
        
        // Then
        assertEquals(AIParameters(), retrievedParams)
    }
}
