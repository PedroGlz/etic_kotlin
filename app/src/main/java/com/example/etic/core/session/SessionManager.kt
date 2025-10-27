package com.example.etic.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
        private val KEY_USERNAME  = stringPreferencesKey("username")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[KEY_LOGGED_IN] ?: false }
    val username:  Flow<String?>  = dataStore.data.map { it[KEY_USERNAME] }

    suspend fun setLoggedIn(value: Boolean) {
        dataStore.edit { it[KEY_LOGGED_IN] = value }
    }

    suspend fun setUsername(name: String) {
        dataStore.edit { it[KEY_USERNAME] = name }
    }

    suspend fun clear() {
        dataStore.edit {
            it[KEY_LOGGED_IN] = false
            it.remove(KEY_USERNAME)
        }
    }
}
