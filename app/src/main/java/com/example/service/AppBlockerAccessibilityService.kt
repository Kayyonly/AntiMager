package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.data.local.AppDatabase
import com.example.data.repository.AppBlockerManager
import com.example.ui.activity.AppBlockerOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppBlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var lastBlockedPackage: String? = null
    private var lastBlockTimestamp: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Never block our own app or system UI / home launcher
        if (packageName == this.packageName ||
            packageName.contains("systemui", ignoreCase = true) ||
            packageName.contains("launcher", ignoreCase = true)
        ) {
            return
        }

        // Check if blocker is enabled
        if (!AppBlockerManager.isBlockerEnabled(this)) {
            return
        }

        // Check if user is currently taking an approved emergency break
        if (AppBlockerManager.isEmergencyBreakActive(this)) {
            return
        }

        // Check if package is in the user's blocked apps list
        val blockedPackages = AppBlockerManager.getBlockedPackages(this)
        if (!blockedPackages.contains(packageName)) {
            return
        }

        // Avoid rapid re-triggering within 1.5 seconds
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && (now - lastBlockTimestamp) < 1500L) {
            return
        }
        lastBlockedPackage = packageName
        lastBlockTimestamp = now

        // Check if there are any incomplete tasks remaining in Room database
        serviceScope.launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val incompleteTasks = db.taskDao().getIncompleteTasks()

                if (incompleteTasks.isNotEmpty()) {
                    val appName = AppBlockerManager.getAppName(applicationContext, packageName)
                    Log.d("AppBlocker", "Blocking app $packageName ($appName) - ${incompleteTasks.size} tasks pending")

                    Handler(Looper.getMainLooper()).post {
                        val overlayIntent = Intent(applicationContext, AppBlockerOverlayActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            putExtra(AppBlockerOverlayActivity.EXTRA_BLOCKED_PACKAGE, packageName)
                            putExtra(AppBlockerOverlayActivity.EXTRA_BLOCKED_APP_NAME, appName)
                            putExtra(AppBlockerOverlayActivity.EXTRA_PENDING_TASK_COUNT, incompleteTasks.size)
                        }
                        startActivity(overlayIntent)
                    }
                }
            } catch (e: Exception) {
                Log.e("AppBlocker", "Error checking pending tasks", e)
            }
        }
    }

    override fun onInterrupt() {
        Log.d("AppBlocker", "Accessibility Service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AppBlocker", "Accessibility Service connected and active")
    }
}
