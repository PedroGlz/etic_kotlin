package com.example.etic.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.etic.ui.theme.FontSizeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object FontPrefs {
    private val KEY_FONT_SIZE = stringPreferencesKey("font_size")

    fun fontSizeFlow(context: Context): Flow<FontSizeOption> =
        context.sessionDataStore.data.map { prefs ->
            when (prefs[KEY_FONT_SIZE]) {
                FontSizeOption.Small.name -> FontSizeOption.Small
                FontSizeOption.Medium.name -> FontSizeOption.Medium
                FontSizeOption.Large.name -> FontSizeOption.Large
                else -> FontSizeOption.Large
            }
        }

    suspend fun setFontSize(context: Context, option: FontSizeOption) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_FONT_SIZE] = option.name
        }
    }
}

