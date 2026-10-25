package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.CodeHeaderBackground
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiSparkBlue
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CodeBlockView(
    code: String,
    language: String = "",
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val cleanLang = language.trim().ifEmpty { "code" }.uppercase()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CodeBackground)
            .border(1.dp, GeminiBorder, RoundedCornerShape(12.dp))
    ) {
        // Header with language tag and Copy button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CodeHeaderBackground)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cleanLang,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GeminiSparkBlue,
                letterSpacing = 0.5.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        isCopied = true
                        coroutineScope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copiar código",
                        tint = if (isCopied) GeminiSparkBlue else GeminiTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = if (isCopied) "¡Copiado!" else "Copiar",
                    fontSize = 11.sp,
                    color = if (isCopied) GeminiSparkBlue else GeminiTextSecondary
                )
            }
        }

        // Code Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(14.dp)
        ) {
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = GeminiTextPrimary
            )
        }
    }
}
