package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.local.entity.ScheduleEntity
import com.example.ui.components.ClassMismatchDialog
import com.example.ui.components.GlassCard
import com.example.ui.components.IosCard
import com.example.ui.components.IosSegmentedControl
import com.example.ui.components.ScanOptionDialog
import com.example.ui.components.ScanningProgressDialog
import com.example.ui.components.ScheduleScanConfirmDialog
import com.example.ui.components.createSampleMultiClassTimetableBitmap
import com.example.ui.components.createSampleTimetableBitmap
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassBorderStandard
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.GlassModalBackground
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosIndigo
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.MintAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.viewmodel.DailySubTab
import com.example.ui.viewmodel.ScheduleViewModel
import com.example.ui.viewmodel.ScheduleViewMode

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var isScanOptionOpen by remember { mutableStateOf(false) }

    // Launcher for Take Picture Preview (Camera)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.scanSchedulePhoto(bitmap)
        }
    }

    // Permission launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto jadwal", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for Pick Visual Media (Zero-permission Android Photo Picker)
    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.scanSchedulePhoto(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memproses gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle error message
    LaunchedEffect(uiState.scanErrorMessage) {
        uiState.scanErrorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    val currentDayName = ScheduleViewModel.getDayName(uiState.currentDayOfWeek)
    val nextDayOfWeek = if (uiState.currentDayOfWeek == 7) 1 else uiState.currentDayOfWeek + 1
    val nextDayName = ScheduleViewModel.getDayName(nextDayOfWeek)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Top Header: Title, Scan Button, and Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Jadwal Pelajaran",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Jadwal kelas, mapel & jam istirahat",
                        fontSize = 12.sp,
                        color = IosTextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tombol Scan Jadwal dari Foto (Apple iOS Minimalist Style)
                    Button(
                        onClick = { isScanOptionOpen = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosBlue.copy(alpha = 0.12f),
                            contentColor = IosBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan Jadwal dari Foto",
                            tint = IosBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan Foto",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Add Slot Button
                    IconButton(
                        onClick = { viewModel.openAddForm() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(IosBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Slot",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Class Banner with Quick Settings Trigger
            IosCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSettings?.invoke() },
                elevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(IosBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = IosBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Profil Kelas Saya: ",
                            fontSize = 12.sp,
                            color = IosTextSecondary
                        )
                        Text(
                            text = uiState.userClass,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosBlue
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ubah di Settings",
                            fontSize = 11.sp,
                            color = IosIndigo,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = IosIndigo,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Selector: [📅 Harian] vs [🗓️ Mingguan] (iOS Segmented Control)
            IosSegmentedControl(
                items = listOf(ScheduleViewMode.DAILY, ScheduleViewMode.WEEKLY),
                selectedItem = uiState.viewMode,
                onItemSelected = { viewModel.setViewMode(it) },
                itemLabel = { mode: ScheduleViewMode -> if (mode == ScheduleViewMode.DAILY) "📅 Jadwal Harian" else "🗓️ Jadwal Mingguan" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Daily Sub-Tabs or Weekly Day Selector Chips
            if (uiState.viewMode == ScheduleViewMode.DAILY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isToday = uiState.dailySubTab == DailySubTab.TODAY
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isToday) MintAccent.copy(alpha = 0.2f) else GlassCardFill)
                            .border(
                                1.dp,
                                if (isToday) MintAccent else GlassCardBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.setDailySubTab(DailySubTab.TODAY) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Hari Ini",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) MintAccent else TextWhitePrimary
                            )
                            Text(
                                text = currentDayName,
                                fontSize = 11.sp,
                                color = if (isToday) MintAccent.copy(alpha = 0.8f) else TextSecondary
                            )
                        }
                    }

                    val isTomorrow = uiState.dailySubTab == DailySubTab.TOMORROW
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isTomorrow) LavenderAccent.copy(alpha = 0.2f) else GlassCardFill)
                            .border(
                                1.dp,
                                if (isTomorrow) LavenderAccent else GlassCardBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.setDailySubTab(DailySubTab.TOMORROW) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Besok",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTomorrow) LavenderAccent else TextWhitePrimary
                            )
                            Text(
                                text = nextDayName,
                                fontSize = 11.sp,
                                color = if (isTomorrow) LavenderAccent.copy(alpha = 0.8f) else TextSecondary
                            )
                        }
                    }
                }
            } else {
                // Weekly Days: Senin, Selasa, Rabu, Kamis, Jumat, Sabtu, Minggu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val days = listOf(
                        1 to "Senin",
                        2 to "Selasa",
                        3 to "Rabu",
                        4 to "Kamis",
                        5 to "Jumat",
                        6 to "Sabtu",
                        7 to "Minggu"
                    )
                    days.forEach { (dayInt, dayStr) ->
                        val isSelected = uiState.selectedDayOfWeek == dayInt
                        val isCurrentDay = uiState.currentDayOfWeek == dayInt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) LavenderAccent.copy(alpha = 0.25f) else GlassCardFill)
                                .border(
                                    1.dp,
                                    if (isSelected) LavenderAccent else GlassCardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setSelectedDayOfWeek(dayInt) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dayStr,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) LavenderAccent else TextWhitePrimary
                                )
                                if (isCurrentDay) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MintAccent)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Schedule List
            if (uiState.displayedSchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🏖️", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tidak ada jadwal pelajaran",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Klik 'Scan Foto' untuk foto jadwal di kertas/papan,\natau klik (+) untuk menambah slot manual.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    items(uiState.displayedSchedules, key = { it.id }) { item ->
                        ScheduleSlotCard(
                            schedule = item,
                            onEdit = { viewModel.openEditForm(item) },
                            onDelete = {
                                viewModel.deleteSchedule(item)
                                Toast.makeText(context, "Slot jadwal dihapus", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal: Pilih Sumber Foto (Kamera, Galeri, atau Contoh Gambar)
    if (isScanOptionOpen) {
        ScanOptionDialog(
            userClass = uiState.userClass,
            onDismiss = { isScanOptionOpen = false },
            onTakePhoto = {
                isScanOptionOpen = false
                val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    takePictureLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onPickGallery = {
                isScanOptionOpen = false
                pickPhotoLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onUseSingleClassSample = {
                isScanOptionOpen = false
                val sampleBitmap = createSampleTimetableBitmap()
                viewModel.scanSchedulePhoto(sampleBitmap)
            },
            onUseMultiClassSample = {
                isScanOptionOpen = false
                val multiBitmap = createSampleMultiClassTimetableBitmap()
                viewModel.scanSchedulePhoto(multiBitmap)
            }
        )
    }

    // Modal: Progress Scanning Gemini Vision AI
    if (uiState.isScanning) {
        ScanningProgressDialog()
    }

    // Modal: Kelas tidak cocok di foto multi-kelas
    if (uiState.isClassMismatchDialogVisible) {
        ClassMismatchDialog(
            userClass = uiState.userClass,
            detectedClasses = uiState.detectedClassesInPhoto,
            scannedBitmap = uiState.scannedBitmap,
            onDismiss = { viewModel.dismissClassMismatchDialog() },
            onSelectClass = { chosenClass, updateProfile ->
                viewModel.onSelectClassFromPhoto(chosenClass, updateProfile)
            }
        )
    }

    // Modal: Preview & Konfirmasi Hasil Scan (dengan identifikasi multi-class & matched class)
    if (uiState.isConfirmDialogVisible) {
        ScheduleScanConfirmDialog(
            extractedItems = uiState.extractedItems,
            scannedBitmap = uiState.scannedBitmap,
            isMultiClass = uiState.isMultiClassDetected,
            matchedClass = uiState.matchedClass,
            detectedClasses = uiState.detectedClassesInPhoto,
            onDismiss = { viewModel.dismissConfirmDialog() },
            onChangeClass = {
                // User can pick a different class from the detected classes
                viewModel.onSelectClassFromPhoto(uiState.matchedClass, false)
            },
            onUpdateItem = { index, updated -> viewModel.updateExtractedItem(index, updated) },
            onDeleteItem = { index -> viewModel.removeExtractedItem(index) },
            onConfirmSave = {
                viewModel.confirmAndSaveExtractedSchedules { savedCount ->
                    Toast.makeText(context, "Berhasil menyimpan $savedCount slot jadwal baru ke database! 🎉", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Add / Edit Manual Dialog
    if (uiState.isFormOpen) {
        val targetDay = if (uiState.viewMode == ScheduleViewMode.DAILY) {
            if (uiState.dailySubTab == DailySubTab.TODAY) uiState.currentDayOfWeek else nextDayOfWeek
        } else {
            uiState.selectedDayOfWeek
        }

        ScheduleAddEditDialog(
            initialSchedule = uiState.editingSchedule,
            initialDayOfWeek = targetDay,
            onDismiss = { viewModel.closeForm() },
            onSave = { id, day, subject, start, end, roomTeacher, isBreak, color ->
                viewModel.saveSchedule(id, day, subject, start, end, roomTeacher, isBreak, color)
                val msg = if (id == 0L) "Slot jadwal berhasil ditambahkan!" else "Slot jadwal berhasil diperbarui!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun ScheduleSlotCard(
    schedule: ScheduleEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isBreak = schedule.isBreak
    val accentColor = if (isBreak) {
        Color(0xFFF59E0B) // Amber for break
    } else {
        try {
            Color(android.graphics.Color.parseColor(schedule.colorHex))
        } catch (e: Exception) {
            CyanAccent
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (isBreak) Color(0xFFF59E0B).copy(alpha = 0.12f) else GlassCardFill,
        borderColor = if (isBreak) Color(0xFFF59E0B).copy(alpha = 0.45f) else GlassCardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Capsule Pill
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = schedule.startTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
                Text(
                    text = "s/d",
                    fontSize = 9.sp,
                    color = TextSecondary
                )
                Text(
                    text = schedule.endTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Subject / Break Content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isBreak) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Coffee,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ISTIRAHAT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = schedule.subject,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )
                }

                if (schedule.roomOrTeacher.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isBreak) Icons.Default.LocationOn else Icons.Default.Person,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = schedule.roomOrTeacher,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Edit & Delete Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Slot",
                        tint = CyanAccent,
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Slot",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleAddEditDialog(
    initialSchedule: ScheduleEntity?,
    initialDayOfWeek: Int,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        dayOfWeek: Int,
        subject: String,
        startTime: String,
        endTime: String,
        roomOrTeacher: String,
        isBreak: Boolean,
        colorHex: String
    ) -> Unit
) {
    var dayOfWeek by remember { mutableIntStateOf(initialSchedule?.dayOfWeek ?: initialDayOfWeek) }
    var subject by remember { mutableStateOf(initialSchedule?.subject ?: "") }
    var startTime by remember { mutableStateOf(initialSchedule?.startTime ?: "07:15") }
    var endTime by remember { mutableStateOf(initialSchedule?.endTime ?: "08:45") }
    var roomOrTeacher by remember { mutableStateOf(initialSchedule?.roomOrTeacher ?: "") }
    var isBreak by remember { mutableStateOf(initialSchedule?.isBreak ?: false) }
    var colorHex by remember { mutableStateOf(initialSchedule?.colorHex ?: "#38BDF8") }

    val days = listOf(
        1 to "Senin", 2 to "Selasa", 3 to "Rabu", 4 to "Kamis", 5 to "Jumat", 6 to "Sabtu", 7 to "Minggu"
    )

    val subjectPresets = listOf(
        "Matematika", "Bahasa Indonesia", "Bahasa Inggris", "IPA / Fisika",
        "Biologi", "IPS", "Informatika", "Istirahat", "Olahraga"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GlassModalBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderStandard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (initialSchedule == null) "Tambah Slot Jadwal 📝" else "Edit Slot Jadwal ✏️",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary
                )
                Text(
                    text = "Sesuaikan mata pelajaran, jam, dan ruang",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Day of Week Picker
                Text(text = "Hari", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    days.forEach { (dInt, dName) ->
                        val isSelected = dayOfWeek == dInt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanAccent.copy(alpha = 0.25f) else GlassCardFill)
                                .border(1.dp, if (isSelected) CyanAccent else GlassCardBorder, RoundedCornerShape(10.dp))
                                .clickable { dayOfWeek = dInt }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = dName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanAccent else TextWhitePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Name
                Text(text = "Mata Pelajaran / Kegiatan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = {
                        subject = it
                        if (it.contains("Istirahat", ignoreCase = true)) {
                            isBreak = true
                        }
                    },
                    placeholder = { Text("Contoh: Matematika Wajib", color = TextMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedContainerColor = GlassCardFill,
                        unfocusedContainerColor = GlassCardFill,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    ),
                    singleLine = true
                )

                // Quick Subject Presets
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjectPresets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LavenderAccent.copy(alpha = 0.15f))
                                .clickable {
                                    subject = preset
                                    if (preset == "Istirahat") {
                                        isBreak = true
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = preset, fontSize = 11.sp, color = LavenderAccent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Slot Istirahat Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isBreak) Color(0xFFF59E0B).copy(alpha = 0.15f) else GlassCardFill)
                        .border(1.dp, if (isBreak) Color(0xFFF59E0B).copy(alpha = 0.4f) else GlassCardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            isBreak = !isBreak
                            if (isBreak && subject.isBlank()) {
                                subject = "Istirahat"
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isBreak,
                        onCheckedChange = {
                            isBreak = it
                            if (it && subject.isBlank()) {
                                subject = "Istirahat"
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Tandai sebagai Slot Istirahat ☕",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBreak) Color(0xFFF59E0B) else TextWhitePrimary
                        )
                        Text(
                            text = "Waktu jeda santai, makan, atau sholat",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time Inputs: Jam Mulai & Jam Selesai
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Jam Mulai", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            placeholder = { Text("07:15", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = GlassCardBorder,
                                focusedContainerColor = GlassCardFill,
                                unfocusedContainerColor = GlassCardFill,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            singleLine = true
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Jam Selesai", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            placeholder = { Text("08:45", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = GlassCardBorder,
                                focusedContainerColor = GlassCardFill,
                                unfocusedContainerColor = GlassCardFill,
                                focusedTextColor = TextWhitePrimary,
                                unfocusedTextColor = TextWhitePrimary
                            ),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Room or Teacher
                Text(text = "Ruang Kelas / Guru (Opsional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = roomOrTeacher,
                    onValueChange = { roomOrTeacher = it },
                    placeholder = { Text("Contoh: Ruang 12 • Bu Sri", color = TextMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedContainerColor = GlassCardFill,
                        unfocusedContainerColor = GlassCardFill,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassCardFill,
                            contentColor = TextWhitePrimary
                        )
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (subject.isNotBlank()) {
                                onSave(
                                    initialSchedule?.id ?: 0L,
                                    dayOfWeek,
                                    subject,
                                    startTime,
                                    endTime,
                                    roomOrTeacher,
                                    isBreak,
                                    colorHex
                                )
                            }
                        },
                        enabled = subject.isNotBlank(),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color(0xFF041E2B)
                        )
                    ) {
                        Text(
                            text = if (initialSchedule == null) "Simpan Slot" else "Perbarui Slot",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
