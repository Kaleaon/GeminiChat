package com.example.aistudioapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.target.Target
import com.example.aistudioapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Main activity that displays the AI Studio promotional banner and CTA button.
 * This activity serves as the entry point for the application.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBanner()
        setupCtaButton()
    }

    /**
     * Sets up the banner image using Coil image loading library.
     * Includes error handling and placeholder configuration.
     */
    private fun setupBanner() {
        binding.bannerImage.load(BANNER_URL) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
            listener(
                onError = { _, result ->
                    Log.e(TAG, "Failed to load banner image", result.throwable)
                }
            )
        }
    }

    /**
     * Sets up the CTA button to open AI Studio in the browser.
     * Includes proper error handling for cases where no browser is available.
     */
    private fun setupCtaButton() {
        binding.startBuildingButton.setOnClickListener {
            openAiStudio()
        }
    }

    /**
     * Opens the AI Studio URL in the default browser.
     * Shows an error message if no browser application is available.
     */
    private fun openAiStudio() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AI_STUDIO_URL)).apply {
                // Add flags to ensure the browser opens in a new task
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Verify that there's an app to handle this intent
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                showNoBrowserError()
            }
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No activity found to handle intent", e)
            showNoBrowserError()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error opening AI Studio", e)
            Snackbar.make(
                binding.root,
                R.string.error_generic,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Displays an error message when no browser is available.
     */
    private fun showNoBrowserError() {
        Snackbar.make(
            binding.root,
            R.string.error_no_browser,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up any resources if needed
    }

    private companion object {
        private const val TAG = "MainActivity"
        private const val BANNER_URL =
            "https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6"
        private const val AI_STUDIO_URL = "https://aistudio.google.com/apps"
    }
}
