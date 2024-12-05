package com.podbike.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.podbike.app.R
import com.podbike.app.data.DistanceUnit
import com.podbike.app.data.SpeedUnit
import com.podbike.app.data.TemperatureUnit
import com.podbike.app.data.UserPreferences
import com.podbike.app.data.UserPreferencesImpl.Companion.KEY_DISTANCE_UNIT
import com.podbike.app.data.UserPreferencesImpl.Companion.KEY_SPEED_UNIT
import com.podbike.app.data.UserPreferencesImpl.Companion.KEY_TEMPERATURE_UNIT
import com.podbike.app.databinding.FragmentSettingsBinding
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgePaddings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false).apply {
            root.adjustEdgeToEdgePaddings()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindNavigation(R.string.SettingsPageTitle, binding.appBar)
        buildSettings(view)
    }

    private fun buildSettings(view: View) {
        val linearLayout = view.findViewById<ViewGroup>(R.id.fragment_settings_linear_layout)

        val settingGroupView =
            SettingGroupView(requireContext(), getString(R.string.SettingsGeneral))
        val languageSettingView =
            SettingView(requireContext(), getString(R.string.SettingsLanguage)) {
                intentManager.openLocaleSettings()
            }
        val manageConnectionsView =
            SettingView(requireContext(), getString(R.string.SettingsDevices)) {
                Navigation.findNavController(view)
                    .navigate(R.id.action_settingsFragment_to_devicesFragment)
            }
        val frikarUpdateView = SettingView(requireContext(), getString(R.string.SettingsUpdate))

        val unitsGroupView = SettingGroupView(requireContext(), getString(R.string.SettingsUnits))
        val speedUnitView = SettingView(requireContext(), getString(R.string.SettingsSpeedUnit)) {
            showUnitSelectionBottomSheet(
                getString(R.string.SettingsSpeedUnit),
                listOf(
                    SpeedUnit.KILOMETERS_PER_HOUR.name to getString(R.string.SettingsSpeedKmph),
                    SpeedUnit.METERS_PER_SECOND.name to getString(R.string.SettingsSpeedMps),
                    SpeedUnit.MILES_PER_HOUR.name to getString(R.string.SettingsSpeedMiph),
                ),
                KEY_SPEED_UNIT
            )
        }
        val distanceUnitView =
            SettingView(requireContext(), getString(R.string.SettingsDistanceUnit)) {
                showUnitSelectionBottomSheet(
                    getString(R.string.SettingsDistanceUnit),
                    listOf(
                        DistanceUnit.KILOMETERS.name to getString(R.string.SettingsDistanceKm),
                        DistanceUnit.METERS.name to getString(R.string.SettingsDistanceM),
                        DistanceUnit.MILES.name to getString(R.string.SettingsDistanceMi)
                    ),
                    KEY_DISTANCE_UNIT
                )
            }
        val temperatureUnitView =
            SettingView(requireContext(), getString(R.string.SettingsTemperatureUnit)) {
                showUnitSelectionBottomSheet(
                    getString(R.string.SettingsTemperatureUnit),
                    listOf(
                        TemperatureUnit.CELSIUS.name to getString(R.string.SettingsTemperatureC),
                        TemperatureUnit.FAHRENHEIT.name to getString(R.string.SettingsTemperatureF)
                    ),
                    KEY_TEMPERATURE_UNIT
                )
            }

        val aboutGroupView = SettingGroupView(requireContext(), getString(R.string.SettingsAbout))
        val aboutDeviceView = SettingView(requireContext(), getString(R.string.AboutDevice))
        val frikarPoliciesView =
            SettingView(requireContext(), getString(R.string.SettingsPolicies)) {
                Navigation.findNavController(view)
                    .navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)
            }

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

    private fun showUnitSelectionBottomSheet(
        title: String,
        options: List<Pair<String, String>>,
        preferenceKey: String
    ) {
        val currentSelection = when (preferenceKey) {
            KEY_SPEED_UNIT -> userPreferences.getSpeedUnit().name
            KEY_DISTANCE_UNIT -> userPreferences.getDistanceUnit().name
            KEY_TEMPERATURE_UNIT -> userPreferences.getTemperatureUnit().name
            else -> options[0].first
        }
        val bottomSheet =
            UnitSelectionBottomSheet(title, options, currentSelection) { selectedUnit ->
                when (preferenceKey) {
                    KEY_SPEED_UNIT -> userPreferences.saveSpeedUnit(SpeedUnit.valueOf(selectedUnit))
                    KEY_DISTANCE_UNIT -> userPreferences.saveDistanceUnit(
                        DistanceUnit.valueOf(
                            selectedUnit
                        )
                    )

                    KEY_TEMPERATURE_UNIT -> userPreferences.saveTemperatureUnit(
                        TemperatureUnit.valueOf(
                            selectedUnit
                        )
                    )
                }
            }
        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }
}