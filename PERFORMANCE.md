# Performance Optimization Documentation

## Overview

This document describes the comprehensive performance optimizations implemented in the NanoBanana Image Edit application, focusing on asynchronous processing, caching, resource management, and resilient error handling.

## Key Features

### 1. LRU Image Caching

**Location**: `app/src/main/java/com/yunho/nanobanana/performance/ImageCache.kt`

The `ImageCache` class provides an LRU (Least Recently Used) cache for generated images:

- **Memory Management**: Uses 1/8 of available device memory for cache
- **Smart Eviction**: Automatically removes least-used images when memory limit reached
- **Key Generation**: MD5 hash of prompt + image dimensions for consistent cache keys
- **Metrics**: Tracks hit/miss rates, eviction counts

**Benefits**:
- Instant results for repeated requests
- Reduced API calls and costs
- Lower network usage
- Better offline experience

### 2. Text Response Caching

**Location**: `app/src/main/java/com/yunho/nanobanana/performance/TextCache.kt`

Caches text responses with Time-To-Live (TTL):

- **TTL**: 30 minutes default expiration
- **Thread-Safe**: ConcurrentHashMap for concurrent access
- **Automatic Cleanup**: Expired entries removed on access
- **Manual Cleanup**: `cleanupExpired()` for background cleanup

### 3. Task Prioritization

**Location**: `app/src/main/java/com/yunho/nanobanana/performance/TaskPriority.kt`

Three-tier priority system for optimal resource allocation:

- **HIGH Priority**: UI-critical operations (Main thread + immediate dispatcher)
- **MEDIUM Priority**: Standard operations like API calls (IO dispatcher)
- **LOW Priority**: Background tasks (Single-threaded, low priority executor)

**Benefits**:
- Smooth UI even during heavy processing
- Prevents UI jank and ANR (Application Not Responding)
- Efficient CPU usage on lower-end devices

### 4. Performance Telemetry

**Location**: `app/src/main/java/com/yunho/nanobanana/performance/PerformanceMetrics.kt`

Comprehensive metrics tracking:

**Tracked Metrics**:
- API call success/failure rates
- Response latencies (average and P95)
- Cache hit rates
- Memory usage over time
- Error type breakdown

**Features**:
- Thread-safe atomic counters
- Periodic memory snapshots (every 30 seconds)
- Detailed logging with `logSummary()`
- Export metrics via `getSummary()`

**Use Cases**:
- Performance optimization decisions
- Identifying bottlenecks
- Monitoring resource usage
- A/B testing different approaches

### 5. Exponential Backoff Retry with Circuit Breaker

**Location**: `app/src/main/java/com/yunho/nanobanana/performance/RetryPolicy.kt`

Enhanced retry mechanism for resilient API calls:

**Retry Logic**:
- Exponential backoff (configurable multiplier, default 2.0x)
- Random jitter to prevent thundering herd
- Max delay cap to avoid excessive waits
- Configurable max attempts (default: 3)

**Circuit Breaker**:
- Opens after 5 consecutive failures
- Prevents cascading failures
- 60-second timeout before retry
- Automatic recovery on success

**Benefits**:
- Graceful handling of temporary failures
- Reduced server load during outages
- Better user experience during network issues

### 6. Resource Monitoring

**Location**: `app/src/main/java/com/yunho/nanobanana/performance/ResourceMonitor.kt`

System resource detection for graceful degradation:

**Monitored Resources**:
- Available memory (MB)
- Low memory state
- Network connectivity status
- Connection quality (WiFi/Cellular/Bandwidth)
- Metered connection detection

**Adaptive Quality**:
- High quality (95%) on good devices with WiFi
- Medium quality (85%) on moderate devices
- Low quality (75%) on low-memory or poor network

**Benefits**:
- Prevents crashes on low-memory devices
- Adapts to network conditions
- Respects user's data plan

### 7. Enhanced Error Messages

**Location**: `app/src/main/java/com/yunho/nanobanana/performance/ErrorMessages.kt`

User-friendly error messages for all failure scenarios:

**Error Types Handled**:
- Network errors (connection, DNS, firewall)
- Timeout errors (slow network, server busy)
- Authentication errors (invalid API key, unauthorized)
- Rate limiting (quota exceeded, too many requests)
- Server errors (500, 503)
- Memory errors (OOM)
- Circuit breaker (service unavailable)

**Features**:
- Clear problem description
- Actionable tips for resolution
- Appropriate emoji for visual clarity
- Short titles for notifications

### 8. Integration in NanoBananaService

**Location**: `app/src/main/java/com/yunho/nanobanana/NanoBananaService.kt`

The service integrates all performance features:

**Flow**:
1. Check cache first (instant result if hit)
2. Check resource constraints (memory, network)
3. Adjust quality based on resources
4. Use retry policy with circuit breaker
5. Track metrics (latency, success/failure)
6. Cache successful results
7. Return enhanced error messages on failure

**Dispatcher Usage**:
- MEDIUM priority for API calls (non-blocking)
- IO dispatcher for network operations

## Performance Impact

### Expected Improvements

1. **Response Time**:
   - Cache hits: Near-instant (< 100ms)
   - Cache misses: Same as before, but with retry resilience

2. **API Calls**:
   - 30-50% reduction through caching (varies by usage pattern)
   - Intelligent retry reduces wasted calls

3. **Memory Usage**:
   - Controlled via LRU eviction
   - Adaptive quality prevents OOM crashes
   - Maximum 12.5% of available memory for cache

4. **Network Usage**:
   - Reduced through caching
   - Adaptive quality on metered connections
   - Fewer failed requests through circuit breaker

5. **User Experience**:
   - Instant results for repeated requests
   - Clear error messages with actionable tips
   - Graceful degradation on low-end devices
   - No UI jank during processing

## Configuration

### Cache Sizes

```kotlin
// Image cache: 1/8 of available memory
val cacheSize = maxMemory / 8

// Text cache: No size limit, but 30-minute TTL
val ttlMillis = 30 * 60 * 1000L
```

### Retry Policy

```kotlin
maxAttempts = 3
initialDelayMs = 1000
maxDelayMs = 10000
backoffMultiplier = 2.0
```

### Circuit Breaker

```kotlin
CIRCUIT_BREAKER_THRESHOLD = 5  // consecutive failures
CIRCUIT_BREAKER_TIMEOUT_MS = 60_000L  // 1 minute
```

### Telemetry

```kotlin
// Memory snapshots every 30 seconds
delay(30_000)

// Keep last 100 latencies and snapshots
```

## Testing

Unit tests are provided for:

1. **ImageCache** (`ImageCacheTest.kt`)
   - Get/put cycle
   - Cache statistics
   - Eviction behavior

2. **TextCache** (`TextCacheTest.kt`)
   - Get/put operations
   - Clear functionality
   - TTL expiration

3. **ErrorMessages** (`ErrorMessagesTest.kt`)
   - Network error messages
   - Timeout error messages
   - API key error messages
   - Circuit breaker messages
   - Degraded mode messages

## Monitoring and Debugging

### View Performance Metrics

```kotlin
val service = NanoBananaService(context)
val metrics = service.getMetrics()

println("API Success Rate: ${metrics.successRate * 100}%")
println("Average Latency: ${metrics.averageLatencyMs}ms")
println("Cache Hit Rate: ${metrics.cacheHitRate * 100}%")
```

### View Cache Statistics

```kotlin
val cache = ImageCache.getInstance()
val stats = cache.getStats()

println("Cache Hit Rate: ${stats.hitRate * 100}%")
println("Current Size: ${stats.currentSize} KB / ${stats.maxSize} KB")
println("Evictions: ${stats.evictionCount}")
```

### Check Resource Status

```kotlin
val monitor = ResourceMonitor(context)
val status = monitor.getResourceStatus()

println("Available Memory: ${status.availableMemoryMB} MB")
println("Connection Quality: ${status.connectionQuality}")
println("Recommended Quality: ${status.recommendedQuality}%")
```

## Future Enhancements

Potential improvements:

1. **Persistent Cache**: Store cache to disk for cross-session persistence
2. **Prefetching**: Predictive loading of likely requests
3. **Compression**: Further reduce memory usage via image compression
4. **Analytics**: Send telemetry to backend for aggregated insights
5. **A/B Testing**: Framework for testing different configurations
6. **Background Sync**: Offline-first architecture with sync
7. **Request Deduplication**: Merge identical concurrent requests

## Troubleshooting

### High Memory Usage

- Check cache size: `ImageCache.getInstance().getStats()`
- Call `clearCache()` to free memory
- Lower cache size configuration if needed

### Low Cache Hit Rate

- Verify cache key generation is consistent
- Check TTL isn't too short for text cache
- Monitor with `getMetrics()` to understand patterns

### Circuit Breaker Open

- Check error logs for root cause
- Wait 60 seconds for automatic recovery
- Call `retryPolicy.resetCircuitBreaker()` if issue resolved

### Poor Performance

- Check telemetry: `metrics.logSummary()`
- Monitor memory snapshots
- Verify network quality
- Check for low memory conditions

## Best Practices

1. **Always check cache first** before making API calls
2. **Use appropriate priority** for different task types
3. **Monitor metrics regularly** to identify issues
4. **Handle degraded mode gracefully** with clear user feedback
5. **Clear cache when needed** to free memory
6. **Log performance metrics** on app lifecycle events
7. **Test on low-end devices** to verify graceful degradation

## Related Documentation

- [MODERNIZATION.md](../MODERNIZATION.md) - Overall app modernization
- [UI_ENHANCEMENTS.md](../UI_ENHANCEMENTS.md) - UI/UX improvements
- [README.md](../README.md) - General app documentation
