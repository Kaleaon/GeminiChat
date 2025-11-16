package com.example.aistudioapp.ui.creator

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewAssetLoader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import com.example.aistudioapp.data.model.AvatarSelection
import com.example.aistudioapp.data.model.AvatarType
import com.example.aistudioapp.databinding.FragmentMakehumanBinding
import com.example.aistudioapp.di.ServiceLocator
import com.example.aistudioapp.ui.creator.adapter.DesignerSliderAdapter
import com.example.aistudioapp.ui.creator.adapter.ModelLibraryAdapter
import com.example.aistudioapp.ui.creator.data.DesignerSliderRepository
import com.example.aistudioapp.ui.creator.data.ModelLibraryRepository
import com.example.aistudioapp.ui.creator.model.DesignerSliderSpec
import com.example.aistudioapp.ui.creator.model.LocalModelEntry
import com.example.aistudioapp.ui.creator.model.SkeletonType
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MakeHumanFragment : Fragment() {

    private var _binding: FragmentMakehumanBinding? = null
    private val binding get() = _binding!!

    private val makeHumanAssetUrl = "file:///android_asset/makehuman/index.html"
    private val characterStudioAssetUrl = "file:///android_asset/characterstudio/index.html"
    private val makeHumanDocs = "https://github.com/makehuman-js/makehuman-js"
    private val characterStudioDocs = "https://github.com/M3-org/CharacterStudio"

    private lateinit var assetLoader: WebViewAssetLoader
    private val sliderRepository by lazy { DesignerSliderRepository(requireContext().applicationContext) }
    private val modelRepository by lazy { ModelLibraryRepository(requireContext().applicationContext) }
    private val modelAdapter by lazy { ModelLibraryAdapter { entry -> loadModelIntoCharacterStudio(entry) } }
    private val avatarStore by lazy { ServiceLocator.provideAvatarStore(requireContext()) }
    private val avatarBridge by lazy { AvatarJavascriptBridge() }
    private val sliderAdapter by lazy {
        DesignerSliderAdapter { spec, value ->
            handleSliderValue(spec, value)
        }
    }
    private val modelPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNullOrEmpty()) return@registerForActivityResult
            viewLifecycleOwner.lifecycleScope.launch {
                val count = modelRepository.addModels(requireContext().contentResolver, uris)
                if (count > 0 && isAdded) {
                    Snackbar.make(
                        binding.root,
                        resources.getQuantityString(
                            com.example.aistudioapp.R.plurals.models_uploaded_toast,
                            count,
                            count
                        ),
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else if (isAdded) {
                    Snackbar.make(
                        binding.root,
                        com.example.aistudioapp.R.string.model_upload_failed,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private var currentMode = DesignerMode.MAKEHUMAN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val modelsDir = File(requireContext().filesDir, "models")
        modelsDir.mkdirs()
        assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(requireContext()))
            .addPathHandler("/models/", WebViewAssetLoader.InternalStoragePathHandler(requireContext(), modelsDir))
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMakehumanBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureMakeHumanWebView()
        configureCharacterStudioWebView()
        binding.sliderRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MakeHumanFragment.sliderAdapter
        }
        binding.modelRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = modelAdapter
        }
        binding.designerToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val targetMode = if (checkedId == binding.tabMakeHuman.id) {
                DesignerMode.MAKEHUMAN
            } else {
                DesignerMode.CHARACTER_STUDIO
            }
            switchMode(targetMode)
        }

        binding.openDocsButton.setOnClickListener {
            val target = if (currentMode == DesignerMode.MAKEHUMAN) {
                makeHumanDocs
            } else {
                characterStudioDocs
            }
            openExternal(Uri.parse(target))
        }
        binding.setAvatarButton.setOnClickListener {
            requestAvatarExport()
        }
        binding.uploadModelsButton.setOnClickListener {
            modelPickerLauncher.launch(
                arrayOf(
                    "model/*",
                    "application/octet-stream",
                    "application/x-blender",
                    "text/plain"
                )
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                modelRepository.models.collect { updateModelSection(it) }
            }
        }

        switchMode(DesignerMode.MAKEHUMAN)
    }

    private fun handleSliderValue(spec: DesignerSliderSpec, value: Float) {
        val script = spec.targetScript.replace("__VALUE__", value.toString())
        val targetWebView = if (currentMode == DesignerMode.MAKEHUMAN) {
            binding.makeHumanWebView
        } else {
            binding.characterStudioWebView
        }
        targetWebView.evaluateJavascript(script, null)
    }

    private fun requestAvatarExport() {
        binding.designerProgress.isVisible = true
        val requestScript = "window.requestGeminiAvatar && window.requestGeminiAvatar();"
        val webView = if (currentMode == DesignerMode.MAKEHUMAN) {
            binding.makeHumanWebView
        } else {
            binding.characterStudioWebView
        }
        webView.evaluateJavascript(requestScript, null)
    }

    private fun updateModelSection(entries: List<LocalModelEntry>) {
        modelAdapter.submitList(entries)
        val hasItems = entries.isNotEmpty()
        binding.modelsSectionTitle.isVisible = true
        binding.modelsSectionTitle.text = if (hasItems) {
            getString(com.example.aistudioapp.R.string.models_section_title)
        } else {
            getString(com.example.aistudioapp.R.string.model_empty_state)
        }
        binding.modelRecycler.isVisible = hasItems
    }

    private fun loadModelIntoCharacterStudio(entry: LocalModelEntry) {
        binding.designerToggle.check(binding.tabCharacterStudio.id)
        binding.characterStudioWebView.evaluateJavascript(
            "window.loadExternalModel('${entry.webPath}');",
            null
        )
    }

    private fun configureMakeHumanWebView() {
        binding.makeHumanWebView.apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.designerProgress.isVisible =
                        currentMode == DesignerMode.MAKEHUMAN && newProgress in 0..95
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val target = request?.url ?: return false
                    return if (target.scheme?.startsWith("http") == true) {
                        openExternal(target)
                        true
                    } else {
                        false
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    if (currentMode == DesignerMode.MAKEHUMAN) {
                        binding.designerProgress.isVisible = false
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    binding.designerProgress.isVisible = false
                    Snackbar.make(
                        binding.root,
                        error?.description ?: "Unable to load designer",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    request ?: return null
                    return assetLoader.shouldInterceptRequest(request.url)
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    url: String?
                ): WebResourceResponse? {
                    url ?: return null
                    return assetLoader.shouldInterceptRequest(Uri.parse(url))
                }
            }

            addJavascriptInterface(avatarBridge, "GeminiAvatarBridge")
            loadUrl(makeHumanAssetUrl)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureCharacterStudioWebView() {
        binding.characterStudioWebView.apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.designerProgress.isVisible =
                        currentMode == DesignerMode.CHARACTER_STUDIO && newProgress in 0..95
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val target = request?.url ?: return false
                    return if (target.scheme?.startsWith("http") == true) {
                        openExternal(target)
                        true
                    } else {
                        false
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    if (currentMode == DesignerMode.CHARACTER_STUDIO) {
                        binding.designerProgress.isVisible = false
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    binding.designerProgress.isVisible = false
                    Snackbar.make(
                        binding.root,
                        error?.description ?: "Unable to load Character Studio",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    request ?: return null
                    return assetLoader.shouldInterceptRequest(request.url)
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    url: String?
                ): WebResourceResponse? {
                    url ?: return null
                    return assetLoader.shouldInterceptRequest(Uri.parse(url))
                }
            }
            addJavascriptInterface(avatarBridge, "GeminiAvatarBridge")
        }
    }

    private fun switchMode(mode: DesignerMode) {
        currentMode = mode
        binding.makeHumanWebView.isVisible = mode == DesignerMode.MAKEHUMAN
        binding.characterStudioWebView.isVisible = mode == DesignerMode.CHARACTER_STUDIO
        binding.designerProgress.isVisible = true
        val sliders = sliderRepository.getSliders(mode)
        sliderAdapter.submitList(sliders)
        binding.sliderSectionTitle.isVisible = sliders.isNotEmpty()
        binding.sliderRecycler.isVisible = sliders.isNotEmpty()
        if (sliders.isEmpty()) {
            binding.sliderSectionTitle.text = ""
        } else {
            binding.sliderSectionTitle.text =
                getString(com.example.aistudioapp.R.string.designer_slider_title)
        }
        val docsText = if (mode == DesignerMode.MAKEHUMAN) {
            com.example.aistudioapp.R.string.makehuman_docs
        } else {
            com.example.aistudioapp.R.string.characterstudio_docs
        }
        binding.openDocsButton.setText(docsText)

        if (mode == DesignerMode.MAKEHUMAN) {
            if (binding.makeHumanWebView.url == null) {
                binding.makeHumanWebView.loadUrl(makeHumanAssetUrl)
            } else {
                binding.designerProgress.isVisible = false
            }
        } else {
            if (binding.characterStudioWebView.url == null) {
                binding.characterStudioWebView.loadUrl(characterStudioAssetUrl)
            } else {
                binding.designerProgress.isVisible = false
            }
        }
    }

    private fun openExternal(uri: Uri) {
        runCatching {
            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
        }.onFailure {
            Snackbar.make(binding.root, "No browser available to view docs.", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroyView() {
        binding.makeHumanWebView.apply {
            stopLoading()
            webChromeClient = null
            destroy()
        }
        binding.characterStudioWebView.apply {
            stopLoading()
            webChromeClient = null
            destroy()
        }
        _binding = null
        super.onDestroyView()
    }

    private fun DesignerMode.toAvatarType(): AvatarType {
        return when (this) {
            DesignerMode.MAKEHUMAN -> AvatarType.MAKEHUMAN
            DesignerMode.CHARACTER_STUDIO -> AvatarType.CHARACTER_STUDIO
        }
    }

    private fun defaultLabel(type: AvatarType): String {
        return when (type) {
            AvatarType.MAKEHUMAN -> "MakeHuman Avatar"
            AvatarType.CHARACTER_STUDIO -> "Character Studio Avatar"
        }
    }

    private inner class AvatarJavascriptBridge {
        @JavascriptInterface
        fun onAvatarExported(label: String?, payload: String?, type: String?) {
            val json = payload ?: return
            val resolvedType = runCatching { AvatarType.valueOf(type ?: currentMode.toAvatarType().name) }
                .getOrDefault(currentMode.toAvatarType())
            val name = label?.takeIf { it.isNotBlank() } ?: defaultLabel(resolvedType)
            val selection = AvatarSelection(
                type = resolvedType,
                label = name,
                payload = json
            )
            viewLifecycleOwner.lifecycleScope.launch {
                avatarStore.update(selection)
                if (isAdded) {
                    binding.designerProgress.isVisible = false
                    Snackbar.make(
                        binding.root,
                        getString(com.example.aistudioapp.R.string.avatar_saved_toast),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
