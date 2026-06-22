/*
 * Copyright (C) 2026 Phal AS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.podbike.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.podbike.app.R

class ButtonsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_buttons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.show_tutorial_button)?.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_showTutorialFragment_to_tutorialFragment)
        }

        view.findViewById<View>(R.id.show_settings_button)?.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_showTutorialFragment_to_settingsFragment)
        }

        view.findViewById<View>(R.id.show_devices_button)?.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_showTutorialFragment_to_devicesFragment)
        }

        view.findViewById<View>(R.id.show_autoconnect_button)?.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_showTutorialFragment_to_autoconnectFragment)
        }

        view.findViewById<View>(R.id.show_dashboard_button)?.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_showTutorialFragment_to_dashboardFragment)
        }
    }
}