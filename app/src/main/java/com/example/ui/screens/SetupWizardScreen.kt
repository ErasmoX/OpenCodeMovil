package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GeminiBackground
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceHigh
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.OpenCodeBlue
import com.example.ui.theme.OpenCodeButtonGradient
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeEmerald
import com.example.ui.theme.OpenCodeGradient
import com.example.ui.theme.OpenCodeMint
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.BackendEngineState
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MASTER_INSTALL_COMMAND =
    "curl -fsSL https://raw.githubusercontent.com/TU_USUARIO/OpenCodeMovil/main/install.sh | bash"

private const val TERMUX_FDROID_URL = "https://f-droid.org/packages/com.termux/"
private const val TERMUX_GITHUB_URL = "https://github.com/termux/termux-app/releases"

@Composable
fun SetupWizardScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var isCommandCopied by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeminiBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // High-Fidelity Hero Branding Section with Official OpenCodeMovil Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            // Logo Image Container with ambient glow border
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF0F1116))
                    .border(
                        width = 2.dp,
                        brush = OpenCodeGradient,
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.opencode_logo),
                    contentDescription = "OpenCodeMovil Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "OpenCodeMovil",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GeminiTextPrimary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tu entorno de IA y desarrollo móvil, impulsado por código abierto",
                fontSize = 13.sp,
                color = OpenCodeMint,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Radar / Ping Health Status Banner
        EngineRadarStatusCard(
            backendState = uiState.backendState,
            localEngineUrl = uiState.localEngineUrl,
            latencyMs = uiState.backendLatencyMs,
            failureReason = uiState.backendFailureReason,
            onRetryPing = { viewModel.checkEngineHealthNow() }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Instructions header
        Text(
            text = "Configuración Asistida en 3 Pasos",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = GeminiTextPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Conecta la interfaz gráfica nativa con tu runtime local en Android:",
            fontSize = 12.sp,
            color = GeminiTextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 12.dp)
        )

        // PASO 1: Instala Termux
        WizardStepCard(
            stepNumber = "1",
            stepColor = OpenCodeBlue,
            title = "Paso 1: Instala Termux",
            subtitle = "Entorno Linux autónomo en Android. Descárgalo desde F-Droid o GitHub Releases para compatibilidad completa."
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { openUrl(context, TERMUX_FDROID_URL) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("download_termux_fdroid_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeminiSurfaceHigh,
                        contentColor = OpenCodeCyan
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeCyan.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Descargar Termux", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { openUrl(context, TERMUX_GITHUB_URL) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("download_termux_github_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GeminiTextSecondary)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GitHub Releases", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PASO 2: Copia el comando maestro
        WizardStepCard(
            stepNumber = "2",
            stepColor = OpenCodeCyan,
            title = "Paso 2: Copia el comando maestro",
            subtitle = "Este script configura Node.js, dependencias y arranca el servidor HTTP en el puerto 3001:"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A0C0F))
                    .border(1.dp, GeminiBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = MASTER_INSTALL_COMMAND,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = OpenCodeCyan
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isCommandCopied) Brush.horizontalGradient(listOf(StatusGreen, StatusGreen))
                            else OpenCodeButtonGradient
                        )
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(MASTER_INSTALL_COMMAND))
                            isCommandCopied = true
                            Toast.makeText(context, "¡Comando copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                delay(2500)
                                isCommandCopied = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("copy_command_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF001F2B)
                        )
                    ) {
                        Icon(
                            imageVector = if (isCommandCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCommandCopied) "¡Copiado al portapapeles!" else "Copiar comando",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PASO 3: Inicia el motor en Termux
        WizardStepCard(
            stepNumber = "3",
            stepColor = OpenCodeEmerald,
            title = "Paso 3: Inicia el motor",
            subtitle = "Abre Termux, pega el comando y mantén la sesión abierta. La app detectará el motor automáticamente."
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(OpenCodeButtonGradient)
            ) {
                Button(
                    onClick = {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.termux")
                        if (launchIntent != null) {
                            context.startActivity(launchIntent)
                        } else {
                            Toast.makeText(
                                context,
                                "Termux aún no está instalado. Descárgalo en el Paso 1.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("launch_termux_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF001F2B)
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Termux y Pegar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Secondary / Bypass affordances
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.setTerminalOpen(true) },
                modifier = Modifier.testTag("wizard_open_terminal_button")
            ) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = GeminiTextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ver Logs del Sistema", fontSize = 12.sp, color = GeminiTextSecondary)
            }

            TextButton(
                onClick = { viewModel.bypassSetupWizard(true) },
                modifier = Modifier.testTag("bypass_wizard_button")
            ) {
                Text(
                    text = "Modo Directo (Cloud OpenRouter) →",
                    fontSize = 12.sp,
                    color = OpenCodeCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Animated radar card with 2s polling indicator.
 */
@Composable
fun EngineRadarStatusCard(
    backendState: BackendEngineState,
    localEngineUrl: String,
    latencyMs: Long,
    failureReason: String,
    onRetryPing: () -> Unit
) {
    val isConnected = backendState == BackendEngineState.CONNECTED

    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("engine_health_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GeminiSurface),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.2.dp,
            color = if (isConnected) StatusGreen.copy(alpha = 0.6f) else GeminiBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Radar Pulsing Circle
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isConnected) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                    alpha = pulseAlpha
                                }
                                .clip(CircleShape)
                                .background(OpenCodeCyan)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) StatusGreen else OpenCodeCyan)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isConnected) "Motor Conectado (Online)" else "Buscando Motor en 127.0.0.1:3001",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) StatusGreen else GeminiTextPrimary
                        )
                        if (!isConnected) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = OpenCodeCyan
                            )
                        }
                    }

                    Text(
                        text = if (isConnected) {
                            "Latencia: ${latencyMs}ms • Listo para procesar"
                        } else {
                            "Polling cada 2s • Esperando servidor Node/Python"
                        },
                        fontSize = 12.sp,
                        color = GeminiTextSecondary
                    )
                }
            }

            IconButton(onClick = onRetryPing) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reintentar ping",
                    tint = OpenCodeCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun WizardStepCard(
    stepNumber: String,
    stepColor: Color,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GeminiSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(stepColor.copy(alpha = 0.2f))
                        .border(1.dp, stepColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = stepColor
                    )
                }

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeminiTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = GeminiTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "No se pudo abrir el navegador", Toast.LENGTH_SHORT).show()
    }
}
