package com.example.data.model

import java.util.UUID

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class AttachedFile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val content: String = "",
    val sizeBytes: Long = 0,
    val mimeType: String = "text/plain",
    val uriString: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachments: List<AttachedFile> = emptyList(),
    val isStreaming: Boolean = false,
    val modelUsed: String? = null
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Nueva Conversación",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val modelId: String = "deepseek/deepseek-chat",
    val messages: List<ChatMessage> = emptyList()
)

data class AiModel(
    val id: String,
    val name: String,
    val provider: String,
    val description: String = "",
    val contextLength: Int = 128000
)

data class ProjectWorkspace(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val treeUriString: String? = null,
    val path: String = "/MisProyectos",
    val files: List<String> = emptyList(),
    val isLinked: Boolean = false
)
