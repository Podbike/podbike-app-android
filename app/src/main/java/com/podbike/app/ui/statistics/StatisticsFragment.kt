package com.podbike.app.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.podbike.app.R
import com.podbike.app.databinding.FragmentStatisticsBinding
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgePaddings
import com.podbike.app.ui.dashboard.DashboardViewModel
import com.podbike.app.ui.settings.SettingView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class StatisticsFragment : BaseFragment() {

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
        subscribeToViewModel(view)

        binding.appBar.appBarBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.appBar.appBarTitle.text = getString(R.string.StatisticsTitle)
        //buildStatistics(view)
    }

    private fun subscribeToViewModel(view: View) {
        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                buildStatistics(state, view)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)


    }


    private fun buildStatistics(state: StatisticsViewModel.StatisticsState, view: View) {
        try {
            // fill with dummy data
            val tableLayout = view.findViewById<ViewGroup>(R.id.fragment_statistics_table_layout)

            val temperatureCo2Row = TableRow(requireContext())
            val distanceTripTimeRow = TableRow(requireContext())
            val averageSpeedRpmRow = TableRow(requireContext())
            val powerBatteryRow = TableRow(requireContext())

            val temperatureView =
                StatisticView(
                    requireContext(),
                    getString(R.string.StatisticTemperature),
                    "°C",
                    state.data?.interiorTemperature?.toString(),
                )
            val co2View =
                StatisticView(requireContext(), getString(R.string.StatisticCO2), "kg", "4")
            val distanceView = StatisticView(
                requireContext(),
                getString(R.string.StatisticTotalDistance),
                "km",
                "12"
            )
            val tripTime =
                StatisticView(requireContext(), getString(R.string.StatisticTime), "min", "30")
            val averageSpeed = StatisticView(
                requireContext(),
                getString(R.string.StatisticAverageSpeed),
                "km/h",
                state.data?.averageSpeed?.toString(),
            )
            val averageRpm = StatisticView(
                requireContext(),
                getString(R.string.StatisticAverageCadence),
                "rpm",
                state.data?.averageRpm?.toString(),
            )
            val powerGenerated =
                StatisticView(
                    requireContext(),
                    getString(R.string.StatisticPower),
                    "W",
                    state.data?.powerGenerated?.toString()
                )
            val batteryRemaining =
                StatisticView(
                    requireContext(),
                    getString(R.string.StatisticBattery),
                    "%",
                    state.data?.batteryRemaining?.toString()
                )


            temperatureCo2Row.run {
                addView(temperatureView)
                addView(co2View)
            }

            distanceTripTimeRow.run {
                addView(distanceView)
                addView(tripTime)
            }

            averageSpeedRpmRow.run {
                addView(averageSpeed)
                addView(averageRpm)
            }

            powerBatteryRow.run {
                addView(powerGenerated)
                addView(batteryRemaining)
            }

            tableLayout.run {
                addView(temperatureCo2Row)
                addView(distanceTripTimeRow)
                addView(averageSpeedRpmRow)
                addView(powerBatteryRow)
            }
        } catch (e: Exception) {
            println(e)
        }
    }
}
