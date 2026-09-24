package com.example.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiTaskParserService
import com.example.data.ai.ParsedTaskResult
import com.example.data.local.AppDatabase
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.IosCard
import com.example.ui.theme.AntiMagerTheme
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGray1
import com.example.ui.theme.IosGray5
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosSeparator
import com.example.ui.theme.IosShadowColor
import com.example.ui.theme.IosSurfaceCard
import com.example.ui.theme.IosSystemBackground
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.util.LocationReminderManager
import com.example.util.TaskReminderScheduler
import com.example.widget.AntiMagerWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceActionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AntiMagerTheme {
                VoiceActionScreen(
                    onTaskSaved = {
                        AntiMagerWidgetProvider.sendUpdateBroadcast(this)
                        Toast.makeText(this, "✅ Tugas berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun VoiceActionScreen(
    onTaskSaved: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var transcribedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var parsedResult by remember { mutableStateOf<ParsedTaskResult?>(null) }
    var statusMessage by remember { mutableStateOf("Menyiapkan perekam suara...") }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            if (spoken.isNotBlank()) {
                transcribedText = spoken
                statusMessage = "Menganalisis dengan Gemini AI..."
                isProcessing = true

                coroutineScope.launch {
                    val parsed = AiTaskParserService.parseWithExternalApi(spoken)
                        ?: AiTaskParserService.parseStory(spoken)

                    parsedResult = parsed
                    isProcessing = false
                    statusMessage = "Tugas berhasil diuraikan!"
                }
            } else {
                statusMessage = "Tidak ada suara yang terdeteksi. Silakan coba lagi."
            }
        } else {
            statusMessage = "Perekaman dibatalkan. Ketik manual di bawah jika diinginkan."
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Katakan tugasmu (contoh: PR Matematika besok jam 8 malam)")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            statusMessage = "Google Speech tidak tersedia di perangkat ini."
        }
    }

    // Auto trigger mic on launch
    LaunchedEffect(Unit) {
        delay(200)
        startListening()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        IosCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(20.dp),
            elevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voice Command 🎙️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosTextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = IosGray1,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mic Pulse Circle Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isProcessing) IosGray5 else IosBlue.copy(alpha = 0.12f))
                        .clickable { startListening() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = IosBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Tap to talk",
                            tint = IosBlue,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = statusMessage,
                    fontSize = 13.sp,
                    color = IosTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // If user text recognized
                if (transcribedText.isNotBlank()) {
                    IosCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = IosSystemBackground,
                        elevation = 0.dp
                    ) {
                        Text(
                            text = "\"$transcribedText\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = IosTextPrimary,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // If parsed task is ready
                parsedResult?.let { parsed ->
                    IosCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = IosBlue.copy(alpha = 0.08f),
                        borderColor = IosBlue.copy(alpha = 0.3f),
                        elevation = 0.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⚡ ${parsed.title}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mata Pelajaran: ${parsed.subject} • Estimasi: ${parsed.estimatedMinutes} menit",
                                fontSize = 12.sp,
                                color = IosTextSecondary
                            )
                            Text(
                                text = "Deadline: ${parsed.deadlineFormatted}",
                                fontSize = 12.sp,
                                color = IosBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Commit button
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val db = AppDatabase.getInstance(context)
                                val entity = TaskEntity(
                                    title = parsed.title,
                                    subject = parsed.subject,
                                    description = parsed.description,
                                    deadlineEpochMillis = parsed.deadlineMillis,
                                    estimatedMinutes = parsed.estimatedMinutes,
                                    priority = parsed.priority,
                                    locationName = parsed.locationTag,
                                    locationTrigger = if (parsed.locationTag.isNullOrBlank()) null else "ENTER",
                                    aiMotivationQuote = parsed.aiAdvice
                                )
                                val newId = db.taskDao().insertTask(entity)
                                val createdTask = entity.copy(id = newId)
                                TaskReminderScheduler.schedule(context, createdTask)
                                if (!createdTask.locationName.isNullOrBlank()) {
                                    LocationReminderManager.refreshGeofencesIfActive(context)
                                }
                                onTaskSaved()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan ke AntiMager",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Manual Input Fallback
                if (parsedResult == null && !isProcessing) {
                    var manualInput by remember { mutableStateOf("") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        placeholder = { Text("Atau ketik tugas langsung...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IosBlue,
                            unfocusedBorderColor = IosSeparator,
                            focusedContainerColor = IosSurfaceCard,
                            unfocusedContainerColor = IosSurfaceCard
                        ),
                        trailingIcon = {
                            if (manualInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        transcribedText = manualInput
                                        isProcessing = true
                                        statusMessage = "Menganalisis tugas..."
                                        coroutineScope.launch {
                                            val parsed = AiTaskParserService.parseWithExternalApi(manualInput)
                                                ?: AiTaskParserService.parseStory(manualInput)
                                            parsedResult = parsed
                                            isProcessing = false
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Submit",
                                        tint = IosBlue
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
