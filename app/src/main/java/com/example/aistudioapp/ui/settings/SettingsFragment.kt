package com.example.aistudioapp.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.aistudioapp.R
import com.example.aistudioapp.data.model.ProviderType
import com.example.aistudioapp.databinding.FragmentSettingsBinding
import com.example.aistudioapp.di.ServiceLocator
import com.example.aistudioapp.ui.common.SimpleTextWatcher
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(ServiceLocator.provideChatRepository(requireContext()))
    }

    private val selectDriveTree = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.setDriveFolder(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProviderDropdown()
        setupListeners()
        observeState()
        observeEvents()
    }

    private fun setupProviderDropdown() {
        val providerNames = ProviderType.values().map { getString(it.displayNameRes) }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, providerNames)
        binding.providerDropdown.setAdapter(adapter)
        binding.providerDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectProvider(ProviderType.values()[position])
        }
    }

    private fun setupListeners() = with(binding) {
        apiKeyInput.addTextChangedListener(SimpleTextWatcher { viewModel.updateApiKey(it) })
        baseUrlInput.addTextChangedListener(SimpleTextWatcher { viewModel.updateBaseUrl(it) })
        modelInput.addTextChangedListener(SimpleTextWatcher { viewModel.updateModel(it) })
        temperatureSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.updateTemperature(value)
        }
        maxTokensSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.updateMaxTokens(value.toInt())
        }
        censoringSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) viewModel.toggleCensoring(isChecked)
        }
        imagesSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) viewModel.toggleImages(isChecked)
        }
        documentsSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) viewModel.toggleDocuments(isChecked)
        }
        filterHateSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) viewModel.updateSafetyFilters(blockHate = isChecked)
        }
        filterViolenceSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) viewModel.updateSafetyFilters(blockViolence = isChecked)
        }
        filterSelfHarmSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) viewModel.updateSafetyFilters(blockSelfHarm = isChecked)
        }
        filterSexualSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) viewModel.updateSafetyFilters(blockSexual = isChecked)
        }
        customTermsInput.addTextChangedListener(
            SimpleTextWatcher { text ->
                val terms = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                viewModel.updateSafetyFilters(customTerms = terms)
            }
        )
        autoCondenseSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) viewModel.setAutoCondense(isChecked)
        }
        condenseThresholdSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setCondenseThreshold(value.toInt())
        }
        selectDriveButton.setOnClickListener { selectDriveTree.launch(null) }
        manualBackupButton.setOnClickListener {
            val uriString = viewModel.uiState.value.appSettings.driveBackupState.folderTreeUri
            if (uriString == null) {
                Snackbar.make(root, R.string.error_no_drive_folder, Snackbar.LENGTH_LONG).show()
            } else {
                viewModel.backupNow(requireContext().contentResolver, Uri.parse(uriString))
            }
        }
        cacheLocalButton.setOnClickListener {
            viewModel.cacheOffline()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val providerIndex = ProviderType.values().indexOf(state.selectedProvider)
                    if (providerIndex >= 0) {
                        val providerLabel = getString(state.selectedProvider.displayNameRes)
                        if (binding.providerDropdown.text.toString() != providerLabel) {
                            binding.providerDropdown.setText(providerLabel, false)
                        }
                    }

                    binding.apiKeyInput.setTextIfDifferent(state.selectedConfig.apiKey)
                    binding.baseUrlInput.setTextIfDifferent(state.selectedConfig.baseUrl.orEmpty())
                    binding.modelInput.setTextIfDifferent(state.selectedConfig.modelName)
                    if (binding.temperatureSlider.value != state.selectedConfig.temperature) {
                        binding.temperatureSlider.value = state.selectedConfig.temperature
                    }
                    if (binding.maxTokensSlider.value != state.selectedConfig.maxOutputTokens.toFloat()) {
                        binding.maxTokensSlider.value = state.selectedConfig.maxOutputTokens.toFloat()
                    }
                    binding.censoringSwitch.isChecked = state.selectedConfig.enableCensoring
                    binding.imagesSwitch.isChecked = state.appSettings.allowImages
                    binding.documentsSwitch.isChecked = state.appSettings.allowDocuments
                    binding.filterHateSwitch.isChecked = state.selectedConfig.safetyFilters.blockHate
                    binding.filterViolenceSwitch.isChecked = state.selectedConfig.safetyFilters.blockViolence
                    binding.filterSelfHarmSwitch.isChecked = state.selectedConfig.safetyFilters.blockSelfHarm
                    binding.filterSexualSwitch.isChecked = state.selectedConfig.safetyFilters.blockSexual
                    binding.customTermsInput.setTextIfDifferent(
                        state.selectedConfig.safetyFilters.customBannedTerms.joinToString(", ")
                    )
                    binding.autoCondenseSwitch.isChecked = state.appSettings.enableCondensePrompt
                    if (binding.condenseThresholdSlider.value != state.appSettings.autoCondenseThreshold.toFloat()) {
                        binding.condenseThresholdSlider.value = state.appSettings.autoCondenseThreshold.toFloat()
                    }

                    val backupState = state.appSettings.driveBackupState
                    binding.backupStatusLabel.text = if (backupState.lastBackupAt == 0L) {
                        getString(R.string.label_backup_never)
                    } else {
                        val formatted = DateFormat.getDateTimeInstance().format(Date(backupState.lastBackupAt))
                        getString(R.string.label_backup_last_run, formatted)
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is SettingsViewModel.SettingsEvent.ShowMessage -> {
                            Snackbar.make(binding.root, event.text, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun View.setTextIfDifferent(value: String) {
        if (this is com.google.android.material.textfield.TextInputEditText) {
            if (text?.toString() != value) {
                setText(value)
                setSelection(value.length)
            }
        }
    }
}
