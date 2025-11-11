# AI Integration Strategy & Data Flow

## Overview

This document provides a detailed strategy for AI integration in NanoBanana, focusing on the dual data streams of visual (image) and semantic (text & reasoning) outputs powered by the Gemini 2.5 Flash Model.

## Architecture Philosophy

### Core Principles

1. **Separation of Concerns**: AI logic is isolated in dedicated layers
2. **Modularity**: Independent components for different AI capabilities
3. **Testability**: All AI operations are mockable and testable
4. **Extensibility**: Easy to add new AI providers or models
5. **Type Safety**: Sealed classes for compile-time safety
6. **Reactive Streams**: Flow-based architecture for real-time updates

## Layer Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI Layer (Compose)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Components: ResultImage, TextOutput, LoadingEffects     │  │
│  │  State: Observes MainUiState from ViewModel              │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Presentation Layer                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  MainViewModel                                            │  │
│  │  - Manages MainUiState (single source of truth)          │  │
│  │  - Coordinates use cases                                 │  │
│  │  - Handles state transitions                             │  │
│  │  - Exposes StateFlow<MainUiState>                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Domain Layer                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Use Cases:                                               │  │
│  │  - GenerateAIContentUseCase (orchestrates AI generation) │  │
│  │  - ManageApiKeyUseCase (handles API key operations)      │  │
│  │  - ManageAIParametersUseCase (manages AI configuration)  │  │
│  │                                                           │  │
│  │  Models:                                                  │  │
│  │  - ImageGenerationRequest (input model)                  │  │
│  │  - AIGenerationResult (sealed class for type-safe        │  │
│  │    results: Success, Error, Loading)                     │  │
│  │  - AIParameters (temperature, maxTokens, etc.)           │  │
│  │  - AIOutputMode (IMAGE_ONLY, TEXT_ONLY, COMBINED)        │  │
│  │                                                           │  │
│  │  Repository Interfaces:                                  │  │
│  │  - AIRepository (contract for AI operations)             │  │
│  │  - SettingsRepository (contract for settings)            │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Data Layer                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Repositories (Implementations):                          │  │
│  │  - AIRepositoryImpl (basic implementation)                │  │
│  │  - EnhancedAIRepositoryImpl (with async architecture)     │  │
│  │  - SettingsRepositoryImpl                                 │  │
│  │                                                           │  │
│  │  Data Sources:                                            │  │
│  │  - GeminiAIDataSource (Gemini API integration)           │  │
│  │  - SettingsDataSource (SharedPreferences wrapper)         │  │
│  │                                                           │  │
│  │  Cache:                                                   │  │
│  │  - AIContentCache (LRU cache with compression)           │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    External Services                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Gemini 2.5 Flash API (via OkHttp)                        │  │
│  │  - Image generation endpoint                             │  │
│  │  - Text generation endpoint                              │  │
│  │  - Combined multi-modal endpoint                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow: Dual Streams

### Combined Generation Flow (Image + Text)

```
User Action: Tap Generate Button
        ↓
UI Layer: Trigger ViewModel.generateContent()
        ↓
ViewModel: Update state to Loading
        ↓
        ├────────────────────┬────────────────────┐
        ↓                    ↓                    ↓
   Use Case              Use Case            Use Case
   (Image)               (Text)            (Reasoning)
        ↓                    ↓                    ↓
   Repository           Repository          Repository
   (Image)               (Text)            (Metadata)
        ↓                    ↓                    ↓
   Data Source          Data Source        Data Source
   (Gemini API)         (Gemini API)      (Gemini API)
        │                    │                    │
        └────────────────────┴────────────────────┘
                             ↓
                    Parallel Processing
                    (Kotlin Coroutines)
                             ↓
                    ┌────────┴────────┐
                    ↓                 ↓
              Image Result      Text Result
                    ↓                 ↓
                 Cache            Cache
                    ↓                 ↓
              Flow Emit         Flow Emit
                    │                 │
                    └────────┬────────┘
                             ↓
                      ViewModel Collect
                             ↓
                    Update MainUiState
                    (Success with both)
                             ↓
                      UI Recompose
                             ↓
              Display Image + Text Together
```

### Error Handling in Dual Streams

```
Scenario 1: Both Success
    Image: ✓  Text: ✓  → Result: Success(image, text)

Scenario 2: Partial Success (Image fails)
    Image: ✗  Text: ✓  → Result: PartialSuccess(text only)

Scenario 3: Partial Success (Text fails)
    Image: ✓  Text: ✗  → Result: PartialSuccess(image only)

Scenario 4: Both Fail
    Image: ✗  Text: ✗  → Result: Error(message)

Scenario 5: Retry Logic
    Initial: Image ✗  Text: ✗
    Retry 1: Image ✓  Text: ✗
    Retry 2: Image ✓  Text: ✓
    → Result: Success(image, text) with retry metadata
```

## AI Service Abstraction

### Interface Design

```kotlin
// Domain layer - pure interfaces
interface AIRepository {
    suspend fun generateImage(request: ImageGenerationRequest): Flow<AIGenerationResult>
    suspend fun generateText(prompt: String, parameters: AIParameters): Flow<String>
    suspend fun generateCombined(request: ImageGenerationRequest): Flow<CombinedResult>
}

// Data layer - implementations
class EnhancedAIRepositoryImpl(
    private val dataSource: AIDataSource,
    private val cache: AIContentCache,
    private val retryHandler: RetryHandler,
    private val telemetry: AITelemetry
) : AIRepository {
    // Implementation with parallel processing, caching, retry logic
}
```

### Multi-Provider Support (Future)

```
┌─────────────────────────────────────────┐
│         AIRepository Interface           │
└────────────────┬────────────────────────┘
                 │
    ┌────────────┼────────────┬───────────────┐
    ↓            ↓            ↓               ↓
GeminiRepo   OpenAIRepo   AnthropicRepo   LocalRepo
    │            │            │               │
    ↓            ↓            ↓               ↓
GeminiDS     OpenAIDS     AnthropicDS     LocalDS
    │            │            │               │
    ↓            ↓            ↓               ↓
Gemini API   OpenAI API   Claude API    Local Model
```

## State Management

### MainUiState Structure

```kotlin
data class MainUiState(
    // Settings
    val apiKey: String = "",
    val aiParameters: AIParameters = AIParameters.default(),
    
    // Input
    val selectedImages: List<Bitmap> = emptyList(),
    val prompt: String = "",
    val selectedStyle: Int = 0,
    
    // Generation State
    val generationState: GenerationState = GenerationState.Idle,
    
    // Output
    val generatedImage: Bitmap? = null,
    val generatedText: String? = null,
    val reasoning: AIReasoning? = null,
    
    // Metadata
    val timestamp: Long? = null,
    val retryCount: Int = 0,
    val processingTime: Long = 0
)

sealed class GenerationState {
    object Idle : GenerationState()
    data class Loading(
        val progress: Float = 0f,
        val stage: GenerationStage = GenerationStage.PREPARING,
        val reasoning: AIReasoning? = null
    ) : GenerationState()
    data class Success(
        val image: Bitmap?,
        val text: String?,
        val metadata: GenerationMetadata
    ) : GenerationState()
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val recoverable: Boolean = true
    ) : GenerationState()
}
```

### State Transitions

```
Idle → Loading → Success
  ↓       ↓
  └──────> Error
           ↓
        (retry)
           ↓
        Loading → Success
```

## Asynchronous Processing

### Parallel Pipeline Architecture

```kotlin
suspend fun generateCombined(request: ImageGenerationRequest): CombinedResult {
    return coroutineScope {
        // Parallel deferred tasks
        val imageDeferred = async(Dispatchers.IO) {
            generateImageWithRetry(request)
        }
        
        val textDeferred = async(Dispatchers.IO) {
            generateTextWithRetry(request.prompt)
        }
        
        val reasoningDeferred = async(Dispatchers.IO) {
            generateReasoningWithRetry(request)
        }
        
        // Await all results
        val image = imageDeferred.await()
        val text = textDeferred.await()
        val reasoning = reasoningDeferred.await()
        
        // Combine results
        CombinedResult(
            image = image,
            text = text,
            reasoning = reasoning,
            timestamp = System.currentTimeMillis()
        )
    }
}
```

### Task Prioritization

```
Priority Levels:
1. USER_INTERACTIVE (UI blocking) - Highest
2. USER_INITIATED (User action) - High
3. BACKGROUND_HIGH (Important background) - Medium
4. BACKGROUND_NORMAL (Regular background) - Low
5. BACKGROUND_LOW (Deferrable) - Lowest

Example Assignment:
- Image generation request: USER_INITIATED
- Text generation request: USER_INITIATED
- Cache warming: BACKGROUND_NORMAL
- Telemetry upload: BACKGROUND_LOW
```

## Caching Strategy

### LRU Cache with Compression

```
┌────────────────────────────────────────┐
│         AIContentCache                  │
├────────────────────────────────────────┤
│  Key: Hash(prompt + images + params)   │
│  Value: CachedContent                   │
│    - Compressed Image (JPEG 60-70%)    │
│    - Text String                        │
│    - Metadata                           │
│    - Timestamp                          │
├────────────────────────────────────────┤
│  Eviction Policy: LRU                   │
│  Max Size: Device-dependent             │
│    - HIGH_END: 50 entries               │
│    - MID: 30 entries                    │
│    - LOW_END: 15 entries                │
└────────────────────────────────────────┘
```

### Cache Hit Flow

```
Request → Hash → Cache Lookup
                     ↓
              ┌──────┴──────┐
              ↓             ↓
          Hit (Fast)      Miss (Slow)
              ↓             ↓
        Return Cache    API Call
              ↓             ↓
          UI Update    Store in Cache
                            ↓
                        UI Update
```

## Retry & Error Handling

### Exponential Backoff

```
Retry Strategy:
- Max Retries: 3
- Base Delay: 1 second
- Multiplier: 2
- Jitter: ±20%

Timeline:
Attempt 1: Immediate
Attempt 2: 1s + jitter (0.8-1.2s)
Attempt 3: 2s + jitter (1.6-2.4s)
Attempt 4: 4s + jitter (3.2-4.8s)

Give up after 4 attempts
```

### Error Classification

```kotlin
sealed class AIError {
    // Transient (Retry recommended)
    object NetworkTimeout : AIError()
    object RateLimited : AIError()
    object ServerError : AIError()
    
    // Permanent (Don't retry)
    data class InvalidInput(val reason: String) : AIError()
    data class AuthenticationError(val reason: String) : AIError()
    data class QuotaExceeded(val resetTime: Long) : AIError()
    
    // Unknown (Retry once)
    data class Unknown(val throwable: Throwable) : AIError()
}
```

## Device Adaptation

### Tier Detection

```kotlin
enum class DeviceTier {
    HIGH_END,   // Flagship devices (2022+)
    MID,        // Mid-range devices (2020+)
    LOW_END     // Budget devices (2019+)
}

fun detectDeviceTier(context: Context): DeviceTier {
    val activityManager = context.getSystemService(ActivityManager::class.java)
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    
    val totalRam = memoryInfo.totalMem / (1024 * 1024) // MB
    val cores = Runtime.getRuntime().availableProcessors()
    
    return when {
        totalRam >= 8000 && cores >= 8 -> DeviceTier.HIGH_END
        totalRam >= 4000 && cores >= 6 -> DeviceTier.MID
        else -> DeviceTier.LOW_END
    }
}
```

### Quality Adaptation

```
HIGH_END:
- Image quality: 100%
- Cache size: 50 entries
- Parallel tasks: 3
- Retry attempts: 3

MID:
- Image quality: 85%
- Cache size: 30 entries
- Parallel tasks: 2
- Retry attempts: 2

LOW_END:
- Image quality: 70%
- Cache size: 15 entries
- Parallel tasks: 1
- Retry attempts: 1
```

## Telemetry & Monitoring

### Metrics Collection

```kotlin
data class AITelemetryMetrics(
    // Latency
    val avgImageGenTime: Long,      // ms
    val avgTextGenTime: Long,       // ms
    val avgCombinedGenTime: Long,   // ms
    val p50Latency: Long,
    val p95Latency: Long,
    val p99Latency: Long,
    
    // Success Rate
    val totalRequests: Int,
    val successfulRequests: Int,
    val failedRequests: Int,
    val partialSuccesses: Int,
    
    // Cache Performance
    val cacheHitRate: Float,        // 0.0 - 1.0
    val cacheMissRate: Float,
    val cacheEvictions: Int,
    
    // Retry Statistics
    val retriesPerformed: Int,
    val avgRetriesPerRequest: Float,
    
    // Error Breakdown
    val networkErrors: Int,
    val timeoutErrors: Int,
    val authErrors: Int,
    val quotaErrors: Int,
    val unknownErrors: Int,
    
    // Resource Usage
    val avgMemoryUsage: Long,       // bytes
    val peakMemoryUsage: Long,
    val avgCpuUsage: Float          // 0.0 - 1.0
)
```

## Security Considerations

### API Key Management

```
Storage: SharedPreferences (Encrypted)
    ↓
Access: SettingsDataSource only
    ↓
Usage: Injected into AIDataSource
    ↓
Never: Logged or exposed in errors
```

### Input Sanitization

```kotlin
fun sanitizePrompt(prompt: String): String {
    return prompt
        .trim()
        .take(MAX_PROMPT_LENGTH)
        .filter { it.isLetterOrDigit() || it.isWhitespace() || it in ALLOWED_PUNCTUATION }
}

fun validateImage(bitmap: Bitmap): ValidationResult {
    return when {
        bitmap.width > MAX_IMAGE_WIDTH -> ValidationResult.TooWide
        bitmap.height > MAX_IMAGE_HEIGHT -> ValidationResult.TooTall
        bitmap.byteCount > MAX_IMAGE_SIZE -> ValidationResult.TooLarge
        else -> ValidationResult.Valid
    }
}
```

## Future Enhancements

### Model Versioning

```kotlin
enum class GeminiModel(val endpoint: String, val capabilities: Set<Capability>) {
    FLASH_2_5("gemini-2.5-flash", setOf(IMAGE, TEXT, REASONING)),
    PRO_2_0("gemini-2.0-pro", setOf(IMAGE, TEXT, REASONING, ADVANCED)),
    ULTRA_1_5("gemini-1.5-ultra", setOf(IMAGE, TEXT, REASONING, ADVANCED, LONG_CONTEXT))
}

// Allow model selection per request
data class AIParameters(
    val model: GeminiModel = GeminiModel.FLASH_2_5,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    // ...
)
```

### Batch Processing

```kotlin
suspend fun generateBatch(requests: List<ImageGenerationRequest>): Flow<BatchResult> = flow {
    requests.chunked(BATCH_SIZE).forEach { chunk ->
        val results = chunk.map { request ->
            async { generate(request) }
        }.awaitAll()
        
        emit(BatchResult(results))
    }
}
```

### Streaming Responses

```kotlin
suspend fun generateTextStreaming(prompt: String): Flow<TextChunk> = flow {
    // SSE or WebSocket connection
    val stream = openStreamConnection(prompt)
    
    stream.collect { chunk ->
        emit(TextChunk(chunk))
    }
}
```

## Testing Strategy

### Unit Tests
- Mock AIDataSource in repository tests
- Test state transitions in ViewModel
- Test caching logic independently
- Test retry mechanism with controlled failures

### Integration Tests
- Test complete generation flow
- Test parallel processing coordination
- Test cache integration
- Test error recovery flows

### UI Tests
- Test loading state rendering
- Test text-image synchronization
- Test error display
- Test accessibility compliance

---

**Document Version**: 1.0  
**Last Updated**: November 2024  
**Next Review**: January 2025  
**Owner**: NanoBanana Architecture Team
