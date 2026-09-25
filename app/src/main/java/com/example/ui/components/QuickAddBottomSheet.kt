package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextMuted
import com.example.ui.theme.AppleTextPlaceholder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.AppleTextTertiary
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    onDismiss: () -> Unit,
    onAddTask: (
        title: String,
        subject: String,
        description: String,
        deadlineEpochMillis: Long,
        estimatedMinutes: Int,
        priority: String,
        isPersistent: Boolean,
        locationName: String?,
        locationTrigger: String?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Umum") }
    var description by remember { mutableStateOf("") }
    var estimatedMinutes by remember { mutableIntStateOf(30) }
    var selectedPriority by remember { mutableStateOf("NORMAL") }
    var isPersistent by remember { mutableStateOf(true) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var selectedLocationTrigger by remember { mutableStateOf("ENTER") }

    val now = Calendar.getInstance()
    val limaMenitLagi = Calendar.getInstance().apply {
        add(Calendar.MINUTE, 5)
    }.timeInMillis

    val nantiSore = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 17); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis

    val malamIni = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 20); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis

    val besokPagi = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
    }.timeInMillis

    val besokSore = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 16); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
    }.timeInMillis

    var deadlineMillis by remember { mutableLongStateOf(besokPagi) }
    var selectedPresetIndex by remember { mutableIntStateOf(3) }

    val subjects = listOf("Umum", "Matematika", "IPA", "IPS", "B. Indonesia", "B. Inggris", "Belanja")
    val durationOptions = listOf(15, 30, 45, 60)
    val locationPresets = listOf("Sekolah", "Rumah", "Indomaret", "Perpustakaan")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xF8161618),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x35FFFFFF))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tugas Baru",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary,
                    letterSpacing = (-0.3).sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = AppleTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Judul tugas", color = AppleTextPlaceholder, fontSize = 15.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppleSystemBlue,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF1F1F22),
                    unfocusedContainerColor = Color(0xFF1F1F22),
                    focusedTextColor = AppleTextPrimary,
                    unfocusedTextColor = AppleTextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Notes / Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Catatan tambahan (opsional)", color = AppleTextPlaceholder, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppleSystemBlue,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF1F1F22),
                    unfocusedContainerColor = Color(0xFF1F1F22),
                    focusedTextColor = AppleTextPrimary,
                    unfocusedTextColor = AppleTextPrimary
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Chips (Neutral labels)
            Text(
                text = "KATEGORI",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppleTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                subjects.forEach { subj ->
                    val isSelected = selectedSubject == subj
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AppleSystemBlue else Color(0xFF242426))
                            .clickable { selectedSubject = subj }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = subj,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else AppleTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Deadline Preset Chips
            Text(
                text = "TENGGAT WAKTU",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppleTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            val deadlineOptions = listOf(
                Pair("5 Menit", limaMenitLagi),
                Pair("Sore (17:00)", nantiSore),
                Pair("Malam (20:00)", malamIni),
                Pair("Besok Pagi (08:00)", besokPagi),
                Pair("Besok Sore (16:00)", besokSore)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                deadlineOptions.forEachIndexed { index, (label, time) ->
                    val isSelected = selectedPresetIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AppleSystemBlue else Color(0xFF242426))
                            .clickable {
                                selectedPresetIndex = index
                                deadlineMillis = time
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else AppleTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Priority Selector: Quiet Segmented style
            Text(
                text = "PRIORITAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppleTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1F1F22))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(
                    Pair("Rendah", "LOW"),
                    Pair("Normal", "NORMAL"),
                    Pair("Mendesak", "HIGH")
                ).forEach { (label, value) ->
                    val isSelected = selectedPriority == value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF323236) else Color.Transparent)
                            .clickable { selectedPriority = value }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (value == "HIGH") {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(AppleSystemRed)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) (if (value == "HIGH") AppleSystemRed else AppleTextPrimary) else AppleTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Location presets
            Text(
                text = "PENGINGAT LOKASI",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppleTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                locationPresets.forEach { loc ->
                    val isSelected = selectedLocation == loc
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AppleSystemBlue else Color(0xFF242426))
                            .clickable {
                                selectedLocation = if (isSelected) null else loc
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = loc,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else AppleTextSecondary
                        )
                    }
                }
            }

            if (selectedLocation != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "PICU SAAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F1F22))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("Masuk lokasi" to "ENTER", "Keluar lokasi" to "EXIT").forEach { (label, value) ->
                        val isSelected = selectedLocationTrigger == value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF323236) else Color.Transparent)
                                .clickable { selectedLocationTrigger = value }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) AppleTextPrimary else AppleTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Persistent Notification Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1F1F22))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Notifikasi Berulang",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = "Muncul di notification shade sampai selesai",
                        fontSize = 11.sp,
                        color = AppleTextSecondary
                    )
                }
                Switch(
                    checked = isPersistent,
                    onCheckedChange = { isPersistent = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppleSystemBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0x35FFFFFF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            GlassButton(
                text = "Simpan",
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(
                            title,
                            selectedSubject,
                            description,
                            deadlineMillis,
                            estimatedMinutes,
                            selectedPriority,
                            isPersistent,
                            selectedLocation,
                            if (selectedLocation != null) selectedLocationTrigger else null
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
