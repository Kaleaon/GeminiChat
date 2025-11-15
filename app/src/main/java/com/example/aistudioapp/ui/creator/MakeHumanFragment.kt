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
import com.google.android.material.snackbar.Snackbar

class MakeHumanFragment : Fragment() {

    private var _binding: FragmentMakehumanBinding? = null
    private val binding get() = _binding!!

    private val assetUrl = "file:///android_asset/makehuman/index.html"
    private val docsUrl = "https://github.com/makehuman-js/makehuman-js"

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

        binding.makeHumanProgress.isVisible = true
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
                    binding.makeHumanProgress.isVisible = newProgress in 0..95
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
                    binding.makeHumanProgress.isVisible = false
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    binding.makeHumanProgress.isVisible = false
                    Snackbar.make(
                        binding.root,
                        error?.description ?: "Unable to load designer",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            loadUrl(assetUrl)
        }

        binding.openDocsButton.setOnClickListener {
            openExternal(Uri.parse(docsUrl))
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
        _binding = null
        super.onDestroyView()
    }
}
