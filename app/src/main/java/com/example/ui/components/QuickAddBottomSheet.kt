package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidGlassTokens
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.AppleSystemOrange
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextMuted
import com.example.ui.theme.AppleTextPlaceholder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.AppleTextTertiary
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderStandard
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassLayer1
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        locationName: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Umum") }
    var estimatedMinutes by remember { mutableIntStateOf(30) }
    var selectedPriority by remember { mutableStateOf("NORMAL") }
    var isPersistent by remember { mutableStateOf(true) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }

    // Custom subject input state
    var showCustomSubjectInput by remember { mutableStateOf(false) }
    var customSubjectText by remember { mutableStateOf("") }

    val subjectsList = remember {
        mutableStateListOf(
            "Umum", "Matematika", "IPA", "IPS", "B. Indonesia", "B. Inggris",
            "PKN", "Penjas", "Seni Budaya", "Agama", "Informatika", "Sejarah",
            "Fisika", "Kimia", "Biologi", "Geografi", "Ekonomi", "Sosiologi",
            "Belanja", "Rumah"
        )
    }

    // Date calculations for quick presets
    val now = Calendar.getInstance()
    val nantiSore = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 17); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis

    val malamIni = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 20); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis

    val besokPagi = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val besokSore = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 16); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val lusa = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 2)
        set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val mingguDepan = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 7)
        set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    var deadlineMillis by remember { mutableLongStateOf(besokPagi) }
    var selectedPresetIndex by remember { mutableIntStateOf(2) } // default: Besok Pagi

    val durationOptions = listOf(15, 30, 45, 60, 90, 120)
    val locationPresets = listOf("Sekolah", "Rumah", "Perpustakaan", "Kampus", "Indomaret")

    // Formatter for selected deadline display
    val deadlineDisplayString = remember(deadlineMillis) {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm 'WIB'", Locale("id", "ID"))
        sdf.format(Date(deadlineMillis))
    }

    // Helper functions for DatePicker & TimePicker
    fun openDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = deadlineMillis }
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = deadlineMillis
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                deadlineMillis = newCal.timeInMillis
                selectedPresetIndex = -1 // Marked as custom
            },
            currentYear,
            currentMonth,
            currentDay
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000L
            show()
        }
    }

    fun openTimePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = deadlineMillis }
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = deadlineMillis
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                deadlineMillis = newCal.timeInMillis
                selectedPresetIndex = -1 // Marked as custom
            },
            currentHour,
            currentMinute,
            true
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xF210131A),
        scrimColor = Color.Black.copy(alpha = 0.72f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.5.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tugas Baru",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Jadwalkan tugas atau pengingat harian",
                        fontSize = 12.sp,
                        color = AppleTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x28FFFFFF))
                        .border(0.7.dp, LiquidGlassTokens.GlassSpecularBorderBrush, CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = AppleTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: Judul & Catatan (iOS Liquid Glass Inset Card)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DETAIL TUGAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Apa yang harus dikerjakan?", color = AppleTextPlaceholder, fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleSystemBlue,
                            unfocusedBorderColor = Color(0x18FFFFFF),
                            focusedContainerColor = Color(0x18FFFFFF),
                            unfocusedContainerColor = Color(0x10FFFFFF),
                            focusedTextColor = AppleTextPrimary,
                            unfocusedTextColor = AppleTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Catatan / instruksi guru (opsional)", color = AppleTextPlaceholder, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleSystemBlue,
                            unfocusedBorderColor = Color(0x18FFFFFF),
                            focusedContainerColor = Color(0x18FFFFFF),
                            unfocusedContainerColor = Color(0x10FFFFFF),
                            focusedTextColor = AppleTextPrimary,
                            unfocusedTextColor = AppleTextPrimary
                        ),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 2: Kategori / Mapel (Comprehensive Preset + Custom Input)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    Text(
                        text = "MATA PELAJARAN / KATEGORI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = selectedSubject,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleSystemBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subject Pill Chips Scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    // + Custom chip button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (showCustomSubjectInput) AppleSystemOrange.copy(alpha = 0.2f) else Color(0xFF2A2A2E))
                            .border(
                                0.8.dp,
                                if (showCustomSubjectInput) AppleSystemOrange else Color(0x30FFFFFF),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                showCustomSubjectInput = !showCustomSubjectInput
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = if (showCustomSubjectInput) AppleSystemOrange else AppleTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kustom",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (showCustomSubjectInput) AppleSystemOrange else AppleTextPrimary
                            )
                        }
                    }

                    // Existing subject chips
                    subjectsList.forEach { subj ->
                        val isSelected = selectedSubject == subj
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AppleSystemBlue else Color(0xFF26262A))
                                .border(
                                    0.6.dp,
                                    if (isSelected) AppleSystemBlue else Color(0x18FFFFFF),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedSubject = subj
                                    showCustomSubjectInput = false
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
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

                // Inline Custom Subject Input Box
                AnimatedVisibility(visible = showCustomSubjectInput) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF26262A))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Tambah Nama Mapel Baru:",
                            fontSize = 12.sp,
                            color = AppleTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customSubjectText,
                                onValueChange = { customSubjectText = it },
                                placeholder = { Text("cth: Mandarin, Robotika, Teater", fontSize = 13.sp, color = AppleTextPlaceholder) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppleSystemBlue,
                                    unfocusedBorderColor = Color(0x20FFFFFF),
                                    focusedContainerColor = Color(0xFF1E1E22),
                                    unfocusedContainerColor = Color(0xFF1E1E22),
                                    focusedTextColor = AppleTextPrimary,
                                    unfocusedTextColor = AppleTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (customSubjectText.isNotBlank()) AppleSystemBlue else Color(0xFF333338))
                                    .clickable(enabled = customSubjectText.isNotBlank()) {
                                        val trimmed = customSubjectText.trim()
                                        if (trimmed.isNotBlank()) {
                                            if (!subjectsList.contains(trimmed)) {
                                                subjectsList.add(0, trimmed)
                                            }
                                            selectedSubject = trimmed
                                            customSubjectText = ""
                                            showCustomSubjectInput = false
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "Pilih",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (customSubjectText.isNotBlank()) Color.White else AppleTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

            // SECTION 3: Tenggat Waktu (Quick Presets + DatePicker & TimePicker)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                    text = "TENGGAT WAKTU (DEADLINE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextSecondary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Presets Row
                val deadlineOptions = listOf(
                    Pair("Sore Ini (17:00)", nantiSore),
                    Pair("Malam Ini (20:00)", malamIni),
                    Pair("Besok Pagi (08:00)", besokPagi),
                    Pair("Besok Sore (16:00)", besokSore),
                    Pair("Lusa (10:00)", lusa),
                    Pair("Minggu Depan", mingguDepan)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    deadlineOptions.forEachIndexed { index, (label, time) ->
                        val isSelected = selectedPresetIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AppleSystemBlue else Color(0xFF26262A))
                                .border(
                                    0.6.dp,
                                    if (isSelected) AppleSystemBlue else Color(0x18FFFFFF),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedPresetIndex = index
                                    deadlineMillis = time
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
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

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Date & Time Picker Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF242428))
                        .border(0.6.dp, Color(0x20FFFFFF), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = AppleSystemBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = deadlineDisplayString,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date Picker Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF323238))
                                .clickable { openDatePicker() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    tint = AppleSystemBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pilih Tanggal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppleTextPrimary
                                )
                            }
                        }

                        // Time Picker Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF323238))
                                .clickable { openTimePicker() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = AppleSystemBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pilih Jam",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppleTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 4: Prioritas & Durasi
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                    text = "TINGKAT PRIORITAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Quiet Segmented Control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF26262A))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    listOf(
                        Pair("Santai", "LOW"),
                        Pair("Normal", "NORMAL"),
                        Pair("Mendesak", "HIGH")
                    ).forEach { (label, value) ->
                        val isSelected = selectedPriority == value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF38383E) else Color.Transparent)
                                .clickable { selectedPriority = value }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (value) {
                                                "HIGH" -> AppleSystemRed
                                                "NORMAL" -> AppleSystemBlue
                                                else -> AppleSystemGreen
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
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

                Text(
                    text = "ESTIMASI DURASI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    durationOptions.forEach { mins ->
                        val isSelected = estimatedMinutes == mins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AppleSystemBlue else Color(0xFF26262A))
                                .border(
                                    0.6.dp,
                                    if (isSelected) AppleSystemBlue else Color(0x18FFFFFF),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { estimatedMinutes = mins }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "$mins mnt",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else AppleTextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

            // SECTION 5: Lokasi & Notifikasi Shade
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PENGINGAT LOKASI (GEOFENCE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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
                                    .background(if (isSelected) AppleSystemOrange else Color(0x1CFFFFFF))
                                    .border(
                                        0.6.dp,
                                        if (isSelected) SolidColor(AppleSystemOrange) else LiquidGlassTokens.GlassSpecularBorderBrush,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedLocation = if (isSelected) null else loc
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else AppleTextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = loc,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else AppleTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Persistent Shade Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x20FFFFFF))
                            .border(0.6.dp, GlassBorderSubtle, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pengingat Anti-Mager (Persistent)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppleTextPrimary
                            )
                            Text(
                                text = "Menetap di tirai notifikasi sampai selesai",
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
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Action Button (Liquid Glass Button)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(
                        elevation = if (title.isNotBlank()) 6.dp else 1.dp,
                        shape = RoundedCornerShape(14.dp),
                        spotColor = if (title.isNotBlank()) Color(0x450A84FF) else Color.Transparent
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (title.isNotBlank()) {
                            Brush.verticalGradient(listOf(Color(0xFF0A84FF), Color(0xFF0071E3)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x14FFFFFF)))
                        }
                    )
                    .background(LiquidGlassTokens.GlassCardSheenBrush)
                    .border(
                        width = 0.85.dp,
                        brush = if (title.isNotBlank()) {
                            Brush.verticalGradient(listOf(Color(0x80FFFFFF), Color(0x20FFFFFF)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0x18FFFFFF), Color(0x08FFFFFF)))
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = title.isNotBlank()) {
                        onAddTask(
                            title.trim(),
                            selectedSubject,
                            description.trim(),
                            deadlineMillis,
                            estimatedMinutes,
                            selectedPriority,
                            isPersistent,
                            selectedLocation
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Simpan Tugas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (title.isNotBlank()) Color.White else AppleTextMuted,
                    letterSpacing = (-0.2).sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
