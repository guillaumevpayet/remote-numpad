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

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.companion.CompanionDeviceManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.preference.ListPreference
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.hid.HidConnectionInterface
import com.guillaumepayet.remotenumpad.settings.BasePreferenceFragment

/**
 * This settings page provides a way to list and select a paired device as the host for a
 * [HidConnectionInterface].
 **/
@Keep
@RequiresApi(Build.VERSION_CODES.P)
class HidPreferenceFragment : BasePreferenceFragment() {

    companion object {
        private val bluetoothAdapter: BluetoothAdapter by lazy {
            BluetoothAdapter.getDefaultAdapter()
        }
    }

    override val host: String
        get() = hostPreference.value


    private val hostPreference: ListPreference by lazy {
        findPreference(getString(R.string.pref_key_hid_host))!!
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.pref_hid)

        if (!bluetoothAdapter.isEnabled)
            disableHid()
        else
            updateDeviceList()

        bindPreferenceSummaryToValue(hostPreference)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_hid_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_hid_pair) {
            HidPairingManager(this).openDialog()
            true
        } else
            super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != HidPairingManager.PAIRING_REQUEST_CODE || resultCode != Activity.RESULT_OK)
            return super.onActivityResult(requestCode, resultCode, data)

        val device: BluetoothDevice? = data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
        device!!.createBond()
    }


    /**
     * Updates the [ListPreference] entries for devices.
     */
    fun updateDeviceList() {
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


    /**
     * Disables the HID option in the settings page.
     */
    private fun disableHid() {
        hostPreference.entries = arrayOf(getString(R.string.pref_no_host_entry))
        hostPreference.entryValues = arrayOf(getString(R.string.pref_no_host_entry_value))
        hostPreference.value = getString(R.string.pref_no_host_entry_value)
    }
}