package com.example.data.service

import com.example.data.local.entity.TaskEntity

/**
 * Facade preserving existing method signature, routing priority sorting
 * directly to GroqPriorityService (Llama 3.3 70B).
 */
object GeminiPriorityService {

    suspend fun sortTasksWithGemini(
        tasks: List<TaskEntity>,
        nowMillis: Long = System.currentTimeMillis()
    ): AiPrioritySortResult {
        return GroqPriorityService.sortTasksWithGroq(tasks, nowMillis)
    }
}
