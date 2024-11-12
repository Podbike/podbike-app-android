package com.podbike.app.ui.base

import androidx.fragment.app.Fragment

class BaseFragment: Fragment() {

    fun setAppBarTitle(title: String) {

        requireActivity().title = title
    }
}