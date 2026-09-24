package com.example.data.repository

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

data class DistractingAppInfo(
    val packageName: String,
    val appName: String,
    val category: String,
    val iconName: String = "phone"
)

object AppBlockerManager {

    private const val PREFS_NAME = "antimager_blocker_prefs"
    private const val KEY_BLOCKER_ENABLED = "key_blocker_enabled"
    private const val KEY_BLOCKED_PACKAGES = "key_blocked_packages"
    private const val KEY_EMERGENCY_UNTIL = "key_emergency_until"

    val DEFAULT_DISTRACTING_APPS = listOf(
        DistractingAppInfo("com.zhiliaoapp.musically", "TikTok", "Media Sosial"),
        DistractingAppInfo("com.ss.android.ugc.trill", "TikTok Lite", "Media Sosial"),
        DistractingAppInfo("com.instagram.android", "Instagram", "Media Sosial"),
        DistractingAppInfo("com.google.android.youtube", "YouTube", "Video"),
        DistractingAppInfo("com.twitter.android", "X (Twitter)", "Media Sosial"),
        DistractingAppInfo("com.facebook.katana", "Facebook", "Media Sosial"),
        DistractingAppInfo("com.mobile.legends", "Mobile Legends", "Game"),
        DistractingAppInfo("com.dts.freefireth", "Free Fire", "Game"),
        DistractingAppInfo("com.netflix.mediaclient", "Netflix", "Hiburan")
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isBlockerEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BLOCKER_ENABLED, false)
    }

    fun setBlockerEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BLOCKER_ENABLED, enabled).apply()
    }

    fun getBlockedPackages(context: Context): Set<String> {
        val defaultSet = setOf(
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.instagram.android",
            "com.google.android.youtube",
            "com.twitter.android",
            "com.mobile.legends"
        )
        return getPrefs(context).getStringSet(KEY_BLOCKED_PACKAGES, defaultSet) ?: defaultSet
    }

    fun setBlockedPackages(context: Context, packages: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_BLOCKED_PACKAGES, packages).apply()
    }

    fun toggleAppBlocked(context: Context, packageName: String): Boolean {
        val current = getBlockedPackages(context).toMutableSet()
        val willBeBlocked = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        setBlockedPackages(context, current)
        return willBeBlocked
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        val expectedServiceName = "com.example/.service.AppBlockerAccessibilityService"
        val expectedPackage = context.packageName

        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == expectedPackage &&
            it.resolveInfo.serviceInfo.name.contains("AppBlockerAccessibilityService")
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun grantEmergencyBreak(context: Context, minutes: Int = 5) {
        val until = System.currentTimeMillis() + (minutes * 60 * 1000L)
        getPrefs(context).edit().putLong(KEY_EMERGENCY_UNTIL, until).apply()
    }

    fun isEmergencyBreakActive(context: Context): Boolean {
        val until = getPrefs(context).getLong(KEY_EMERGENCY_UNTIL, 0L)
        return System.currentTimeMillis() < until
    }

    fun getRemainingEmergencyMinutes(context: Context): Int {
        val until = getPrefs(context).getLong(KEY_EMERGENCY_UNTIL, 0L)
        val remaining = until - System.currentTimeMillis()
        return if (remaining > 0) (remaining / (60 * 1000L)).toInt() + 1 else 0
    }

    fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            DEFAULT_DISTRACTING_APPS.firstOrNull { it.packageName == packageName }?.appName ?: packageName
        }
    }
}
