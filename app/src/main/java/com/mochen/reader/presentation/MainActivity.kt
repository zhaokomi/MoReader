package com.mochen.reader.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.mochen.reader.data.datastore.SettingsDataStore
import com.mochen.reader.presentation.navigation.MoReaderNavHost
import com.mochen.reader.presentation.theme.MoReaderTheme
import com.mochen.reader.presentation.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Observe dark mode setting from DataStore
            val darkModeSetting by settingsDataStore.darkMode.collectAsState(initial = "system")
            val dynamicColor by settingsDataStore.dynamicColor.collectAsState(initial = true)

            val themeMode = when (darkModeSetting) {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }

            MoReaderTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MoReaderNavHost()
                }
            }
        }

        // Observe language setting and apply locale changes
        lifecycleScope.launch {
            settingsDataStore.language.collect { language ->
                applyLanguage(language)
            }
        }
    }

    private fun applyLanguage(language: String) {
        val locale = when (language) {
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "en" -> Locale.ENGLISH
            else -> Locale.getDefault()
        }

        val currentLocale = resources.configuration.locales.get(0)
        if (currentLocale != locale) {
            val config = resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
            recreate()
        }
    }
}
