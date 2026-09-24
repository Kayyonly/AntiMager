package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Shield
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemBlueSubtle
import com.example.ui.theme.AppleTextTertiary
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderStandard
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassShadowAmbient
import com.example.ui.theme.GlassShadowSpot
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGray1
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosShadowColor
import com.example.ui.theme.IosSurfaceCard
import com.example.ui.theme.IosSystemBackground
import com.example.ui.theme.IosTextPrimary
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
    AI_CHAT("AI Asisten", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    HABITS("Kebiasaan", Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment),
    SHIELD("Proteksi", Icons.Filled.Shield, Icons.Outlined.Shield)
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

    Scaffold(
        containerColor = IosSystemBackground,
        contentColor = IosTextPrimary,
        bottomBar = {
            if (!isSettingsOpen) {
                IosBottomNavigationBar(
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
                            MainTab.SHIELD -> LocationAndSettingsScreen(
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

/**
 * Apple iOS Modern Liquid Glass Floating Bottom Navigation Bar
 * Translucent floating dock with specular top chamfer highlight, balanced spacing, and refined active pill.
 */
@Composable
fun IosBottomNavigationBar(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val dockShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = dockShape,
                    spotColor = Color(0x33000000),
                    ambientColor = Color(0x20000000)
                )
                .clip(dockShape)
                .background(Color(0xF0141417))
                .border(
                    width = 0.6.dp,
                    color = GlassBorderSubtle,
                    shape = dockShape
                )
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectTab(tab) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) AppleSystemBlue else AppleTextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) AppleSystemBlue else AppleTextTertiary,
                                letterSpacing = (-0.1).sp
                            )
                        }
                    }
                }
            }
        }
    }
}
