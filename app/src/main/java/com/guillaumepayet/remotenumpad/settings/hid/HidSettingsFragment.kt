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

package com.guillaumepayet.remotenumpad.settings.hid

import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.hid.HidConnectionInterface
import com.guillaumepayet.remotenumpad.settings.AbstractSettingsFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * This settings page provides a way to list and select a paired device as the host for a
 * [HidConnectionInterface].
 **/
@Keep
@RequiresApi(Build.VERSION_CODES.P)
class HidSettingsFragment : AbstractSettingsFragment() {

    companion object {
        private val bluetoothAdapter: BluetoothAdapter? by lazy {
            BluetoothAdapter.getDefaultAdapter()
        }
    }


    private val hostPreference: ListPreference by lazy {
        val preference = findPreference<ListPreference>(getString(R.string.pref_key_hid_host))!!

        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            host = value.toString()
            updateSummary(preference, host)
            true
        }

        preference
    }

    private lateinit var pairingManager: HidPairingManager


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setHasOptionsMenu(true)
        setPreferencesFromResource(R.xml.pref_hid, rootKey)

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled)
            disableHid()
        else {
            updateDeviceList()
            pairingManager = HidPairingManager(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_hid_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_hid_pair -> {
            pairingManager.openDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        pairingManager.release()
    }


    /**
     * Updates the [ListPreference] entries for devices.
     */
    fun updateDeviceList() {
        val devices = bluetoothAdapter!!.bondedDevices

        val (entries, entryValues) = if (devices.isEmpty()) {
            Pair(listOf(getString(R.string.pref_no_host_entry)), listOf(getString(R.string.pref_no_host_entry_value)))
        } else {
            Pair(devices.map { it.name }, devices.map { it.address })
        }

        hostPreference.entries = entries.toTypedArray()
        hostPreference.entryValues = entryValues.toTypedArray()

        if (hostPreference.value !in entryValues)
            hostPreference.value = entryValues.last()

        GlobalScope.launch(Dispatchers.Main) {
            hostPreference.onPreferenceChangeListener.onPreferenceChange(hostPreference, hostPreference.value)
        }
    }


    /**
     * Disables the HID option in the settings page.
     */
    private fun disableHid() {
        hostPreference.entries = arrayOf(getString(R.string.pref_no_host_entry))
        hostPreference.entryValues = arrayOf(getString(R.string.pref_no_host_entry_value))
        hostPreference.value = getString(R.string.pref_no_host_entry_value)
    }
}