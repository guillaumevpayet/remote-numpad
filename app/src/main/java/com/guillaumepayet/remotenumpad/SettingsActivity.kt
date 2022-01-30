/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2022 Guillaume Payet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.guillaumepayet.remotenumpad

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.preference.Preference
import com.guillaumepayet.remotenumpad.databinding.ActivitySettingsBinding
import com.guillaumepayet.remotenumpad.settings.CommonSettingsFragment
import java.util.*

/**
 * An [AppCompatActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatActivity() {

    companion object {

        /**
         * The package where all the implementations' sub-packages are located.
         */
        val SETTINGS_PACKAGE = CommonSettingsFragment::class.java.`package`?.name
    }

    private lateinit var commonSettings: CommonSettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        commonSettings = supportFragmentManager.findFragmentById(R.id.common_settings) as CommonSettingsFragment

        commonSettings.onConnectionInterfaceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            val stringValue = value.toString()
            val packageName = "${SETTINGS_PACKAGE}.$stringValue"
            val className =
                stringValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } + "SettingsFragment"
            val clazz = Class.forName("$packageName.$className")
            val newFragment = clazz.newInstance() as Fragment

            if (newFragment::class.java.canonicalName != this::class.java.canonicalName)
                supportFragmentManager.commit {
                    replace(R.id.connection_interface_settings, newFragment)
                }

            true
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (!hasFocus) return
        commonSettings.onFocus()
    }
}
