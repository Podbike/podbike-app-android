package com.podbike.app.ui.base

import androidx.fragment.app.Fragment
import com.podbike.app.ui.navigation.IntentManager
import javax.inject.Inject

open class BaseFragment: Fragment() {

    @Inject
    lateinit var intentManager: IntentManager

}