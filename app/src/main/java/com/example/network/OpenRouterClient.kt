package com.example.network

import com.example.data.model.AiModel
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class OpenRouterClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val defaultModels = listOf(
        AiModel(
            id = "deepseek/deepseek-chat",
            name = "DeepSeek-Chat (V3)",
            provider = "DeepSeek",
            description = "Excelente razonamiento y capacidades avanzadas de código.",
            contextLength = 64000
        ),
        AiModel(
            id = "anthropic/claude-3.5-sonnet",
            name = "Claude 3.5 Sonnet",
            provider = "Anthropic",
            description = "Líder de la industria en arquitectura de software y generación de código.",
            contextLength = 200000
        ),
        AiModel(
            id = "google/gemini-2.0-flash-exp:free",
            name = "Gemini 2.0 Flash (Free)",
            provider = "Google",
            description = "Respuesta ultra rápida con alta calidad y contexto masivo.",
            contextLength = 1000000
        ),
        AiModel(
            id = "google/gemini-flash-1.5",
            name = "Gemini 1.5 Flash",
            provider = "Google",
            description = "Modelo optimizado multimodal con gran velocidad de respuesta.",
            contextLength = 1000000
        ),
        AiModel(
            id = "meta-llama/llama-3.3-70b-instruct",
            name = "Llama 3.3 70B Instruct",
            provider = "Meta",
            description = "Modelo abierto de última generación altamente capaz.",
            contextLength = 128000
        ),
        AiModel(
            id = "qwen/qwen-2.5-coder-32b-instruct",
            name = "Qwen 2.5 Coder 32B",
            provider = "Qwen",
            description = "Especializado en resolución de bugs, refactor y programación.",
            contextLength = 128000
        ),
        AiModel(
            id = "openai/gpt-4o-mini",
            name = "GPT-4o Mini",
            provider = "OpenAI",
            description = "Rápido, económico y de alto rendimiento en tareas generales.",
            contextLength = 128000
        )
    )

    /**
     * Fetches models dynamically from the given Base URL using the standard OpenAI/OpenRouter /models schema.
     */
    suspend fun fetchModels(baseUrl: String, apiKey: String): List<AiModel> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext defaultModels
        }

        val endpoint = "${baseUrl.trimEnd('/')}/models"
        val requestBuilder = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")

        try {
            val response = client.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext defaultModels
            }

            val body = response.body?.string() ?: return@withContext defaultModels
            val json = JSONObject(body)
            val dataArray = json.optJSONArray("data") ?: return@withContext defaultModels

            val list = mutableListOf<AiModel>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val id = item.optString("id", "")
                if (id.isBlank()) continue
                val rawName = item.optString("name", id)
                val description = item.optString("description", "")
                val contextLength = item.optInt("context_length", 128000)

                val provider = if (id.contains("/")) {
                    id.substringBefore("/").replaceFirstChar { it.uppercase() }
                } else {
                    "Custom"
                }

                list.add(
                    AiModel(
                        id = id,
                        name = if (rawName.isBlank()) id else rawName,
                        provider = provider,
                        description = description,
                        contextLength = contextLength
                    )
                )
            }

            if (list.isEmpty()) defaultModels else list
        } catch (_: Exception) {
            defaultModels
        }
    }

    /**
     * Streams chat completions token-by-token using Server-Sent Events (SSE).
     */
    fun streamChatCompletion(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>,
        systemPrompt: String
    ): Flow<String> = flow {
        val endpoint = "${baseUrl.trimEnd('/')}/chat/completions"

        // Build messages array
        val jsonMessages = JSONArray()

        if (systemPrompt.isNotBlank()) {
            val sysObj = JSONObject()
            sysObj.put("role", "system")
            sysObj.put("content", systemPrompt)
            jsonMessages.put(sysObj)
        }

        for (msg in messages) {
            val mObj = JSONObject()
            val roleStr = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "system"
            }
            mObj.put("role", roleStr)

            // Include attached context if any
            val fullContent = if (msg.attachments.isNotEmpty()) {
                val attachmentsText = msg.attachments.joinToString(separator = "\n\n") { file ->
                    "--- Archivo Adjunto: ${file.name} ---\n${file.content}"
                }
                "$attachmentsText\n\n${msg.content}"
            } else {
                msg.content
            }

            mObj.put("content", fullContent)
            jsonMessages.put(mObj)
        }

        val requestBodyJson = JSONObject().apply {
            put("model", model)
            put("messages", jsonMessages)
            put("stream", true)
            put("temperature", 0.7)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://opencode.local")
            .addHeader("X-Title", "OpenCodeMovil")

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val call = client.newCall(requestBuilder.build())
        val response = call.execute()

        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: ""
            val errMessage = try {
                val errJson = JSONObject(errBody)
                errJson.optJSONObject("error")?.optString("message", "") ?: errBody
            } catch (_: Exception) {
                errBody
            }
            val finalMsg = if (errMessage.isNotBlank()) {
                "Error (${response.code}): $errMessage"
            } else {
                "Error en la petición: HTTP ${response.code}"
            }
            throw RuntimeException(finalMsg)
        }

        val responseBody = response.body ?: throw RuntimeException("Respuesta vacía del servidor")
        val reader = BufferedReader(InputStreamReader(responseBody.byteStream(), Charsets.UTF_8))

        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (currentLine.isEmpty() || currentLine.startsWith(":")) {
                    // SSE comment or keep-alive heartbeat
                    continue
                }

                if (currentLine.startsWith("data:")) {
                    val data = currentLine.removePrefix("data:").trim()
                    if (data == "[DONE]") {
                        break
                    }

                    try {
                        val chunkJson = JSONObject(data)
                        val choices = chunkJson.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val firstChoice = choices.getJSONObject(0)
                            val delta = firstChoice.optJSONObject("delta")
                            val textDelta = delta?.optString("content", "")
                                ?: firstChoice.optString("text", "")

                            if (!textDelta.isNullOrEmpty()) {
                                emit(textDelta)
                            }
                        }
                    } catch (_: Exception) {
                        // Skip malformed SSE chunks
                    }
                }
            }
        } finally {
            try {
                reader.close()
                responseBody.close()
            } catch (_: Exception) {
            }
        }
    }.flowOn(Dispatchers.IO)
}
