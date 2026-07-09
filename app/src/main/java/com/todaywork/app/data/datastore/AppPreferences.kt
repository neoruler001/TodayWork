package com.todaywork.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "todaywork_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // 키 정의
    object Keys {
        val ACTIVE_PATTERN_ID = longPreferencesKey("active_pattern_id")
        val LAST_VIEWED_YEAR = intPreferencesKey("last_viewed_year")
        val LAST_VIEWED_MONTH = intPreferencesKey("last_viewed_month")
        val SHOW_LUNAR = booleanPreferencesKey("show_lunar")
        val SHOW_HOLIDAY = booleanPreferencesKey("show_holiday")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val CALENDAR_START_MONDAY = booleanPreferencesKey("calendar_start_monday")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
    }

    val showLunar: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[Keys.SHOW_LUNAR] ?: false }

    val showHoliday: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[Keys.SHOW_HOLIDAY] ?: true }

    val darkMode: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[Keys.DARK_MODE] ?: false }

    val calendarStartMonday: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[Keys.CALENDAR_START_MONDAY] ?: false }

    val notificationEnabled: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[Keys.NOTIFICATION_ENABLED] ?: true }

    val activePatternId: Flow<Long> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[Keys.ACTIVE_PATTERN_ID] ?: -1L }

    suspend fun setShowLunar(value: Boolean) {
        dataStore.edit { it[Keys.SHOW_LUNAR] = value }
    }

    suspend fun setShowHoliday(value: Boolean) {
        dataStore.edit { it[Keys.SHOW_HOLIDAY] = value }
    }

    suspend fun setDarkMode(value: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = value }
    }

    suspend fun setCalendarStartMonday(value: Boolean) {
        dataStore.edit { it[Keys.CALENDAR_START_MONDAY] = value }
    }

    suspend fun setNotificationEnabled(value: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATION_ENABLED] = value }
    }

    suspend fun setActivePatternId(id: Long) {
        dataStore.edit { it[Keys.ACTIVE_PATTERN_ID] = id }
    }
}
