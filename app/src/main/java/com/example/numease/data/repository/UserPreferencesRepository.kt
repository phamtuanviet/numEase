package com.example.numease.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.numease.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Khởi tạo DataStore
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_SOUND_ENABLED = booleanPreferencesKey("is_sound_enabled")
        val FONT_SIZE_MULTIPLIER = floatPreferencesKey("font_size_multiplier")
        val CURRENT_CHILD_ID = stringPreferencesKey("current_child_id")
        val CURRENT_VIEW_MODE = stringPreferencesKey("current_view_mode")
    }

    // Đọc dữ liệu từ DataStore và map vào Data Class của bạn
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            UserPreferences(
                isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: false,
                isSoundEnabled = preferences[PreferencesKeys.IS_SOUND_ENABLED] ?: true,
                fontSizeMultiplier = preferences[PreferencesKeys.FONT_SIZE_MULTIPLIER] ?: 1.0f,
                currentChildId = preferences[PreferencesKeys.CURRENT_CHILD_ID],
                currentViewMode = preferences[PreferencesKeys.CURRENT_VIEW_MODE] ?: "PARENT"
            )
        }

    suspend fun updateViewMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.CURRENT_VIEW_MODE] = mode }
    }

    suspend fun updateCurrentChild(childId: String?) {
        context.dataStore.edit { prefs ->
            if (childId != null) prefs[PreferencesKeys.CURRENT_CHILD_ID] = childId
            else prefs.remove(PreferencesKeys.CURRENT_CHILD_ID)
        }
    }

    suspend fun updateTheme(isDarkMode: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_DARK_MODE] = isDarkMode }
    }

    suspend fun updateSound(isSoundEnabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_SOUND_ENABLED] = isSoundEnabled }
    }
}