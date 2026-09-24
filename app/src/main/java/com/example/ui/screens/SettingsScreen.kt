package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.IosSectionHeader
import com.example.ui.components.LiquidGlassTokens
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemBlueSubtle
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.AppleSystemIndigo
import com.example.ui.theme.AppleSystemOrange
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextMuted
import com.example.ui.theme.AppleTextPlaceholder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderStandard
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassLayer1
import com.example.ui.theme.GlassLayer2
import com.example.ui.theme.LiquidDarkBackground
import com.example.ui.theme.LiquidDarkCard
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveMessage) {
        uiState.saveMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveMessage()
        }
    }

    val quickClasses = listOf(
        "VII.1", "VII.2", "VII.3", "VII.4",
        "VIII.1", "VIII.2", "VIII.3", "VIII.4",
        "IX.1", "IX.2", "IX.3", "IX.4", "IX.5", "IX.6", "IX.7", "IX.8", "IX.9"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidDarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Row (Apple iOS Navigation Bar style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassLayer1)
                        .border(0.8.dp, GlassBorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = AppleSystemBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column {
                Text(
                    text = "PENGATURAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextSecondary,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Profil & Sistem",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary,
                    letterSpacing = (-0.6).sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 1: PROFIL KELAS SAYA
        IosSectionHeader(title = "Profil Kelas (Scan Jadwal)")

        GlassCard(elevation = 2.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AppleSystemBlueSubtle)
                            .border(0.8.dp, AppleSystemBlue.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = AppleSystemBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Kelas Saya",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextPrimary,
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            text = "Filter otomatis saat scan foto multi-kelas",
                            fontSize = 12.sp,
                            color = AppleTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Field: Kelas Saya
                OutlinedTextField(
                    value = uiState.userClass,
                    onValueChange = { viewModel.onClassChange(it) },
                    placeholder = { Text("Contoh: IX.7 atau X IPA 2", color = AppleTextPlaceholder, fontSize = 14.sp) },
                    label = { Text("Nama Kelas Kamu", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LiquidGlassTokens.RadiusInput),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleSystemBlue,
                        unfocusedBorderColor = GlassBorderStandard,
                        focusedTextColor = AppleTextPrimary,
                        unfocusedTextColor = AppleTextPrimary,
                        focusedContainerColor = LiquidDarkCard,
                        unfocusedContainerColor = LiquidDarkCard
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PILIHAN CEPAT KELAS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextSecondary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickClasses.forEach { cls ->
                        val isSelected = uiState.userClass.equals(cls, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AppleSystemBlue else GlassLayer1)
                                .border(
                                    0.8.dp,
                                    if (isSelected) AppleSystemBlue else GlassBorderSubtle,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.onClassChange(cls) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cls,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else AppleTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Student Name
                OutlinedTextField(
                    value = uiState.studentName,
                    onValueChange = { viewModel.onStudentNameChange(it) },
                    placeholder = { Text("Contoh: Budi Santoso", color = AppleTextPlaceholder, fontSize = 14.sp) },
                    label = { Text("Nama Lengkap (Opsional)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LiquidGlassTokens.RadiusInput),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleSystemBlue,
                        unfocusedBorderColor = GlassBorderStandard,
                        focusedTextColor = AppleTextPrimary,
                        unfocusedTextColor = AppleTextPrimary,
                        focusedContainerColor = LiquidDarkCard,
                        unfocusedContainerColor = LiquidDarkCard
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Profile Button
                GlassButton(
                    text = "Simpan Profil Kelas",
                    onClick = { viewModel.saveProfile() },
                    icon = Icons.Default.Check,
                    variant = GlassButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 2: AI & ENGINE INTEGRATION
        IosSectionHeader(title = "AI Intelligence Engine")

        GlassCard(elevation = 2.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x225E5CE6))
                            .border(0.8.dp, AppleSystemIndigo.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AppleSystemIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Google Gemini 3.5 Flash",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextPrimary,
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            text = "Model aktif untuk analisis bahasa & visual scan",
                            fontSize = 12.sp,
                            color = AppleTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Filter Jadwal Multi-Kelas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppleTextPrimary
                        )
                        Text(
                            text = "Filter otomatis hanya baris/kolom \"${uiState.userClass}\"",
                            fontSize = 12.sp,
                            color = AppleTextSecondary
                        )
                    }

                    Switch(
                        checked = uiState.autoFilterSchedule,
                        onCheckedChange = { viewModel.toggleAutoFilterSchedule(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppleSystemGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0x35FFFFFF)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 3: SYSTEM AUDIT STATUS
        IosSectionHeader(title = "Status Fitur Proteksi")

        GlassCard(elevation = 2.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Item 1: App Blocker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = AppleSystemRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App Blocker (Accessibility)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppleTextPrimary)
                        Text("Mendeteksi foreground window TikTok, IG, dll", fontSize = 11.sp, color = AppleTextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppleSystemGreen.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Aktif", fontSize = 11.sp, color = AppleSystemGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(GlassBorderSubtle))
                Spacer(modifier = Modifier.height(10.dp))

                // Item 2: Smart Priority Sorting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AppleSystemIndigo, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Priority Sorting", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppleTextPrimary)
                        Text("Gemini 3.5 Flash API + reasoning rationale", fontSize = 11.sp, color = AppleTextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppleSystemGreen.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Aktif", fontSize = 11.sp, color = AppleSystemGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(GlassBorderSubtle))
                Spacer(modifier = Modifier.height(10.dp))

                // Item 3: Reminder Lokasi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AppleSystemGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Background Geofencing", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppleTextPrimary)
                        Text("Foreground Location Service + Broadcast Receiver", fontSize = 11.sp, color = AppleTextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppleSystemGreen.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Aktif", fontSize = 11.sp, color = AppleSystemGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(GlassBorderSubtle))
                Spacer(modifier = Modifier.height(10.dp))

                // Item 4: Voice Command
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = AppleSystemBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Voice Command Luar App", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppleTextPrimary)
                        Text("App Shortcut + Home Screen Widget 1-Tap", fontSize = 11.sp, color = AppleTextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppleSystemGreen.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Aktif", fontSize = 11.sp, color = AppleSystemGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(95.dp))
    }
}
