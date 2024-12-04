package com.podbike.app.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import com.podbike.app.R
import com.podbike.app.databinding.FragmentStatisticsBinding
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgePaddings
import com.podbike.app.ui.settings.SettingView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment : BaseFragment() {

    private lateinit var binding: FragmentStatisticsBinding

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

        binding.appBar.appBarBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.appBar.appBarTitle.text = getString(R.string.StatisticsTitle)
        buildStatistics(view)

    }

    private fun buildStatistics(view: View) {
        try {
            // fill with dummy data
            val tableLayout = view.findViewById<ViewGroup>(R.id.fragment_statistics_table_layout)

            val tableRow = TableRow(requireContext())
            val temperatureView = StatisticView(requireContext(), "Temperature", "°C", "25")

            tableRow.addView(temperatureView)

            tableLayout.addView(tableRow)
        } catch (e: Exception) {
            println(e)
        }
    }
}
