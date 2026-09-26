package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.HabitEntity
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.LiquidGlassTokens
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.AppleSystemIndigo
import com.example.ui.theme.AppleSystemOrange
import com.example.ui.theme.AppleTextMuted
import com.example.ui.theme.AppleTextPlaceholder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderStandard
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassLayer1
import com.example.ui.theme.GlassLayer2
import com.example.ui.theme.GlassModalBackground
import com.example.ui.theme.LiquidDarkBackground
import com.example.ui.theme.LiquidDarkCard
import com.example.ui.viewmodel.HabitViewModel

@Composable
fun HabitScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val todayEpochDay = remember { System.currentTimeMillis() / (1000 * 60 * 60 * 24) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // iOS Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Hari ini",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppleTextSecondary
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "Kebiasaan",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        letterSpacing = (-0.8).sp
                    )
                }

                GlassIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Tambah kebiasaan",
                    onClick = { showAddDialog = true },
                    tint = AppleSystemBlue,
                    containerColor = Color(0x1FFFFFFF)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Compact Health-style overview in one continuous glass panel.
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HabitStat(
                        icon = Icons.Default.LocalFireDepartment,
                        value = uiState.totalActiveStreaks.toString(),
                        label = "Aktif",
                        accent = AppleSystemOrange,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(46.dp)
                            .background(Color(0x20FFFFFF))
                    )

                    HabitStat(
                        icon = Icons.Default.Bolt,
                        value = "${uiState.bestStreakOverall} hari",
                        label = "Rekor",
                        accent = AppleSystemIndigo,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(46.dp)
                            .background(Color(0x20FFFFFF))
                    )

                    HabitStat(
                        icon = Icons.Default.Check,
                        value = "${uiState.todayCompletedCount}/${uiState.habits.size}",
                        label = "Hari ini",
                        accent = AppleSystemGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habits List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                if (uiState.habits.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "🌱", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Mulai Kebiasaan Baru",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleTextPrimary
                                )
                                Text(
                                    text = "Ketuk tombol + di atas untuk mencatat habit harianmu.",
                                    fontSize = 13.sp,
                                    color = AppleTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.habits, key = { it.id }) { habit ->
                        val isCompletedToday = habit.lastCompletedEpochDay == todayEpochDay

                        HabitItemCard(
                            habit = habit,
                            isCompletedToday = isCompletedToday,
                            onCheckIn = {
                                viewModel.checkIn(habit.id)
                                val msg = if (isCompletedToday) "Check-in dibatalkan" else "🔥 Streak bertambah! Hebat!"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                viewModel.deleteHabit(habit)
                                Toast.makeText(context, "Habit dihapus", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onAddHabit = { name, desc, icon, color ->
                viewModel.addHabit(name, desc, icon, color)
                Toast.makeText(context, "Habit baru ditambahkan!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun HabitStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppleTextPrimary,
            letterSpacing = (-0.25).sp
        )
        Text(
            text = label,
            fontSize = 10.5.sp,
            color = AppleTextSecondary
        )
    }
}

@Composable
fun HabitItemCard(
    habit: HabitEntity,
    isCompletedToday: Boolean,
    onCheckIn: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        AppleSystemBlue
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LiquidGlassTokens.RadiusCard),
        borderColor = if (isCompletedToday) GlassBorderSubtle else GlassBorderStandard,
        elevation = if (isCompletedToday) 1.dp else 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Habit Icon Capsule
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x16FFFFFF))
                    .border(0.7.dp, Color(0x2AFFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (habit.iconName) {
                    "water" -> Icon(Icons.Default.WaterDrop, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                    "clean" -> Icon(Icons.Default.CleaningServices, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                    else -> Icon(Icons.Default.Bolt, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompletedToday) AppleTextMuted else AppleTextPrimary,
                    letterSpacing = (-0.3).sp
                )

                if (habit.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = habit.description,
                        fontSize = 12.sp,
                        color = AppleTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Streak flame counter and best record
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x12FFFFFF))
                            .border(0.6.dp, Color(0x20FFFFFF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = AppleSystemOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${habit.streakCount} Hari",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleSystemOrange
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Rekor: ${habit.bestStreak} hari",
                        fontSize = 11.sp,
                        color = AppleTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Delete Action
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(LiquidGlassTokens.RadiusSmall))
                    .background(GlassLayer1)
                    .border(0.8.dp, GlassBorderSubtle, RoundedCornerShape(LiquidGlassTokens.RadiusSmall))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Hapus Habit",
                    tint = AppleTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Circular Check-in Button (iOS tactile style)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isCompletedToday) AppleSystemGreen else Color(0x10FFFFFF))
                    .border(
                        width = if (isCompletedToday) 0.dp else 1.5.dp,
                        color = if (isCompletedToday) Color.Transparent else Color(0x55FFFFFF),
                        shape = CircleShape
                    )
                    .clickable { onCheckIn() },
                contentAlignment = Alignment.Center
            ) {
                if (isCompletedToday) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selesai",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAddHabit: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("bolt") }
    var selectedColor by remember { mutableStateOf("#0A84FF") }

    val iconOptions = listOf(
        "bolt" to "Energi ⚡",
        "water" to "Minum Air 💧",
        "clean" to "Beres-Beres 🧹"
    )

    val colorOptions = listOf(
        "#0A84FF" to "Biru",
        "#30D158" to "Hijau",
        "#FF9F0A" to "Oranye",
        "#BF5AF2" to "Ungu",
        "#FF453A" to "Merah"
    )

    val dialogShape = RoundedCornerShape(LiquidGlassTokens.RadiusPanel)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = dialogShape,
            color = GlassModalBackground,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(GlassBorderHighlight, GlassBorderStandard, Color(0x10FFFFFF))
                    ),
                    shape = dialogShape
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Habit Baru",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "Langkah kecil sehari-hari untuk konsistensi",
                    fontSize = 12.sp,
                    color = AppleTextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Habit", fontSize = 12.sp) },
                    placeholder = { Text("Misal: Baca buku 15 menit", fontSize = 13.sp, color = AppleTextPlaceholder) },
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

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi / Target (Opsional)", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: Setiap sebelum tidur", fontSize = 13.sp, color = AppleTextPlaceholder) },
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

                // Icon Picker
                Text(text = "Pilih Ikon", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppleTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.forEach { (iconKey, label) ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(LiquidGlassTokens.RadiusSmall))
                                .background(if (isSelected) AppleSystemBlue.copy(alpha = 0.2f) else GlassLayer1)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) AppleSystemBlue else GlassBorderSubtle,
                                    shape = RoundedCornerShape(LiquidGlassTokens.RadiusSmall)
                                )
                                .clickable { selectedIcon = iconKey }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AppleSystemBlue else AppleTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Picker
                Text(text = "Pilih Warna", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppleTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorOptions.forEach { (colorHex, _) ->
                        val color = Color(android.graphics.Color.parseColor(colorHex))
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassButton(
                        text = "Batal",
                        onClick = onDismiss,
                        variant = GlassButtonVariant.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )

                    GlassButton(
                        text = "Simpan",
                        onClick = {
                            if (name.isNotBlank()) {
                                onAddHabit(name, description, selectedIcon, selectedColor)
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank(),
                        variant = GlassButtonVariant.PRIMARY,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
