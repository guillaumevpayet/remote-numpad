package com.guillaumepayet.remotenumpad.controller

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.databinding.FragmentNumpadBinding

open class NumpadFragment : Fragment() {

    protected open val keyNumlock
        get() = fragmentBinding.keyNumlock

    protected open val keyBackspace
        get() = fragmentBinding.keyBackspace


    private lateinit var preferences: SharedPreferences
    private lateinit var fragmentBinding: FragmentNumpadBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflateView(inflater)

    override fun onResume() {
        super.onResume()
        onBackspaceChanged()
    }


    protected open fun inflateView(inflater: LayoutInflater): View {
        fragmentBinding = FragmentNumpadBinding.inflate(inflater)
        return fragmentBinding.root
    }


    fun onBackspaceChanged() {
        if (preferences.getBoolean(getString(R.string.pref_key_backspace), false)) {
            keyNumlock.visibility = View.INVISIBLE
            keyBackspace.visibility = View.VISIBLE
        } else {
            keyNumlock.visibility = View.VISIBLE
            keyBackspace.visibility = View.INVISIBLE
        }
    }
}