package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "opencode_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_API_KEY = stringPreferencesKey("opencode_api_key")
        val KEY_BASE_URL = stringPreferencesKey("opencode_base_url")
        val KEY_SELECTED_MODEL = stringPreferencesKey("opencode_selected_model")
        val KEY_SYSTEM_PROMPT = stringPreferencesKey("opencode_system_prompt")
        val KEY_WORKSPACE_URI = stringPreferencesKey("opencode_workspace_uri")
        val KEY_WORKSPACE_NAME = stringPreferencesKey("opencode_workspace_name")
        val KEY_LOCAL_ENGINE_URL = stringPreferencesKey("opencode_local_engine_url")
        val KEY_BYPASS_SETUP = booleanPreferencesKey("opencode_bypass_setup")

        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1"
        const val DEFAULT_LOCAL_ENGINE_URL = "http://127.0.0.1:3001"
        const val DEFAULT_MODEL = "deepseek/deepseek-chat"
        const val DEFAULT_SYSTEM_PROMPT =
            "Eres un asistente de programación experto y conciso. Proporciona soluciones claras y código limpio y moderno con explicaciones técnicas breves."
    }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_API_KEY] ?: ""
    }

    val baseUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_BASE_URL] ?: DEFAULT_BASE_URL
    }

    val localEngineUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOCAL_ENGINE_URL] ?: DEFAULT_LOCAL_ENGINE_URL
    }

    val bypassSetupFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BYPASS_SETUP] ?: false
    }

    val selectedModelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_MODEL] ?: DEFAULT_MODEL
    }

    val systemPromptFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SYSTEM_PROMPT] ?: DEFAULT_SYSTEM_PROMPT
    }

    val workspaceUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_WORKSPACE_URI]
    }

    val workspaceNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_WORKSPACE_NAME] ?: "MisProyectos"
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_API_KEY] = key.trim()
        }
    }

    suspend fun saveBaseUrl(url: String) {
        var cleanUrl = url.trim()
        if (cleanUrl.endsWith("/")) {
            cleanUrl = cleanUrl.dropLast(1)
        }
        context.dataStore.edit { preferences ->
            preferences[KEY_BASE_URL] = cleanUrl
        }
    }

    suspend fun saveLocalEngineUrl(url: String) {
        var cleanUrl = url.trim()
        if (cleanUrl.endsWith("/")) {
            cleanUrl = cleanUrl.dropLast(1)
        }
        context.dataStore.edit { preferences ->
            preferences[KEY_LOCAL_ENGINE_URL] = cleanUrl
        }
    }

    suspend fun saveBypassSetup(bypass: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BYPASS_SETUP] = bypass
        }
    }

    suspend fun saveSelectedModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_MODEL] = model
        }
    }

    suspend fun saveSystemPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SYSTEM_PROMPT] = prompt
        }
    }

    suspend fun saveWorkspace(uriString: String?, name: String) {
        context.dataStore.edit { preferences ->
            if (uriString != null) {
                preferences[KEY_WORKSPACE_URI] = uriString
            } else {
                preferences.remove(KEY_WORKSPACE_URI)
            }
            preferences[KEY_WORKSPACE_NAME] = name
        }
    }
}
