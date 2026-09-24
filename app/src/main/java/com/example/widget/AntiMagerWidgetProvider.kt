package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entity.TaskEntity
import com.example.ui.activity.VoiceActionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AntiMagerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_antimager)

            // Intent to open Main App
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                1001,
                openAppIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.btn_widget_open, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Intent to open Voice Command directly
            val voiceIntent = Intent(context, VoiceActionActivity::class.java).apply {
                action = "com.aistudio.antimager.VOICE_COMMAND"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val voicePendingIntent = PendingIntent.getActivity(
                context,
                1002,
                voiceIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.btn_widget_voice, voicePendingIntent)

            // Asynchronously fetch pending tasks to show in widget
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val pending = db.taskDao().getIncompleteTasks()

                    views.setTextViewText(R.id.tv_widget_count, "${pending.size} Tugas")

                    if (pending.isNotEmpty()) {
                        val top = pending.first()
                        views.setTextViewText(
                            R.id.tv_widget_top_task,
                            "⚡ ${top.title} (${top.subject})"
                        )
                    } else {
                        views.setTextViewText(
                            R.id.tv_widget_top_task,
                            "Semua tugas tuntas! Waktunya santai ☕"
                        )
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        fun sendUpdateBroadcast(context: Context) {
            val intent = Intent(context, AntiMagerWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, AntiMagerWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
