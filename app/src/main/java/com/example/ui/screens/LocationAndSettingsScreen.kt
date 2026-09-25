package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppBlockerManager
import com.example.ui.activity.AppBlockerOverlayActivity
import com.example.ui.activity.VoiceActionActivity
import com.example.ui.components.IosCard
import com.example.ui.components.IosSectionHeader
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGray1
import com.example.ui.theme.IosGray5
import com.example.ui.theme.IosGray6
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosIndigo
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosSurfaceCard
import com.example.ui.theme.IosSystemBackground
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.viewmodel.TaskViewModel
import com.example.util.LocationReminderManager

@Composable
fun LocationAndSettingsScreen(
    viewModel: TaskViewModel,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // App Blocker States
    var isBlockerActive by remember { mutableStateOf(AppBlockerManager.isBlockerEnabled(context)) }
    var blockedAppsSet by remember { mutableStateOf(AppBlockerManager.getBlockedPackages(context)) }
    var isAccessibilityActive by remember { mutableStateOf(AppBlockerManager.isAccessibilityServiceEnabled(context)) }

    // Geofencing States
    var isGeofenceActive by remember { mutableStateOf(LocationReminderManager.isGeofencingActive(context)) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // iOS Large Title
        item {
            Column {
                Text(
                    text = "KONTROL & PROTEKSI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IosGray1,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Proteksi AntiMager",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextPrimary,
                    letterSpacing = (-0.8).sp
                )
            }
        }

        // Section: Profil Kelas & Pengaturan
        item {
            IosCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSettings?.invoke() },
                elevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(IosBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = IosBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Profil Kelas & Pemindai AI",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IosTextPrimary
                        )
                        Text(
                            text = "Atur \"Kelas Saya\" & scan jadwal multi-kelas",
                            fontSize = 13.sp,
                            color = IosTextSecondary
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Open",
                        tint = IosGray1,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ==========================================
        // FEATURE 1: APP BLOCKER (ACCESSIBILITY SERVICE)
        // ==========================================
        item {
            IosSectionHeader(title = "1. App Blocker (Accessibility Shield)")

            IosCard(elevation = 1.5.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Master Switch Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isBlockerActive) IosRed.copy(alpha = 0.12f) else IosGray5),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = if (isBlockerActive) IosRed else IosGray1,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Anti-Distraction Shield",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IosTextPrimary
                                )
                                Text(
                                    text = if (isBlockerActive) "Memblokir saat ada tugas tertunda" else "Shield nonaktif",
                                    fontSize = 12.sp,
                                    color = IosTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isBlockerActive,
                            onCheckedChange = { enabled ->
                                isBlockerActive = enabled
                                AppBlockerManager.setBlockerEnabled(context, enabled)
                                Toast.makeText(context, if (enabled) "App Blocker Diaktifkan" else "App Blocker Dimatikan", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IosGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = IosGray5
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IosSeparator))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Accessibility Service Status Banner
                    val isServiceOn = AppBlockerManager.isAccessibilityServiceEnabled(context)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isServiceOn) IosGreen.copy(alpha = 0.08f) else IosOrange.copy(alpha = 0.10f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isServiceOn) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isServiceOn) IosGreen else IosOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isServiceOn) "Accessibility Service Aktif" else "Aksesibilitas Belum Diizinkan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isServiceOn) IosGreen else IosOrange
                            )
                            Text(
                                text = if (isServiceOn) "Mendeteksi perpindahan aplikasi latar depan" else "Wajib aktifkan agar bisa mendeteksi app lain",
                                fontSize = 11.sp,
                                color = IosTextSecondary
                            )
                        }
                        if (!isServiceOn) {
                            Button(
                                onClick = { AppBlockerManager.openAccessibilitySettings(context) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = IosOrange,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Izinkan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Distracting Apps Checklist
                    Text(
                        text = "APLIKASI TARGET BLOKIR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IosGray1,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AppBlockerManager.DEFAULT_DISTRACTING_APPS.take(6).forEach { app ->
                        val isChecked = blockedAppsSet.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    AppBlockerManager.toggleAppBlocked(context, app.packageName)
                                    blockedAppsSet = AppBlockerManager.getBlockedPackages(context)
                                }
                                .padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = app.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = IosTextPrimary
                                )
                                Text(
                                    text = app.category,
                                    fontSize = 11.sp,
                                    color = IosTextSecondary
                                )
                            }
                            Switch(
                                checked = isChecked,
                                onCheckedChange = {
                                    AppBlockerManager.toggleAppBlocked(context, app.packageName)
                                    blockedAppsSet = AppBlockerManager.getBlockedPackages(context)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = IosRed,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = IosGray5
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Direct Test Overlay Button
                    OutlinedButton(
                        onClick = {
                            val testIntent = Intent(context, AppBlockerOverlayActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                putExtra(AppBlockerOverlayActivity.EXTRA_BLOCKED_PACKAGE, "com.zhiliaoapp.musically")
                                putExtra(AppBlockerOverlayActivity.EXTRA_BLOCKED_APP_NAME, "TikTok")
                                putExtra(AppBlockerOverlayActivity.EXTRA_PENDING_TASK_COUNT, uiState.totalPending.coerceAtLeast(1))
                            }
                            context.startActivity(testIntent)
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IosRed)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tes Layar Blokir Sekarang", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ==========================================
        // FEATURE 3: BACKGROUND GEOFENCING (REMINDER LOKASI)
        // ==========================================
        item {
            IosSectionHeader(title = "2. Reminder Lokasi (Real Background Geofencing)")

            IosCard(elevation = 1.5.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Master Geofence Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isGeofenceActive) IosGreen.copy(alpha = 0.12f) else IosGray5),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isGeofenceActive) IosGreen else IosGray1,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Geofencing Latar Belakang",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IosTextPrimary
                                )
                                Text(
                                    text = if (isGeofenceActive) "Service Aktif di Background" else "Geofencing nonaktif",
                                    fontSize = 12.sp,
                                    color = IosTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isGeofenceActive,
                            onCheckedChange = { active ->
                                if (active) {
                                    LocationReminderManager.startRealBackgroundGeofencing(context) { success, msg ->
                                        isGeofenceActive = success
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    LocationReminderManager.stopRealBackgroundGeofencing(context) { success, msg ->
                                        isGeofenceActive = !success
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IosGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = IosGray5
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IosSeparator))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "AREA PRESET GEOFENCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IosGray1,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LocationReminderManager.PRESET_LOCATIONS.forEach { area ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📍 ${area.name}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = IosTextPrimary
                                )
                                Text(
                                    text = "${area.description} • Radius ${area.radiusMeters.toInt()}m",
                                    fontSize = 11.sp,
                                    color = IosTextSecondary
                                )
                            }

                            // Interactive Simulation Button for quick demo
                            Button(
                                onClick = {
                                    viewModel.simulateLocationEvent(area.name, true)
                                    Toast.makeText(context, "Masuk area ${area.name}!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = IosGray5,
                                    contentColor = IosTextPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Tes Masuk", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // FEATURE 4: VOICE COMMAND (SHORTCUTS & WIDGET)
        // ==========================================
        item {
            IosSectionHeader(title = "3. Voice Command (Jalan Pintas Luar App)")

            IosCard(elevation = 1.5.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(IosBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = IosBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Akses Voice dari Luar Aplikasi",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IosTextPrimary
                            )
                            Text(
                                text = "Bicara langsung tanpa harus buka app terlebih dahulu",
                                fontSize = 12.sp,
                                color = IosTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "1. Home Screen Widget: Pasang widget AntiMager di layar depan HP-mu, ada tombol Mic 1-ketuk langsung merekam.\n2. App Shortcut: Tahan lama ikon aplikasi di layar utama, lalu pilih 'Voice Task'.\n3. Google Assistant: Ucapkan 'Buka Voice Task AntiMager'.",
                        fontSize = 13.sp,
                        color = IosTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val intent = Intent(context, VoiceActionActivity::class.java).apply {
                                action = "com.aistudio.antimager.VOICE_COMMAND"
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Voice Command Sekarang", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
