package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiSparkBlue
import com.example.ui.theme.GeminiSparkCyan
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiTextMuted
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.BackendEngineState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalBottomSheet(
    sheetState: SheetState,
    logs: List<String>,
    backendState: BackendEngineState,
    engineUrl: String,
    onRefresh: () -> Unit,
    onClearLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val isConnected = backendState == BackendEngineState.CONNECTED

    // Auto-scroll to bottom of logs
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F1013),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(GeminiBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = GeminiSparkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Consola de Motor Local",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeminiTextPrimary
                    )

                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isConnected) StatusGreen.copy(alpha = 0.2f) else StatusAmber.copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                if (isConnected) StatusGreen else StatusAmber,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isConnected) "ONLINE" else "OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) StatusGreen else StatusAmber
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = GeminiSparkBlue, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(logs.joinToString("\n")))
                            isCopied = true
                            coroutineScope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copiar logs",
                            tint = if (isCopied) StatusGreen else GeminiTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Limpiar consola", tint = GeminiTextMuted, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = GeminiTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Console Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF07080A))
                    .border(1.dp, GeminiBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "Sin registros aún. El comprobador de salud consulta cada 2s.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = GeminiTextMuted
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { line ->
                            val color = when {
                                line.contains("[CONNECTED]") || line.contains("[OK]") -> StatusGreen
                                line.contains("[ERROR]") || line.contains("[FAIL]") -> StatusRed
                                line.contains("[WARN]") -> StatusAmber
                                line.contains("[MODEL]") || line.contains("[STREAM]") -> GeminiSparkCyan
                                line.contains("[CHAT]") || line.contains("[HEALTH]") -> GeminiSparkBlue
                                else -> GeminiTextSecondary
                            }

                            Text(
                                text = line,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = color
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Conectado a: $engineUrl/api/logs • Polling cada 2s",
                fontSize = 11.sp,
                color = GeminiTextMuted
            )
        }
    }
}
