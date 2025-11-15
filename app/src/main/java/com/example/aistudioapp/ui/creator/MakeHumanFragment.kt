package com.example.aistudioapp.ui.creator

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.aistudioapp.databinding.FragmentMakehumanBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aistudioapp.ui.creator.adapter.DesignerSliderAdapter
import com.example.aistudioapp.ui.creator.data.DesignerSliderRepository
import com.example.aistudioapp.ui.creator.model.DesignerSliderSpec
import com.google.android.material.snackbar.Snackbar

class MakeHumanFragment : Fragment() {

    private var _binding: FragmentMakehumanBinding? = null
    private val binding get() = _binding!!

    private val makeHumanAssetUrl = "file:///android_asset/makehuman/index.html"
    private val characterStudioAssetUrl = "file:///android_asset/characterstudio/index.html"
    private val makeHumanDocs = "https://github.com/makehuman-js/makehuman-js"
    private val characterStudioDocs = "https://github.com/M3-org/CharacterStudio"

    private val sliderRepository by lazy { DesignerSliderRepository(requireContext().applicationContext) }
    private val sliderAdapter by lazy {
        DesignerSliderAdapter { spec, value ->
            handleSliderValue(spec, value)
        }
    }

    private var currentMode = DesignerMode.MAKEHUMAN

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
            }

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
            }
        }
    }

    private fun switchMode(mode: DesignerMode) {
        currentMode = mode
        binding.makeHumanWebView.isVisible = mode == DesignerMode.MAKEHUMAN
        binding.characterStudioWebView.isVisible = mode == DesignerMode.CHARACTER_STUDIO
        binding.designerProgress.isVisible = true
        val sliders = sliderRepository.getSliders(mode)
        sliderAdapter.submitList(sliders)
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

}
