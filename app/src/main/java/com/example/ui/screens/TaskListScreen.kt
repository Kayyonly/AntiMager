package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.components.IosSegmentedControl
import com.example.ui.components.LiquidGlassTokens
import com.example.ui.components.QuickAddBottomSheet
import com.example.ui.components.TaskCard
import com.example.ui.components.VoiceCommandDialog
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextMuted
import com.example.ui.theme.AppleTextPlaceholder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.AppleTextTertiary
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.LiquidDarkBackground
import com.example.ui.theme.LiquidDarkCard
import com.example.ui.viewmodel.TaskFilter
import com.example.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showQuickAddSheet by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, dd MMMM", Locale("id", "ID"))
        sdf.format(Date())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Native Apple Large Title Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = todayFormatted.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tugas",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        letterSpacing = (-0.6).sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showSearchBar = !showSearchBar },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = if (showSearchBar) AppleSystemBlue else AppleTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showVoiceDialog = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Rekam Suara",
                            tint = AppleSystemBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Search Bar Input
            AnimatedVisibility(visible = showSearchBar) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Cari judul, mapel, atau lokasi...", fontSize = 14.sp, color = AppleTextPlaceholder) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleSystemBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFF1C1C1E),
                            unfocusedContainerColor = Color(0xFF1C1C1E),
                            focusedTextColor = AppleTextPrimary,
                            unfocusedTextColor = AppleTextPrimary
                        ),
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = AppleTextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quiet Native Status Summary (No heavy bordered boxes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141416))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BELUM SELESAI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextSecondary,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${uiState.totalPending}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleSystemBlue,
                        letterSpacing = (-0.4).sp
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(GlassBorderSubtle)
                )

                Column {
                    Text(
                        text = "MENDESAK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextSecondary,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${uiState.urgentCount}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.urgentCount > 0) AppleSystemRed else AppleTextPrimary,
                        letterSpacing = (-0.4).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smart Priority: Quiet Native Row (No neon gradients, no model advertising)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141416))
                    .clickable { viewModel.triggerGroqAiSort() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Priority",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextPrimary
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = if (uiState.isAiSortingLoading) "Menganalisis urgensi tugas..." else (uiState.aiGlobalAdvice ?: "Ketuk untuk menyortir urutan fokus"),
                        fontSize = 12.sp,
                        color = AppleTextSecondary,
                        maxLines = 1
                    )
                }

                if (uiState.isAiSortingLoading) {
                    CircularProgressIndicator(
                        color = AppleSystemBlue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Urutkan Prioritas",
                        tint = AppleTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // iOS Native Segmented Control
            IosSegmentedControl(
                items = listOf(TaskFilter.PENDING, TaskFilter.ALL, TaskFilter.COMPLETED),
                selectedItem = uiState.filter,
                onItemSelected = { viewModel.setFilter(it) },
                itemLabel = { it.label }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Task List (Apple Reminders flat list style)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                if (uiState.tasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when (uiState.filter) {
                                    TaskFilter.PENDING -> "Tidak ada tugas tertunda"
                                    TaskFilter.ALL -> "Daftar tugas kosong"
                                    TaskFilter.COMPLETED -> "Belum ada tugas selesai"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppleTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ketuk + untuk menambahkan tugas baru",
                                fontSize = 13.sp,
                                color = AppleTextTertiary
                            )
                        }
                    }
                } else {
                    items(
                        items = uiState.tasks,
                        key = { it.id }
                    ) { task ->
                        TaskCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskComplete(task) },
                            onSnooze = { viewModel.snoozeTask(task, 15) },
                            onDelete = { viewModel.deleteTask(task) },
                            onTriggerNotification = { viewModel.triggerPersistentNotification(task) },
                            aiRationale = uiState.taskAiRationaleMap[task.id],
                            aiBadge = uiState.taskAiBadgeMap[task.id]
                        )
                    }
                }
            }
        }

        // Native Refined FAB (Bottom Right)
        FloatingActionButton(
            onClick = { showQuickAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .size(50.dp),
            containerColor = AppleSystemBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Tugas",
                modifier = Modifier.size(24.dp)
            )
        }

        // Quick Add Bottom Sheet
        if (showQuickAddSheet) {
            QuickAddBottomSheet(
                onDismiss = { showQuickAddSheet = false },
                onAddTask = { title, subject, desc, deadline, duration, priority, isPersistent, location, locationTrigger ->
                    viewModel.addTask(
                        title = title,
                        subject = subject,
                        description = desc,
                        deadlineMillis = deadline,
                        estimatedMinutes = duration,
                        priority = priority,
                        isPersistent = isPersistent,
                        locationName = location,
                        locationTrigger = locationTrigger
                    )
                    showQuickAddSheet = false
                    Toast.makeText(context, "Tugas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Voice Command In-App Dialog
        if (showVoiceDialog) {
            VoiceCommandDialog(
                onDismiss = { showVoiceDialog = false },
                onSaveParsedTask = { parsed ->
                    viewModel.commitVoiceTask(parsed)
                    showVoiceDialog = false
                    Toast.makeText(context, "Tugas disimpan dari suara", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
