package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidGlassTokens
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemBlueSubtle
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
import com.example.ui.theme.LiquidDarkBackground
import com.example.ui.theme.LiquidDarkCard
import com.example.ui.viewmodel.AiChatViewModel
import com.example.ui.viewmodel.ChatSender

@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
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
        "Beli binder di Indomaret",
        "Lusa tugas Matematika 45m"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidDarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // iOS Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "INTELLIGENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleTextSecondary,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "AI Asisten",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary,
                    letterSpacing = (-0.6).sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x225E5CE6))
                    .border(0.8.dp, AppleSystemIndigo.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Gemini 3.5 Flash",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD0BCFF)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Prompts Chips (Liquid Glass Pill Scroll)
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
                        .background(GlassLayer1)
                        .border(0.8.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                        .clickable { viewModel.updateInputText(prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        fontSize = 12.sp,
                        color = AppleTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Messages List (Apple Messages style)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp)
        ) {
            items(uiState.messages) { msg ->
                val isUser = msg.sender == ChatSender.USER

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (isUser) {
                        // User Bubble: Apple iOS solid blue with subtle specular edge
                        val bubbleShape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .clip(bubbleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(AppleSystemBlue, Color(0xFF0066D6))
                                    )
                                )
                                .border(
                                    0.8.dp,
                                    Brush.verticalGradient(
                                        listOf(Color(0x40FFFFFF), Color.Transparent)
                                    ),
                                    bubbleShape
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                letterSpacing = (-0.2).sp
                            )
                        }
                    } else {
                        // AI Bubble: Apple Intelligence translucent glass card
                        val bubbleShape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        )
                        Column(modifier = Modifier.fillMaxWidth(0.92f)) {
                            GlassCard(
                                shape = bubbleShape,
                                backgroundColor = LiquidDarkCard,
                                borderColor = GlassBorderStandard,
                                elevation = 1.5.dp
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = msg.text,
                                        color = AppleTextPrimary,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )

                                    // Parsed Task Preview inside bubble
                                    msg.parsedTask?.let { parsed ->
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(LiquidGlassTokens.RadiusSmall))
                                                .background(GlassLayer1)
                                                .border(0.8.dp, GlassBorderSubtle, RoundedCornerShape(LiquidGlassTokens.RadiusSmall))
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "HASIL DETEKSI",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppleSystemBlue,
                                                    letterSpacing = 0.5.sp
                                                )
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

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = parsed.title,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppleTextPrimary,
                                                letterSpacing = (-0.2).sp
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                                                Text(
                                                    text = "⏱ ${parsed.estimatedMinutes} menit",
                                                    fontSize = 12.sp,
                                                    color = AppleTextSecondary
                                                )
                                            }

                                            if (!parsed.locationTag.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "📍 Lokasi: ${parsed.locationTag}",
                                                    fontSize = 12.sp,
                                                    color = AppleSystemOrange
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Save Button
                                            if (msg.isSavedToDatabase) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(AppleSystemGreen.copy(alpha = 0.16f))
                                                        .border(0.8.dp, AppleSystemGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                        .padding(vertical = 8.dp),
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
                                                        text = "Tersimpan di Pengingat",
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
                            color = AppleSystemIndigo,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI menganalisis ucapanmu...",
                            fontSize = 12.sp,
                            color = AppleTextSecondary
                        )
                    }
                }
            }
        }

        // Apple Messages Glass Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 85.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speech Mic Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GlassLayer1)
                    .border(0.8.dp, GlassBorderSubtle, CircleShape)
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
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = { viewModel.updateInputText(it) },
                placeholder = { Text("Ceritakan tugasmu...", color = AppleTextPlaceholder, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(LiquidGlassTokens.RadiusInput),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppleSystemBlue,
                    unfocusedBorderColor = GlassBorderStandard,
                    focusedContainerColor = LiquidDarkCard,
                    unfocusedContainerColor = LiquidDarkCard,
                    focusedTextColor = AppleTextPrimary,
                    unfocusedTextColor = AppleTextPrimary
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (uiState.inputText.isNotBlank()) AppleSystemBlue else Color(0x20FFFFFF)
                    )
                    .clickable(enabled = uiState.inputText.isNotBlank()) {
                        viewModel.sendPrompt()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Kirim",
                    tint = if (uiState.inputText.isNotBlank()) Color.White else AppleTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
