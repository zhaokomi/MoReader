package com.mochen.reader.data.datastore

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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        // Theme
        val DARK_MODE = stringPreferencesKey("dark_mode") // "system", "light", "dark"
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")

        // Reading Settings
        val FONT_SIZE = intPreferencesKey("font_size")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val PARAGRAPH_SPACING = intPreferencesKey("paragraph_spacing")
        val PAGE_MARGIN = intPreferencesKey("page_margin")
        val PAGE_MODE = stringPreferencesKey("page_mode") // "simulation", "cover", "fade", "none", "vertical"
        val READER_THEME = stringPreferencesKey("reader_theme") // "white", "cream", "green", "gray", "black", "custom"
        val CUSTOM_THEME_COLOR = longPreferencesKey("custom_theme_color")

        // Brightness
        val BRIGHTNESS = floatPreferencesKey("brightness")
        val BRIGHTNESS_FOLLOW_SYSTEM = booleanPreferencesKey("brightness_follow_system")

        // Screen
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val SCREEN_ORIENTATION = stringPreferencesKey("screen_orientation") // "auto", "portrait", "landscape"

        // Night Mode
        val NIGHT_MODE_AUTO = booleanPreferencesKey("night_mode_auto")
        val NIGHT_MODE_START = intPreferencesKey("night_mode_start") // hour
        val NIGHT_MODE_END = intPreferencesKey("night_mode_end") // hour

        // Statistics
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")

        // Timer
        val SHUTDOWN_TIMER_MINUTES = intPreferencesKey("shutdown_timer_minutes")

        // App Lock
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_TYPE = stringPreferencesKey("app_lock_type") // "pin", "fingerprint"

        // Language
        val LANGUAGE = stringPreferencesKey("language") // "system", "zh", "en"

        // WiFi Transfer
        val WIFI_TRANSFER_PORT = intPreferencesKey("wifi_transfer_port")
        val WIFI_TRANSFER_ENABLED = booleanPreferencesKey("wifi_transfer_enabled")

        // Chapter Detection
        val CHAPTER_REGEX = stringPreferencesKey("chapter_regex")

        // Encoding
        val DEFAULT_ENCODING = stringPreferencesKey("default_encoding")
    }

    // Theme
    val darkMode: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.DARK_MODE] ?: "system" }

    val dynamicColor: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.DYNAMIC_COLOR] ?: true }

    suspend fun setDarkMode(mode: String) {
        context.dataStore.edit { it[Keys.DARK_MODE] = mode }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    // Reading Settings
    val fontSize: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.FONT_SIZE] ?: 16 }

    val fontFamily: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.FONT_FAMILY] ?: "system" }

    val lineSpacing: Flow<Float> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.LINE_SPACING] ?: 1.5f }

    val paragraphSpacing: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.PARAGRAPH_SPACING] ?: 16 }

    val pageMargin: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.PAGE_MARGIN] ?: 16 }

    val pageMode: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.PAGE_MODE] ?: "simulation" }

    val readerTheme: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.READER_THEME] ?: "white" }

    val customThemeColor: Flow<Long> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.CUSTOM_THEME_COLOR] ?: 0xFFFFFFFF }

    suspend fun setFontSize(size: Int) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = size.coerceIn(12, 36) }
    }

    suspend fun setFontFamily(family: String) {
        context.dataStore.edit { it[Keys.FONT_FAMILY] = family }
    }

    suspend fun setLineSpacing(spacing: Float) {
        context.dataStore.edit { it[Keys.LINE_SPACING] = spacing.coerceIn(1.0f, 3.0f) }
    }

    suspend fun setParagraphSpacing(spacing: Int) {
        context.dataStore.edit { it[Keys.PARAGRAPH_SPACING] = spacing.coerceIn(0, 48) }
    }

    suspend fun setPageMargin(margin: Int) {
        context.dataStore.edit { it[Keys.PAGE_MARGIN] = margin.coerceIn(0, 48) }
    }

    suspend fun setPageMode(mode: String) {
        context.dataStore.edit { it[Keys.PAGE_MODE] = mode }
    }

    suspend fun setReaderTheme(theme: String) {
        context.dataStore.edit { it[Keys.READER_THEME] = theme }
    }

    suspend fun setCustomThemeColor(color: Long) {
        context.dataStore.edit { it[Keys.CUSTOM_THEME_COLOR] = color }
    }

    // Brightness
    val brightness: Flow<Float> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.BRIGHTNESS] ?: 0.5f }

    val brightnessFollowSystem: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.BRIGHTNESS_FOLLOW_SYSTEM] ?: true }

    suspend fun setBrightness(value: Float) {
        context.dataStore.edit { it[Keys.BRIGHTNESS] = value.coerceIn(0f, 1f) }
    }

    suspend fun setBrightnessFollowSystem(follow: Boolean) {
        context.dataStore.edit { it[Keys.BRIGHTNESS_FOLLOW_SYSTEM] = follow }
    }

    // Screen
    val keepScreenOn: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.KEEP_SCREEN_ON] ?: true }

    val screenOrientation: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.SCREEN_ORIENTATION] ?: "auto" }

    suspend fun setKeepScreenOn(keepOn: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = keepOn }
    }

    suspend fun setScreenOrientation(orientation: String) {
        context.dataStore.edit { it[Keys.SCREEN_ORIENTATION] = orientation }
    }

    // Night Mode
    val nightModeAuto: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.NIGHT_MODE_AUTO] ?: false }

    val nightModeStart: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.NIGHT_MODE_START] ?: 22 }

    val nightModeEnd: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.NIGHT_MODE_END] ?: 6 }

    suspend fun setNightModeAuto(auto: Boolean) {
        context.dataStore.edit { it[Keys.NIGHT_MODE_AUTO] = auto }
    }

    suspend fun setNightModeTime(start: Int, end: Int) {
        context.dataStore.edit {
            it[Keys.NIGHT_MODE_START] = start
            it[Keys.NIGHT_MODE_END] = end
        }
    }

    // Statistics
    val dailyGoalMinutes: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.DAILY_GOAL_MINUTES] ?: 30 }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_MINUTES] = minutes.coerceIn(0, 480) }
    }

    // Timer
    val shutdownTimerMinutes: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.SHUTDOWN_TIMER_MINUTES] ?: 0 }

    suspend fun setShutdownTimerMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.SHUTDOWN_TIMER_MINUTES] = minutes }
    }

    // App Lock
    val appLockEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.APP_LOCK_ENABLED] ?: false }

    val appLockType: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.APP_LOCK_TYPE] ?: "pin" }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setAppLockType(type: String) {
        context.dataStore.edit { it[Keys.APP_LOCK_TYPE] = type }
    }

    // Language
    val language: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.LANGUAGE] ?: "system" }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    // WiFi Transfer
    val wifiTransferPort: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.WIFI_TRANSFER_PORT] ?: 8080 }

    val wifiTransferEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.WIFI_TRANSFER_ENABLED] ?: false }

    suspend fun setWifiTransferPort(port: Int) {
        context.dataStore.edit { it[Keys.WIFI_TRANSFER_PORT] = port }
    }

    suspend fun setWifiTransferEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIFI_TRANSFER_ENABLED] = enabled }
    }

    // Chapter Detection
    val chapterRegex: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.CHAPTER_REGEX] ?: "^(第[一二三四五六七八九十百千万\\d]+[章节部卷集])|^(Chapter|CH|第)\\s*\\d+" }

    suspend fun setChapterRegex(regex: String) {
        context.dataStore.edit { it[Keys.CHAPTER_REGEX] = regex }
    }

    // Encoding
    val defaultEncoding: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.DEFAULT_ENCODING] ?: "auto" }

    suspend fun setDefaultEncoding(encoding: String) {
        context.dataStore.edit { it[Keys.DEFAULT_ENCODING] = encoding }
    }
}
