package com.podbike.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.podbike.app.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO add generic back handling
        view.findViewById<View>(R.id.app_bar_back).setOnClickListener {
            requireActivity().onBackPressed()
        }
        view.findViewById<TextView>(R.id.app_bar_title).text =
            getString(R.string.SettingsPageTitle)

        buildSettings()
    }

    private fun buildSettings() {
        val linearLayout = view?.findViewById<ViewGroup>(R.id.fragment_settings_linear_layout)

        val settingGroupView =
            SettingGroupView(requireContext(), getString(R.string.SettingsGeneral))
        val languageSettingView =
            SettingView(requireContext(), getString(R.string.SettingsLanguage))
        val manageConnectionsView =
            SettingView(requireContext(), getString(R.string.SettingsDevices))
        val frikarUpdateView = SettingView(requireContext(), getString(R.string.SettingsUpdate))

        val unitsGroupView = SettingGroupView(requireContext(), getString(R.string.SettingsUnits))
        val speedUnitView = SettingView(requireContext(), getString(R.string.SettingsSpeedUnit))
        val distanceUnitView =
            SettingView(requireContext(), getString(R.string.SettingsDistanceUnit))
        val temperatureUnitView =
            SettingView(requireContext(), getString(R.string.SettingsTemperatureUnit))

        val aboutGroupView = SettingGroupView(requireContext(), getString(R.string.SettingsAbout))
        val aboutDeviceView = SettingView(requireContext(), getString(R.string.AboutDevice))
        val frikarPoliciesView = SettingView(requireContext(), getString(R.string.SettingsPolicies))

        val appVersion = requireContext().packageManager.getPackageInfo(
            requireContext().packageName,
            0
        ).versionName
        val appVersionText = getString(R.string.SettingsAppVersion) + " " + appVersion
        val appVersionView = SettingFooterView(requireContext(), appVersionText)

        linearLayout?.run {
            addView(settingGroupView)
            addView(languageSettingView)
            addView(manageConnectionsView)
            addView(frikarUpdateView)
            addView(unitsGroupView)
            addView(speedUnitView)
            addView(distanceUnitView)
            addView(temperatureUnitView)
            addView(aboutGroupView)
            addView(aboutDeviceView)
            addView(frikarPoliciesView)
            addView(appVersionView)
        }
    }
}