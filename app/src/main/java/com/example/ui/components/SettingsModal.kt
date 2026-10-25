package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiSparkBlue
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceContainer
import com.example.ui.theme.GeminiSurfaceHigh
import com.example.ui.theme.GeminiTextMuted
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.StatusGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsModal(
    currentApiKey: String,
    currentBaseUrl: String,
    currentSystemPrompt: String,
    isFetchingModels: Boolean,
    onSaveSettings: (apiKey: String, baseUrl: String, systemPrompt: String) -> Unit,
    onTestConnection: () -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf(currentApiKey) }
    var baseUrl by remember { mutableStateOf(currentBaseUrl) }
    var systemPrompt by remember { mutableStateOf(currentSystemPrompt) }
    var isKeyVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GeminiSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ajustes de Conexión",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeminiTextPrimary
                        )
                        Text(
                            text = "OpenRouter / OpenAI / Ollama compatible",
                            fontSize = 12.sp,
                            color = GeminiTextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = GeminiTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // API Key Field
                Text(
                    text = "API Key",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GeminiTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input"),
                    placeholder = {
                        Text("sk-or-v1-...", color = GeminiTextMuted, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = GeminiSparkBlue)
                    },
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "Ocultar" else "Mostrar",
                                tint = GeminiTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GeminiSurfaceContainer,
                        unfocusedContainerColor = GeminiSurfaceContainer,
                        focusedBorderColor = GeminiSparkBlue,
                        unfocusedBorderColor = GeminiBorder,
                        focusedTextColor = GeminiTextPrimary,
                        unfocusedTextColor = GeminiTextPrimary
                    )
                )
                Text(
                    text = "Almacenada localmente en DataStore. Soporta OpenRouter, OpenAI, Groq, etc.",
                    fontSize = 11.sp,
                    color = GeminiTextMuted,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Base URL Field
                Text(
                    text = "Base URL",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GeminiTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("base_url_input"),
                    placeholder = {
                        Text("https://openrouter.ai/api/v1", color = GeminiTextMuted, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Language, contentDescription = null, tint = GeminiSparkBlue)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GeminiSurfaceContainer,
                        unfocusedContainerColor = GeminiSurfaceContainer,
                        focusedBorderColor = GeminiSparkBlue,
                        unfocusedBorderColor = GeminiBorder,
                        focusedTextColor = GeminiTextPrimary,
                        unfocusedTextColor = GeminiTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets
                Text(
                    text = "Proveedores rápidos:",
                    fontSize = 11.sp,
                    color = GeminiTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "OpenRouter" to "https://openrouter.ai/api/v1",
                        "OpenAI" to "https://api.openai.com/v1",
                        "Ollama Local" to "http://10.0.2.2:11434/v1"
                    )
                    for ((name, url) in presets) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GeminiSurfaceHigh)
                                .border(1.dp, GeminiBorder, RoundedCornerShape(8.dp))
                                .clickable { baseUrl = url }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = name, fontSize = 11.sp, color = GeminiSparkBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // System Prompt Field
                Text(
                    text = "Prompt de Sistema (Rol)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GeminiTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("system_prompt_input"),
                    leadingIcon = {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = GeminiSparkBlue)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GeminiSurfaceContainer,
                        unfocusedContainerColor = GeminiSurfaceContainer,
                        focusedBorderColor = GeminiSparkBlue,
                        unfocusedBorderColor = GeminiBorder,
                        focusedTextColor = GeminiTextPrimary,
                        unfocusedTextColor = GeminiTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onTestConnection,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_connection_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GeminiSparkBlue
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
                    ) {
                        if (isFetchingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = GeminiSparkBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("Probar & Sincronizar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onSaveSettings(apiKey, baseUrl, systemPrompt)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_settings_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeminiSparkBlue,
                            contentColor = Color(0xFF002244)
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
