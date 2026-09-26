package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.IosSegmentedControl
import com.example.ui.components.LiquidGlassTokens
import com.example.ui.components.QuickAddBottomSheet
import com.example.ui.components.TaskCard
import com.example.ui.components.VoiceCommandDialog
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextPlaceholder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.AppleTextTertiary
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
        SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID")).format(Date())
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Large-title header, close to the visual language of iOS system apps.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todayFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppleTextSecondary
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "Tugas",
                        fontSize = 34.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        letterSpacing = (-0.8).sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassIconButton(
                        icon = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Cari tugas",
                        onClick = { showSearchBar = !showSearchBar },
                        tint = if (showSearchBar) AppleSystemBlue else AppleTextPrimary,
                        containerColor = Color(0x1FFFFFFF)
                    )
                    GlassIconButton(
                        icon = Icons.Default.Mic,
                        contentDescription = "Perintah suara",
                        onClick = { showVoiceDialog = true },
                        tint = AppleSystemBlue,
                        containerColor = Color(0x1FFFFFFF)
                    )
                }
            }

            AnimatedVisibility(visible = showSearchBar) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = 3.dp
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::setSearchQuery,
                            placeholder = {
                                Text(
                                    "Cari tugas, mapel, atau lokasi",
                                    fontSize = 14.sp,
                                    color = AppleTextPlaceholder
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = AppleTextTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Hapus pencarian",
                                            tint = AppleTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = AppleTextPrimary,
                                unfocusedTextColor = AppleTextPrimary,
                                cursorColor = AppleSystemBlue
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // One quiet glass overview instead of several loud dashboard cards.
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 15.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Belum selesai",
                                fontSize = 12.sp,
                                color = AppleTextSecondary
                            )
                            Text(
                                text = uiState.totalPending.toString(),
                                fontSize = 27.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleTextPrimary,
                                letterSpacing = (-0.6).sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color(0x24FFFFFF))
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 18.dp)
                        ) {
                            Text(
                                "Mendesak",
                                fontSize = 12.sp,
                                color = AppleTextSecondary
                            )
                            Text(
                                text = uiState.urgentCount.toString(),
                                fontSize = 27.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.urgentCount > 0) AppleSystemRed else AppleTextPrimary,
                                letterSpacing = (-0.6).sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x1AFFFFFF))
                    )
                    Spacer(modifier = Modifier.height(11.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.triggerGeminiAiSort() }
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0x220A84FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isAiSortingLoading) {
                                CircularProgressIndicator(
                                    color = AppleSystemBlue,
                                    strokeWidth = 1.8.dp,
                                    modifier = Modifier.size(15.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = AppleSystemBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Smart Priority",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleTextPrimary
                            )
                            Text(
                                text = if (uiState.isAiSortingLoading) {
                                    "Mengurutkan fokus…"
                                } else {
                                    uiState.aiGlobalAdvice ?: "Urutkan tugas berdasarkan urgensi"
                                },
                                fontSize = 12.sp,
                                color = AppleTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            IosSegmentedControl(
                items = listOf(TaskFilter.PENDING, TaskFilter.ALL, TaskFilter.COMPLETED),
                selectedItem = uiState.filter,
                onItemSelected = viewModel::setFilter,
                itemLabel = { it.label }
            )

            Spacer(modifier = Modifier.height(11.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 2.dp, bottom = 104.dp)
            ) {
                if (uiState.tasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 54.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x16FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = AppleTextTertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when (uiState.filter) {
                                    TaskFilter.PENDING -> "Semua beres"
                                    TaskFilter.ALL -> "Belum ada tugas"
                                    TaskFilter.COMPLETED -> "Belum ada yang selesai"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleTextSecondary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                "Tekan + untuk menambahkan tugas",
                                fontSize = 13.sp,
                                color = AppleTextTertiary
                            )
                        }
                    }
                } else {
                    items(items = uiState.tasks, key = { it.id }) { task ->
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

        // Floating glass add control: white-glass outer shell + iOS blue core.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 84.dp)
                .size(58.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    spotColor = Color(0x70000000),
                    ambientColor = Color(0x30000000)
                )
                .clip(CircleShape)
                .background(LiquidGlassTokens.GlassDockSurfaceBrush)
                .background(LiquidGlassTokens.GlassCardSheenBrush)
                .border(
                    width = 0.8.dp,
                    brush = LiquidGlassTokens.GlassSpecularBorderBrushElevated,
                    shape = CircleShape
                )
                .clickable { showQuickAddSheet = true },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF168CFF), Color(0xFF0071E3))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah tugas",
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

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
