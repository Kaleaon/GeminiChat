package com.example.aistudioapp.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.aistudioapp.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gemini_settings"
)

class SettingsDataStore(context: Context) {

    private val dataStore = context.settingsDataStore
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val settingsKey = stringPreferencesKey("app_settings_json")

    val appSettings: Flow<AppSettings> = dataStore.data.map { preferences ->
        preferences[settingsKey]?.let { stored ->
            runCatching { json.decodeFromString(AppSettings.serializer(), stored) }
                .getOrNull()
        } ?: AppSettings()
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { preferences ->
            val current = preferences[settingsKey]?.let {
                runCatching { json.decodeFromString(AppSettings.serializer(), it) }
                    .getOrNull()
            } ?: AppSettings()

            val updated = transform(current)
            preferences[settingsKey] = json.encodeToString(AppSettings.serializer(), updated)
        }
    }
}
