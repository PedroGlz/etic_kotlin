package com.example.etic.core.session

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Un DataStore por app
val Context.sessionDataStore by preferencesDataStore(name = "session_prefs")
