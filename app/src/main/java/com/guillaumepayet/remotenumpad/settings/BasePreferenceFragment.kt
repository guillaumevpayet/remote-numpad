/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2018 Guillaume Payet
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
import androidx.core.content.edit
import androidx.fragment.app.commit
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.guillaumepayet.remotenumpad.R

/**
 * The base of a settings page. This page contains the general settings and can be extended to
 * provide more options.
 *
 * Created by guillaume on 1/15/18.
 */
open class BasePreferenceFragment : PreferenceFragmentCompat() {

    companion object {

        /**
         * The package where all the implementations' sub-packages are located.
         */
        private val SETTINGS_PACKAGE = this::class.java.`package`?.name
    }


    /**
     * The selected host to which to connect.
     */
    protected open val host: String
        get() = getString(R.string.pref_no_host_entry_value)

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    protected val summaryToValueBinder = Preference.OnPreferenceChangeListener { preference, value ->

        val stringValue = value.toString()

        if (preference is ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            val index = preference.findIndexOfValue(stringValue)

            // Set the summary to reflect the new value.
            preference.setSummary(
                    if (index >= 0)
                        preference.entries[index]
                    else
                        null)

            val context = preference.context

            if (preference.key == context.getString(R.string.pref_key_connection_interface)) {

                val packageName = "$SETTINGS_PACKAGE.$stringValue"
                val className = stringValue.capitalize() + "PreferenceFragment"

                val fragment = try {
                    val clazz = Class.forName("$packageName.$className")
                    clazz.newInstance() as BasePreferenceFragment
                } catch (e: Exception) {
                    BasePreferenceFragment()
                }

                if (fragment::class.java.canonicalName != this::class.java.canonicalName) {
                    fragmentManager?.commit { replace(android.R.id.content, fragment) }
                }
            }
        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.summary = stringValue
        }

        true
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setHasOptionsMenu(true)

        // Load the base preferences
        setPreferencesFromResource(R.xml.pref_base, rootKey)

        val preference: ListPreference = findPreference(getString(R.string.pref_key_connection_interface))!!
        val entries = preference.entries.toMutableList()
        val entryValues = preference.entryValues.toMutableList()

        entries.zip(entryValues).forEach { (entry, entryValue) ->
            val packageName = "$SETTINGS_PACKAGE.$entryValue"
            val className = entryValue.toString().capitalize() + "Validator"

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

        preference.entries = entries.toTypedArray()
        preference.entryValues = entryValues.toTypedArray()

        if (!entryValues.contains(preference.value))
            preference.value = entryValues[0].toString()

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(preference)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences.edit {
            putString(getString(R.string.pref_key_host), host)
        }
    }


    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.

     * @see .sBindPreferenceSummaryToValueListener
     */
    protected fun bindPreferenceSummaryToValue(preference: Preference, listener: Preference.OnPreferenceChangeListener = summaryToValueBinder) {
        // Set the listener to watch for value changes.
        preference.onPreferenceChangeListener = listener

        val value = when (preference) {
            is ListPreference -> preference.value
            is EditTextPreference -> preference.text
            else -> getString(R.string.pref_no_host_entry_value)
        }

        // Trigger the listener immediately with the preference's current value.
        listener.onPreferenceChange(preference, value)
    }
}