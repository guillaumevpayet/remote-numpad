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

package com.guillaumepayet.remotenumpad.settings.hid

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.IntentSender
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.guillaumepayet.remotenumpad.connection.hid.HidServiceFacade

/**
 * Manager class to handle the process of pairing a new device ready for the Bluetooth HID profile.
 */
@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.P)
class HidPairingManager(fragment: HidSettingsFragment) {

    private val pairingLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val companionDeviceManager: CompanionDeviceManager =
        fragment.requireContext().getSystemService(CompanionDeviceManager::class.java)

    private val hidDeviceListener = object: BluetoothHidDevice.Callback() {

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)

            when (state) {
                BluetoothProfile.STATE_CONNECTED -> HidServiceFacade.service?.disconnect(device)
            }
        }
    }

    private val companionDeviceListener = object : CompanionDeviceManager.Callback() {

        // NOTE: This method is required for older versions of Android
        @Deprecated("Deprecated in Java", ReplaceWith("onAssociationPending(intentSender)"))
        override fun onDeviceFound(intentSender: IntentSender) {
            onAssociationPending(intentSender)
        }

        override fun onAssociationPending(intentSender: IntentSender) {
            val pairingRequest = IntentSenderRequest.Builder(intentSender).build()

            try {
                sendPairingRequest(pairingRequest)
            } catch (e: java.lang.IllegalStateException) {
                return
            }
        }

        override fun onFailure(error: CharSequence?) {}
    }


    init {
        HidServiceFacade.registerHidDeviceListener(fragment.requireContext(), hidDeviceListener)

        val pairingContract = ActivityResultContracts.StartIntentSenderForResult()

        pairingLauncher = fragment.registerForActivityResult(pairingContract) { result ->
            if (result.resultCode != Activity.RESULT_OK)
                return@registerForActivityResult

            val device = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                @Suppress("DEPRECATION")
                result.data!!.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
            else
                result.data!!.getParcelableExtra(CompanionDeviceManager.EXTRA_ASSOCIATION, BluetoothDevice::class.java)

            device!!.createBond()
        }
    }


    fun openDialog() {
        val deviceFilter = BluetoothDeviceFilter.Builder().build()
        val pairingRequest = AssociationRequest.Builder().addDeviceFilter(deviceFilter).build()
        companionDeviceManager.associate(pairingRequest, companionDeviceListener, null)
    }

    fun release() {
        HidServiceFacade.unregisterHidDeviceListener()
    }


    private fun sendPairingRequest(pairingRequest: IntentSenderRequest) =
            pairingLauncher.launch(pairingRequest)
}