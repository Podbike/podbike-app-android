package com.podbike.app.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.podbike.app.R
import com.podbike.app.data.UserPreferences
import com.podbike.app.data.bluetooth.manager.ConnectionManager
import com.podbike.app.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var navController: NavController

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavController()
        setupOnBackPressedDispatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        ConnectionManager.cancelConnection()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupNavController() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val device = userPreferences.getMostRecentDevice()
        val startDestination =
            if (userPreferences.isTutorialCompleted() == false) {
                R.id.tutorialFragment
            } else if (device != null) {
                R.id.autoConnectFragment
            } else {
                R.id.devicesFragment
            }

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
            setStartDestination(startDestination)
        }
        navController.graph = navGraph
    }

    private fun setupOnBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!navController.popBackStack()) {
                    finish()
                }
            }
        })
    }
}