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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
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
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.MintAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhitePrimary
import com.example.util.SmartPrioritySorter
import com.example.util.WhatsAppShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onSnooze: () -> Unit,
    onDelete: () -> Unit,
    onTriggerNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val urgency = SmartPrioritySorter.calculateUrgency(task)
    val sdf = SimpleDateFormat("EEE, dd MMM • HH:mm", Locale.forLanguageTag("id-ID"))
    val deadlineFormatted = sdf.format(Date(task.deadlineEpochMillis))

    val subjectColor = when (task.subject) {
        "IPS" -> Color(0xFFF59E0B)
        "IPA", "Biologi", "Fisika" -> Color(0xFF10B981)
        "Matematika" -> CyanAccent
        "B. Indonesia", "B. Inggris" -> LavenderAccent
        "Belanja" -> Color(0xFFEC4899)
        else -> Color(0xFF94A3B8)
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = if (task.isCompleted) GlassCardFill.copy(alpha = 0.08f) else GlassCardFill,
        borderColor = if (task.isCompleted) GlassCardBorder.copy(alpha = 0.2f) else GlassCardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Subject Chip & Urgency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Subject Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(subjectColor.copy(alpha = 0.18f))
                            .border(1.dp, subjectColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.subject,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor
                        )
                    }

                    // Location Chip if available
                    if (!task.locationName.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFBBF24).copy(alpha = 0.15f))
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = task.locationName,
                                fontSize = 11.sp,
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Urgency Badge
                UrgencyBadge(
                    label = urgency.badgeLabel,
                    colorHex = urgency.badgeColorHex
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Complete Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Interactive Checkbox
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.isCompleted) MintAccent else GlassCardFill
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (task.isCompleted) MintAccent else GlassCardBorder,
                            shape = CircleShape
                        )
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selesai",
                            tint = Color(0xFF00331F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) TextMuted else TextWhitePrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = task.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timing Details & Snooze counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Deadline Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (task.isCompleted) TextMuted else CyanAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (task.isCompleted) "Selesai" else urgency.humanTimeRemaining,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (task.isCompleted) TextMuted else CyanAccent
                        )
                    }

                    // Duration estimate
                    Text(
                        text = "⏱ ${task.estimatedMinutes}m",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Snooze tag if snoozed
                if (task.snoozeCount > 0 && !task.isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LavenderAccent.copy(alpha = 0.16f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "😴 Ditunda ${task.snoozeCount}x",
                            fontSize = 10.sp,
                            color = LavenderAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: WhatsApp Share, Persistent Alert, Snooze, Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share to WhatsApp Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF25D366).copy(alpha = 0.18f))
                        .border(1.dp, Color(0xFF25D366).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable { WhatsAppShareHelper.shareToWhatsApp(context, task) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share WhatsApp",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "WhatsApp",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF25D366)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Snooze 15m Button
                    if (!task.isCompleted) {
                        IconButton(
                            onClick = onSnooze,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = "Tunda 15 Menit",
                                tint = LavenderAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Test/Trigger Persistent Notification Button
                    if (!task.isCompleted) {
                        IconButton(
                            onClick = onTriggerNotification,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Bunyikan Notifikasi Persisten",
                                tint = CyanAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Tugas",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
