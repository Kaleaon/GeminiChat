package com.example.aistudioapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.aistudioapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupFab()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.chatFragment, R.id.settingsFragment)
        )

        binding.topAppBar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.topAppBar.title = destination.label
            binding.newChatFab.show()
        }
    }

    private fun setupFab() {
        binding.newChatFab.setOnClickListener {
            if (navController.currentDestination?.id != R.id.chatFragment) {
                navController.navigate(R.id.chatFragment)
            }
            supportFragmentManager.setFragmentResult(REQUEST_NEW_CHAT, bundleOf())
            Snackbar.make(binding.root, R.string.toast_new_chat_ready, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    companion object {
        const val REQUEST_NEW_CHAT = "request_new_chat"
    }
}
