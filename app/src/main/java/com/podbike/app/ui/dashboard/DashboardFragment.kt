package com.podbike.app.ui.dashboard

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.podbike.app.R
import com.podbike.app.databinding.FragmentDashboardBinding
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
import kotlin.getValue

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
    }

    override fun onResume() {
        super.onResume()
        validatePermissions()
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
        }
    }

    private fun processUiState(state: DashboardViewModel.DashboardState) {
        with(binding) {
            fragmentDashboardWelcomeBack.isInvisible = state.deviceData != null
            fragmentDashboardSpeed.isInvisible = state.deviceData == null

            if (state.isLoading) {
                showConnectingDialog()
            } else {
                hideConnectingDialog()
            }

            getTooltips(state.speedAbbreviation).forEach { tooltip ->
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

            state.deviceData?.let {
                fragmentDashboardSpeed.text = it.speed
                fragmentDashboardBatteryIndicator.setProgress(
                    it.battery,
                    "${it.range} ${state.rangeAbbreviation}"
                )
                fragmentDashboardDistance.text = it.distance.toString()
                fragmentDashboardAssistance.currentAssistance = it.assist
                fragmentDashboardCadence.currentCadence = it.cadence

                fragmentDashboardIconsLayout.isVisible = it.isMoving
                fragmentDashboardMenuLayout.isVisible = !it.isMoving

                if (it.isFreezing) {
                    fragmentDashboardIcon1.setColorFilter(requireContext().getColor(R.color.white))
                } else {
                    fragmentDashboardIcon1.setColorFilter(requireContext().getColor(R.color.gray))
                }

                fragmentDashboardTurnIndicator.setTurnIndicators(
                    it.lightStatus.indicatorLeft,
                    it.lightStatus.indicatorRight
                )


                val bothTurnIndicatorsOn =
                    it.lightStatus.indicatorLeft && it.lightStatus.indicatorRight
                fragmentDashboardHazardIndicator.setHazardIndicator(bothTurnIndicatorsOn)
                if (bothTurnIndicatorsOn) {
                    fragmentDashboardTurnIndicator.isVisible = false
                    fragmentDashboardHazardIndicator.isVisible = true
                    fragmentDashboardLayout.isVisible = false
                } else if (it.lightStatus.indicatorLeft || it.lightStatus.indicatorRight) {
                    fragmentDashboardTurnIndicator.isVisible = true
                    fragmentDashboardHazardIndicator.isVisible = false
                    fragmentDashboardLayout.isVisible = false
                } else {
                    fragmentDashboardTurnIndicator.isVisible = false
                    fragmentDashboardHazardIndicator.isVisible = false
                    fragmentDashboardLayout.isVisible = true
                }

                fragmentDashboardLights.setImageResource(
                    if (state.isLoading) {
                        R.drawable.ic_baseline_bluetooth_disabled_24
                    } else if (it.lightStatus.highBeam) {
                        R.drawable.ic_material_car_light_high
                    } else if (it.lightStatus.lowBeam) {
                        R.drawable.ic_material_car_light_dimmed
                    } else {
                        0
                    }
                )

            }
        }
    }

    private fun getTooltips(speedAbbreviation: String): List<TooltipInfo> = listOf(
        TooltipInfo(
            R.id.fragment_dashboard_speed,
            "Speed",
            "This is your current speed. It is displayed in $speedAbbreviation"
        ),
        TooltipInfo(
            R.id.fragment_dashboard_battery_indicator,
            "Battery",
            "This is your current battery level with remaining range",
            Gravity.TOP
        ),
        TooltipInfo(
            R.id.fragment_dashboard_distance,
            "Distance",
            "This is the distance you have traveled",
            Gravity.TOP
        ),
        TooltipInfo(
            R.id.fragment_dashboard_assistance,
            "Assistance",
            "This is the level of assistance you are currently receiving",
            Gravity.TOP
        ),
        TooltipInfo(
            R.id.fragment_dashboard_cadence,
            "Cadence",
            "This is the number of revolutions per minute",
            Gravity.TOP
        ),
        TooltipInfo(
            R.id.fragment_dashboard_icon_1,
            "Temperature",
            "Lights up when temperature is below 4*C"
        ),
        TooltipInfo(
            R.id.fragment_dashboard_icon_2,
            "Traction Control System",
            "It activates when sensors detect that one or more wheels are losing grip, such as on slippery or uneven surfaces"
        ),
        TooltipInfo(
            R.id.fragment_dashboard_icon_3,
            "Headlights",
            "This symbol indicates that there is an issue with headlights"
        ),
        TooltipInfo(
            R.id.fragment_dashboard_icon_4,
            "Tire pressure",
            "It means that one or more tires have low pressure"
        ),
        TooltipInfo(
            R.id.fragment_dashboard_icon_5,
            "Handbreak",
            "The symbol indicates that the parking break is engaged"
        )
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
                binding.fragmentDashboardTooltip.setTitle("Help Section")
                binding.fragmentDashboardTooltip.setMessage("Here you'll discover how each component works. Simply click on any component to learn more.\n\n\nTap the Podbike logo to see Statistics\n\nHeads up: These icons will soon switch to warning icons, giving you insights into their functionality")
                binding.fragmentDashboardTooltip.showOkButton(true, View.OnClickListener {
                    binding.fragmentDashboardTooltip.visibility = View.GONE
                    viewModel.processAction(DashboardAction.EnableInteractiveTutorial)
                })
                binding.fragmentDashboardTooltip.visibility = View.VISIBLE
//                startBounceAnimation()
            }

            DashboardEffect.NavigateToStatistics -> findNavController().navigate(R.id.action_dashboardFragment_to_statisticsFragment)
        }
    }

    private fun enableBluetooth() {
        try {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ActivityCompat.startActivityForResult(requireActivity(), enableBtIntent, 0, null)
        } catch (e: Exception) {
            intentManager.openBluetoothSettings()
        }
    }

    private fun validatePermissions() {
        if (!permissionManager.hasBluetoothPermission() || !permissionManager.isBluetoothEnabled() || !permissionManager.isLocationEnabled()) {
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