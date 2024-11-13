package com.podbike.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
import android.provider.Settings.ACTION_LOCALE_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.podbike.app.R
import com.podbike.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO add generic back handling
        view.findViewById<View>(R.id.app_bar_back).setOnClickListener {
            requireActivity().onBackPressed()
        }
        view.findViewById<TextView>(R.id.app_bar_title).text =
            getString(R.string.SettingsPageTitle)

        buildSettings(view)
    }

    private fun buildSettings(view: View) {
        val linearLayout = view.findViewById<ViewGroup>(R.id.fragment_settings_linear_layout)

        val settingGroupView =
            SettingGroupView(requireContext(), getString(R.string.SettingsGeneral))
        val languageSettingView =
            SettingView(requireContext(), getString(R.string.SettingsLanguage)) {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Intent(
                        ACTION_APP_LOCALE_SETTINGS
                    )
                } else {
                    Intent(
                        ACTION_LOCALE_SETTINGS
                    )
                }
                val uri = Uri.fromParts("package", context?.packageName, null)
                intent.data = uri
                startActivity(intent)
            }
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