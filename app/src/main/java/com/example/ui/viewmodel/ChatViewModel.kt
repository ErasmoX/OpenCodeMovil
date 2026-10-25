package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AiModel
import com.example.data.model.AttachedFile
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.MessageRole
import com.example.data.model.ProjectWorkspace
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.ProjectManager
import com.example.network.HealthCheckResult
import com.example.network.LocalEngineClient
import com.example.network.OpenRouterClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class BackendEngineState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

data class ChatUiState(
    val currentSession: ChatSession = ChatSession(),
    val sessions: List<ChatSession> = emptyList(),
    val apiKey: String = "",
    val baseUrl: String = SettingsDataStore.DEFAULT_BASE_URL,
    val localEngineUrl: String = SettingsDataStore.DEFAULT_LOCAL_ENGINE_URL,
    val selectedModel: String = SettingsDataStore.DEFAULT_MODEL,
    val availableModels: List<AiModel> = emptyList(),
    val systemPrompt: String = SettingsDataStore.DEFAULT_SYSTEM_PROMPT,
    val isStreaming: Boolean = false,
    val isFetchingModels: Boolean = false,
    val errorMessage: String? = null,
    val pendingAttachments: List<AttachedFile> = emptyList(),
    val projects: List<ProjectWorkspace> = emptyList(),
    val activeProject: ProjectWorkspace? = null,
    val isSettingsOpen: Boolean = false,
    val isModelSelectorOpen: Boolean = false,
    val isCreateProjectDialogOpen: Boolean = false,
    // Local Backend Engine State
    val backendState: BackendEngineState = BackendEngineState.CONNECTING,
    val hasBypassedSetup: Boolean = false,
    val isSetupWizardForcedOpen: Boolean = false,
    val backendLatencyMs: Long = 0,
    val backendFailureReason: String = "Conectando al motor local...",
    val terminalLogs: List<String> = emptyList(),
    val isTerminalOpen: Boolean = false,
    val isInitialized: Boolean = false
) {
    val shouldShowSetupWizard: Boolean
        get() = isInitialized && ((backendState != BackendEngineState.CONNECTED && !hasBypassedSetup) || isSetupWizardForcedOpen)
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)
    private val openRouterClient = OpenRouterClient()
    private val localEngineClient = LocalEngineClient()
    private val projectManager = ProjectManager(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null
    private var healthCheckJob: Job? = null

    init {
        // Initial chat message welcoming the developer
        val initialSession = ChatSession(
            title = "Nueva Conversación",
            messages = listOf(
                ChatMessage(
                    role = MessageRole.ASSISTANT,
                    content = """¡Hola! Soy **OpenCodeMovil**, tu entorno de IA nativo para desarrollo y arquitectura de software.

- Motor backend local: `http://127.0.0.1:3001` (Termux).
- Modelos disponibles arriba (DeepSeek, Claude 3.5 Sonnet, Gemini Flash, Llama 3).
- Abre la **Terminal inferior** para inspeccionar logs del motor en tiempo real.
- Respuestas recibidas vía **streaming SSE** token por token.

¿En qué proyecto o código quieres trabajar hoy?""",
                    modelUsed = "OpenCodeMovil Engine"
                )
            )
        )

        val defaultProjects = projectManager.getDefaultProjects()

        val initialLogs = listOf(
            "[INIT] OpenCodeMovil Client v1.0 iniciado",
            "[HEALTH] Iniciando monitor continuo de salud hacia 127.0.0.1:3001",
            "[SYSTEM] Esperando heartbeat del motor local en Termux..."
        )

        _uiState.update {
            it.copy(
                currentSession = initialSession,
                sessions = listOf(initialSession),
                availableModels = openRouterClient.defaultModels,
                projects = defaultProjects,
                activeProject = defaultProjects.firstOrNull(),
                terminalLogs = initialLogs
            )
        }

        // Load saved settings
        viewModelScope.launch {
            val key = settingsDataStore.apiKeyFlow.first()
            val url = settingsDataStore.baseUrlFlow.first()
            val engineUrl = settingsDataStore.localEngineUrlFlow.first()
            val bypass = settingsDataStore.bypassSetupFlow.first()
            val model = settingsDataStore.selectedModelFlow.first()
            val prompt = settingsDataStore.systemPromptFlow.first()
            val workspaceUri = settingsDataStore.workspaceUriFlow.first()
            val workspaceName = settingsDataStore.workspaceNameFlow.first()

            _uiState.update { current ->
                current.copy(
                    apiKey = key,
                    baseUrl = url,
                    localEngineUrl = engineUrl,
                    hasBypassedSetup = bypass,
                    selectedModel = model,
                    systemPrompt = prompt,
                    isInitialized = true,
                    activeProject = if (workspaceUri != null) {
                        ProjectWorkspace(
                            name = workspaceName,
                            treeUriString = workspaceUri,
                            isLinked = true
                        )
                    } else current.activeProject
                )
            }

            fetchModels()
        }

        // Start 2s continuous Health Check ping polling
        startHealthCheckLoop()
    }

    private fun startHealthCheckLoop() {
        healthCheckJob?.cancel()
        healthCheckJob = viewModelScope.launch {
            delay(600) // Allow initial composition to settle
            while (isActive) {
                checkEngineHealth()
                delay(2000)
            }
        }
    }

    suspend fun checkEngineHealth() {
        val currentUrl = _uiState.value.localEngineUrl
        val result = localEngineClient.checkHealth(currentUrl)

        when (result) {
            is HealthCheckResult.Success -> {
                val previousState = _uiState.value.backendState
                val newLog = if (previousState != BackendEngineState.CONNECTED) {
                    "[CONNECTED] ¡Motor local en $currentUrl respondió 200 OK (${result.latencyMs}ms)!"
                } else null

                _uiState.update { curr ->
                    val updatedLogs = if (newLog != null) curr.terminalLogs + newLog else curr.terminalLogs
                    curr.copy(
                        backendState = BackendEngineState.CONNECTED,
                        backendLatencyMs = result.latencyMs,
                        backendFailureReason = "",
                        isSetupWizardForcedOpen = false, // Auto-transition to main screen!
                        terminalLogs = updatedLogs.takeLast(100)
                    )
                }

                // Periodically fetch real logs from backend
                val serverLogs = localEngineClient.fetchLogs(currentUrl)
                if (serverLogs.isNotEmpty()) {
                    _uiState.update { it.copy(terminalLogs = (it.terminalLogs + serverLogs).distinct().takeLast(120)) }
                }
            }
            is HealthCheckResult.Failure -> {
                _uiState.update { curr ->
                    curr.copy(
                        backendState = BackendEngineState.DISCONNECTED,
                        backendFailureReason = result.reason
                    )
                }
            }
        }
    }

    fun checkEngineHealthNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(backendState = BackendEngineState.CONNECTING) }
            checkEngineHealth()
        }
    }

    fun bypassSetupWizard(bypass: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveBypassSetup(bypass)
            _uiState.update {
                it.copy(
                    hasBypassedSetup = bypass,
                    isSetupWizardForcedOpen = false
                )
            }
        }
    }

    fun openSetupWizard() {
        _uiState.update { it.copy(isSetupWizardForcedOpen = true) }
    }

    fun setTerminalOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isTerminalOpen = isOpen) }
    }

    fun clearTerminalLogs() {
        _uiState.update { it.copy(terminalLogs = listOf("[LOGS] Consola limpiada por el usuario")) }
    }

    fun addTerminalLog(log: String) {
        _uiState.update { it.copy(terminalLogs = (it.terminalLogs + log).takeLast(120)) }
    }

    fun fetchModels() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isFetchingModels = true) }
            val models = openRouterClient.fetchModels(state.baseUrl, state.apiKey)
            _uiState.update { current ->
                current.copy(
                    availableModels = models,
                    isFetchingModels = false
                )
            }
        }
    }

    fun selectModel(modelId: String) {
        viewModelScope.launch {
            settingsDataStore.saveSelectedModel(modelId)
            _uiState.update { it.copy(selectedModel = modelId, isModelSelectorOpen = false) }
            addTerminalLog("[MODEL] Modelo activo cambiado a: $modelId")
        }
    }

    fun updateSettings(apiKey: String, baseUrl: String, systemPrompt: String) {
        viewModelScope.launch {
            settingsDataStore.saveApiKey(apiKey)
            settingsDataStore.saveBaseUrl(baseUrl)
            settingsDataStore.saveSystemPrompt(systemPrompt)
            _uiState.update {
                it.copy(
                    apiKey = apiKey.trim(),
                    baseUrl = baseUrl.trim(),
                    systemPrompt = systemPrompt.trim(),
                    isSettingsOpen = false,
                    errorMessage = null
                )
            }
            addTerminalLog("[CONFIG] Ajustes actualizados (Base URL: $baseUrl)")
            fetchModels()
        }
    }

    fun updateLocalEngineUrl(url: String) {
        viewModelScope.launch {
            settingsDataStore.saveLocalEngineUrl(url)
            _uiState.update { it.copy(localEngineUrl = url.trim()) }
            checkEngineHealthNow()
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        val currentAttachments = _uiState.value.pendingAttachments
        if (trimmed.isEmpty() && currentAttachments.isEmpty()) return

        val state = _uiState.value

        // If API key is blank and we are not using a local backend with its own key, notify user
        if (state.apiKey.isBlank() && state.baseUrl.contains("openrouter")) {
            _uiState.update {
                it.copy(
                    isSettingsOpen = true,
                    errorMessage = "Ingresa tu API Key de OpenRouter u otro proveedor en Ajustes para comenzar."
                )
            }
            return
        }

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = trimmed,
            attachments = currentAttachments
        )

        val updatedMessages = state.currentSession.messages + userMessage
        val activeModelId = state.selectedModel

        // Auto-title session if it's the first user message
        val updatedTitle = if (state.currentSession.messages.size <= 1) {
            if (trimmed.length > 28) trimmed.take(28) + "..." else trimmed
        } else {
            state.currentSession.title
        }

        val updatedSession = state.currentSession.copy(
            title = updatedTitle,
            lastModifiedAt = System.currentTimeMillis(),
            messages = updatedMessages
        )

        val assistantMessageId = java.util.UUID.randomUUID().toString()
        val assistantPlaceholder = ChatMessage(
            id = assistantMessageId,
            role = MessageRole.ASSISTANT,
            content = "",
            isStreaming = true,
            modelUsed = activeModelId
        )

        val messagesWithAssistant = updatedMessages + assistantPlaceholder
        val sessionWithAssistant = updatedSession.copy(messages = messagesWithAssistant)

        _uiState.update { current ->
            current.copy(
                currentSession = sessionWithAssistant,
                sessions = current.sessions.map { s -> if (s.id == updatedSession.id) sessionWithAssistant else s },
                pendingAttachments = emptyList(),
                isStreaming = true,
                errorMessage = null
            )
        }

        addTerminalLog("[CHAT] Solicitud enviada a $activeModelId (streaming activado)")

        // Launch SSE streaming request
        streamingJob?.cancel()
        streamingJob = viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                openRouterClient.streamChatCompletion(
                    baseUrl = state.baseUrl,
                    apiKey = state.apiKey,
                    model = activeModelId,
                    messages = updatedMessages,
                    systemPrompt = state.systemPrompt
                ).collect { chunk ->
                    accumulated.append(chunk)
                    val streamingText = accumulated.toString()

                    _uiState.update { curr ->
                        val updatedList = curr.currentSession.messages.map { msg ->
                            if (msg.id == assistantMessageId) {
                                msg.copy(content = streamingText, isStreaming = true)
                            } else msg
                        }
                        val s = curr.currentSession.copy(messages = updatedList)
                        curr.copy(
                            currentSession = s,
                            sessions = curr.sessions.map { sess -> if (sess.id == s.id) s else sess }
                        )
                    }
                }

                // Streaming finished cleanly
                _uiState.update { curr ->
                    val finalText = if (accumulated.isEmpty()) {
                        "Respuesta completada sin texto adicional."
                    } else accumulated.toString()

                    val updatedList = curr.currentSession.messages.map { msg ->
                        if (msg.id == assistantMessageId) {
                            msg.copy(content = finalText, isStreaming = false)
                        } else msg
                    }
                    val s = curr.currentSession.copy(
                        messages = updatedList,
                        lastModifiedAt = System.currentTimeMillis()
                    )
                    curr.copy(
                        currentSession = s,
                        sessions = curr.sessions.map { sess -> if (sess.id == s.id) s else sess },
                        isStreaming = false
                    )
                }
                addTerminalLog("[STREAM] Respuesta generada exitosamente (${accumulated.length} caracteres)")
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Error de conexión"
                addTerminalLog("[ERROR] Fallo en streaming: $errorMsg")
                _uiState.update { curr ->
                    val updatedList = curr.currentSession.messages.map { msg ->
                        if (msg.id == assistantMessageId) {
                            msg.copy(
                                content = if (accumulated.isNotEmpty()) {
                                    "${accumulated}\n\n⚠️ *[Streaming interrumpido: $errorMsg]*"
                                } else {
                                    "⚠️ *Error al generar respuesta:*\n$errorMsg"
                                },
                                isStreaming = false
                            )
                        } else msg
                    }
                    val s = curr.currentSession.copy(messages = updatedList)
                    curr.copy(
                        currentSession = s,
                        sessions = curr.sessions.map { sess -> if (sess.id == s.id) s else sess },
                        isStreaming = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    fun cancelStreaming() {
        streamingJob?.cancel()
        streamingJob = null
        addTerminalLog("[STREAM] Generación cancelada por el usuario")
        _uiState.update { curr ->
            val updatedList = curr.currentSession.messages.map { msg ->
                if (msg.isStreaming) msg.copy(isStreaming = false) else msg
            }
            val s = curr.currentSession.copy(messages = updatedList)
            curr.copy(
                currentSession = s,
                sessions = curr.sessions.map { sess -> if (sess.id == s.id) s else sess },
                isStreaming = false
            )
        }
    }

    fun createNewChat() {
        val newSession = ChatSession(
            title = "Nueva Conversación",
            modelId = _uiState.value.selectedModel,
            messages = emptyList()
        )
        _uiState.update { current ->
            current.copy(
                currentSession = newSession,
                sessions = listOf(newSession) + current.sessions,
                pendingAttachments = emptyList(),
                errorMessage = null
            )
        }
        addTerminalLog("[CHAT] Nueva conversación creada")
    }

    fun selectChat(sessionId: String) {
        val target = _uiState.value.sessions.firstOrNull { it.id == sessionId } ?: return
        _uiState.update { it.copy(currentSession = target, pendingAttachments = emptyList()) }
    }

    fun deleteChat(sessionId: String) {
        _uiState.update { current ->
            val remaining = current.sessions.filterNot { it.id == sessionId }
            val nextCurrent = if (current.currentSession.id == sessionId) {
                remaining.firstOrNull() ?: ChatSession(title = "Nueva Conversación")
            } else {
                current.currentSession
            }
            current.copy(
                sessions = remaining.ifEmpty { listOf(nextCurrent) },
                currentSession = nextCurrent
            )
        }
    }

    fun addAttachment(file: AttachedFile) {
        _uiState.update { current ->
            current.copy(pendingAttachments = current.pendingAttachments + file)
        }
        addTerminalLog("[CONTEXT] Archivo adjunto: ${file.name}")
    }

    fun removeAttachment(fileId: String) {
        _uiState.update { current ->
            current.copy(pendingAttachments = current.pendingAttachments.filterNot { it.id == fileId })
        }
    }

    fun attachFileFromUri(uri: Uri, displayName: String) {
        viewModelScope.launch {
            val content = projectManager.readFileContent(uri)
            val attached = AttachedFile(
                name = displayName,
                content = content,
                sizeBytes = content.toByteArray().size.toLong(),
                uriString = uri.toString()
            )
            addAttachment(attached)
        }
    }

    fun setWorkspaceTreeUri(treeUri: Uri, folderName: String) {
        viewModelScope.launch {
            settingsDataStore.saveWorkspace(treeUri.toString(), folderName)
            val files = projectManager.listFilesFromTreeUri(treeUri)
            val project = ProjectWorkspace(
                name = folderName,
                treeUriString = treeUri.toString(),
                path = "/$folderName",
                files = files.map { it.name },
                isLinked = true
            )
            _uiState.update { current ->
                val updatedProjects = listOf(project) + current.projects.filterNot { it.name == folderName }
                current.copy(
                    activeProject = project,
                    projects = updatedProjects
                )
            }
            addTerminalLog("[STORAGE] Carpeta /${folderName} vinculada con éxito (${files.size} archivos)")
        }
    }

    fun createLocalProject(projectName: String) {
        val name = projectName.trim().ifEmpty { "Proyecto-${System.currentTimeMillis() % 1000}" }
        val newProj = ProjectWorkspace(
            name = name,
            path = "/MisProyectos/$name",
            files = listOf("README.md", "main.kt"),
            isLinked = false
        )
        _uiState.update { current ->
            current.copy(
                projects = listOf(newProj) + current.projects,
                activeProject = newProj,
                isCreateProjectDialogOpen = false
            )
        }
        addTerminalLog("[STORAGE] Nuevo proyecto creado: $name")
    }

    fun selectProject(project: ProjectWorkspace) {
        _uiState.update { it.copy(activeProject = project) }
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen) }
    }

    fun setModelSelectorOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isModelSelectorOpen = isOpen) }
    }

    fun setCreateProjectDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isCreateProjectDialogOpen = isOpen) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
