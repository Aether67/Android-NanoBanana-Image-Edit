package com.yunho.nanobanana.performance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Task priority levels for optimized resource allocation
 */
enum class TaskPriority {
    /**
     * UI-critical tasks like image rendering, user interactions
     */
    HIGH,
    
    /**
     * Standard tasks like API calls, data processing
     */
    MEDIUM,
    
    /**
     * Background tasks like caching, logging, telemetry
     */
    LOW
}

/**
 * Provides optimized coroutine dispatchers for different task priorities
 */
object DispatcherProvider {
    
    /**
     * High-priority dispatcher for UI-critical operations
     * Uses main thread and limited background threads
     */
    val highPriority: CoroutineDispatcher = Dispatchers.Main.immediate
    
    /**
     * Medium-priority dispatcher for standard operations
     * Uses shared IO dispatcher
     */
    val mediumPriority: CoroutineDispatcher = Dispatchers.IO
    
    /**
     * Low-priority dispatcher for background operations
     * Uses dedicated single-threaded executor to avoid resource contention
     */
    val lowPriority: CoroutineDispatcher = 
        Executors.newSingleThreadExecutor { r ->
            Thread(r, "LowPriority").apply {
                priority = Thread.MIN_PRIORITY
                isDaemon = true
            }
        }.asCoroutineDispatcher()
    
    /**
     * Gets dispatcher for given priority level
     */
    fun getDispatcher(priority: TaskPriority): CoroutineDispatcher {
        return when (priority) {
            TaskPriority.HIGH -> highPriority
            TaskPriority.MEDIUM -> mediumPriority
            TaskPriority.LOW -> lowPriority
        }
    }
}
