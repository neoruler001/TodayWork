package com.todaywork.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.widgetDataStore by preferencesDataStore(name = "widget_prefs")

data class WidgetSettings(
    val bgAlpha: Float = 1.0f,
    val bgColor: String = "white",
    val fontSizeLarge: Boolean = false,
    val showDivider: Boolean = true,
    val memoSize: Int = 2,
    val dateSize: Int = 2,
    val workSize: Int = 2,
    val fix6Weeks: Boolean = false,
    val memoWrite: Boolean = false,
    val showOvertime: Boolean = false,
    val showLunar: Boolean = false,
    val highlightChanged: Boolean = false,
    val displayYear: Int = LocalDate.now().year,
    val displayMonth: Int = LocalDate.now().monthValue,
)

@Singleton
class WidgetPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.widgetDataStore

    object Keys {
        val BG_ALPHA = floatPreferencesKey("w_bg_alpha")
        val BG_COLOR = stringPreferencesKey("w_bg_color")
        val FONT_SIZE_LARGE = booleanPreferencesKey("w_font_large")
        val SHOW_DIVIDER = booleanPreferencesKey("w_show_divider")
        val MEMO_SIZE = intPreferencesKey("w_memo_size")
        val DATE_SIZE = intPreferencesKey("w_date_size")
        val WORK_SIZE = intPreferencesKey("w_work_size")
        val FIX_6_WEEKS = booleanPreferencesKey("w_fix6")
        val MEMO_WRITE = booleanPreferencesKey("w_memo_write")
        val SHOW_OVERTIME = booleanPreferencesKey("w_overtime")
        val SHOW_LUNAR = booleanPreferencesKey("w_lunar")
        val HIGHLIGHT_CHANGED = booleanPreferencesKey("w_highlight")
        val DISPLAY_YEAR = intPreferencesKey("w_year")
        val DISPLAY_MONTH = intPreferencesKey("w_month")
    }

    val settingsFlow: Flow<WidgetSettings> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toSettings() }

    suspend fun getSettings(): WidgetSettings = settingsFlow.first()

    suspend fun save(settings: WidgetSettings) {
        store.edit { p ->
            p[Keys.BG_ALPHA] = settings.bgAlpha
            p[Keys.BG_COLOR] = settings.bgColor
            p[Keys.FONT_SIZE_LARGE] = settings.fontSizeLarge
            p[Keys.SHOW_DIVIDER] = settings.showDivider
            p[Keys.MEMO_SIZE] = settings.memoSize
            p[Keys.DATE_SIZE] = settings.dateSize
            p[Keys.WORK_SIZE] = settings.workSize
            p[Keys.FIX_6_WEEKS] = settings.fix6Weeks
            p[Keys.MEMO_WRITE] = settings.memoWrite
            p[Keys.SHOW_OVERTIME] = settings.showOvertime
            p[Keys.SHOW_LUNAR] = settings.showLunar
            p[Keys.HIGHLIGHT_CHANGED] = settings.highlightChanged
            p[Keys.DISPLAY_YEAR] = settings.displayYear
            p[Keys.DISPLAY_MONTH] = settings.displayMonth
        }
    }

    suspend fun update(block: WidgetSettings.() -> WidgetSettings) {
        save(getSettings().block())
    }

    private fun Preferences.toSettings() = WidgetSettings(
        bgAlpha = this[Keys.BG_ALPHA] ?: 1.0f,
        bgColor = this[Keys.BG_COLOR] ?: "white",
        fontSizeLarge = this[Keys.FONT_SIZE_LARGE] ?: false,
        showDivider = this[Keys.SHOW_DIVIDER] ?: true,
        memoSize = this[Keys.MEMO_SIZE] ?: 2,
        dateSize = this[Keys.DATE_SIZE] ?: 2,
        workSize = this[Keys.WORK_SIZE] ?: 2,
        fix6Weeks = this[Keys.FIX_6_WEEKS] ?: false,
        memoWrite = this[Keys.MEMO_WRITE] ?: false,
        showOvertime = this[Keys.SHOW_OVERTIME] ?: false,
        showLunar = this[Keys.SHOW_LUNAR] ?: false,
        highlightChanged = this[Keys.HIGHLIGHT_CHANGED] ?: false,
        displayYear = this[Keys.DISPLAY_YEAR] ?: LocalDate.now().year,
        displayMonth = this[Keys.DISPLAY_MONTH] ?: LocalDate.now().monthValue,
    )
}
