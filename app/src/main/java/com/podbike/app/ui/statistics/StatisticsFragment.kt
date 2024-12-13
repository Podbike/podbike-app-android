package com.podbike.app.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.podbike.app.R
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.databinding.FragmentStatisticsBinding
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgePaddings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class StatisticsFragment : BaseFragment() {

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    private lateinit var binding: FragmentStatisticsBinding
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false).apply {
            root.adjustEdgeToEdgePaddings()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()

        binding.appBar.appBarBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.appBar.appBarTitle.text = getString(R.string.StatisticsTitle)
    }

    private fun subscribeToViewModel() {

        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::buildStatistics)
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.statistics()
    }


    private fun buildStatistics(state: StatisticsViewModel.StatisticsState) {
        with(binding) {
            state.data.let {
                val selectedDeviceData = bluetoothManager.selectedDevice?.data

                val temperatureBatteryRow = TableRow(requireContext())
                val rpmPowerGeneratedRow = TableRow(requireContext())
                val co2SavedMaxSpeedRow = TableRow(requireContext())
                val totalDistanceTripTimeRow = TableRow(requireContext())
                val speedTotalTripRow = TableRow(requireContext())

                try {
                    val temperatureView =
                        StatisticView(
                            requireContext(),
                            getString(R.string.StatisticTemperature),
                            if (it.interiorTemperature.isEmpty()) "" else it.temperatureUnit,
                            it.interiorTemperature.ifEmpty { "N/A" },
                        )

                    val batteryRemaining =
                        StatisticView(
                            requireContext(),
                            getString(R.string.StatisticBattery),
                            "%",
                            it.batteryRemaining.toString(),
                        )

                    val co2View =
                        StatisticView(
                            requireContext(),
                            getString(R.string.StatisticCO2),
                            "kg",
                            "%.2f".format(it.co2Saved)
                        )

                    val maxSpeed = StatisticView(
                        requireContext(),
                        getString(R.string.StatisticMaxTripSpeed),
                        it.speedUnit,
                        it.maxSpeed,
                    )

                    val distanceView = StatisticView(
                        requireContext(),
                        getString(R.string.StatisticTotalDistance),
                        it.distanceUnit,
                        it.totalDistance.ifEmpty { "0" },
                    )
                    val tripTimeInMinutes =
                        selectedDeviceData?.tripStart?.elapsedNow()?.inWholeMinutes
                    val tripTime =
                        StatisticView(
                            requireContext(),
                            getString(R.string.StatisticTime),
                            "min",
                            tripTimeInMinutes?.toString() ?: "0",
                        )
                    val averageSpeedTotal = StatisticView(
                        requireContext(),
                        getString(R.string.StatisticAverageSpeedTotal),
                        it.speedUnit,
                        it.averageSpeed,
                    )
                    val averageRpm = StatisticView(
                        requireContext(),
                        getString(R.string.StatisticAverageCadence),
                        "rpm",
                        it.averageRpm.toString(),
                    )
                    val powerGenerated =
                        StatisticView(
                            requireContext(),
                            getString(R.string.StatisticPower),
                            "W",
                            it.powerGenerated.toString(),
                        )

                    val averageSpeedTrip = StatisticView(
                        requireContext(),
                        getString(R.string.StatisticAverageSpeedTrip),
                        it.speedUnit,
                        it.averageTripSpeed,
                    )

                    temperatureBatteryRow.removeAllViews()
                    rpmPowerGeneratedRow.removeAllViews()
                    co2SavedMaxSpeedRow.removeAllViews()
                    totalDistanceTripTimeRow.removeAllViews()
                    speedTotalTripRow.removeAllViews()

                    temperatureBatteryRow.run {
                        addView(temperatureView)
                        addView(batteryRemaining)
                    }

                    rpmPowerGeneratedRow.run {
                        addView(averageRpm)
                        addView(powerGenerated)
                    }

                    co2SavedMaxSpeedRow.run {
                        addView(co2View)
                        addView(maxSpeed)
                    }

                    totalDistanceTripTimeRow.run {
                        addView(distanceView)
                        addView(tripTime)
                    }

                    speedTotalTripRow.run {
                        addView(averageSpeedTotal)
                        addView(averageSpeedTrip)
                    }

                    fragmentStatisticsTableLayout.removeAllViews()

                    fragmentStatisticsTableLayout.run {
                        addView(temperatureBatteryRow)
                        addView(rpmPowerGeneratedRow)
                        addView(co2SavedMaxSpeedRow)
                        addView(totalDistanceTripTimeRow)
                        addView(speedTotalTripRow)
                    }
                } catch (e: Exception) {
                    println("Failed to build statistics: $e")
                    e.printStackTrace()
                }
            }

        }

    }
}
