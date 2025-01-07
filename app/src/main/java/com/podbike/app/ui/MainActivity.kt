package com.podbike.app.ui

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.podbike.app.R
import com.podbike.app.data.UserPreferences
import com.podbike.app.data.bluetooth.manager.ConnectionManager
import com.podbike.app.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber.Forest.i
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var navController: NavController

    @Inject
    lateinit var userPreferences: UserPreferences

    private val _bluetoothStateFlow = MutableSharedFlow<Boolean>(replay = 1)
    val bluetoothStateFlow = _bluetoothStateFlow.asSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavController()
        setupOnBackPressedDispatcher()
        registerReceiver(
            bluetoothBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ConnectionManager.cancelConnection()
        unregisterReceiver(bluetoothBroadcastReceiver)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun attachBaseContext(newBase: Context?) {
        val newOverride = Configuration(newBase?.resources?.configuration)
        newOverride.fontScale = 1.0f
        applyOverrideConfiguration(newOverride)
        super.attachBaseContext(newBase)
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

    private val bluetoothBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> {
                        _bluetoothStateFlow.tryEmit(false)
                        i("Bluetooth", "State OFF")
                    }

                    BluetoothAdapter.STATE_ON -> {
                        _bluetoothStateFlow.tryEmit(true)
                        i("Bluetooth", "State ON")
                    }
                }
            }
        }
    }
}