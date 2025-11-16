package com.example.aistudioapp.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.aistudioapp.data.model.AvatarSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.avatarDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "avatar_preferences"
)

class AvatarPreferencesStore(context: Context) {

    private val dataStore = context.avatarDataStore
    private val json = Json { ignoreUnknownKeys = true }
    private val avatarKey = stringPreferencesKey("active_avatar_json")

    val activeAvatar: Flow<AvatarSelection?> = dataStore.data.map { prefs ->
        prefs[avatarKey]?.let { raw ->
            runCatching { json.decodeFromString(AvatarSelection.serializer(), raw) }
                .getOrNull()
        }
    }

    suspend fun update(selection: AvatarSelection) {
        dataStore.edit { prefs ->
            prefs[avatarKey] = json.encodeToString(AvatarSelection.serializer(), selection)
        }
    }
}
