package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidGlassTokens
import com.example.ui.components.MarkdownText
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemBlueSubtle
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.AppleSystemIndigo
import com.example.ui.theme.AppleSystemOrange
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextMuted
import com.example.ui.theme.AppleTextPlaceholder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.AppleTextTertiary
import com.example.ui.theme.GlassBorderStandard
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassLayer1
import com.example.ui.theme.LiquidDarkBackground
import com.example.ui.theme.LiquidDarkCard
import com.example.ui.theme.LiquidGlassUserBubbleBrush
import com.example.ui.viewmodel.AiChatViewModel
import com.example.ui.viewmodel.ChatSender

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val isImeVisible = WindowInsets.isImeVisible

    // Auto-scroll to bottom on new message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Auto-scroll to bottom smoothly when keyboard appears
    LaunchedEffect(isImeVisible) {
        if (isImeVisible && uiState.messages.isNotEmpty()) {
            delay(120)
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognized = spokenMatches?.firstOrNull() ?: ""
            if (recognized.isNotBlank()) {
                viewModel.sendPrompt(recognized)
            }
        }
    }

    val quickPrompts = listOf(
        "PR IPS besok jam 8 pagi",
        "Nanti malam resume biologi 30m",
        "Jumat kumpul tugas IPA",
        "Beli binder di Indomaret",
        "Besok sebelum sekolah bawa seragam olahraga"
    )

    // Smooth layout transitions when keyboard toggles
    val headerTopSpacer by animateDpAsState(
        targetValue = if (isImeVisible) 6.dp else 16.dp,
        animationSpec = tween(220),
        label = "header_top_spacer"
    )
    val headerBottomSpacer by animateDpAsState(
        targetValue = if (isImeVisible) 4.dp else 14.dp,
        animationSpec = tween(220),
        label = "header_bottom_spacer"
    )
    val titleFontSize by animateFloatAsState(
        targetValue = if (isImeVisible) 22f else 32f,
        animationSpec = tween(220),
        label = "header_title_size"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(headerTopSpacer))

        // iOS Apple Intelligence Minimalist Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                if (!isImeVisible) {
                    Text(
                        text = "INTELLIGENCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextSecondary,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = "AI Asisten",
                    fontSize = titleFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary,
                    letterSpacing = (-0.6).sp
                )
            }

            // Intelligence Engine Capsule
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18F97316))
                    .border(0.7.dp, Color(0xFFF97316).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF97316))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Llama 3.3 70B (Groq)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFB380)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(headerBottomSpacer))

        // Quick Suggestion Chips (Frosted glass chips)
        AnimatedVisibility(
            visible = !isImeVisible,
            enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(150)),
            exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(100))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPrompts.forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(LiquidGlassTokens.GlassCardSurfaceBrush)
                                .background(LiquidGlassTokens.GlassCardSheenBrush)
                                .border(0.8.dp, LiquidGlassTokens.GlassSpecularBorderBrush, RoundedCornerShape(14.dp))
                                .clickable { viewModel.updateInputText(prompt) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                color = AppleTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Messages List (Native Apple Messages Style)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .imeNestedScroll()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                },
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 10.dp)
        ) {
            items(uiState.messages) { msg ->
                val isUser = msg.sender == ChatSender.USER

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (isUser) {
                        // User Bubble: iOS Blue Liquid Glass
                        val bubbleShape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .shadow(2.dp, bubbleShape, spotColor = Color(0x350A84FF))
                                .clip(bubbleShape)
                                .background(LiquidGlassUserBubbleBrush)
                                .background(LiquidGlassTokens.GlassCardSheenBrush)
                                .border(
                                    0.85.dp,
                                    Brush.verticalGradient(
                                        listOf(Color(0x80FFFFFF), Color(0x20FFFFFF))
                                    ),
                                    bubbleShape
                                )
                                .padding(horizontal = 14.dp, vertical = 11.dp)
                        ) {
                            MarkdownText(
                                text = msg.text,
                                textColor = Color.White,
                                fontSize = 15.sp,
                                lineHeight = 21.sp,
                                bulletColor = Color.White
                            )
                        }
                    } else {
                        // AI Bubble: Apple Intelligence Translucent Inset Liquid Glass
                        val bubbleShape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        )
                        Column(modifier = Modifier.fillMaxWidth(0.92f)) {
                            GlassCard(
                                shape = bubbleShape,
                                elevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Render formatted markdown text (bold, bullets, paragraphs cleanly)
                                    MarkdownText(
                                        text = msg.text,
                                        textColor = AppleTextPrimary,
                                        fontSize = 14.sp,
                                        lineHeight = 21.sp,
                                        bulletColor = AppleSystemBlue
                                    )

                                    // Parsed Task Preview inside bubble
                                    msg.parsedTask?.let { parsed ->
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0x35202838))
                                                .background(LiquidGlassTokens.GlassCardSheenBrush)
                                                .border(0.75.dp, LiquidGlassTokens.GlassSpecularBorderBrush, RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = AppleSystemBlue,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                    Text(
                                                        text = "TERDETEKSI OTOMATIS",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AppleSystemBlue,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(AppleSystemBlueSubtle)
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = parsed.subject,
                                                        fontSize = 11.sp,
                                                        color = AppleSystemBlue,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = parsed.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppleTextPrimary,
                                                letterSpacing = (-0.2).sp
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Schedule,
                                                        contentDescription = null,
                                                        tint = AppleTextSecondary,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = parsed.deadlineFormatted,
                                                        fontSize = 12.sp,
                                                        color = AppleTextSecondary
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Timer,
                                                        contentDescription = null,
                                                        tint = AppleTextSecondary,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${parsed.estimatedMinutes} menit",
                                                        fontSize = 12.sp,
                                                        color = AppleTextSecondary
                                                    )
                                                }
                                            }

                                            if (!parsed.locationTag.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        tint = AppleSystemOrange,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = parsed.locationTag,
                                                        fontSize = 12.sp,
                                                        color = AppleSystemOrange
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Save Button
                                            if (msg.isSavedToDatabase) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(AppleSystemGreen.copy(alpha = 0.16f))
                                                    .border(0.6.dp, AppleSystemGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                                    .padding(vertical = 9.dp),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = AppleSystemGreen,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Tersimpan di Pengingat & Jadwal",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = AppleSystemGreen
                                                    )
                                                }
                                            } else {
                                                GlassButton(
                                                    text = "Simpan ke Pengingat",
                                                    onClick = {
                                                        viewModel.saveTaskToDatabase(msg.id, parsed)
                                                        Toast.makeText(context, "Tugas berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    variant = GlassButtonVariant.PRIMARY,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isThinking) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AppleSystemBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI menguraikan tugasmu...",
                            fontSize = 12.sp,
                            color = AppleTextSecondary
                        )
                    }
                }
            }
        }

        // Apple Messages iOS Frosted Dock Input Bar Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color(0x45000000),
                    ambientColor = Color(0x25000000)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(LiquidGlassTokens.GlassDockSurfaceBrush)
                .background(LiquidGlassTokens.GlassCardSheenBrush)
                .border(
                    width = 0.85.dp,
                    brush = LiquidGlassTokens.GlassSpecularBorderBrushElevated,
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speech Mic Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x25FFFFFF))
                        .border(0.7.dp, LiquidGlassTokens.GlassSpecularBorderBrush, CircleShape)
                        .clickable {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Katakan tugasmu...")
                            }
                            speechLauncher.launch(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = AppleSystemBlue,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Text Input
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    placeholder = { Text("Tulis tugasmu (cth: PR IPS besok)...", color = AppleTextPlaceholder, fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleSystemBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0x18FFFFFF),
                        unfocusedContainerColor = Color(0x12FFFFFF),
                        focusedTextColor = AppleTextPrimary,
                        unfocusedTextColor = AppleTextPrimary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button with animated color state
                val sendEnabled = uiState.inputText.isNotBlank()
                val buttonColor by animateColorAsState(
                    targetValue = if (sendEnabled) AppleSystemBlue else Color(0x25FFFFFF),
                    animationSpec = tween(150),
                    label = "send_btn_bg"
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(buttonColor)
                        .border(
                            width = if (sendEnabled) 0.85.dp else 0.dp,
                            brush = if (sendEnabled) Brush.verticalGradient(listOf(Color(0x80FFFFFF), Color(0x20FFFFFF))) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                            shape = CircleShape
                        )
                        .clickable(enabled = sendEnabled) {
                            viewModel.sendPrompt()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Kirim",
                        tint = if (sendEnabled) Color.White else AppleTextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
