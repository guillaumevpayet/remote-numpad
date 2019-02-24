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

package com.guillaumepayet.remotenumpad.settings.socket

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.socket.SocketConnectionInterface
import com.guillaumepayet.remotenumpad.settings.BasePreferenceFragment


/**
 * This preference screen provides a way to scan and pick the host to connect to via the
 * [SocketConnectionInterface]. There is also an option to key in the host's IP address if known.
 *
 * Created by guillaume on 1/15/18.
 */
class SocketPreferenceFragment : BasePreferenceFragment() {

    override val host: String
        get() =
            if (customHostPreference.isEnabled)
                customHostPreference.text
            else
                hostPreference.value

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var hostPreference: ListPreference
    private lateinit var customHostPreference: EditTextPreference

    private val customHostPreferenceHider = Preference.OnPreferenceChangeListener { preference, value ->
        customHostPreference.isEnabled = value == getString(R.string.pref_manual_host_entry_value)
        summaryToValueBinder.onPreferenceChange(preference, value)
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.pref_socket)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        customHostPreference = findPreference(getString(R.string.pref_key_custom_host)) as EditTextPreference
        bindPreferenceSummaryToValue(customHostPreference)

        hostPreference = findPreference(getString(R.string.pref_key_socket_host)) as ListPreference
        startHostScan()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_socket_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_socket_refresh -> {
                startHostScan()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

        if (!entryValues.contains(hostPreference.value))
            hostPreference.value = entryValues.last()

        bindPreferenceSummaryToValue(hostPreference, customHostPreferenceHider)
        hostPreference.isEnabled = true
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