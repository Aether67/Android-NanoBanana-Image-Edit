# Asynchronous Architecture Documentation

## Overview

The NanoBanana app features a highly scalable and resilient asynchronous architecture designed to support simultaneous image editing and text generation operations powered by the Gemini 2.5 Flash Image Preview AI model.

## Core Components

### 1. AIContentCache
**Location:** `app/src/main/java/com/yunho/nanobanana/data/cache/AIContentCache.kt`

Thread-safe LRU cache with compressed storage for optimal memory usage.

**Features:**
- Separate caches for images and text with independent size limits
- JPEG compression (configurable quality, default 85%)
- Automatic LRU eviction when cache is full
- Thread-safe read/write operations with ReentrantReadWriteLock
- Comprehensive telemetry (hit/miss rates, eviction counts)

**Configuration:**
```kotlin
AIContentCache(
    maxImageCacheSize = 20,  // Max images to cache
    maxTextCacheSize = 50,   // Max text responses
    compressionQuality = 85  // JPEG quality (0-100)
)
```

### 2. TaskPrioritizationManager
**Location:** `app/src/main/java/com/yunho/nanobanana/async/TaskPrioritizationManager.kt`

Adaptive task scheduling with 5 priority levels ensuring UI responsiveness.

**Priority Levels:**
- `CRITICAL`: Immediate UI feedback (preview updates, incremental output)
- `HIGH`: Quick AI responses, cache lookups
- `NORMAL`: Standard AI generation
- `LOW`: Background computations, batch processing
- `BACKGROUND`: Telemetry, cleanup

**Usage:**
```kotlin
taskManager.submitTask(TaskPriority.CRITICAL) {
    // High-priority task that affects UI
}
```

### 3. RetryHandler
**Location:** `app/src/main/java/com/yunho/nanobanana/async/RetryHandler.kt`

Exponential backoff retry mechanism with jitter for AI API failures.

**Configuration:**
```kotlin
RetryConfig(
    maxAttempts = 3,          // Max retry attempts
    initialDelayMs = 1000,    // Initial delay
    maxDelayMs = 10000,       // Maximum delay cap
    multiplier = 2.0,         // Exponential multiplier
    jitterFactor = 0.1        // Random jitter (10%)
)
```

**Backoff Formula:**
```
delay = min(initialDelay * (multiplier ^ attemptNumber), maxDelay) + jitter
```

### 4. AITelemetry
**Location:** `app/src/main/java/com/yunho/nanobanana/async/AITelemetry.kt`

Comprehensive monitoring system tracking:
- Request latency (with histogram buckets)
- Success/failure rates
- Error distribution by type
- Cache hit/miss ratios
- UI frame drop rates

**Metrics Tracked:**
- Total requests, successful requests, failed requests
- Average latency, latency distribution
- Error types and counts
- Frame rate performance

### 5. DeviceCapabilityDetector
**Location:** `app/src/main/java/com/yunho/nanobanana/async/DeviceCapabilityDetector.kt`

Detects device performance tier and recommends optimal settings.

**Performance Tiers:**
- `HIGH_END`: 8GB+ RAM, 8+ CPU cores → Quality: 95%, Cache: 30 items, Max concurrent: 4
- `MID_RANGE`: 3-8GB RAM → Quality: 85%, Cache: 20 items, Max concurrent: 3
- `LOW_END`: <3GB RAM or low-memory device → Quality: 75%, Cache: 10 items, Max concurrent: 2

### 6. ParallelAIPipeline
**Location:** `app/src/main/java/com/yunho/nanobanana/async/ParallelAIPipeline.kt`

Orchestrates simultaneous image and text generation using Kotlin coroutines.

**Features:**
- Parallel execution with `async`/`await`
- Progress callbacks for incremental UI updates
- Automatic retry integration
- Cancellation support

**Example:**
```kotlin
pipeline.processParallel(
    imageOperation = { generateImage() },
    textOperation = { generateText() },
    priority = TaskPriority.NORMAL
)
```

### 7. BitmapOptimizer
**Location:** `app/src/main/java/com/yunho/nanobanana/async/BitmapOptimizer.kt`

Adaptive bitmap processing based on device capabilities.

**Optimizations:**
- Dynamic quality adjustment (60-95% based on device tier)
- Automatic resolution scaling (512px - 2048px max)
- Memory-efficient compression
- Target-specific optimization (display, cache, thumbnail)

### 8. GracefulDegradationManager
**Location:** `app/src/main/java/com/yunho/nanobanana/async/GracefulDegradationManager.kt`

Automatically adjusts features based on device constraints and API limits.

**Degradation Modes:**

#### NORMAL
- Full features enabled
- Parallel processing
- Real-time reasoning
- Max resolution: 2048px
- 3 retry attempts

#### REDUCED
- Parallel processing enabled
- Real-time reasoning disabled
- Max resolution: 1536px
- 2 retry attempts
- Slightly lower quality

#### MINIMAL
- Sequential processing only
- Max resolution: 1024px
- 1 retry attempt
- Basic features only

#### EMERGENCY
- Minimal operations
- No caching (save memory)
- Max resolution: 512px
- No retries
- Critical operations only

**Triggers:**
- Memory pressure detection
- Consecutive API failures (rate limits)
- Low-end device detection

## Architecture Flow

### Request Processing Flow

```
User Request
    ↓
EnhancedAIRepositoryImpl
    ↓
1. Evaluate Degradation Mode
    ↓
2. Check Device Memory Pressure
    ↓
3. Cache Lookup (if enabled)
    ↓
4. Parallel/Sequential Processing
    ↓
    ├─→ Image Pipeline
    │   ├─→ Task Prioritization
    │   ├─→ Retry Handler
    │   ├─→ AI Data Source
    │   └─→ Bitmap Optimizer
    │
    └─→ Text Pipeline
        ├─→ Task Prioritization
        ├─→ Retry Handler
        └─→ AI Data Source
    ↓
5. Cache Result (if successful)
    ↓
6. Update Telemetry
    ↓
7. Adjust Degradation Mode
    ↓
Return Result to UI
```

### Error Handling Flow

```
API Failure
    ↓
Record in Telemetry
    ↓
Retry Handler (with backoff)
    ↓
Update Degradation Manager
    ↓
Evaluate Mode Adjustment
    ↓
    ├─→ Success: Recover mode
    └─→ Persistent failure: Degrade further
```

## Performance Optimizations

### 1. Memory Management
- Compressed bitmap caching (JPEG at 60-95% quality)
- LRU eviction prevents unbounded growth
- Emergency mode disables cache under extreme pressure

### 2. CPU Optimization
- Concurrent task limit based on CPU cores
- Priority-based scheduling prevents UI blocking
- Sequential processing in degraded modes

### 3. Network Optimization
- Exponential backoff prevents API hammering
- Automatic retry for transient failures
- Rate limit detection and graceful degradation

### 4. UI Responsiveness
- Critical tasks prioritized for immediate feedback
- Incremental progress updates
- Frame rate monitoring

## Testing Strategy

### Unit Tests
**Location:** `app/src/test/java/com/yunho/nanobanana/async/`

- `AIContentCacheTest`: Cache functionality, LRU eviction
- `RetryHandlerTest`: Retry logic, backoff calculation
- `TaskPrioritizationManagerTest`: Priority queue, concurrency
- `AITelemetryTest`: Metrics tracking, reporting

### Integration Testing
Test complete flows with mocked AI data sources:
1. Parallel processing with cache misses
2. Degradation mode transitions
3. Retry scenarios with eventual success/failure
4. Memory pressure simulation

## Configuration

### Default Settings

```kotlin
// Cache
maxImageCacheSize = device-dependent (10-30)
maxTextCacheSize = device-dependent (20-60)
compressionQuality = device-dependent (75-95)

// Task Prioritization
maxConcurrentTasks = CPU cores (1-4)

// Retry
maxAttempts = 3 (degradation-dependent)
initialDelayMs = 1000
maxDelayMs = 10000

// Bitmap Optimization
maxImageResolution = degradation-dependent (512-2048)
```

### Device-Specific Tuning

The system automatically configures based on:
- Total RAM
- CPU core count
- Low-memory device flag
- Current memory pressure

## Monitoring & Diagnostics

### Telemetry Access

```kotlin
val repository = enhancedAIRepository

// Cache statistics
val cacheStats = repository.getCacheStats()
println("Image cache hit rate: ${cacheStats.imageHitRate * 100}%")

// Telemetry report
val report = repository.getTelemetryReport()
println("Success rate: ${report.successRate}%")
println("Average latency: ${report.averageLatencyMs}ms")

// Device capabilities
val capabilities = repository.getDeviceCapabilities()
println("Performance tier: ${capabilities.performanceTier}")

// Degradation status
val degradation = repository.getDegradationStatus()
println("Current mode: ${degradation.mode}")
```

### Logging

All components use Android `Log` with appropriate tags:
- `AIContentCache`: Cache operations
- `TaskPrioritization`: Task scheduling
- `RetryHandler`: Retry attempts
- `AITelemetry`: Metrics and summaries
- `DeviceCapability`: Device detection
- `ParallelAIPipeline`: Pipeline execution
- `BitmapOptimizer`: Optimization results
- `GracefulDegradation`: Mode transitions
- `EnhancedAIRepository`: High-level operations

## Best Practices

### 1. For Developers

- Use appropriate priority levels for tasks
- Handle memory pressure warnings
- Monitor telemetry for performance insights
- Test on low-end devices

### 2. For Users

- Clear cache periodically to reclaim space
- Understand degradation mode messages
- Expect quality reduction on older devices

## Future Enhancements

1. **Persistent Cache**: Save cache to disk for session recovery
2. **Smart Prefetching**: Predict and preload likely requests
3. **Bandwidth Detection**: Adapt to network conditions
4. **Machine Learning**: Learn optimal settings per device
5. **Advanced Telemetry**: Push metrics to analytics service

## Troubleshooting

### High Memory Usage
- Check cache sizes in telemetry
- Verify degradation mode is activating
- Clear cache manually if needed

### Slow Performance
- Check device performance tier
- Verify degradation mode settings
- Review concurrent task limits

### Frequent Failures
- Check error distribution in telemetry
- Verify API key validity
- Check for rate limiting (HTTP 429)

## References

- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Android Performance Best Practices](https://developer.android.com/topic/performance)
- [Material Design - Motion](https://m3.material.io/styles/motion)
