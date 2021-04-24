/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2020 Guillaume Payet
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

package com.guillaumepayet.remotenumpad.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.guillaumepayet.remotenumpad.NumpadActivity
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.SettingsActivity
import java.util.*

class CommonSettingsFragment : AbstractSettingsFragment() {

    private val connectionInterfacePreference: ListPreference by lazy {
        val preference = findPreference<ListPreference>(getString(R.string.pref_key_connection_interface))!!

        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            onConnectionInterfaceChangeListener?.onPreferenceChange(preference, value)
            updateSummary(preference, value.toString())
            true
        }

        preference
    }

    var onConnectionInterfaceChangeListener: Preference.OnPreferenceChangeListener? = null
        set(value) {
            field = value
            updateConnectionInterfaceList()
            value?.onPreferenceChange(connectionInterfacePreference, connectionInterfacePreference.value)
        }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_base, rootKey)

        val themePreference = findPreference<ListPreference>(getString(R.string.pref_key_theme))!!

        themePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            val stringValue = value.toString()
            NumpadActivity.setNightMode(themePreference.context, stringValue)
            updateSummary(themePreference, stringValue)
            true
        }

        updateSummary(connectionInterfacePreference, connectionInterfacePreference.value)
        updateSummary(themePreference, themePreference.value)
    }

    fun onFocus() = updateConnectionInterfaceList()


    private fun updateConnectionInterfaceList() {
        val entries = resources.getStringArray(R.array.pref_connection_interface_titles).toMutableList()
        val entryValues = resources.getStringArray(R.array.pref_connection_interface_values).toMutableList()

        entries.zip(entryValues).forEach { (entry, entryValue) ->
            val packageName = "${SettingsActivity.SETTINGS_PACKAGE}.$entryValue"
            val className = entryValue.toString().capitalize(Locale.ROOT) + "Validator"

            try {
                val clazz = Class.forName("$packageName.$className")
                val validator = clazz.newInstance() as IConnectionInterfaceValidator

                if (!validator.isInterfaceAvailable) {
                    entries.remove(entry)
                    entryValues.remove(entryValue)
                }
            } catch (e: Exception) {
            }
        }

        connectionInterfacePreference.entries = entries.toTypedArray()
        connectionInterfacePreference.entryValues = entryValues.toTypedArray()

        if (connectionInterfacePreference.value in connectionInterfacePreference.entryValues) {
            updateSummary(connectionInterfacePreference, connectionInterfacePreference.value)
        } else {
            connectionInterfacePreference.value = entryValues.first().toString()
            connectionInterfacePreference.callChangeListener(connectionInterfacePreference.value)
        }

    }
}