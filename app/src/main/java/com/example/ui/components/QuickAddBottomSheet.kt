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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.AppleSystemOrange
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextMuted
import com.example.ui.theme.AppleTextPlaceholder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val SheetBackground = Color(0xFF0D1016)
private val SectionBackground = Color(0xFF151922)
private val ControlBackground = Color(0xFF20252F)
private val Hairline = Color(0x24FFFFFF)

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
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Umum") }
    var estimatedMinutes by remember { mutableIntStateOf(30) }
    var selectedPriority by remember { mutableStateOf("NORMAL") }
    var isPersistent by remember { mutableStateOf(true) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var selectedLocationTrigger by remember { mutableStateOf("ENTER") }

    var showCustomSubjectInput by remember { mutableStateOf(false) }
    var customSubjectText by remember { mutableStateOf("") }

    val subjects = remember {
        mutableStateListOf(
            "Umum", "Matematika", "IPA", "IPS", "B. Indonesia", "B. Inggris",
            "PKN", "Agama", "Informatika", "Seni Budaya", "Penjas", "Sejarah"
        )
    }

    val now = Calendar.getInstance()

    fun preset(dayOffset: Int, hour: Int, minute: Int = 0): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    val deadlineOptions = listOf(
        "Sore ini" to preset(0, 17),
        "Malam ini" to preset(0, 20),
        "Besok pagi" to preset(1, 8),
        "Besok sore" to preset(1, 16),
        "Lusa" to preset(2, 10)
    )

    var deadlineMillis by remember { mutableLongStateOf(deadlineOptions[2].second) }
    var selectedPresetIndex by remember { mutableIntStateOf(2) }

    val durationOptions = listOf(15, 30, 45, 60, 90, 120)
    val locationPresets = listOf("Sekolah", "Rumah", "Perpustakaan", "Indomaret")

    val deadlineDisplay = remember(deadlineMillis) {
        SimpleDateFormat("EEE, dd MMM yyyy • HH:mm", Locale("id", "ID"))
            .format(Date(deadlineMillis))
    }

    fun openDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = deadlineMillis }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                deadlineMillis = Calendar.getInstance().apply {
                    timeInMillis = deadlineMillis
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                }.timeInMillis
                selectedPresetIndex = -1
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000L
            show()
        }
    }

    fun openTimePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = deadlineMillis }
        TimePickerDialog(
            context,
            { _, hour, minute ->
                deadlineMillis = Calendar.getInstance().apply {
                    timeInMillis = deadlineMillis
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                selectedPresetIndex = -1
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    @Composable
    fun SectionCard(content: @Composable () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SectionBackground)
                .border(0.7.dp, Hairline, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            content()
        }
    }

    @Composable
    fun SectionTitle(text: String) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppleTextSecondary,
            letterSpacing = 0.4.sp
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = SheetBackground,
        scrimColor = Color.Black.copy(alpha = 0.64f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x50FFFFFF))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tugas Baru",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = "Tambahkan detail yang memang dibutuhkan.",
                        fontSize = 12.sp,
                        color = AppleTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ControlBackground)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = AppleTextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            SectionCard {
                Column {
                    SectionTitle("DETAIL TUGAS")
                    Spacer(Modifier.height(9.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text("Apa yang harus dikerjakan?", color = AppleTextPlaceholder)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleSystemBlue,
                            unfocusedBorderColor = Hairline,
                            focusedContainerColor = ControlBackground,
                            unfocusedContainerColor = ControlBackground,
                            focusedTextColor = AppleTextPrimary,
                            unfocusedTextColor = AppleTextPrimary
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = {
                            Text("Catatan (opsional)", color = AppleTextPlaceholder)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleSystemBlue,
                            unfocusedBorderColor = Hairline,
                            focusedContainerColor = ControlBackground,
                            unfocusedContainerColor = ControlBackground,
                            focusedTextColor = AppleTextPrimary,
                            unfocusedTextColor = AppleTextPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SectionTitle("KATEGORI")
                        Text(
                            text = selectedSubject,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleSystemBlue
                        )
                    }

                    Spacer(Modifier.height(9.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ControlBackground)
                                .clickable { showCustomSubjectInput = !showCustomSubjectInput }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = AppleTextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Kustom", color = AppleTextPrimary, fontSize = 12.sp)
                            }
                        }

                        subjects.forEach { subject ->
                            val selected = subject == selectedSubject
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) AppleSystemBlue else ControlBackground)
                                    .clickable {
                                        selectedSubject = subject
                                        showCustomSubjectInput = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = subject,
                                    color = if (selected) Color.White else AppleTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    AnimatedVisibility(showCustomSubjectInput) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customSubjectText,
                                onValueChange = { customSubjectText = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("Nama kategori", color = AppleTextPlaceholder) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppleSystemBlue,
                                    unfocusedBorderColor = Hairline,
                                    focusedContainerColor = ControlBackground,
                                    unfocusedContainerColor = ControlBackground,
                                    focusedTextColor = AppleTextPrimary,
                                    unfocusedTextColor = AppleTextPrimary
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (customSubjectText.isNotBlank()) AppleSystemBlue else ControlBackground)
                                    .clickable(enabled = customSubjectText.isNotBlank()) {
                                        val value = customSubjectText.trim()
                                        if (value.isNotBlank()) {
                                            if (!subjects.contains(value)) subjects.add(0, value)
                                            selectedSubject = value
                                            customSubjectText = ""
                                            showCustomSubjectInput = false
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = "Pilih",
                                    color = if (customSubjectText.isNotBlank()) Color.White else AppleTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                Column {
                    SectionTitle("DEADLINE")
                    Spacer(Modifier.height(9.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        deadlineOptions.forEachIndexed { index, option ->
                            val selected = selectedPresetIndex == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) AppleSystemBlue else ControlBackground)
                                    .clickable {
                                        selectedPresetIndex = index
                                        deadlineMillis = option.second
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = option.first,
                                    color = if (selected) Color.White else AppleTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ControlBackground)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = AppleSystemBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = deadlineDisplay,
                            modifier = Modifier.weight(1f),
                            color = AppleTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ControlBackground)
                                .clickable { openDatePicker() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    tint = AppleSystemBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text("Tanggal", color = AppleTextPrimary, fontSize = 12.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ControlBackground)
                                .clickable { openTimePicker() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = AppleSystemBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text("Jam", color = AppleTextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                Column {
                    SectionTitle("PRIORITAS")
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ControlBackground)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf(
                            Triple("Santai", "LOW", AppleSystemGreen),
                            Triple("Normal", "NORMAL", AppleSystemBlue),
                            Triple("Mendesak", "HIGH", AppleSystemRed)
                        ).forEach { (label, value, dotColor) ->
                            val selected = selectedPriority == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) Color(0xFF313744) else Color.Transparent)
                                    .clickable { selectedPriority = value }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = label,
                                        color = if (selected) AppleTextPrimary else AppleTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    SectionTitle("ESTIMASI DURASI")
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        durationOptions.forEach { minutes ->
                            val selected = estimatedMinutes == minutes
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (selected) AppleSystemBlue else ControlBackground)
                                    .clickable { estimatedMinutes = minutes }
                                    .padding(horizontal = 13.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "$minutes mnt",
                                    color = if (selected) Color.White else AppleTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                Column {
                    SectionTitle("PENGINGAT LOKASI")
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        locationPresets.forEach { location ->
                            val selected = selectedLocation == location
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (selected) AppleSystemOrange else ControlBackground)
                                    .clickable {
                                        selectedLocation = if (selected) null else location
                                    }
                                    .padding(horizontal = 11.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (selected) Color.White else AppleTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = location,
                                        color = if (selected) Color.White else AppleTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(selectedLocation != null) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "Picu saat",
                                color = AppleTextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ControlBackground)
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                listOf("Masuk" to "ENTER", "Keluar" to "EXIT").forEach { item ->
                                    val selected = selectedLocationTrigger == item.second
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) Color(0xFF313744) else Color.Transparent)
                                            .clickable { selectedLocationTrigger = item.second }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.first,
                                            color = if (selected) AppleTextPrimary else AppleTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ControlBackground)
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notifikasi tetap",
                                color = AppleTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Tetap tampil sampai selesai atau ditunda.",
                                color = AppleTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isPersistent,
                            onCheckedChange = { isPersistent = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = AppleSystemBlue,
                                checkedThumbColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (title.isNotBlank()) AppleSystemBlue else ControlBackground)
                    .clickable(enabled = title.isNotBlank()) {
                        onAddTask(
                            title.trim(),
                            selectedSubject,
                            description.trim(),
                            deadlineMillis,
                            estimatedMinutes,
                            selectedPriority,
                            isPersistent,
                            selectedLocation,
                            if (selectedLocation != null) selectedLocationTrigger else null
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Simpan Tugas",
                    color = if (title.isNotBlank()) Color.White else AppleTextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
