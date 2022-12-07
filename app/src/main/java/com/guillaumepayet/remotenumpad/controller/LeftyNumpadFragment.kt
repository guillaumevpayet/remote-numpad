package com.guillaumepayet.remotenumpad.controller

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.allViews
import com.guillaumepayet.remotenumpad.databinding.FragmentNumpadLeftyBinding

class LeftyNumpadFragment : NumpadFragment() {

    override val keyNumlock
        get() = fragmentBinding.keyNumlock

    override val keyBackspace
        get() = fragmentBinding.keyBackspace


    private lateinit var fragmentBinding: FragmentNumpadLeftyBinding


    override fun inflateView(inflater: LayoutInflater): View {
        fragmentBinding = FragmentNumpadLeftyBinding.inflate(inflater)

        for (view in fragmentBinding.root.allViews) {
            if (view == fragmentBinding.root || view is Key) {
                view.scaleX = -1f
            }
        }

        return fragmentBinding.root
    }
}