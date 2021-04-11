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

import androidx.core.content.edit
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.guillaumepayet.remotenumpad.R
import java.lang.IllegalStateException

/**
 * The base of a settings page. This page contains the general settings and can be extended to
 * provide more options.
 **/
abstract class AbstractSettingsFragment : PreferenceFragmentCompat() {

    /**
     * The selected host to which to connect.
     */
    protected var host = ""
        get() =
            try {
                preferenceManager.sharedPreferences.getString(getString(R.string.pref_key_host), getString(R.string.pref_no_host_entry_value))!!
            } catch (e: IllegalStateException) {
                ""
            }
        set(value) {
            preferenceManager.sharedPreferences.edit {
                try {
                    putString(getString(R.string.pref_key_host), value)
                } catch (e: IllegalStateException) {
                }
            }

            field = value
        }


    /**
     * A method that updates a preference's summary to reflect its new value.
     */
    protected fun updateSummary(preference: Preference, value: String) {
        // The default behaviour is to set the summary to the value's simple string representation.
        preference.summary = value

        if (preference is ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            val index = preference.findIndexOfValue(value)

            // Set the summary to reflect the new value.
            preference.summary = if (index >= 0) preference.entries[index] else null
        }
    }
}