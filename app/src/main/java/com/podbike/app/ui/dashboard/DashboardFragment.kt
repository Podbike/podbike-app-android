package com.podbike.app.ui.dashboard

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.podbike.app.R
import com.podbike.app.databinding.FragmentDashboardBinding
import com.podbike.app.ui.MainActivity
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgeMargins
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardAction
import com.podbike.app.ui.dashboard.DashboardViewModel.DashboardEffect
import com.podbike.app.ui.dashboard.view.TooltipView
import com.podbike.app.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : BaseFragment() {

    private lateinit var binding: FragmentDashboardBinding
    val viewModel: DashboardViewModel by viewModels()

    @Inject
    lateinit var permissionManager: PermissionManager

    private var connectingDialog: AlertDialog? = null

    //TODO export to permission manager
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasBluetoothPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                    permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else {
            permissions[Manifest.permission.BLUETOOTH] == true &&
                    permissions[Manifest.permission.BLUETOOTH_ADMIN] == true
        }
        val hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

        viewModel.processAction(
            DashboardAction.PermissionsChanged(
                hasBluetoothPermissions = hasBluetoothPermission,
                isBluetoothEnabled = permissionManager.isBluetoothEnabled(),
                isLocationEnabled = permissionManager.isLocationEnabled()
            )
        )
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.processAction(
                DashboardAction.PermissionsChanged(
                    hasBluetoothPermissions = permissionManager.hasBluetoothPermission(),
                    isBluetoothEnabled = true,
                    isLocationEnabled = permissionManager.isLocationEnabled()
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false).apply {
            root.adjustEdgeToEdgeMargins()
        }
        permissionManager.initializePermissionLauncher(requestPermissionsLauncher)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        setupBindings()
        (activity as? MainActivity)?.bluetoothStateFlow?.onEach { isEnabled ->
            validatePermissions()
        }?.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onStart() {
        super.onStart()
        validatePermissions()
    }

    override fun onResume() {
        super.onResume()
        viewModel.processAction(DashboardAction.Resume)
    }

    private fun subscribeToViewModel() {
        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::processUiState)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.uiEffect
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::processEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupBindings() {
        with(binding) {
            fragmentDashboardLogo.setOnClickListener {
                viewModel.processAction(DashboardAction.StatisticsClicked)
            }
            fragmentDashboardSettings.setOnClickListener {
                viewModel.processAction(DashboardAction.SettingsClicked)
            }
            fragmentDashboardHelp.setOnClickListener {
                viewModel.processAction(DashboardAction.HelpClicked)
            }
            fragmentDashboardReturnButton.setOnClickListener {
                viewModel.processAction(DashboardAction.ReturnToDashboardClicked)
            }
            fragmentDashboardEnableBluetoothButton.setOnClickListener {
                viewModel.processAction(DashboardAction.EnableBluetooth)
            }
        }
    }

    private fun processUiState(state: DashboardViewModel.DashboardState) {
        with(binding) {
            updateDashboardState(state)

            if (state.isConnected || state.isUpdateOngoing) {
                hideConnectingDialog()
            } else {
                showConnectingDialog()
            }

            when (state.error) {
                is DashboardViewModel.ErrorTypeSealed.ConnectToDeviceError -> {}
                is DashboardViewModel.ErrorTypeSealed.DashboardTimeoutError -> {}
                is DashboardViewModel.ErrorTypeSealed.FrikarUpdateFailed -> {
                    showAlertDialog(getString(R.string.UpdateFailed))
                }

                null -> {}
            }

            getTooltips(
                state.speedAbbreviation,
                state.distanceAbbreviation,
                state.temperatureAbbreviation,
                state.freezingTemperature
            ).forEach { tooltip ->
                val view = binding.root.findViewById<View>(tooltip.widgetId)
                if (state.isInteractiveTutorialEnabled) {
                    view.setOnClickListener {
                        setupTooltip(tooltip.title, tooltip.message, view, tooltip.gravity)
                    }
                } else {
                    view.setOnClickListener(null)
                }
            }

            fragmentDashboardReturnButton.isVisible = state.isInteractiveTutorialEnabled

            if (state.isInteractiveTutorialEnabled) {
                binding.fragmentDashboardIconsLayout.isVisible = true
                binding.fragmentDashboardMenuLayout.isVisible = false
                return@with
            }

            fragmentDashboardDistanceUnit.text = state.distanceAbbreviation
            if (state.isBluetoothEnabled) {
                fragmentDashboardDistance.visibility = View.VISIBLE
                fragmentDashboardDistanceUnit.visibility = View.VISIBLE
            } else {
                fragmentDashboardDistance.visibility = View.INVISIBLE
                fragmentDashboardDistanceUnit.visibility = View.INVISIBLE
            }

            if (state.deviceData != null && state.isBluetoothEnabled) {
                fragmentDashboardSpeed.text = state.deviceData.speed
                fragmentDashboardBatteryIndicator.setProgress(
                    state.deviceData.battery,
                    "${state.deviceData.range} ${state.rangeAbbreviation}"
                )
                fragmentDashboardDistance.text = state.deviceData.distance
                fragmentDashboardAssistance.currentAssistance = state.deviceData.assist
                fragmentDashboardCadence.currentCadence = state.deviceData.cadence

                fragmentDashboardIconsLayout.isVisible = state.deviceData.isMoving
                fragmentDashboardMenuLayout.isVisible = !state.deviceData.isMoving

                if (state.deviceData.isFreezing) {
                    fragmentDashboardIcon1.setColorFilter(requireContext().getColor(R.color.white))
                } else {
                    fragmentDashboardIcon1.setColorFilter(requireContext().getColor(R.color.gray))
                }

                with(state.deviceData.lightStatus) {
                    val isHazardIndicator = indicatorLeft && indicatorRight
                    fragmentDashboardHazardIndicator.setHazardIndicator(isHazardIndicator)
                    fragmentDashboardTurnIndicator.setTurnIndicators(indicatorLeft, indicatorRight)
                    if (isHazardIndicator) {
                        fragmentDashboardTurnIndicator.isVisible = false
                        fragmentDashboardHazardIndicator.isVisible = true
                    } else if (indicatorLeft || indicatorRight) {
                        fragmentDashboardTurnIndicator.isVisible = true
                        fragmentDashboardHazardIndicator.isVisible = false
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            fragmentDashboardTurnIndicator.isVisible = false
                            fragmentDashboardHazardIndicator.isVisible = false
                        }, 200)
                    }
                }

                fragmentDashboardLights.setImageResource(
                    if (!state.isConnected) {
                        R.drawable.ic_baseline_bluetooth_disabled_24
                    } else if (state.deviceData.lightStatus.highBeam) {
                        R.drawable.ic_material_car_light_high
                    } else if (state.deviceData.lightStatus.lowBeam) {
                        R.drawable.ic_material_car_light_dimmed
                    } else {
                        0
                    }
                )
            } else {
                clearDashboard(state)
            }
        }
    }

    private fun FragmentDashboardBinding.updateDashboardState(state: DashboardViewModel.DashboardState) {
        if (state.isInteractiveTutorialEnabled) {
            fragmentDashboardEnableBluetoothButton.visibility = View.INVISIBLE
            fragmentDashboardSpeed.visibility = View.VISIBLE
            fragmentDashboardWelcomeBack.visibility = View.INVISIBLE
            fragmentDashboardUpdateOngoing.visibility = View.INVISIBLE
        } else if (state.isBluetoothEnabled == false) {
            clearDashboard(state)
            fragmentDashboardEnableBluetoothButton.visibility = View.VISIBLE
            fragmentDashboardSpeed.visibility = View.INVISIBLE
            fragmentDashboardWelcomeBack.visibility = View.INVISIBLE
            fragmentDashboardUpdateOngoing.visibility = View.INVISIBLE
        } else if (state.isUpdateOngoing) {
            fragmentDashboardEnableBluetoothButton.visibility = View.INVISIBLE
            fragmentDashboardSpeed.visibility = View.INVISIBLE
            fragmentDashboardWelcomeBack.visibility = View.INVISIBLE
            fragmentDashboardUpdateOngoing.visibility = View.VISIBLE
        } else if (state.deviceData == null) {
            fragmentDashboardEnableBluetoothButton.visibility = View.INVISIBLE
            fragmentDashboardSpeed.visibility = View.INVISIBLE
            fragmentDashboardWelcomeBack.visibility = View.VISIBLE
            fragmentDashboardUpdateOngoing.visibility = View.INVISIBLE
        } else {
            fragmentDashboardEnableBluetoothButton.visibility = View.INVISIBLE
            fragmentDashboardSpeed.visibility = View.VISIBLE
            fragmentDashboardWelcomeBack.visibility = View.INVISIBLE
            fragmentDashboardUpdateOngoing.visibility = View.INVISIBLE
        }
    }

    private fun FragmentDashboardBinding.clearDashboard(state: DashboardViewModel.DashboardState) {
        fragmentDashboardBatteryIndicator.setProgress(
            0,
            "0 ${state.rangeAbbreviation}"
        )
        val isConnected = state.isConnected && state.isBluetoothEnabled
        fragmentDashboardDistance.visibility = if (isConnected) View.VISIBLE else View.INVISIBLE
        fragmentDashboardDistanceUnit.visibility = if (isConnected) View.VISIBLE else View.INVISIBLE
        fragmentDashboardDistance.text = "0"
        fragmentDashboardAssistance.currentAssistance = 0
        fragmentDashboardCadence.currentCadence = 0
        fragmentDashboardLights.setImageResource(
            if (!isConnected) {
                R.drawable.ic_baseline_bluetooth_disabled_24
            } else {
                R.drawable.ic_material_car_light_dimmed
            }
        )
    }

    private fun getTooltips(
        speedAbbreviation: String,
        distanceAbbreviation: String,
        temperatureAbbreviation: String,
        freezingTemperature: Int = 0
    ): List<TooltipInfo> = listOf(
        TooltipInfo(
            R.id.fragment_dashboard_speed,
            getString(R.string.HelpSpeedometerTitle),
            getString(R.string.HelpSpeedometerDescription, speedAbbreviation)
        ),
        TooltipInfo(
            R.id.fragment_dashboard_battery_indicator,
            getString(R.string.HelpBatteryAndRangeTitle),
            getString(R.string.HelpBatteryAndRangeDescription, distanceAbbreviation),
            Gravity.TOP
        ),
        TooltipInfo(
            R.id.fragment_dashboard_distance,
            getString(R.string.HelpOdometerTitle),
            getString(R.string.HelpOdometerDescription),
            Gravity.TOP
        ),
        TooltipInfo(
            R.id.fragment_dashboard_assistance,
            getString(R.string.HelpAssistanceTitle),
            getString(R.string.HelpAssistanceDescription),
            Gravity.TOP
        ),
        TooltipInfo(
            R.id.fragment_dashboard_cadence,
            getString(R.string.HelpCadenceTitle),
            getString(R.string.HelpCadenceDescription),
            Gravity.TOP
        ),
        TooltipInfo(
            R.id.fragment_dashboard_icon_1,
            getString(R.string.HelpSnowAlertTitle),
            getString(
                R.string.HelpSnowAlertDescription,
                "$freezingTemperature$temperatureAbbreviation"
            )
        ),
        TooltipInfo(
            R.id.fragment_dashboard_icon_2,
            getString(R.string.HelpTractionControlTitle),
            getString(R.string.HelpTractionControlDescription)
        ),
        TooltipInfo(
            R.id.fragment_dashboard_icon_3,
            getString(R.string.HelpLightAlertTitle),
            getString(R.string.HelpLightAlertDescription)
        ),
        TooltipInfo(
            R.id.fragment_dashboard_icon_4,
            getString(R.string.HelpTireAlertTitle),
            getString(R.string.HelpTireAlertDescription)
        ),
        TooltipInfo(
            R.id.fragment_dashboard_icon_5,
            getString(R.string.HelpBrakeAlertTitle),
            getString(R.string.HelpBrakeAlertDescription)
        ),
        TooltipInfo(
            R.id.fragment_dashboard_lights,
            getString(R.string.HelpModeAndLightsTitle),
            getString(R.string.HelpModeAndLightsDescription),
            Gravity.TOP
        ),
    )

    private fun setupTooltip(
        title: String,
        message: String,
        view: View,
        gravity: Int = Gravity.BOTTOM
    ) {
        val oldTitle = binding.fragmentDashboardTooltip.getTitle()

        val tooltipView = TooltipView(requireContext())
        tooltipView.setTitle(title)
        tooltipView.setMessage(message)

        SimpleTooltip.Builder(requireContext())
            .anchorView(view)
            .text(message)
            .contentView(tooltipView, R.id.tooltipMessage)
            .textColor(requireContext().getColor(R.color.black))
            .arrowColor(requireContext().getColor(R.color.tooltip_gray))
            .backgroundColor(requireContext().getColor(R.color.tooltip_gray))
            .gravity(gravity)
            .animated(false)
            .transparentOverlay(false)
            .ignoreOverlay(true)
            .build()
            .show();
    }

    private fun processEffect(effect: DashboardEffect) {
        when (effect) {
            DashboardEffect.NavigateBack -> findNavController().popBackStack()
            DashboardEffect.NavigateToBluetoothPermissions -> permissionManager.requestPermissions()
            DashboardEffect.NavigateToBluetoothSettings -> enableBluetooth()
            DashboardEffect.NavigateToLocationPermissions -> permissionManager.requestPermissions()
            DashboardEffect.NavigateToLocationSettings -> intentManager.openLocationSettings()
            DashboardEffect.NavigateToAppSettings -> findNavController().navigate(R.id.action_dashboardFragment_to_settingsFragment)
            DashboardEffect.NavigateToHelp -> {
                binding.fragmentDashboardTooltip.setTitle(getString(R.string.HelpInfoModalTitle))
                binding.fragmentDashboardTooltip.setMessage(getString(R.string.HelpInfoModalDescription))
                binding.fragmentDashboardTooltip.showOkButton(true, View.OnClickListener {
                    binding.fragmentDashboardTooltip.visibility = View.GONE
                    viewModel.processAction(DashboardAction.EnableInteractiveTutorial)
                })
                binding.fragmentDashboardTooltip.visibility = View.VISIBLE
                startBounceAnimation()
            }

            DashboardEffect.NavigateToStatistics -> findNavController().navigate(R.id.action_dashboardFragment_to_statisticsFragment)
            DashboardEffect.HideNavigationIcons -> {
                binding.fragmentDashboardIconsLayout.isVisible = false
                binding.fragmentDashboardMenuLayout.isVisible = true
            }

            DashboardEffect.FrikarUpdated -> showAlertDialog(getString(R.string.UpdateComplete))
            DashboardEffect.FrikarUpdateFailed -> showAlertDialog(getString(R.string.UpdateFailed))
        }
    }

    private fun enableBluetooth() {
        try {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            enableBluetoothLauncher.launch(enableBtIntent)
        } catch (e: Exception) {
            intentManager.openBluetoothSettings()
        }
    }

    private fun validatePermissions() {
        if (!permissionManager.hasBluetoothPermission() || !permissionManager.isBluetoothEnabled() || !permissionManager.isLocationEnabled()) {
            enableBluetooth()
            permissionManager.requestPermissions()
        } else {
            viewModel.processAction(
                DashboardAction.PermissionsChanged(
                    hasBluetoothPermissions = permissionManager.hasBluetoothPermission(),
                    isBluetoothEnabled = permissionManager.isBluetoothEnabled(),
                    isLocationEnabled = permissionManager.isLocationEnabled()
                )
            )
        }
    }

    private fun showConnectingDialog() {
        if (connectingDialog == null) {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_connecting_to_frikar, null)
            connectingDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()

            val deviceName = viewModel.uiState.value.deviceData?.name ?: "FRIKAR"
            dialogView.findViewById<TextView>(R.id.dialog_connecting_to_frikar_title).text =
                "${getString(R.string.ConnectingTo)} $deviceName"

            dialogView.findViewById<Button>(R.id.dialog_cancel_button).setOnClickListener {
                findNavController().navigate(R.id.action_dashboardFragment_to_devicesFragment)
                connectingDialog?.dismiss()
            }
        }
        connectingDialog?.show()
    }

    private fun hideConnectingDialog() {
        connectingDialog?.dismiss()
        connectingDialog = null
    }

    private fun startBounceAnimation() {
        val bounceAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        binding.fragmentDashboardLogo.startAnimation(bounceAnimation)
    }

}