package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class HealthCheckResult {
    data class Success(val status: String, val latencyMs: Long) : HealthCheckResult()
    data class Failure(val reason: String) : HealthCheckResult()
}

class LocalEngineClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(800, TimeUnit.MILLISECONDS)
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .build()

    /**
     * Pings the local backend running on Termux at http://127.0.0.1:3001/api/health
     */
    suspend fun checkHealth(baseUrl: String = "http://127.0.0.1:3001"): HealthCheckResult =
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val endpoint = "${baseUrl.trimEnd('/')}/api/health"

            val request = Request.Builder()
                .url(endpoint)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()
                val latency = System.currentTimeMillis() - startTime
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val status = try {
                        val json = JSONObject(body)
                        json.optString("status", "ok")
                    } catch (_: Exception) {
                        "ok"
                    }
                    HealthCheckResult.Success(status = status, latencyMs = latency)
                } else {
                    HealthCheckResult.Failure("HTTP ${response.code}: Backend no preparado")
                }
            } catch (e: Exception) {
                HealthCheckResult.Failure(e.localizedMessage ?: "Conexión rechazada (Motor inactivo)")
            }
        }

    /**
     * Fetches real-time log entries from http://127.0.0.1:3001/api/logs
     */
    suspend fun fetchLogs(baseUrl: String = "http://127.0.0.1:3001"): List<String> =
        withContext(Dispatchers.IO) {
            val endpoint = "${baseUrl.trimEnd('/')}/api/logs"
            val request = Request.Builder().url(endpoint).get().build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    if (body.trim().startsWith("{")) {
                        val json = JSONObject(body)
                        val logsArray = json.optJSONArray("logs")
                        if (logsArray != null) {
                            val list = mutableListOf<String>()
                            for (i in 0 until logsArray.length()) {
                                list.add(logsArray.optString(i))
                            }
                            return@withContext list
                        }
                    }
                    // Plain text lines
                    body.lines().filter { it.isNotBlank() }
                } else {
                    emptyList()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
}
