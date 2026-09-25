package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GlassBackground
import com.example.ui.components.LiquidGlassTokens
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

@OptIn(ExperimentalLayoutApi::class)
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
    val isImeVisible = WindowInsets.isImeVisible

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = IosTextPrimary,
            bottomBar = {
                AnimatedVisibility(
                    visible = !isSettingsOpen && !isImeVisible,
                    enter = slideInVertically(
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                    ) { it } + fadeIn(animationSpec = tween(durationMillis = 200)),
                    exit = slideOutVertically(
                        animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                    ) { it } + fadeOut(animationSpec = tween(durationMillis = 150))
                ) {
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
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                Crossfade(targetState = isSettingsOpen, label = "settings_overlay_transition") { settingsActive ->
                    if (settingsActive) {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { isSettingsOpen = false }
                        )
                    } else {
                        Crossfade(targetState = selectedTab, label = "tab_transition") { tab ->
                            val tabModifier = if (tab == MainTab.AI_CHAT) {
                                Modifier.padding(bottom = if (!isImeVisible) paddingValues.calculateBottomPadding() else 0.dp)
                            } else {
                                Modifier
                            }
                            when (tab) {
                                MainTab.TASKS -> TaskListScreen(viewModel = taskViewModel, modifier = tabModifier)
                                MainTab.SCHEDULE -> ScheduleScreen(
                                    viewModel = scheduleViewModel,
                                    onOpenSettings = { isSettingsOpen = true },
                                    modifier = tabModifier
                                )
                                MainTab.AI_CHAT -> AiChatScreen(viewModel = aiChatViewModel, modifier = tabModifier)
                                MainTab.HABITS -> HabitScreen(viewModel = habitViewModel, modifier = tabModifier)
                                MainTab.SHIELD -> LocationAndSettingsScreen(
                                    viewModel = taskViewModel,
                                    onOpenSettings = { isSettingsOpen = true },
                                    modifier = tabModifier
                                )
                            }
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
    val dockShape = RoundedCornerShape(30.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = dockShape,
                    spotColor = Color(0x6A000000),
                    ambientColor = Color(0x36000000)
                )
                .clip(dockShape)
                .background(LiquidGlassTokens.GlassDockSurfaceBrush)
                .background(LiquidGlassTokens.GlassCardSheenBrush)
                .border(
                    width = 0.75.dp,
                    brush = LiquidGlassTokens.GlassSpecularBorderBrushElevated,
                    shape = dockShape
                )
                .padding(horizontal = 5.dp, vertical = 5.dp)
        ) {
            // Bright top reflection gives the dock the same "floating glass" read
            // as native iOS chrome while keeping the center visually quiet.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.72f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x66FFFFFF),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainTab.entries.forEach { tab ->
                    val selected = tab == selectedTab
                    val itemShape = RoundedCornerShape(20.dp)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(itemShape)
                            .background(
                                if (selected) Color(0x2BFFFFFF)
                                else Color.Transparent
                            )
                            .then(
                                if (selected) {
                                    Modifier.border(
                                        width = 0.6.dp,
                                        color = Color(0x3FFFFFFF),
                                        shape = itemShape
                                    )
                                } else Modifier
                            )
                            .clickable { onSelectTab(tab) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (selected) AppleSystemBlue else AppleTextTertiary,
                                modifier = Modifier.size(if (selected) 21.dp else 20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                fontSize = 9.5.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) AppleTextPrimary else AppleTextTertiary,
                                letterSpacing = (-0.15).sp
                            )
                        }
                    }
                }
            }
        }
    }
}
