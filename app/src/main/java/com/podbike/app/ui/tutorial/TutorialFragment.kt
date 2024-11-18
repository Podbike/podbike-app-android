package com.podbike.app.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.podbike.app.R
import com.podbike.app.databinding.FragmentTutorialBinding
import com.podbike.app.ui.base.adjustEdgeToEdgePaddings

class TutorialFragment : Fragment() {

    private lateinit var binding: FragmentTutorialBinding

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
                Navigation.findNavController(view)
                    .navigate(R.id.action_tutorialFragment_to_showTutorialFragment)
            }
        )
        viewPager.adapter = tutorialPagerAdapter
    }
}