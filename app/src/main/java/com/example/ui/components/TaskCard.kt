package com.example.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.AppleSystemOrange
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextMuted
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.AppleTextTertiary
import com.example.ui.theme.GlassBorderSubtle
import com.example.util.SmartPrioritySorter
import com.example.util.WhatsAppShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Native iOS Reminders List Row
 * Clean, flat, quiet, typography-driven hierarchy without boxes inside boxes.
 */
@Composable
fun TaskCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onSnooze: () -> Unit,
    onDelete: () -> Unit,
    onTriggerNotification: () -> Unit,
    aiRationale: String? = null,
    aiBadge: Pair<String, String>? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val urgency = SmartPrioritySorter.calculateUrgency(task)
    val sdf = SimpleDateFormat("EEE, dd MMM • HH:mm", Locale.forLanguageTag("id-ID"))
    val deadlineFormatted = sdf.format(Date(task.deadlineEpochMillis))

    val isUrgent = urgency.score >= 100 ||
                   (aiBadge?.first?.contains("Mendesak", ignoreCase = true) == true)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleComplete() }
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Native Apple Reminders Circular Toggle
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (task.isCompleted) 0.dp else 1.2.dp,
                        color = if (task.isCompleted) Color.Transparent else Color(0x60FFFFFF),
                        shape = CircleShape
                    )
                    .background(if (task.isCompleted) AppleSystemBlue else Color.Transparent)
                    .clickable { onToggleComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selesai",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Content: Title, Description, Metadata
            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) AppleTextMuted else AppleTextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    letterSpacing = (-0.2).sp
                )

                // Optional Description
                if (task.description.isNotBlank() && task.description != task.title) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.description,
                        fontSize = 13.sp,
                        color = AppleTextSecondary,
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Metadata Line with Interpuncts: Deadline · Subject · Location · Urgency
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Urgent red dot indicator
                    if (isUrgent && !task.isCompleted) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AppleSystemRed)
                        )
                        Text(
                            text = "Mendesak",
                            fontSize = 12.sp,
                            color = AppleSystemRed,
                            fontWeight = FontWeight.Medium
                        )
                        Text(text = "·", fontSize = 12.sp, color = AppleTextTertiary)
                    }

                    // Deadline
                    Text(
                        text = deadlineFormatted,
                        fontSize = 12.sp,
                        color = if (isUrgent && !task.isCompleted) AppleSystemRed else AppleTextSecondary
                    )

                    // Subject (Neutral label, no screaming colored pill)
                    if (task.subject.isNotBlank()) {
                        Text(text = "·", fontSize = 12.sp, color = AppleTextTertiary)
                        Text(
                            text = task.subject,
                            fontSize = 12.sp,
                            color = AppleTextTertiary
                        )
                    }

                    // Location if present
                    if (!task.locationName.isNullOrBlank()) {
                        Text(text = "·", fontSize = 12.sp, color = AppleTextTertiary)
                        Text(
                            text = task.locationName!!,
                            fontSize = 12.sp,
                            color = AppleTextTertiary
                        )
                    }

                    // Snooze count if any
                    if (task.snoozeCount > 0) {
                        Text(text = "·", fontSize = 12.sp, color = AppleTextTertiary)
                        Text(
                            text = "${task.snoozeCount}x tunda",
                            fontSize = 11.sp,
                            color = AppleSystemOrange
                        )
                    }
                }

                // AI Rationale (Subtle, calm footnote without glowing purple boxes)
                val rationaleText = aiRationale ?: task.aiMotivationQuote
                if (!rationaleText.isNullOrBlank() && !task.isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rationaleText,
                        fontSize = 12.sp,
                        color = AppleTextTertiary,
                        lineHeight = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quiet, understated action icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Snooze
                IconButton(
                    onClick = onSnooze,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "Tunda 15 Menit",
                        tint = AppleTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Share to WhatsApp
                IconButton(
                    onClick = { WhatsAppShareHelper.shareTask(context, task) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan ke WhatsApp",
                        tint = AppleTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Notification preview
                IconButton(
                    onClick = onTriggerNotification,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notifikasi",
                        tint = AppleTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Tugas",
                        tint = AppleTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        // Subtle native iOS hairline separator
        Box(
            modifier = Modifier
                .padding(start = 34.dp)
                .fillMaxWidth()
                .height(0.5.dp)
                .background(GlassBorderSubtle)
        )
    }
}
