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

package com.guillaumepayet.remotenumpad.settings.hid

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.preference.ListPreference
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.hid.HidServiceFacade
import com.guillaumepayet.remotenumpad.settings.BasePreferenceFragment

@Keep
@RequiresApi(Build.VERSION_CODES.P)
class HidPreferenceFragment : BasePreferenceFragment() {

    override val host: String
        get() = hostPreference.value


    private val hostPreference: ListPreference by lazy {
        findPreference(getString(R.string.pref_key_hid_host))!!
    }

    private val companionDeviceManager: CompanionDeviceManager by lazy {
        requireContext().getSystemService(CompanionDeviceManager::class.java)
    }

    private val hidDeviceListener = object: BluetoothHidDevice.Callback() {

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)

            when (state) {
                BluetoothProfile.STATE_CONNECTED -> HidServiceFacade.service.disconnect(device)
                BluetoothProfile.STATE_DISCONNECTED -> updateDeviceList()
            }
        }
    }

    private val companionDeviceManagerListener = object: CompanionDeviceManager.Callback() {

        override fun onDeviceFound(chooserLauncher: IntentSender?) {
            startIntentSenderForResult(
                    chooserLauncher,
                    PAIRING_REQUEST_CODE,
                    null,
                    0,
                    0,
                    0,
                    null)
        }

        override fun onFailure(error: CharSequence?) {
            Log.e(TAG, "Failed to pair with device: $error")
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.pref_hid)

        if (!bluetoothAdapter.isEnabled) {
            disableHid()
        } else {
            updateDeviceList()
            HidServiceFacade.registerHidDeviceListener(hidDeviceListener)
        }

        bindPreferenceSummaryToValue(hostPreference)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_hid_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.action_hid_pair)
            return super.onOptionsItemSelected(item)

        if (!HidServiceFacade.isRegistered)
            HidServiceFacade.registerApp()

        val deviceFilter = BluetoothDeviceFilter.Builder().build()

        val pairingRequest = AssociationRequest.Builder()
                .addDeviceFilter(deviceFilter)
                .build()

        companionDeviceManager.associate(
                pairingRequest,
                companionDeviceManagerListener,
                null)

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != PAIRING_REQUEST_CODE || resultCode != Activity.RESULT_OK)
            return super.onActivityResult(requestCode, resultCode, data)

        val device: BluetoothDevice? = data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
        device!!.createBond()
    }

    override fun onDestroy() {
        super.onDestroy()
        HidServiceFacade.unregisterHidDeviceListener(hidDeviceListener)
    }


    private fun updateDeviceList() {
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

    companion object {

        private const val TAG = "HidPreferenceFragment"
        private const val PAIRING_REQUEST_CODE = 3

        private val bluetoothAdapter: BluetoothAdapter by lazy {
            BluetoothAdapter.getDefaultAdapter()
        }
    }
}