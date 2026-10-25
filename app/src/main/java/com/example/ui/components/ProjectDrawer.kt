package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.ChatSession
import com.example.data.model.ProjectWorkspace
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiSparkBlue
import com.example.ui.theme.GeminiSparkCyan
import com.example.ui.theme.GeminiSparkPurple
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceContainer
import com.example.ui.theme.GeminiSurfaceHigh
import com.example.ui.theme.GeminiTextMuted
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.StatusGreen

@Composable
fun ProjectDrawerContent(
    sessions: List<ChatSession>,
    currentSessionId: String,
    projects: List<ProjectWorkspace>,
    activeProject: ProjectWorkspace?,
    selectedModel: String,
    onNewChatClick: () -> Unit,
    onSelectChat: (String) -> Unit,
    onDeleteChat: (String) -> Unit,
    onSelectProject: (ProjectWorkspace) -> Unit,
    onLinkFolderClick: () -> Unit,
    onCreateProjectClick: () -> Unit,
    onOpenSetupWizard: () -> Unit,
    onOpenTerminal: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        drawerContainerColor = GeminiSurface,
        drawerContentColor = GeminiTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(18.dp)
        ) {
            // Header: Branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F1116))
                            .border(1.dp, OpenCodeCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.opencode_isotype),
                            contentDescription = "OpenCode Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }

                    Column {
                        Text(
                            text = "OpenCodeMovil",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeminiTextPrimary
                        )
                        Text(
                            text = "AI Coding Studio",
                            fontSize = 11.sp,
                            color = GeminiTextSecondary
                        )
                    }
                }

                IconButton(onClick = onCloseDrawer, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar menú",
                        tint = GeminiTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // "+ Nueva Conversación" Button
            Button(
                onClick = {
                    onNewChatClick()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drawer_new_chat_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GeminiSurfaceHigh,
                    contentColor = GeminiTextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = GeminiSparkBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nueva Conversación",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Scrollable Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Termux Local Engine
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "MOTOR LOCAL (TERMUX)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeminiTextSecondary,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GeminiSurfaceContainer)
                                .clickable {
                                    onCloseDrawer()
                                    onOpenSetupWizard()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = GeminiSparkCyan, modifier = Modifier.size(16.dp))
                            Column {
                                Text("Asistente Termux", fontSize = 13.sp, color = GeminiTextPrimary, fontWeight = FontWeight.Medium)
                                Text("Vincular motor en 127.0.0.1:3001", fontSize = 10.sp, color = GeminiTextMuted)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GeminiSurfaceContainer)
                                .clickable {
                                    onCloseDrawer()
                                    onOpenTerminal()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = GeminiSparkBlue, modifier = Modifier.size(16.dp))
                            Column {
                                Text("Consola de Logs", fontSize = 13.sp, color = GeminiTextPrimary, fontWeight = FontWeight.Medium)
                                Text("Visor en tiempo real de eventos", fontSize = 10.sp, color = GeminiTextMuted)
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(color = GeminiBorder, thickness = 0.8.dp)
                }

                // Section: Projects
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "MIS PROYECTOS (/MisProyectos)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeminiTextSecondary,
                            letterSpacing = 0.5.sp
                        )

                        // Project actions buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = onLinkFolderClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("link_storage_folder_button"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GeminiSparkBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Vincular", fontSize = 11.sp, color = GeminiSparkBlue)
                            }

                            OutlinedButton(
                                onClick = onCreateProjectClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("create_project_button"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
                            ) {
                                Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = GeminiSparkCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Crear", fontSize = 11.sp, color = GeminiSparkCyan)
                            }
                        }

                        // Projects list
                        for (project in projects) {
                            val isSelected = activeProject?.name == project.name
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GeminiSurfaceHigh else Color.Transparent)
                                    .clickable { onSelectProject(project) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = if (project.isLinked) StatusGreen else GeminiSparkBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = project.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) GeminiSparkBlue else GeminiTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (project.isLinked) "Carpeta vinculada" else "${project.files.size} archivos",
                                            fontSize = 10.sp,
                                            color = GeminiTextMuted
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(GeminiSparkBlue)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(color = GeminiBorder, thickness = 0.8.dp)
                }

                // Section: Chat Sessions History
                item {
                    Text(
                        text = "HISTORIAL DE CONVERSACIONES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeminiTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                items(sessions, key = { it.id }) { session ->
                    val isSelected = session.id == currentSessionId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GeminiSurfaceHigh else Color.Transparent)
                            .clickable {
                                onSelectChat(session.id)
                                onCloseDrawer()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("session_item_${session.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = if (isSelected) GeminiSparkBlue else GeminiTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = session.title,
                                fontSize = 13.sp,
                                color = if (isSelected) GeminiSparkBlue else GeminiTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (sessions.size > 1) {
                            IconButton(
                                onClick = { onDeleteChat(session.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar conversación",
                                    tint = GeminiTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GeminiBorder, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Model Summary and Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GeminiSurfaceContainer)
                    .clickable {
                        onCloseDrawer()
                        onOpenSettings()
                    }
                    .padding(12.dp)
                    .testTag("drawer_settings_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = GeminiSparkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Ajustes de API",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeminiTextPrimary
                        )
                        Text(
                            text = selectedModel.substringAfter("/"),
                            fontSize = 11.sp,
                            color = GeminiTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
