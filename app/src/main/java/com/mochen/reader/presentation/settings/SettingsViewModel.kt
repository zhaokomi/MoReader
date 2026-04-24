package com.mochen.reader.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochen.reader.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val darkMode: String = "system",
    val dynamicColor: Boolean = true,
    val language: String = "system",
    val appLockEnabled: Boolean = false,
    val appLockType: String = "pin",
    val wifiTransferEnabled: Boolean = false,
    val wifiTransferPort: Int = 8080,
    val cacheSize: String = "0 MB"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.darkMode.collect { value ->
                _uiState.update { it.copy(darkMode = value) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.dynamicColor.collect { value ->
                _uiState.update { it.copy(dynamicColor = value) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.language.collect { value ->
                _uiState.update { it.copy(language = value) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.appLockEnabled.collect { value ->
                _uiState.update { it.copy(appLockEnabled = value) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.appLockType.collect { value ->
                _uiState.update { it.copy(appLockType = value) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.wifiTransferEnabled.collect { value ->
                _uiState.update { it.copy(wifiTransferEnabled = value) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.wifiTransferPort.collect { value ->
                _uiState.update { it.copy(wifiTransferPort = value) }
            }
        }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch {
            settingsDataStore.setDarkMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDynamicColor(enabled)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsDataStore.setLanguage(language)
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAppLockEnabled(enabled)
        }
    }

    fun setAppLockType(type: String) {
        viewModelScope.launch {
            settingsDataStore.setAppLockType(type)
        }
    }

    fun setWifiTransferEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setWifiTransferEnabled(enabled)
        }
    }

    fun setWifiTransferPort(port: Int) {
        viewModelScope.launch {
            settingsDataStore.setWifiTransferPort(port)
        }
    }
}
