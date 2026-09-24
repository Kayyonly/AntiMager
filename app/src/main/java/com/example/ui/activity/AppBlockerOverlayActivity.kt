package com.example.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.AppBlockerManager
import com.example.ui.components.IosCard
import com.example.ui.theme.AntiMagerTheme
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGray1
import com.example.ui.theme.IosGray5
import com.example.ui.theme.IosGray6
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosShadowColor
import com.example.ui.theme.IosSurfaceCard
import com.example.ui.theme.IosSystemBackground
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AppBlockerOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
        const val EXTRA_BLOCKED_APP_NAME = "extra_blocked_app_name"
        const val EXTRA_PENDING_TASK_COUNT = "extra_pending_task_count"
    }

    private val tasksFlow = MutableStateFlow<List<TaskEntity>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appName = intent.getStringExtra(EXTRA_BLOCKED_APP_NAME) ?: "Aplikasi Ini"
        val pendingCount = intent.getIntExtra(EXTRA_PENDING_TASK_COUNT, 1)

        // Load incomplete tasks to display
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val list = db.taskDao().getIncompleteTasks().take(3)
                tasksFlow.value = list
            } catch (e: Exception) {
                // ignore
            }
        }

        setContent {
            AntiMagerTheme {
                val tasks by tasksFlow.collectAsState()

                BlockerScreenContent(
                    appName = appName,
                    pendingCount = pendingCount,
                    tasks = tasks,
                    onOpenAntiMager = {
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(mainIntent)
                        finish()
                    },
                    onEmergencyBreak = {
                        AppBlockerManager.grantEmergencyBreak(this, 5)
                        finish()
                    },
                    onGoHome = {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun BlockerScreenContent(
    appName: String,
    pendingCount: Int,
    tasks: List<TaskEntity>,
    onOpenAntiMager: () -> Unit,
    onEmergencyBreak: () -> Unit,
    onGoHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IosSystemBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Apple iOS Style Warning Icon Box
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(IosRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = IosRed,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Anti-Mager Shield Aktif 🛡️",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = IosTextPrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Aplikasi $appName diblokir sementara karena kamu masih punya $pendingCount tugas yang belum tuntas.",
                fontSize = 14.sp,
                color = IosTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Task List Preview
            if (tasks.isNotEmpty()) {
                IosCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TUGAS HARUS DISELESAIKAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IosGray1,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        tasks.forEachIndexed { index, task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(IosRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = IosTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = task.subject,
                                    fontSize = 11.sp,
                                    color = IosTextSecondary
                                )
                            }
                            if (index < tasks.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(IosGray6)
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Primary Button: Go to AntiMager
            Button(
                onClick = onOpenAntiMager,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IosBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buka AntiMager & Kerjakan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary: Emergency Break
            Button(
                onClick = onEmergencyBreak,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IosGray5,
                    contentColor = IosTextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = IosTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Minta Jeda Darurat 5 Menit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tertiary: Go to home screen
            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = IosTextSecondary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, IosSeparator)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kembali ke Beranda HP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
