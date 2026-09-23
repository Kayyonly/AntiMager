package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.service.ExtractedScheduleItem
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.MintAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhitePrimary

@Composable
fun ScanOptionDialog(
    userClass: String,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit,
    onUseSingleClassSample: () -> Unit,
    onUseMultiClassSample: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(LavenderAccent.copy(alpha = 0.2f))
                                .border(1.dp, LavenderAccent.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LavenderAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Scan Jadwal Pelajaran",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "Filter Otomatis: Kelas $userClass ✨",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanAccent
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Pilih foto jadwal yang ingin di-scan. Jika foto berisi banyak kelas sekaligus, AI otomatis memfilter khusus untuk kelas $userClass.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Option 1: Buka Kamera
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassCardFill)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(16.dp))
                        .clickable { onTakePhoto() }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ambil Foto via Kamera 📸",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "Foto kertas atau papan tulis kelas",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 2: Upload dari Galeri
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassCardFill)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(16.dp))
                        .clickable { onPickGallery() }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LavenderAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = LavenderAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pilih dari Galeri Foto 🖼️",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "Upload screenshot atau file foto jadwal",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 3: Simulasi Jadwal Multi-Kelas (Tabel Gabungan)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LavenderAccent.copy(alpha = 0.12f))
                        .border(1.dp, LavenderAccent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .clickable { onUseMultiClassSample() }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LavenderAccent.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = LavenderAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tes Jadwal Gabungan Banyak Kelas 🏫",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LavenderAccent
                            )
                            Text(
                                text = "Tabel multi-kolom (X IPA 1, X IPA 2, X IPS 1)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 4: Simulasi Jadwal 1 Kelas Biasa
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MintAccent.copy(alpha = 0.12f))
                        .border(1.dp, MintAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable { onUseSingleClassSample() }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MintAccent.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MintAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tes Jadwal 1 Kelas Biasa 📄",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintAccent
                            )
                            Text(
                                text = "Tabel jadwal standar untuk satu kelas saja",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog when user's configured class was NOT found in the multi-class table photo.
 * User can re-enter/echo their class name, or pick directly from the detected classes in the photo!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClassMismatchDialog(
    userClass: String,
    detectedClasses: List<String>,
    scannedBitmap: Bitmap?,
    onDismiss: () -> Unit,
    onSelectClass: (chosenClass: String, updateProfile: Boolean) -> Unit
) {
    var manualInputClass by remember { mutableStateOf("") }
    var updateProfileChecked by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Kelas Tidak Ditemukan ⚠️",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "Jadwal gabungan banyak kelas terdeteksi",
                                fontSize = 11.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explanatory Message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Foto ini berisi jadwal dari beberapa kelas, tetapi nama kelas kamu \"$userClass\" tidak cocok dengan data di tabel foto. Silakan pilih kelasmu dari daftar yang terbaca di bawah, atau ketik ulang nama kelasnya.",
                        fontSize = 12.sp,
                        color = TextWhitePrimary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detected Classes Chips in Photo
                Text(
                    text = "Daftar Kelas yang Terbaca di Foto (${detectedClasses.size} Kelas):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (detectedClasses.isEmpty()) {
                    Text(
                        text = "Tidak ada nama kelas spesifik yang dapat dikenali secara otomatis.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        detectedClasses.forEach { cls ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyanAccent.copy(alpha = 0.2f))
                                    .border(1.2.dp, CyanAccent, RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelectClass(cls, updateProfileChecked)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cls,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhitePrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Or manual input field (Echo / ketik ulang nama kelas)
                Text(
                    text = "Atau Ketik Ulang Nama Kelas Kamu:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = manualInputClass,
                    onValueChange = { manualInputClass = it },
                    placeholder = { Text("Contoh: X IPA 1 atau 10-A", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary,
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.6f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.6f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Checkbox: Jadikan sebagai profil kelas saya sekarang
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = updateProfileChecked,
                        onCheckedChange = { updateProfileChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanAccent)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Simpan juga sebagai Profil Kelas Saya di Pengaturan",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassCardFill,
                            contentColor = TextWhitePrimary
                        )
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (manualInputClass.isNotBlank()) {
                                onSelectClass(manualInputClass.trim(), updateProfileChecked)
                            }
                        },
                        enabled = manualInputClass.isNotBlank(),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color(0xFF041E2B)
                        )
                    ) {
                        Text(
                            text = "Filter Kelas Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScanningProgressDialog() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(68.dp)
                            .rotate(rotation),
                        color = CyanAccent,
                        trackColor = CyanAccent.copy(alpha = 0.2f),
                        strokeWidth = 4.dp
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = LavenderAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Menganalisis Foto Jadwal...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Gemini Vision membaca teks, mendeteksi kelas, dan memfilter jadwal sesuai profilmu",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * Enhanced Confirmation Dialog:
 * Shows the preview of the filtered schedule items, with clear multi-class badge,
 * and allows user to edit before saving into Room Database!
 */
@Composable
fun ScheduleScanConfirmDialog(
    extractedItems: List<ExtractedScheduleItem>,
    scannedBitmap: Bitmap?,
    isMultiClass: Boolean,
    matchedClass: String,
    detectedClasses: List<String>,
    onDismiss: () -> Unit,
    onChangeClass: () -> Unit,
    onUpdateItem: (index: Int, updated: ExtractedScheduleItem) -> Unit,
    onDeleteItem: (index: Int) -> Unit,
    onConfirmSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0B1120),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassCardBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Preview Hasil Filter Scan",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyanAccent.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${extractedItems.size} Slot",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                        }
                        Text(
                            text = "Periksa keakuratan data sebelum disimpan ke database",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Multi-class status banner
                if (isMultiClass && matchedClass.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LavenderAccent.copy(alpha = 0.16f))
                            .border(1.dp, LavenderAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LavenderAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Jadwal Gabungan Difilter Khusus: $matchedClass ✨",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LavenderAccent
                                    )
                                    if (detectedClasses.size > 1) {
                                        Text(
                                            text = "Tersedia juga: ${detectedClasses.filter { it != matchedClass }.take(3).joinToString(", ")}",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            if (detectedClasses.size > 1) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable { onChangeClass() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Ganti Kelas",
                                        fontSize = 11.sp,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Scanned Thumbnail preview if available
                if (scannedBitmap != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassCardFill)
                            .border(1.dp, GlassCardBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = scannedBitmap.asImageBitmap(),
                            contentDescription = "Foto Jadwal",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Foto Terbaca & Tervalidasi ✓",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MintAccent
                            )
                            Text(
                                text = "Bisa edit nama pelajaran, jam, atau ruang jika perlu koreksi",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Extracted Items Editable List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(extractedItems) { index, item ->
                        EditableScheduleItemCard(
                            item = item,
                            onUpdate = { updated -> onUpdateItem(index, updated) },
                            onDelete = { onDeleteItem(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassCardFill,
                            contentColor = TextWhitePrimary
                        )
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = onConfirmSave,
                        enabled = extractedItems.isNotEmpty(),
                        modifier = Modifier
                            .weight(1.6f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color(0xFF041E2B)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simpan ke Jadwal (${extractedItems.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditableScheduleItemCard(
    item: ExtractedScheduleItem,
    onUpdate: (ExtractedScheduleItem) -> Unit,
    onDelete: () -> Unit
) {
    var subject by remember(item.subject) { mutableStateOf(item.subject) }
    var startTime by remember(item.startTime) { mutableStateOf(item.startTime) }
    var endTime by remember(item.endTime) { mutableStateOf(item.endTime) }
    var roomOrTeacher by remember(item.roomOrTeacher) { mutableStateOf(item.roomOrTeacher) }
    var dayOfWeek by remember(item.dayOfWeek) { mutableStateOf(item.dayOfWeek) }
    var isBreak by remember(item.isBreak) { mutableStateOf(item.isBreak) }

    val days = listOf(
        1 to "Senin", 2 to "Selasa", 3 to "Rabu", 4 to "Kamis", 5 to "Jumat", 6 to "Sabtu", 7 to "Minggu"
    )

    val accentColor = if (isBreak) Color(0xFFF59E0B) else CyanAccent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isBreak) Color(0xFFF59E0B).copy(alpha = 0.10f) else GlassCardFill)
            .border(
                1.dp,
                if (isBreak) Color(0xFFF59E0B).copy(alpha = 0.4f) else GlassCardBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            // Top Row: Day Selector chips & Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day chips
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.forEach { (dInt, dStr) ->
                        val isSelected = dayOfWeek == dInt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.5f))
                                .border(1.dp, if (isSelected) accentColor else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    dayOfWeek = dInt
                                    onUpdate(
                                        item.copy(
                                            dayOfWeek = dInt,
                                            dayName = dStr,
                                            subject = subject,
                                            startTime = startTime,
                                            endTime = endTime,
                                            roomOrTeacher = roomOrTeacher,
                                            isBreak = isBreak
                                        )
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = dStr,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) accentColor else TextSecondary
                            )
                        }
                    }
                }

                // Delete item button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject and Break Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = {
                        subject = it
                        val autoBreak = it.contains("Istirahat", ignoreCase = true)
                        if (autoBreak) isBreak = true
                        onUpdate(
                            item.copy(
                                subject = it,
                                isBreak = isBreak,
                                dayOfWeek = dayOfWeek,
                                startTime = startTime,
                                endTime = endTime,
                                roomOrTeacher = roomOrTeacher
                            )
                        )
                    },
                    label = { Text("Mata Pelajaran", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary,
                        focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.7f),
                        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Toggle Istirahat chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isBreak) Color(0xFFF59E0B).copy(alpha = 0.25f) else GlassCardFill)
                        .border(1.dp, if (isBreak) Color(0xFFF59E0B) else GlassCardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            isBreak = !isBreak
                            onUpdate(
                                item.copy(
                                    isBreak = isBreak,
                                    subject = if (isBreak && subject.isBlank()) "Istirahat" else subject,
                                    dayOfWeek = dayOfWeek,
                                    startTime = startTime,
                                    endTime = endTime,
                                    roomOrTeacher = roomOrTeacher
                                )
                            )
                        }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = null,
                            tint = if (isBreak) Color(0xFFF59E0B) else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBreak) "Istirahat" else "Pelajaran",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBreak) Color(0xFFF59E0B) else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time and Room row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = {
                        startTime = it
                        onUpdate(
                            item.copy(
                                startTime = it,
                                endTime = endTime,
                                subject = subject,
                                dayOfWeek = dayOfWeek,
                                isBreak = isBreak,
                                roomOrTeacher = roomOrTeacher
                            )
                        )
                    },
                    label = { Text("Mulai", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary,
                        focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.7f),
                        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = endTime,
                    onValueChange = {
                        endTime = it
                        onUpdate(
                            item.copy(
                                endTime = it,
                                startTime = startTime,
                                subject = subject,
                                dayOfWeek = dayOfWeek,
                                isBreak = isBreak,
                                roomOrTeacher = roomOrTeacher
                            )
                        )
                    },
                    label = { Text("Selesai", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary,
                        focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.7f),
                        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = roomOrTeacher,
                    onValueChange = {
                        roomOrTeacher = it
                        onUpdate(
                            item.copy(
                                roomOrTeacher = it,
                                startTime = startTime,
                                endTime = endTime,
                                subject = subject,
                                dayOfWeek = dayOfWeek,
                                isBreak = isBreak
                            )
                        )
                    },
                    label = { Text("Ruang / Guru", fontSize = 10.sp) },
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary,
                        focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.7f),
                        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )
            }
        }
    }
}

/**
 * Creates a synthetic schedule table image bitmap for 1 single class
 */
fun createSampleTimetableBitmap(): Bitmap {
    val width = 800
    val height = 600
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#F8FAFC") }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val borderPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#CBD5E1")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    canvas.drawRect(20f, 20f, (width - 20).toFloat(), (height - 20).toFloat(), borderPaint)

    val titlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#0F172A")
        textSize = 32f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText("JADWAL PELAJARAN KELAS XI", 40f, 70f, titlePaint)

    val subtitlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#64748B")
        textSize = 20f
        isAntiAlias = true
    }
    canvas.drawText("Semester Ganjil 2026/2027", 40f, 105f, subtitlePaint)

    val headerBg = Paint().apply { color = android.graphics.Color.parseColor("#E2E8F0") }
    canvas.drawRect(40f, 130f, 760f, 175f, headerBg)

    val textPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E293B")
        textSize = 20f
        isAntiAlias = true
    }
    val boldPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#0F172A")
        textSize = 20f
        isFakeBoldText = true
        isAntiAlias = true
    }

    canvas.drawText("HARI", 50f, 160f, boldPaint)
    canvas.drawText("WAKTU", 160f, 160f, boldPaint)
    canvas.drawText("MATA PELAJARAN", 340f, 160f, boldPaint)
    canvas.drawText("RUANG / GURU", 580f, 160f, boldPaint)

    val rows = listOf(
        "Senin" to listOf("07:15 - 08:45", "Fisika Modern", "Lab Fisika • Bu Dewi"),
        "Senin" to listOf("08:45 - 10:15", "Matematika Peminatan", "Ruang 10 • Pak Bambang"),
        "Senin" to listOf("10:15 - 10:45", "ISTIRAHAT", "Kantin"),
        "Senin" to listOf("10:45 - 12:15", "Bahasa Inggris Lanjutan", "Ruang 10 • Mr. John"),
        "Selasa" to listOf("07:15 - 08:45", "Biologi Genetika", "Lab Biologi • Bu Ratna"),
        "Selasa" to listOf("08:45 - 09:30", "ISTIRAHAT", "Kantin"),
        "Selasa" to listOf("09:30 - 11:00", "Kimia Larutan", "Lab Kimia • Pak Dani")
    )

    var currentY = 215f
    val linePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#E2E8F0")
        strokeWidth = 2f
    }

    rows.forEach { (day, data) ->
        val isBreak = data[1] == "ISTIRAHAT"
        if (isBreak) {
            val breakBg = Paint().apply { color = android.graphics.Color.parseColor("#FEF3C7") }
            canvas.drawRect(40f, currentY - 28f, 760f, currentY + 12f, breakBg)
        }

        canvas.drawText(day, 50f, currentY, textPaint)
        canvas.drawText(data[0], 160f, currentY, textPaint)
        val subjectPaint = if (isBreak) Paint(textPaint).apply { color = android.graphics.Color.parseColor("#D97706"); isFakeBoldText = true } else textPaint
        canvas.drawText(data[1], 340f, currentY, subjectPaint)
        canvas.drawText(data[2], 580f, currentY, textPaint)

        canvas.drawLine(40f, currentY + 18f, 760f, currentY + 18f, linePaint)
        currentY += 50f
    }

    return bitmap
}

/**
 * Creates a synthetic MASTER MULTI-CLASS timetable image bitmap
 * Table with multiple class columns (e.g. WAKTU | X IPA 1 | X IPA 2 | X IPS 1)
 * Perfect for instantaneously testing AI multi-class detection and filtering!
 */
fun createSampleMultiClassTimetableBitmap(): Bitmap {
    val width = 900
    val height = 650
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#F1F5F9") }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val borderPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#94A3B8")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    canvas.drawRect(15f, 15f, (width - 15).toFloat(), (height - 15).toFloat(), borderPaint)

    val titlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#0F172A")
        textSize = 28f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText("JADWAL PELAJARAN MASTER KELAS X & XI", 35f, 55f, titlePaint)

    val subtitlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#475569")
        textSize = 17f
        isAntiAlias = true
    }
    canvas.drawText("Tabel Jadwal Gabungan Seluruh Kelas • Semester 1", 35f, 85f, subtitlePaint)

    // Table Header with multi-class columns
    val headerBg = Paint().apply { color = android.graphics.Color.parseColor("#38BDF8") }
    canvas.drawRect(35f, 110f, 865f, 155f, headerBg)

    val headerTextPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#0F172A")
        textSize = 17f
        isFakeBoldText = true
        isAntiAlias = true
    }

    canvas.drawText("WAKTU", 45f, 138f, headerTextPaint)
    canvas.drawText("X IPA 1", 200f, 138f, headerTextPaint)
    canvas.drawText("X IPA 2", 420f, 138f, headerTextPaint)
    canvas.drawText("X IPS 1", 640f, 138f, headerTextPaint)

    val rows = listOf(
        "07:15 - 08:45" to listOf("Kimia (Lab)", "Fisika (Lab)", "Sosiologi (R14)"),
        "08:45 - 10:15" to listOf("Matematika", "Matematika", "Ekonomi (R14)"),
        "10:15 - 10:45" to listOf("ISTIRAHAT", "ISTIRAHAT", "ISTIRAHAT"),
        "10:45 - 12:15" to listOf("B. Inggris", "B. Inggris", "Geografi (R14)"),
        "12:15 - 13:00" to listOf("ISTIRAHAT", "ISTIRAHAT", "ISTIRAHAT"),
        "13:00 - 14:30" to listOf("Biologi (Lab)", "Kimia (Lab)", "Sejarah (R14)")
    )

    var currentY = 195f
    val cellTextPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E293B")
        textSize = 16f
        isAntiAlias = true
    }

    val linePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#CBD5E1")
        strokeWidth = 2f
    }

    rows.forEach { (time, classData) ->
        val isBreak = classData[0] == "ISTIRAHAT"
        if (isBreak) {
            val breakBg = Paint().apply { color = android.graphics.Color.parseColor("#FEF3C7") }
            canvas.drawRect(35f, currentY - 26f, 865f, currentY + 12f, breakBg)
        }

        canvas.drawText(time, 45f, currentY, cellTextPaint)
        val textPaintToUse = if (isBreak) Paint(cellTextPaint).apply { color = android.graphics.Color.parseColor("#B45309"); isFakeBoldText = true } else cellTextPaint

        canvas.drawText(classData[0], 200f, currentY, textPaintToUse)
        canvas.drawText(classData[1], 420f, currentY, textPaintToUse)
        canvas.drawText(classData[2], 640f, currentY, textPaintToUse)

        canvas.drawLine(35f, currentY + 18f, 865f, currentY + 18f, linePaint)
        currentY += 50f
    }

    return bitmap
}
