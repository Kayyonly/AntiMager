package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GlassBackground
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.MintAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.viewmodel.AiChatViewModel
import com.example.ui.viewmodel.HabitViewModel
import com.example.ui.viewmodel.ScheduleViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.TaskViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    TASKS("Tugas", Icons.AutoMirrored.Filled.FormatListBulleted, Icons.AutoMirrored.Outlined.FormatListBulleted),
    SCHEDULE("Jadwal", Icons.Filled.DateRange, Icons.Outlined.DateRange),
    AI_CHAT("AI Chat", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    HABITS("Habit", Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment),
    LOCATION("Lokasi", Icons.Filled.LocationOn, Icons.Outlined.LocationOn)
}

@Composable
fun MainScreen(
    taskViewModel: TaskViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    aiChatViewModel: AiChatViewModel = viewModel(),
    habitViewModel: HabitViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(MainTab.TASKS) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = TextWhitePrimary,
            bottomBar = {
                if (!isSettingsOpen) {
                    FrostedGlassNavigationBar(
                        selectedTab = selectedTab,
                        onSelectTab = { selectedTab = it }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(targetState = isSettingsOpen, label = "settings_overlay_transition") { settingsActive ->
                    if (settingsActive) {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { isSettingsOpen = false }
                        )
                    } else {
                        Crossfade(targetState = selectedTab, label = "tab_transition") { tab ->
                            when (tab) {
                                MainTab.TASKS -> TaskListScreen(viewModel = taskViewModel)
                                MainTab.SCHEDULE -> ScheduleScreen(
                                    viewModel = scheduleViewModel,
                                    onOpenSettings = { isSettingsOpen = true }
                                )
                                MainTab.AI_CHAT -> AiChatScreen(viewModel = aiChatViewModel)
                                MainTab.HABITS -> HabitScreen(viewModel = habitViewModel)
                                MainTab.LOCATION -> LocationAndSettingsScreen(
                                    viewModel = taskViewModel,
                                    onOpenSettings = { isSettingsOpen = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FrostedGlassNavigationBar(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Floating Frosted Glass Capsule Bar with Liquid Glass styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E293B).copy(alpha = 0.70f),
                            Color(0xFF0F172A).copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.40f),
                            GlassCardBorder.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.10f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            // Top specular light reflection line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val activeColor = when (tab) {
                        MainTab.TASKS -> CyanAccent
                        MainTab.SCHEDULE -> Color(0xFF38BDF8)
                        MainTab.AI_CHAT -> LavenderAccent
                        MainTab.HABITS -> Color(0xFFFB923C)
                        MainTab.LOCATION -> MintAccent
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            activeColor.copy(alpha = 0.28f),
                                            activeColor.copy(alpha = 0.12f)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) activeColor.copy(alpha = 0.45f) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectTab(tab) }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) activeColor else TextMuted,
                                modifier = Modifier.size(19.dp)
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
