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
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Icon
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
import com.example.ui.theme.AppleSystemOrange
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.AppleTextTertiary
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
    aiRationale: String? = null,
    aiBadge: Pair<String, String>? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val urgency = SmartPrioritySorter.calculateUrgency(task)
    val deadline = SimpleDateFormat(
        "EEE, d MMM • HH:mm",
        Locale.forLanguageTag("id-ID")
    ).format(Date(task.deadlineEpochMillis))

    val isUrgent = urgency.score >= 100 ||
        (aiBadge?.first?.contains("Mendesak", ignoreCase = true) == true)

    val cardShape = RoundedCornerShape(20.dp)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        elevation = if (task.isCompleted) 1.5.dp else 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, top = 14.dp, end = 12.dp, bottom = 11.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.isCompleted) AppleSystemBlue
                            else Color(0x10FFFFFF)
                        )
                        .border(
                            width = if (task.isCompleted) 0.dp else 1.2.dp,
                            color = if (isUrgent && !task.isCompleted) {
                                AppleSystemRed.copy(alpha = 0.8f)
                            } else {
                                Color(0x66FFFFFF)
                            },
                            shape = CircleShape
                        )
                        .clickable(onClick = onToggleComplete),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selesai",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.title,
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (task.isCompleted) AppleTextTertiary else AppleTextPrimary,
                            letterSpacing = (-0.25).sp,
                            textDecoration = if (task.isCompleted) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isUrgent && !task.isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AppleSystemRed)
                            )
                        }
                    }

                    if (task.description.isNotBlank() && task.description != task.title) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            fontSize = 13.sp,
                            lineHeight = 17.sp,
                            color = AppleTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = deadline,
                            fontSize = 11.5.sp,
                            fontWeight = if (isUrgent && !task.isCompleted) {
                                FontWeight.Medium
                            } else {
                                FontWeight.Normal
                            },
                            color = if (isUrgent && !task.isCompleted) {
                                AppleSystemRed
                            } else {
                                AppleTextSecondary
                            }
                        )

                        if (task.subject.isNotBlank()) {
                            Text("•", color = AppleTextTertiary, fontSize = 10.sp)
                            Text(
                                text = task.subject,
                                fontSize = 11.5.sp,
                                color = AppleTextTertiary,
                                maxLines = 1
                            )
                        }

                        if (!task.locationName.isNullOrBlank()) {
                            Text("•", color = AppleTextTertiary, fontSize = 10.sp)
                            Text(
                                text = task.locationName!!,
                                fontSize = 11.5.sp,
                                color = AppleTextTertiary,
                                maxLines = 1
                            )
                        }
                    }

                    val rationale = aiRationale ?: task.aiMotivationQuote
                    if (!rationale.isNullOrBlank() && !task.isCompleted) {
                        Spacer(modifier = Modifier.height(7.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x10FFFFFF))
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = rationale,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                color = AppleTextTertiary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (!task.isCompleted) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.snoozeCount > 0) {
                        Text(
                            text = "${task.snoozeCount}× ditunda",
                            fontSize = 11.sp,
                            color = AppleSystemOrange,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    SmallGlassAction(
                        icon = Icons.Default.Snooze,
                        description = "Tunda 15 menit",
                        onClick = onSnooze
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    SmallGlassAction(
                        icon = Icons.Default.Share,
                        description = "Bagikan",
                        onClick = { WhatsAppShareHelper.shareTask(context, task) }
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    SmallGlassAction(
                        icon = Icons.Default.NotificationsNone,
                        description = "Tes notifikasi",
                        onClick = onTriggerNotification
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    SmallGlassAction(
                        icon = Icons.Default.DeleteOutline,
                        description = "Hapus",
                        onClick = onDelete,
                        tint = AppleSystemRed.copy(alpha = 0.82f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallGlassAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color = AppleTextSecondary
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0x12FFFFFF))
            .border(0.6.dp, Color(0x20FFFFFF), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}
