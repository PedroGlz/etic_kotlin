package com.example.etic.core.settings

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SETTINGS_STORE_NAME = "settings"

val Context.settingsDataStore by preferencesDataStore(name = SETTINGS_STORE_NAME)

class EticPrefs(private val dataStore: DataStore<Preferences>) {
    private object Keys {
        val ROOT_TREE_URI = stringPreferencesKey("root_tree_uri")
        val ACTIVE_INSPECTION_NUM = stringPreferencesKey("active_inspection_num")
    }

    val rootTreeUriFlow: Flow<String?> =
        dataStore.data.map { it[Keys.ROOT_TREE_URI]?.takeIf { v -> v.isNotBlank() } }

    val activeInspectionNumFlow: Flow<String?> =
        dataStore.data.map { it[Keys.ACTIVE_INSPECTION_NUM]?.takeIf { v -> v.isNotBlank() } }

    suspend fun setRootTreeUri(uri: Uri?) {
        dataStore.edit { prefs ->
            if (uri == null) prefs.remove(Keys.ROOT_TREE_URI)
            else prefs[Keys.ROOT_TREE_URI] = uri.toString()
        }
    }

    suspend fun setActiveInspectionNum(value: String?) {
        dataStore.edit { prefs ->
            if (value.isNullOrBlank()) prefs.remove(Keys.ACTIVE_INSPECTION_NUM)
            else prefs[Keys.ACTIVE_INSPECTION_NUM] = value
        }
    }
}
