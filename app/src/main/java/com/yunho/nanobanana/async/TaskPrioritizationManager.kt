package com.yunho.nanobanana.async

import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Task priority levels for adaptive task scheduling
 */
enum class TaskPriority(val value: Int) {
    /**
     * Critical tasks affecting immediate UI feedback (preview updates, incremental output)
     */
    CRITICAL(0),
    
    /**
     * High priority tasks (quick AI responses, cache lookups)
     */
    HIGH(1),
    
    /**
     * Normal priority tasks (standard AI generation)
     */
    NORMAL(2),
    
    /**
     * Low priority tasks (background computations, batch processing)
     */
    LOW(3),
    
    /**
     * Background tasks (telemetry, cleanup)
     */
    BACKGROUND(4)
}

/**
 * Prioritized task wrapper
 */
private data class PrioritizedTask(
    val priority: TaskPriority,
    val sequenceNumber: Int,
    val task: suspend () -> Unit
) : Comparable<PrioritizedTask> {
    override fun compareTo(other: PrioritizedTask): Int {
        // First compare by priority
        val priorityCompare = priority.value.compareTo(other.priority.value)
        if (priorityCompare != 0) return priorityCompare
        
        // If same priority, use sequence number (FIFO)
        return sequenceNumber.compareTo(other.sequenceNumber)
    }
}

/**
 * Adaptive task prioritization mechanism
 * Ensures UI responsiveness by prioritizing tasks that affect perceived user feedback
 */
class TaskPrioritizationManager(
    private val maxConcurrentTasks: Int = 3
) {
    private val taskQueue = PriorityBlockingQueue<PrioritizedTask>()
    private val sequenceGenerator = AtomicInteger(0)
    private val activeTasks = AtomicInteger(0)
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    @Volatile
    private var isProcessing = false
    
    /**
     * Submit a task with priority
     */
    fun submitTask(
        priority: TaskPriority,
        task: suspend () -> Unit
    ): Job {
        val prioritizedTask = PrioritizedTask(
            priority = priority,
            sequenceNumber = sequenceGenerator.getAndIncrement(),
            task = task
        )
        
        taskQueue.offer(prioritizedTask)
        Log.d(TAG, "Task queued with priority ${priority.name} (queue size: ${taskQueue.size})")
        
        startProcessingIfNeeded()
        
        return scope.launch {
            // Wait for this task to complete
            // This is a simplified approach; in production, you'd want more sophisticated tracking
        }
    }
    
    /**
     * Start processing tasks if not already running
     */
    private fun startProcessingIfNeeded() {
        synchronized(this) {
            if (!isProcessing) {
                isProcessing = true
                scope.launch {
                    processTaskQueue()
                }
            }
        }
    }
    
    /**
     * Process tasks from the priority queue
     */
    private suspend fun processTaskQueue() {
        while (taskQueue.isNotEmpty() || activeTasks.get() > 0) {
            // Wait if we've reached max concurrent tasks
            while (activeTasks.get() >= maxConcurrentTasks && taskQueue.isNotEmpty()) {
                delay(50) // Small delay to avoid busy waiting
            }
            
            val task = taskQueue.poll()
            if (task != null) {
                activeTasks.incrementAndGet()
                
                scope.launch {
                    try {
                        Log.d(TAG, "Executing task with priority ${task.priority.name}")
                        task.task()
                    } catch (e: Exception) {
                        Log.e(TAG, "Task execution failed", e)
                    } finally {
                        activeTasks.decrementAndGet()
                    }
                }
            } else if (activeTasks.get() == 0) {
                // No more tasks to process
                break
            } else {
                // Wait for active tasks to complete
                delay(100)
            }
        }
        
        synchronized(this) {
            isProcessing = false
        }
    }
    
    /**
     * Get queue statistics
     */
    fun getQueueStats(): QueueStats {
        return QueueStats(
            queuedTasks = taskQueue.size,
            activeTasks = activeTasks.get(),
            maxConcurrent = maxConcurrentTasks
        )
    }
    
    /**
     * Clear all queued tasks
     */
    fun clearQueue() {
        taskQueue.clear()
        Log.d(TAG, "Task queue cleared")
    }
    
    /**
     * Shutdown the manager
     */
    fun shutdown() {
        scope.cancel()
        taskQueue.clear()
        Log.d(TAG, "Task prioritization manager shutdown")
    }
    
    companion object {
        private const val TAG = "TaskPrioritization"
    }
}

/**
 * Queue statistics
 */
data class QueueStats(
    val queuedTasks: Int,
    val activeTasks: Int,
    val maxConcurrent: Int
)
