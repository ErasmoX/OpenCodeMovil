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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModel
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiSparkBlue
import com.example.ui.theme.GeminiSparkPurple
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceContainer
import com.example.ui.theme.GeminiSurfaceHigh
import com.example.ui.theme.GeminiTextMuted
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorSheet(
    sheetState: SheetState,
    models: List<AiModel>,
    selectedModelId: String,
    onModelSelected: (String) -> Unit,
    onRefreshModels: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredModels = remember(models, searchQuery) {
        if (searchQuery.isBlank()) {
            models
        } else {
            models.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true) ||
                it.provider.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GeminiSurface,
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Seleccionar Modelo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeminiTextPrimary
                    )
                    Text(
                        text = "Estilo Open WebUI • Proveedor activo",
                        fontSize = 13.sp,
                        color = GeminiTextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onRefreshModels,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar modelos",
                            tint = GeminiSparkBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            onDismiss()
                            onOpenSettings()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Ajustes",
                            tint = GeminiTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("model_search_input"),
                placeholder = {
                    Text("Buscar modelo (Claude, DeepSeek, Llama...)", color = GeminiTextMuted, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = GeminiTextSecondary
                    )
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

            Spacer(modifier = Modifier.height(14.dp))

            // Models List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredModels, key = { it.id }) { model ->
                    val isSelected = model.id == selectedModelId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) GeminiSurfaceHigh else GeminiSurfaceContainer)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) GeminiSparkBlue else GeminiBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onModelSelected(model.id)
                            }
                            .padding(14.dp)
                            .testTag("model_item_${model.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = model.name,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) GeminiSparkBlue else GeminiTextPrimary
                                )

                                // Provider Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF262930))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = model.provider,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GeminiSparkPurple
                                    )
                                }
                            }

                            if (model.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = model.description,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = GeminiTextSecondary,
                                    maxLines = 2
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = model.id,
                                fontSize = 11.sp,
                                color = GeminiTextMuted
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(GeminiSparkBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
