package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.AttachedFile
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole
import com.example.ui.components.MarkdownText
import com.example.ui.components.ModelSelectorSheet
import com.example.ui.components.ProjectDrawerContent
import com.example.ui.components.SettingsModal
import com.example.ui.components.TerminalBottomSheet
import com.example.ui.theme.GeminiBackground
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiBubbleAi
import com.example.ui.theme.GeminiBubbleUser
import com.example.ui.theme.GeminiSparkBlue
import com.example.ui.theme.GeminiSparkCyan
import com.example.ui.theme.GeminiSparkPurple
import com.example.ui.theme.GeminiSurfaceContainer
import com.example.ui.theme.GeminiSurfaceHigh
import com.example.ui.theme.GeminiTextMuted
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeEmerald
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.BackendEngineState
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }

    val context = LocalContext.current

    // SAF Document Tree Picker for /MisProyectos
    val openDocumentTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {
            }
            val folderName = uri.lastPathSegment?.substringAfterLast(':') ?: "MisProyectos"
            viewModel.setWorkspaceTreeUri(uri, folderName)
        }
    }

    // Single Document/Code File Picker
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "archivo_codigo.txt"
            viewModel.attachFileFromUri(uri, fileName)
        }
    }

    // Zero-permission Photo Picker
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addAttachment(
                AttachedFile(
                    name = "imagen_${System.currentTimeMillis() % 10000}.jpg",
                    content = "[Contexto visual adjunto: Imagen]",
                    mimeType = "image/*",
                    uriString = uri.toString()
                )
            )
        }
    }

    // Speech-to-Text Voice Recognizer
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
            inputText = if (inputText.isBlank()) spokenText else "$inputText $spokenText"
        }
    }

    // Auto-scroll when messages update or during streaming
    val messagesCount = uiState.currentSession.messages.size
    val lastMessageContentLength = uiState.currentSession.messages.lastOrNull()?.content?.length ?: 0
    LaunchedEffect(messagesCount, lastMessageContentLength, uiState.isStreaming) {
        if (messagesCount > 0) {
            listState.animateScrollToItem(messagesCount - 1)
        }
    }

    // Show error in snackbar if present
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProjectDrawerContent(
                sessions = uiState.sessions,
                currentSessionId = uiState.currentSession.id,
                projects = uiState.projects,
                activeProject = uiState.activeProject,
                selectedModel = uiState.selectedModel,
                onNewChatClick = { viewModel.createNewChat() },
                onSelectChat = { id -> viewModel.selectChat(id) },
                onDeleteChat = { id -> viewModel.deleteChat(id) },
                onSelectProject = { proj -> viewModel.selectProject(proj) },
                onLinkFolderClick = { openDocumentTreeLauncher.launch(null) },
                onCreateProjectClick = { viewModel.setCreateProjectDialogOpen(true) },
                onOpenSetupWizard = { viewModel.openSetupWizard() },
                onOpenTerminal = { viewModel.setTerminalOpen(true) },
                onOpenSettings = { viewModel.setSettingsOpen(true) },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(GeminiBackground),
            containerColor = GeminiBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                GeminiTopBar(
                    selectedModelId = uiState.selectedModel,
                    backendState = uiState.backendState,
                    latencyMs = uiState.backendLatencyMs,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onModelPillClick = { viewModel.setModelSelectorOpen(true) },
                    onEngineBadgeClick = { viewModel.openSetupWizard() },
                    onSettingsClick = { viewModel.setSettingsOpen(true) }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(uiState.currentSession.messages, key = { it.id }) { message ->
                        ChatMessageItem(
                            message = message,
                            activeProject = uiState.activeProject
                        )
                    }

                    // Spacer so content isn't covered by floating dock
                    item {
                        Spacer(modifier = Modifier.height(110.dp))
                    }
                }

                // Docked Floating Input Bar (Gemini Style)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Pending Attachments Chips
                    if (uiState.pendingAttachments.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            items(uiState.pendingAttachments, key = { it.id }) { file ->
                                PendingAttachmentChip(
                                    file = file,
                                    onRemove = { viewModel.removeAttachment(file.id) }
                                )
                            }
                        }
                    }

                    // Floating Capsule Dock with Terminal Button
                    DockedInputBar(
                        inputText = inputText,
                        onInputTextChanged = { inputText = it },
                        isStreaming = uiState.isStreaming,
                        onSend = {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        },
                        onCancelStreaming = {
                            viewModel.cancelStreaming()
                        },
                        onOpenAttachments = {
                            showAttachmentOptions = !showAttachmentOptions
                        },
                        onOpenTerminal = {
                            viewModel.setTerminalOpen(true)
                        },
                        onVoiceInput = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla tu consulta para OpenCode...")
                            }
                            try {
                                speechRecognizerLauncher.launch(intent)
                            } catch (_: Exception) {
                            }
                        }
                    )
                }
            }
        }
    }

    // Terminal BottomSheet (Consola Deslizable)
    if (uiState.isTerminalOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        TerminalBottomSheet(
            sheetState = sheetState,
            logs = uiState.terminalLogs,
            backendState = uiState.backendState,
            engineUrl = uiState.localEngineUrl,
            onRefresh = { viewModel.checkEngineHealthNow() },
            onClearLogs = { viewModel.clearTerminalLogs() },
            onDismiss = { viewModel.setTerminalOpen(false) }
        )
    }

    // Attachment Options Sheet / Menu Dialog
    if (showAttachmentOptions) {
        AlertDialog(
            onDismissRequest = { showAttachmentOptions = false },
            title = {
                Text(
                    text = "Adjuntar Contexto al Chat",
                    fontWeight = FontWeight.Bold,
                    color = GeminiTextPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selecciona archivos de tu proyecto o capturas para el análisis de código:",
                        fontSize = 13.sp,
                        color = GeminiTextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GeminiSurfaceHigh)
                            .clickable {
                                showAttachmentOptions = false
                                openDocumentLauncher.launch(
                                    arrayOf(
                                        "text/*",
                                        "application/json",
                                        "application/javascript",
                                        "application/xml",
                                        "*/*"
                                    )
                                )
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = GeminiSparkBlue)
                        Column {
                            Text("Archivo de Código o Documento", fontSize = 14.sp, color = GeminiTextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Kotlin, Python, JSON, Markdown, etc.", fontSize = 11.sp, color = GeminiTextMuted)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GeminiSurfaceHigh)
                            .clickable {
                                showAttachmentOptions = false
                                pickMediaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = GeminiSparkCyan)
                        Column {
                            Text("Captura de Pantalla / Diagrama", fontSize = 14.sp, color = GeminiTextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Para explicar errores visuales o mockups", fontSize = 11.sp, color = GeminiTextMuted)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentOptions = false }) {
                    Text("Cerrar", color = GeminiSparkBlue)
                }
            },
            containerColor = GeminiSurfaceContainer,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Create Project Dialog
    if (uiState.isCreateProjectDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setCreateProjectDialogOpen(false) },
            title = {
                Text("Nuevo Proyecto en /MisProyectos", color = GeminiTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Crea un workspace para organizar tus archivos y contexto:",
                        color = GeminiTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        placeholder = { Text("Ej: Backend-Microservicio", color = GeminiTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GeminiTextPrimary,
                            unfocusedTextColor = GeminiTextPrimary,
                            focusedBorderColor = GeminiSparkBlue,
                            unfocusedBorderColor = GeminiBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createLocalProject(newProjectName)
                        newProjectName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiSparkBlue)
                ) {
                    Text("Crear", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setCreateProjectDialogOpen(false) }) {
                    Text("Cancelar", color = GeminiTextSecondary)
                }
            },
            containerColor = GeminiSurfaceContainer,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Model Selector Modal Bottom Sheet
    if (uiState.isModelSelectorOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModelSelectorSheet(
            sheetState = sheetState,
            models = uiState.availableModels,
            selectedModelId = uiState.selectedModel,
            onModelSelected = { modelId -> viewModel.selectModel(modelId) },
            onRefreshModels = { viewModel.fetchModels() },
            onOpenSettings = { viewModel.setSettingsOpen(true) },
            onDismiss = { viewModel.setModelSelectorOpen(false) }
        )
    }

    // Settings Modal
    if (uiState.isSettingsOpen) {
        SettingsModal(
            currentApiKey = uiState.apiKey,
            currentBaseUrl = uiState.baseUrl,
            currentSystemPrompt = uiState.systemPrompt,
            isFetchingModels = uiState.isFetchingModels,
            onSaveSettings = { key, url, prompt ->
                viewModel.updateSettings(key, url, prompt)
            },
            onTestConnection = { viewModel.fetchModels() },
            onDismiss = { viewModel.setSettingsOpen(false) }
        )
    }
}

/**
 * Minimalist Gemini Header with official isotype, app title, model selector pill, and Termux engine status badge.
 */
@Composable
fun GeminiTopBar(
    selectedModelId: String,
    backendState: BackendEngineState,
    latencyMs: Long,
    onMenuClick: () -> Unit,
    onModelPillClick: () -> Unit,
    onEngineBadgeClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val isConnected = backendState == BackendEngineState.CONNECTED

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = GeminiBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Menu button + Isotype + "OpenCodeMovil" Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menú de proyectos",
                        tint = GeminiTextPrimary
                    )
                }

                // Official Reduced Isotype Icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1116))
                        .border(1.dp, OpenCodeCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.opencode_isotype),
                        contentDescription = "OpenCodeMovil Isotype",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Text(
                    text = "OpenCodeMovil",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeminiTextPrimary,
                    letterSpacing = 0.2.sp
                )
            }

            // Right: Model Selector Pill + Engine Health Badge + Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Model Selector Pill (Open WebUI style)
                val modelShortName = remember(selectedModelId) {
                    val clean = selectedModelId.substringAfter("/")
                    if (clean.length > 12) clean.take(10) + "…" else clean
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(GeminiSurfaceContainer)
                        .border(1.dp, GeminiBorder, RoundedCornerShape(16.dp))
                        .clickable { onModelPillClick() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("model_selector_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = modelShortName,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeminiTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Cambiar modelo",
                        tint = GeminiTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Local Engine Health Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isConnected) Color(0xFF14241B) else Color(0xFF261D16))
                        .border(
                            1.dp,
                            if (isConnected) StatusGreen.copy(alpha = 0.5f) else StatusAmber.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onEngineBadgeClick() }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                        .testTag("top_engine_status_badge"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) StatusGreen else StatusAmber)
                    )
                    Text(
                        text = if (isConnected) "3001" else "Offline",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) StatusGreen else StatusAmber
                    )
                }

                // Settings Button
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Ajustes",
                        tint = GeminiTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Chat Message Item with User right bubble vs Assistant left bubble + Gemini sparkle.
 */
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    activeProject: com.example.data.model.ProjectWorkspace?,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // User Message Bubble (#282A2C)
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                    .background(GeminiBubbleUser)
                    .border(1.dp, GeminiBorder, RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Display attached files chips inside user message
                if (message.attachments.isNotEmpty()) {
                    for (att in message.attachments) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E2024))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = GeminiSparkBlue,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = att.name,
                                fontSize = 11.sp,
                                color = GeminiSparkBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = message.content,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = GeminiTextPrimary
                )
            }
        } else {
            // Assistant Message: Left aligned with Gemini Sparkle Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Sparkle Icon Avatar
                GeminiSparkleAvatar(isStreaming = message.isStreaming)

                Column(modifier = Modifier.weight(1f)) {
                    // Model Tag
                    if (!message.modelUsed.isNullOrBlank()) {
                        Text(
                            text = message.modelUsed.substringAfter("/"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeminiTextMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (message.content.isEmpty() && message.isStreaming) {
                        // Pulsing Shimmer Loading Indicator
                        PulsingTypingIndicator()
                    } else {
                        MarkdownText(
                            text = message.content,
                            textColor = GeminiTextPrimary
                        )

                        // If streaming active, show mini typing pulse at end
                        if (message.isStreaming) {
                            Spacer(modifier = Modifier.height(4.dp))
                            InlinePulsingDot()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Gemini Sparkle Avatar with glowing cyan-blue-purple gradient.
 */
@Composable
fun GeminiSparkleAvatar(isStreaming: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle_anim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        GeminiSparkBlue.copy(alpha = if (isStreaming) glowAlpha else 0.85f),
                        GeminiSparkPurple.copy(alpha = if (isStreaming) glowAlpha else 0.85f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("✦", fontSize = 16.sp, color = Color.White)
    }
}

/**
 * Pulsing Typing Indicator for initial streaming state.
 */
@Composable
fun PulsingTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typing_alpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GeminiBubbleAi)
            .border(1.dp, GeminiBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Generando código...",
            fontSize = 13.sp,
            color = GeminiSparkBlue.copy(alpha = alpha)
        )
    }
}

@Composable
fun InlinePulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(GeminiSparkBlue.copy(alpha = alpha))
    )
}

/**
 * Docked Floating Input Bar with Terminal Console button, attachment button, dynamic field, mic, and send.
 */
@Composable
fun DockedInputBar(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    isStreaming: Boolean,
    onSend: () -> Unit,
    onCancelStreaming: () -> Unit,
    onOpenAttachments: () -> Unit,
    onOpenTerminal: () -> Unit,
    onVoiceInput: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(GeminiSurfaceContainer)
            .border(1.2.dp, GeminiBorder, RoundedCornerShape(28.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Attachment Button (+)
        IconButton(
            onClick = onOpenAttachments,
            modifier = Modifier
                .size(40.dp)
                .testTag("attach_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adjuntar archivos o fotos",
                tint = GeminiTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Terminal Console Button
        IconButton(
            onClick = onOpenTerminal,
            modifier = Modifier
                .size(40.dp)
                .testTag("terminal_toggle_button")
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Abrir consola de logs",
                tint = GeminiSparkCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        // Expandable Dynamic TextField
        BasicTextField(
            value = inputText,
            onValueChange = onInputTextChanged,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp, vertical = 10.dp)
                .testTag("chat_input_textfield"),
            textStyle = TextStyle(
                color = GeminiTextPrimary,
                fontSize = 15.sp,
                lineHeight = 21.sp
            ),
            cursorBrush = SolidColor(GeminiSparkBlue),
            maxLines = 6,
            decorationBox = { innerTextField ->
                if (inputText.isEmpty()) {
                    Text(
                        text = "Pregunta o escribe código...",
                        color = GeminiTextMuted,
                        fontSize = 15.sp
                    )
                }
                innerTextField()
            }
        )

        // Mic Button
        if (inputText.isBlank() && !isStreaming) {
            IconButton(
                onClick = onVoiceInput,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Dictar por voz",
                    tint = GeminiTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Dynamic Send / Stop Button
        if (isStreaming) {
            // Stop Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3B2024))
                    .clickable { onCancelStreaming() }
                    .testTag("stop_streaming_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Detener generación",
                    tint = StatusRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            val hasContent = inputText.isNotBlank()
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (hasContent) GeminiSparkBlue else Color.Transparent)
                    .border(
                        width = if (hasContent) 0.dp else 1.dp,
                        color = if (hasContent) Color.Transparent else GeminiBorder,
                        shape = CircleShape
                    )
                    .clickable(enabled = hasContent) { onSend() }
                    .testTag("send_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar mensaje",
                    tint = if (hasContent) Color(0xFF002A66) else GeminiTextMuted,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

/**
 * Chip showing pending attachments before sending message.
 */
@Composable
fun PendingAttachmentChip(
    file: AttachedFile,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GeminiSurfaceHigh)
            .border(1.dp, GeminiBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = GeminiSparkBlue,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = file.name,
            fontSize = 12.sp,
            color = GeminiTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Eliminar adjunto",
            tint = GeminiTextSecondary,
            modifier = Modifier
                .size(16.dp)
                .clickable { onRemove() }
        )
    }
}
