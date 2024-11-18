package com.podbike.app.ui.privacy_policy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.podbike.app.R
import com.podbike.app.databinding.FragmentPrivacyPolicyBinding
import com.podbike.app.ui.base.adjustEdgeToEdgeMargins

class PrivacyPolicyFragment : Fragment() {

    private lateinit var binding: FragmentPrivacyPolicyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false).apply {
            root.adjustEdgeToEdgeMargins()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppBar()
        setupTabLayout()
    }

    private fun setupAppBar() {
        binding.appBar.appBarBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.appBar.appBarTitle.text = "Privacy"
    }

    private fun setupTabLayout() {
        val viewPager = binding.privacyPolicyViewPager
        val tabLayout = binding.privacyPolicyTabLayout

        viewPager.adapter = PrivacyPolicyPagerAdapter()

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.PolicyPrivacy)
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.privacy_policy_24, null)
                }
                1 -> {
                    tab.text = getString(R.string.PolicyTermsConditions)
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.terms_of_conditions_24, null)
                }
            }
        }.attach()
    }
}