package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiSparkBlue
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary

private sealed class ContentSegment {
    data class TextSegment(val text: String) : ContentSegment()
    data class CodeBlockSegment(val language: String, val code: String) : ContentSegment()
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = GeminiTextPrimary
) {
    val segments = parseContentSegments(text)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (segment in segments) {
            when (segment) {
                is ContentSegment.CodeBlockSegment -> {
                    CodeBlockView(
                        code = segment.code,
                        language = segment.language
                    )
                }
                is ContentSegment.TextSegment -> {
                    val lines = segment.text.split("\n")
                    for (line in lines) {
                        RenderMarkdownLine(line = line, defaultColor = textColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderMarkdownLine(line: String, defaultColor: Color) {
    val trimmed = line.trimStart()

    when {
        trimmed.startsWith("### ") -> {
            Text(
                text = formatAnnotatedText(trimmed.removePrefix("### "), defaultColor),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = GeminiSparkBlue,
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
            )
        }
        trimmed.startsWith("## ") -> {
            Text(
                text = formatAnnotatedText(trimmed.removePrefix("## "), defaultColor),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = GeminiSparkBlue,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }
        trimmed.startsWith("# ") -> {
            Text(
                text = formatAnnotatedText(trimmed.removePrefix("# "), defaultColor),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GeminiSparkBlue,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )
        }
        trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "•",
                    color = GeminiSparkBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatAnnotatedText(trimmed.drop(2), defaultColor),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = defaultColor
                )
            }
        }
        trimmed.startsWith("> ") -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E2024))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = formatAnnotatedText(trimmed.removePrefix("> "), GeminiTextSecondary),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp,
                    color = GeminiTextSecondary
                )
            }
        }
        trimmed.isEmpty() -> {
            // Empty line spacer
        }
        else -> {
            Text(
                text = formatAnnotatedText(line, defaultColor),
                fontSize = 15.sp,
                lineHeight = 23.sp,
                color = defaultColor
            )
        }
    }
}

/**
 * Splits text into markdown text and fenced code blocks (```lang ... ```).
 */
private fun parseContentSegments(raw: String): List<ContentSegment> {
    val result = mutableListOf<ContentSegment>()
    val lines = raw.split("\n")
    var inCodeBlock = false
    var currentLanguage = ""
    val codeAccumulator = StringBuilder()
    val textAccumulator = StringBuilder()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                // Closing fence
                result.add(
                    ContentSegment.CodeBlockSegment(
                        language = currentLanguage,
                        code = codeAccumulator.toString().trimEnd()
                    )
                )
                codeAccumulator.clear()
                currentLanguage = ""
                inCodeBlock = false
            } else {
                // Opening fence
                if (textAccumulator.isNotEmpty()) {
                    result.add(ContentSegment.TextSegment(textAccumulator.toString()))
                    textAccumulator.clear()
                }
                currentLanguage = trimmed.removePrefix("```").trim()
                inCodeBlock = true
            }
        } else {
            if (inCodeBlock) {
                if (codeAccumulator.isNotEmpty()) codeAccumulator.append("\n")
                codeAccumulator.append(line)
            } else {
                if (textAccumulator.isNotEmpty()) textAccumulator.append("\n")
                textAccumulator.append(line)
            }
        }
    }

    if (inCodeBlock) {
        result.add(
            ContentSegment.CodeBlockSegment(
                language = currentLanguage,
                code = codeAccumulator.toString()
            )
        )
    } else if (textAccumulator.isNotEmpty()) {
        result.add(ContentSegment.TextSegment(textAccumulator.toString()))
    }

    return result
}

/**
 * Parses bold **text**, italics *text*, and inline `code`.
 */
private fun formatAnnotatedText(text: String, defaultColor: Color) = buildAnnotatedString {
    var index = 0
    val length = text.length

    while (index < length) {
        when {
            // Inline code `...`
            text[index] == '`' -> {
                val nextBacktick = text.indexOf('`', index + 1)
                if (nextBacktick != -1) {
                    val codeContent = text.substring(index + 1, nextBacktick)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFF24272C),
                            color = GeminiSparkBlue,
                            fontSize = 13.5.sp
                        )
                    )
                    append(" $codeContent ")
                    pop()
                    index = nextBacktick + 1
                } else {
                    append(text[index])
                    index++
                }
            }
            // Bold **...**
            text.startsWith("**", index) -> {
                val nextDoubleStar = text.indexOf("**", index + 2)
                if (nextDoubleStar != -1) {
                    val boldContent = text.substring(index + 2, nextDoubleStar)
                    pushStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = defaultColor
                        )
                    )
                    append(boldContent)
                    pop()
                    index = nextDoubleStar + 2
                } else {
                    append(text[index])
                    index++
                }
            }
            // Italic *...*
            text[index] == '*' && (index == 0 || text[index - 1] != '*') -> {
                val nextStar = text.indexOf('*', index + 1)
                if (nextStar != -1 && (nextStar == length - 1 || text[nextStar + 1] != '*')) {
                    val italicContent = text.substring(index + 1, nextStar)
                    pushStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = defaultColor
                        )
                    )
                    append(italicContent)
                    pop()
                    index = nextStar + 1
                } else {
                    append(text[index])
                    index++
                }
            }
            else -> {
                append(text[index])
                index++
            }
        }
    }
}
