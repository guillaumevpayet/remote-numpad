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

package com.guillaumepayet.remotenumpad.settings.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.support.v7.preference.ListPreference
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.bluetooth.BluetoothConnectionInterface
import com.guillaumepayet.remotenumpad.settings.BasePreferenceFragment

/**
 * This settings page provides a way to list and select a paired device as the host for a
 * [BluetoothConnectionInterface].
 *
 * Created by guillaume on 1/17/18.
 */
class BluetoothPreferenceFragment : BasePreferenceFragment() {

    override val host: String
        get() = hostPreference.value

    private lateinit var hostPreference: ListPreference


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.pref_bluetooth)

        hostPreference = findPreference(getString(R.string.pref_key_bluetooth_host)) as ListPreference

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            disableBluetooth()
        } else {
            val devices = bluetoothAdapter.bondedDevices

            val (entries, entryValues) = if (devices.isEmpty()) {
                Pair(listOf(getString(R.string.pref_no_host_entry)), listOf(getString(R.string.pref_no_host_entry_value)))
            } else {
                Pair(devices.map { it.name }, devices.map { it.address })
            }

            hostPreference.entries = entries.toTypedArray()
            hostPreference.entryValues = entryValues.toTypedArray()

            if (!entryValues.contains(hostPreference.value))
                hostPreference.value = entryValues[0]
        }

        bindPreferenceSummaryToValue(hostPreference)
    }


    /**
     * Disables the bluetooth option in the settings page.
     */
    private fun disableBluetooth() {
        hostPreference.entries = arrayOf(getString(R.string.pref_no_host_entry))
        hostPreference.entryValues = arrayOf(getString(R.string.pref_no_host_entry_value))
        hostPreference.value = getString(R.string.pref_no_host_entry_value)
    }
}