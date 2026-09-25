package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.ai.AiTaskParserService
import com.example.data.ai.ParsedTaskResult
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextPlaceholder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.AppleTextTertiary
import java.util.Locale

@Composable
fun VoiceCommandDialog(
    onDismiss: () -> Unit,
    onSaveParsedTask: (ParsedTaskResult) -> Unit
) {
    val context = LocalContext.current
    var spokenText by remember { mutableStateOf("") }
    var parsedResult by remember { mutableStateOf<ParsedTaskResult?>(null) }
    var isListening by remember { mutableStateOf(false) }
    var isParsing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Ketuk mikrofon untuk mulai bicara") }

    // Fallback system activity launcher
    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                spokenText = spoken
                statusText = "Menganalisis tugas..."
                val parsed = AiTaskParserService.parseStory(spoken)
                parsedResult = parsed
                statusText = if (parsed.isAmbiguous) parsed.aiAdvice else "Tugas berhasil dikenali!"
            } else {
                statusText = "Tidak ada suara yang terdeteksi"
            }
        } else {
            statusText = "Perekaman dibatalkan"
        }
    }

    // Android SpeechRecognizer instance managed cleanly
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun processRecognizedText(text: String) {
        spokenText = text
        statusText = "Menganalisis tugas dengan AI..."
        isParsing = true
        val parsed = AiTaskParserService.parseStory(text)
        parsedResult = parsed
        isParsing = false
        statusText = if (parsed.isAmbiguous) parsed.aiAdvice else "Tugas berhasil dikenali!"
    }

    fun startListeningWithRecognizer(ctx: Context) {
        if (!SpeechRecognizer.isRecognitionAvailable(ctx)) {
            // Fallback to system voice recognition intent
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "id-ID")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Katakan tugasmu...")
                }
                speechIntentLauncher.launch(intent)
            } catch (e: Exception) {
                statusText = "Fitur suara tidak tersedia di perangkat ini"
            }
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(ctx)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    statusText = "Mendengarkan... Katakan tugasmu sekarang"
                }

                override fun onBeginningOfSpeech() {
                    statusText = "Mendengarkan ucapanmu..."
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListening = false
                    statusText = "Memproses suara..."
                }

                override fun onError(error: Int) {
                    isListening = false
                    statusText = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Tidak ada suara yang dikenali. Coba bicara lebih dekat."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Waktu bicara habis. Ketuk mikrofon untuk coba lagi."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Koneksi internet bermasalah."
                        SpeechRecognizer.ERROR_AUDIO -> "Gagal mengakses input audio mikrofon."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Izin mikrofon belum diberikan."
                        SpeechRecognizer.ERROR_CLIENT -> "Perekaman dibatalkan."
                        else -> "Tidak dapat memproses suara. Coba lagi."
                    }
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        processRecognizedText(text)
                    } else {
                        statusText = "Tidak ada teks yang terdeteksi"
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    partialMatches?.firstOrNull()?.let {
                        spokenText = it
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "id-ID")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            recognizer.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            statusText = "Gagal memulai perekam suara: ${e.localizedMessage}"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListeningWithRecognizer(context)
        } else {
            isListening = false
            statusText = "Izin mikrofon diperlukan untuk mendengarkan suara"
            Toast.makeText(context, "Izin mikrofon diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    fun initiateListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListeningWithRecognizer(context)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                // Ignore disposal exceptions
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        val modalShape = RoundedCornerShape(28.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape = modalShape,
                    spotColor = Color(0x70000000),
                    ambientColor = Color(0x34000000)
                )
                .clip(modalShape)
                .background(LiquidGlassTokens.GlassModalSurfaceBrush)
                .background(LiquidGlassTokens.GlassCardSheenBrush)
                .border(
                    width = 0.8.dp,
                    brush = LiquidGlassTokens.GlassSpecularBorderBrushElevated,
                    shape = modalShape
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perintah Suara",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        letterSpacing = (-0.3).sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = AppleTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Clean Mic Button (Apple Voice style)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (isListening) AppleSystemBlue else Color(0x24FFFFFF))
                        .border(
                            0.8.dp,
                            if (isListening) Color.White.copy(alpha = 0.28f) else Color(0x38FFFFFF),
                            CircleShape
                        )
                        .clickable {
                            if (isListening) {
                                try {
                                    speechRecognizer?.stopListening()
                                } catch (e: Exception) {
                                    // ignore
                                }
                                isListening = false
                            } else {
                                initiateListening()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isParsing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mulai Bicara",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    color = if (isListening) AppleSystemBlue else AppleTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Text field fallback
                OutlinedTextField(
                    value = spokenText,
                    onValueChange = {
                        spokenText = it
                        if (it.isNotBlank()) {
                            val parsed = AiTaskParserService.parseStory(it)
                            parsedResult = parsed
                            statusText = if (parsed.isAmbiguous) parsed.aiAdvice else "Tugas dikenali!"
                        } else {
                            parsedResult = null
                            statusText = "Ketik atau katakan tugasmu..."
                        }
                    },
                    placeholder = {
                        Text("Atau ketik ucapan di sini...", color = AppleTextPlaceholder, fontSize = 13.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleSystemBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0x18FFFFFF),
                        unfocusedContainerColor = Color(0x18FFFFFF),
                        focusedTextColor = AppleTextPrimary,
                        unfocusedTextColor = AppleTextPrimary
                    ),
                    maxLines = 2
                )

                // Quick preset voice prompts
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "PR IPS besok jam 8",
                        "Jumat kumpul tugas IPA",
                        "Beli alat tulis"
                    )
                    presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x18FFFFFF))
                                .clickable {
                                    processRecognizedText(preset)
                                }
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset,
                                fontSize = 10.sp,
                                color = AppleTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Parsed Task Preview
                AnimatedVisibility(visible = parsedResult != null && !parsedResult!!.isAmbiguous) {
                    parsedResult?.let { parsed ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x18FFFFFF))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = parsed.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleTextPrimary
                                )
                                Text(
                                    text = parsed.subject,
                                    fontSize = 11.sp,
                                    color = AppleTextTertiary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = AppleTextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = parsed.deadlineFormatted,
                                        fontSize = 12.sp,
                                        color = AppleTextSecondary
                                    )
                                }
                                Text(
                                    text = "· ${parsed.estimatedMinutes}m",
                                    fontSize = 12.sp,
                                    color = AppleTextTertiary
                                )
                            }

                            if (!parsed.locationTag.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "📍 ${parsed.locationTag}",
                                    fontSize = 11.sp,
                                    color = AppleTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            GlassButton(
                                text = "Konfirmasi & Simpan",
                                onClick = {
                                    onSaveParsedTask(parsed)
                                    onDismiss()
                                },
                                icon = Icons.Default.Done,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
