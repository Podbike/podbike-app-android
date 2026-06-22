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

package com.podbike.app.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.podbike.app.R
import com.podbike.app.data.UserPreferences
import com.podbike.app.databinding.FragmentTutorialBinding
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgePaddings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TutorialFragment : BaseFragment() {

    private lateinit var binding: FragmentTutorialBinding

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTutorialBinding.inflate(inflater, container, false).apply {
            root.adjustEdgeToEdgePaddings()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = binding.viewPager
        val tutorialPagerAdapter = TutorialPagerAdapter(
            onNextClick = {
                viewPager.currentItem += 1
            },
            onPreviousClick = {
                viewPager.currentItem -= 1
            },
            onExitClick = {
                userPreferences.setTutorialCompleted(true)
                val device = userPreferences.getMostRecentDevice()
                val destination =
                    if (device != null) {
                        R.id.autoConnectFragment
                    } else {
                        R.id.devicesFragment
                    }

                val navOptions = navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = false }
                }

                findNavController().navigate(
                    destination,
                    null,
                    navOptions
                )
            }
        )
        viewPager.adapter = tutorialPagerAdapter
    }
}