package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.AiTaskParserService
import com.example.data.local.entity.TaskEntity
import com.example.util.SmartPrioritySorter
import com.example.util.SortMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AntiMager", appName)
    }

    @Test
    fun `ai parser extracts subject and deadline from story`() {
        val prompt = "PR IPS bab 3 besok pagi jam 8 kerjain 35 menit"
        val parsed = AiTaskParserService.parseStory(prompt)

        assertEquals("IPS", parsed.subject)
        assertEquals(35, parsed.estimatedMinutes)
        assertTrue(parsed.title.contains("PR IPS", ignoreCase = true) || parsed.title.contains("Bab 3", ignoreCase = true))
        assertNotNull(parsed.deadlineFormatted)
    }

    @Test
    fun `smart priority sorts urgent tasks first`() {
        val now = System.currentTimeMillis()
        val urgentTask = TaskEntity(
            id = 1,
            title = "PR Mendesak",
            deadlineEpochMillis = now + (2 * 3600 * 1000), // 2 hours
            estimatedMinutes = 60,
            priority = "HIGH"
        )
        val relaxedTask = TaskEntity(
            id = 2,
            title = "Tugas Santai",
            deadlineEpochMillis = now + (5 * 24 * 3600 * 1000), // 5 days
            estimatedMinutes = 15,
            priority = "LOW"
        )

        val sorted = SmartPrioritySorter.sortTasks(listOf(relaxedTask, urgentTask), SortMode.SMART_AI, now)
        assertEquals("PR Mendesak", sorted.first().title)
    }
}
