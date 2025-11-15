package com.example.aistudioapp.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aistudioapp.data.model.AppSettings
import com.example.aistudioapp.data.model.ProviderConfig
import com.example.aistudioapp.data.model.ProviderType
import com.example.aistudioapp.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collectLatest { settings ->
                val provider = settings.activeProvider
                val config = settings.providerConfigs[provider] ?: ProviderConfig(providerType = provider)
                _uiState.value = SettingsUiState(
                    appSettings = settings,
                    selectedProvider = provider,
                    selectedConfig = config
                )
            }
        }
    }

    fun selectProvider(providerType: ProviderType) {
        val config = _uiState.value.appSettings.providerConfigs[providerType]
            ?: ProviderConfig(providerType = providerType)
        _uiState.update {
            it.copy(
                selectedProvider = providerType,
                selectedConfig = config
            )
        }
    }

    fun updateApiKey(value: String) = updateProviderConfig { it.copy(apiKey = value) }
    fun updateBaseUrl(value: String) = updateProviderConfig { it.copy(baseUrl = value.ifBlank { null }) }
    fun updateModel(value: String) = updateProviderConfig { it.copy(modelName = value) }
    fun updateTemperature(value: Float) = updateProviderConfig { it.copy(temperature = value) }
    fun updateMaxTokens(value: Int) = updateProviderConfig { it.copy(maxOutputTokens = value) }
    fun toggleCensoring(enabled: Boolean) = updateProviderConfig { it.copy(enableCensoring = enabled) }

    fun toggleImages(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { settings ->
                settings.copy(allowImages = enabled)
            }
        }
    }

    fun toggleDocuments(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { settings ->
                settings.copy(allowDocuments = enabled)
            }
        }
    }

    fun updateSafetyFilters(
        blockHate: Boolean? = null,
        blockViolence: Boolean? = null,
        blockSelfHarm: Boolean? = null,
        blockSexual: Boolean? = null,
        customTerms: List<String>? = null
    ) {
        updateProviderConfig { config ->
            val filters = config.safetyFilters.copy(
                blockHate = blockHate ?: config.safetyFilters.blockHate,
                blockViolence = blockViolence ?: config.safetyFilters.blockViolence,
                blockSelfHarm = blockSelfHarm ?: config.safetyFilters.blockSelfHarm,
                blockSexual = blockSexual ?: config.safetyFilters.blockSexual,
                customBannedTerms = customTerms ?: config.safetyFilters.customBannedTerms
            )
            config.copy(safetyFilters = filters)
        }
    }

    fun setDriveFolder(uri: Uri) {
        viewModelScope.launch {
            repository.updateSettings { settings ->
                settings.copy(
                    driveBackupState = settings.driveBackupState.copy(
                        folderTreeUri = uri.toString()
                    )
                )
            }
            _events.emit(SettingsEvent.ShowMessage("Drive folder saved"))
        }
    }

    fun setAutoCondense(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { settings ->
                settings.copy(enableCondensePrompt = enabled)
            }
        }
    }

    fun setCondenseThreshold(value: Int) {
        viewModelScope.launch {
            repository.updateSettings { settings ->
                settings.copy(autoCondenseThreshold = value)
            }
        }
    }

    fun backupNow(contentResolver: ContentResolver, treeUri: Uri) {
        viewModelScope.launch {
            repository.backupLatestTranscript(contentResolver, treeUri)
                .onSuccess {
                    _events.emit(SettingsEvent.ShowMessage("Backup saved to Drive"))
                }
                .onFailure {
                    _events.emit(SettingsEvent.ShowMessage(it.message ?: "Backup failed"))
                }
        }
    }

    fun cacheOffline() {
        viewModelScope.launch {
            repository.cacheLatestTranscript()
                .onSuccess {
                    _events.emit(SettingsEvent.ShowMessage("Transcript cached locally"))
                }
                .onFailure {
                    _events.emit(SettingsEvent.ShowMessage(it.message ?: "Unable to cache chat"))
                }
        }
    }

    private fun updateProviderConfig(transform: (ProviderConfig) -> ProviderConfig) {
        val provider = _uiState.value.selectedProvider
        val currentConfig = _uiState.value.selectedConfig
        val updatedConfig = transform(currentConfig)
        _uiState.update { it.copy(selectedConfig = updatedConfig) }
        viewModelScope.launch {
            repository.updateSettings { settings ->
                val updatedMap = settings.providerConfigs.toMutableMap().apply {
                    put(provider, updatedConfig)
                }
                settings.copy(
                    providerConfigs = updatedMap,
                    activeProvider = provider
                )
            }
        }
    }

    data class SettingsUiState(
        val appSettings: AppSettings = AppSettings(),
        val selectedProvider: ProviderType = AppSettings().activeProvider,
        val selectedConfig: ProviderConfig = ProviderConfig()
    )

    sealed class SettingsEvent {
        data class ShowMessage(val text: String) : SettingsEvent()
    }

    class Factory(private val repository: ChatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}
