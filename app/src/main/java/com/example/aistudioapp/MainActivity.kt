package com.example.aistudioapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.aistudioapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBanner()
        setupCtaButton()
    }

    private fun setupBanner() {
        binding.bannerImage.load(BANNER_URL) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
        }
    }

    private fun setupCtaButton() {
        binding.startBuildingButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AI_STUDIO_URL))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Snackbar.make(binding.root, R.string.error_no_browser, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private companion object {
        const val BANNER_URL =
            "https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6"
        const val AI_STUDIO_URL = "https://aistudio.google.com/apps"
    }
}
