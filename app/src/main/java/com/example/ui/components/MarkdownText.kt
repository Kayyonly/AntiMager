package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

/**
 * Clean, lightweight Markdown Text renderer for Apple iOS style chat bubbles.
 * Handles:
 * - Bold: **text** and *text*
 * - Italic: _text_
 * - Bullet lists: •, *, -
 * - Numbered lists: 1., 2., etc.
 * - Clean paragraph breaks without raw markdown symbols leaking to UI.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = AppleTextPrimary,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp,
    bulletColor: Color = AppleSystemBlue
) {
    val paragraphs = remember(text) { parseMarkdownBlocks(text) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        paragraphs.forEach { block ->
            when (block) {
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = bulletColor,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(14.dp)
                        )
                        Text(
                            text = block.annotatedText,
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = textColor
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            color = bulletColor,
                            fontSize = fontSize,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(20.dp)
                        )
                        Text(
                            text = block.annotatedText,
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = textColor
                        )
                    }
                }
                is MarkdownBlock.HeaderItem -> {
                    Text(
                        text = block.annotatedText,
                        fontSize = (fontSize.value + (4 - block.level.coerceAtMost(3))).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = lineHeight,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = block.annotatedText,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        color = textColor
                    )
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Paragraph(val annotatedText: AnnotatedString) : MarkdownBlock()
    data class BulletItem(val annotatedText: AnnotatedString) : MarkdownBlock()
    data class NumberedItem(val number: String, val annotatedText: AnnotatedString) : MarkdownBlock()
    data class HeaderItem(val level: Int, val annotatedText: AnnotatedString) : MarkdownBlock()
}

/**
 * Parses raw text into semantic markdown blocks and formats inline styles.
 */
private fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            continue
        }

        // Header check: # Header, ## Header
        val headerMatch = Regex("^(#{1,3})\\s+(.+)").find(trimmed)
        if (headerMatch != null) {
            val level = headerMatch.groupValues[1].length
            val content = headerMatch.groupValues[2]
            blocks.add(MarkdownBlock.HeaderItem(level, parseInlineMarkdown(content)))
            continue
        }

        // Bullet check: * Item, - Item, • Item
        val bulletMatch = Regex("^[\\*\\-•]\\s+(.+)").find(trimmed)
        if (bulletMatch != null) {
            val content = bulletMatch.groupValues[1]
            blocks.add(MarkdownBlock.BulletItem(parseInlineMarkdown(content)))
            continue
        }

        // Numbered list check: 1. Item, 2. Item
        val numberMatch = Regex("^(\\d+)[\\.\\)]\\s+(.+)").find(trimmed)
        if (numberMatch != null) {
            val num = numberMatch.groupValues[1]
            val content = numberMatch.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(num, parseInlineMarkdown(content)))
            continue
        }

        // Standard paragraph
        blocks.add(MarkdownBlock.Paragraph(parseInlineMarkdown(trimmed)))
    }

    return blocks
}

/**
 * Formats inline bold (**text** or *text*), italic (_text_), and code (`code`).
 */
fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val length = text.length

        // Regex to capture bold (**text** or *text*), italic (_text_), and code (`text`)
        // Priority: **bold**, then `code`, then *bold/italic*, then _italic_
        val tokenRegex = Regex("(\\*\\*(.*?)\\*\\*)|(`(.*?)`)|(\\*([^\\*]+)\\*)|(_([^_]+)_)")
        val matches = tokenRegex.findAll(text)

        for (match in matches) {
            // Append plain text before match
            if (match.range.first > cursor) {
                append(text.substring(cursor, match.range.first))
            }

            val fullMatch = match.value
            when {
                // Double asterisks: **bold**
                fullMatch.startsWith("**") && fullMatch.endsWith("**") && fullMatch.length >= 4 -> {
                    val inner = fullMatch.substring(2, fullMatch.length - 2)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(inner)
                    pop()
                }
                // Backticks: `code`
                fullMatch.startsWith("`") && fullMatch.endsWith("`") && fullMatch.length >= 2 -> {
                    val inner = fullMatch.substring(1, fullMatch.length - 1)
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x22FFFFFF)))
                    append(" $inner ")
                    pop()
                }
                // Single asterisks: *bold* or *emphasis*
                fullMatch.startsWith("*") && fullMatch.endsWith("*") && fullMatch.length >= 2 -> {
                    val inner = fullMatch.substring(1, fullMatch.length - 1)
                    pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                    append(inner)
                    pop()
                }
                // Underscores: _italic_
                fullMatch.startsWith("_") && fullMatch.endsWith("_") && fullMatch.length >= 2 -> {
                    val inner = fullMatch.substring(1, fullMatch.length - 1)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(inner)
                    pop()
                }
                else -> {
                    append(fullMatch)
                }
            }

            cursor = match.range.last + 1
        }

        // Append remaining text
        if (cursor < length) {
            append(text.substring(cursor))
        }
    }
}
