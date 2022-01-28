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

package com.guillaumepayet.remotenumpad.settings.socket

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.Keep
import androidx.preference.*
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.socket.SocketConnectionInterface
import com.guillaumepayet.remotenumpad.settings.AbstractSettingsFragment


/**
 * This preference screen provides a way to scan and pick the host to connect to via the
 * [SocketConnectionInterface]. There is also an option to key in the host's IP address if known.
 **/
@Keep
class SocketSettingsFragment : AbstractSettingsFragment() {

    private val hostPreference: ListPreference by lazy {
        val preference = findPreference<ListPreference>(getString(R.string.pref_key_socket_host))!!

        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            val isCustom = value == getString(R.string.pref_manual_host_entry_value)
            customHostPreference.isEnabled = isCustom

            if (!isCustom)
                host = value.toString()
            else
                customHostPreference.onPreferenceChangeListener.onPreferenceChange(
                        customHostPreference,
                        customHostPreference.text)

            updateSummary(preference, value.toString())
            preference.isEnabled = true
            true
        }

        preference
    }

    private val customHostPreference: EditTextPreference by lazy {
        val preference = findPreference<EditTextPreference>(getString(R.string.pref_key_custom_host))!!

        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            if (preference.isEnabled)
                host = value.toString()

            updateSummary(preference, host)
            true
        }

        preference
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setHasOptionsMenu(true)
        setPreferencesFromResource(R.xml.pref_socket, rootKey)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
            inflater.inflate(R.menu.menu_socket_settings, menu)

    override fun onResume() {
        super.onResume()
        startHostScan()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_socket_refresh -> {
            startHostScan()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    /**
     * Updates the list of hosts.
     *
     * @param hosts a list of hosts as pairs of (label, address)
     */
    fun updateHosts(hosts: Iterable<Pair<String, String>>) {
        val entries = hosts.map { it.first } + getString(R.string.pref_manual_host_entry)
        val entryValues = hosts.map { it.second } + getString(R.string.pref_manual_host_entry_value)

        hostPreference.entries = entries.toTypedArray()
        hostPreference.entryValues = entryValues.toTypedArray()

        if (hostPreference.value !in entryValues)
            hostPreference.value = entryValues.last()

        hostPreference.onPreferenceChangeListener.onPreferenceChange(hostPreference, hostPreference.value)
    }


    /**
     * Starts scanning for hosts.
     */
    private fun startHostScan() {
        customHostPreference.isEnabled = false
        hostPreference.isEnabled = false
        hostPreference.summary = getString(R.string.pref_summary_host_scanning)
        SocketHostScanner(this).scan()
    }
}