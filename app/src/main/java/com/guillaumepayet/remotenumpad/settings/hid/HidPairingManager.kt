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

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.IntentSender
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.guillaumepayet.remotenumpad.connection.hid.HidServiceFacade
import java.lang.IllegalStateException

/**
 * Manager class to handle the process of pairing a new device ready for the Bluetooth HID profile.
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidPairingManager(private val preferenceFragment: HidPreferenceFragment) : CompanionDeviceManager.Callback() {

    companion object {
        private const val TAG = "HidPairingManager"

        const val PAIRING_REQUEST_CODE = 3
    }

    private val companionDeviceManager: CompanionDeviceManager =
        preferenceFragment.requireContext().getSystemService(CompanionDeviceManager::class.java)

    private val hidDeviceListener = object: BluetoothHidDevice.Callback() {

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)

            when (state) {
                BluetoothProfile.STATE_CONNECTED -> HidServiceFacade.service.disconnect(device)
                BluetoothProfile.STATE_DISCONNECTED -> {
                    HidServiceFacade.unregisterHidDeviceListener(this)

                    if (preferenceFragment.isResumed)
                        preferenceFragment.updateDeviceList()
                }
            }
        }
    }


    init {
        HidServiceFacade.openService()
        HidServiceFacade.registerHidDeviceListener(hidDeviceListener)
    }


    override fun onDeviceFound(chooserLauncher: IntentSender?) {
        try {
            preferenceFragment.startIntentSenderForResult(
                    chooserLauncher,
                    PAIRING_REQUEST_CODE,
                    null,
                    0,
                    0,
                    0,
                    null)
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Could not start device chooser (${e.javaClass.name}): ${e.message}")
        }
    }

    override fun onFailure(error: CharSequence?) {
        Log.e(TAG, "Failed to pair with device: $error")
    }


    fun openDialog() {
        HidServiceFacade.registerApp()

        val deviceFilter = BluetoothDeviceFilter.Builder().build()
        val pairingRequest = AssociationRequest.Builder().addDeviceFilter(deviceFilter).build()
        companionDeviceManager.associate(pairingRequest, this, null)
    }
}