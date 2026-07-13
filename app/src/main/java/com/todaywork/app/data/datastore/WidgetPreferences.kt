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

val Context.widgetDataStore by preferencesDataStore(name = "widget_prefs")

data class WidgetSettings(
    val bgAlpha: Float = 1.0f,
    val bgColor: String = "white",
    val fontSizeLarge: Boolean = false,
    val showDivider: Boolean = true,
    val memoSize: Int = 2,
    val dateSize: Int = 2,
    val workSize: Int = 2,
    val memoWrite: Boolean = false,
    val showLunar: Boolean = false,
    val displayYear: Int = LocalDate.now().year,
    val displayMonth: Int = LocalDate.now().monthValue,
)

object WidgetPrefKeys {
    val BG_ALPHA        = floatPreferencesKey("w_bg_alpha")
    val BG_COLOR        = stringPreferencesKey("w_bg_color")
    val FONT_SIZE_LARGE = booleanPreferencesKey("w_font_large")
    val SHOW_DIVIDER    = booleanPreferencesKey("w_show_divider")
    val MEMO_SIZE       = intPreferencesKey("w_memo_size")
    val DATE_SIZE       = intPreferencesKey("w_date_size")
    val WORK_SIZE       = intPreferencesKey("w_work_size")
    val MEMO_WRITE      = booleanPreferencesKey("w_memo_write")
    val SHOW_LUNAR      = booleanPreferencesKey("w_lunar")
    val DISPLAY_YEAR    = intPreferencesKey("w_year")
    val DISPLAY_MONTH   = intPreferencesKey("w_month")
}

fun Preferences.toWidgetSettings() = WidgetSettings(
    bgAlpha       = this[WidgetPrefKeys.BG_ALPHA]        ?: 1.0f,
    bgColor       = this[WidgetPrefKeys.BG_COLOR]        ?: "white",
    fontSizeLarge = this[WidgetPrefKeys.FONT_SIZE_LARGE] ?: false,
    showDivider   = this[WidgetPrefKeys.SHOW_DIVIDER]    ?: true,
    memoSize      = this[WidgetPrefKeys.MEMO_SIZE]       ?: 2,
    dateSize      = this[WidgetPrefKeys.DATE_SIZE]       ?: 2,
    workSize      = this[WidgetPrefKeys.WORK_SIZE]       ?: 2,
    memoWrite     = this[WidgetPrefKeys.MEMO_WRITE]      ?: false,
    showLunar     = this[WidgetPrefKeys.SHOW_LUNAR]      ?: false,
    displayYear   = this[WidgetPrefKeys.DISPLAY_YEAR]    ?: LocalDate.now().year,
    displayMonth  = this[WidgetPrefKeys.DISPLAY_MONTH]   ?: LocalDate.now().monthValue,
)

suspend fun Preferences.MutationScope.applyWidgetSettings(s: WidgetSettings) {
    this[WidgetPrefKeys.BG_ALPHA]        = s.bgAlpha
    this[WidgetPrefKeys.BG_COLOR]        = s.bgColor
    this[WidgetPrefKeys.FONT_SIZE_LARGE] = s.fontSizeLarge
    this[WidgetPrefKeys.SHOW_DIVIDER]    = s.showDivider
    this[WidgetPrefKeys.MEMO_SIZE]       = s.memoSize
    this[WidgetPrefKeys.DATE_SIZE]       = s.dateSize
    this[WidgetPrefKeys.WORK_SIZE]       = s.workSize
    this[WidgetPrefKeys.MEMO_WRITE]      = s.memoWrite
    this[WidgetPrefKeys.SHOW_LUNAR]      = s.showLunar
    this[WidgetPrefKeys.DISPLAY_YEAR]    = s.displayYear
    this[WidgetPrefKeys.DISPLAY_MONTH]   = s.displayMonth
}

// Hilt 불필요한 곳(ActionCallback 등)에서 직접 접근
suspend fun Context.getWidgetSettings(): WidgetSettings =
    widgetDataStore.data.first().toWidgetSettings()

suspend fun Context.saveWidgetSettings(settings: WidgetSettings) {
    widgetDataStore.edit { it.applyWidgetSettings(settings) }
}

suspend fun Context.shiftWidgetMonth(delta: Int) {
    widgetDataStore.edit { p ->
        val year  = p[WidgetPrefKeys.DISPLAY_YEAR]  ?: LocalDate.now().year
        val month = p[WidgetPrefKeys.DISPLAY_MONTH] ?: LocalDate.now().monthValue
        val next  = LocalDate.of(year, month, 1).plusMonths(delta.toLong())
        p[WidgetPrefKeys.DISPLAY_YEAR]  = next.year
        p[WidgetPrefKeys.DISPLAY_MONTH] = next.monthValue
    }
}

// ── Hilt 주입용 싱글톤 (WidgetSettingsActivity에서 사용) ─────────
@Singleton
class WidgetPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val settingsFlow: Flow<WidgetSettings> = context.widgetDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toWidgetSettings() }

    suspend fun getSettings(): WidgetSettings = context.getWidgetSettings()
    suspend fun save(settings: WidgetSettings) = context.saveWidgetSettings(settings)
}
