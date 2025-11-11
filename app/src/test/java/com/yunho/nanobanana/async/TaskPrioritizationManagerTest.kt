package com.yunho.nanobanana.async

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for TaskPrioritizationManager
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskPrioritizationManagerTest {
    
    private lateinit var manager: TaskPrioritizationManager
    
    @Before
    fun setup() {
        manager = TaskPrioritizationManager(maxConcurrentTasks = 2)
    }
    
    @After
    fun tearDown() {
        manager.shutdown()
    }
    
    @Test
    fun `tasks should be executed based on priority`() = runTest {
        // Given
        val executionOrder = mutableListOf<String>()
        
        // When - submit tasks with different priorities
        manager.submitTask(TaskPriority.LOW) {
            delay(50)
            executionOrder.add("low")
        }
        
        manager.submitTask(TaskPriority.CRITICAL) {
            delay(50)
            executionOrder.add("critical")
        }
        
        manager.submitTask(TaskPriority.HIGH) {
            delay(50)
            executionOrder.add("high")
        }
        
        // Wait for all tasks to complete
        delay(500)
        
        // Then - critical should execute first
        assertEquals("critical", executionOrder.first())
    }
    
    @Test
    fun `getQueueStats should return correct statistics`() = runTest {
        // Given
        manager.submitTask(TaskPriority.NORMAL) {
            delay(100)
        }
        
        manager.submitTask(TaskPriority.NORMAL) {
            delay(100)
        }
        
        // When - check stats while tasks are running
        delay(50) // Let tasks start
        val stats = manager.getQueueStats()
        
        // Then
        assertEquals(2, stats.maxConcurrent)
        assertTrue(stats.activeTasks > 0 || stats.queuedTasks > 0)
    }
    
    @Test
    fun `clearQueue should remove all queued tasks`() = runTest {
        // Given
        manager.submitTask(TaskPriority.NORMAL) { delay(1000) }
        manager.submitTask(TaskPriority.NORMAL) { delay(1000) }
        manager.submitTask(TaskPriority.NORMAL) { delay(1000) }
        
        delay(50) // Let some tasks start
        
        // When
        manager.clearQueue()
        
        // Then
        val stats = manager.getQueueStats()
        assertEquals(0, stats.queuedTasks)
    }
}
