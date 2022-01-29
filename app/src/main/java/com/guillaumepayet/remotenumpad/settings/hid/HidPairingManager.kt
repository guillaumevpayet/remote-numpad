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

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.guillaumepayet.remotenumpad.connection.hid.HidServiceFacade

/**
 * Manager class to handle the process of pairing a new device ready for the Bluetooth HID profile.
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidPairingManager(fragment: HidSettingsFragment) {

    private val pairingLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val companionDeviceManager: CompanionDeviceManager =
        fragment.requireContext().getSystemService(CompanionDeviceManager::class.java)

    private val hidDeviceListener = object: BluetoothHidDevice.Callback() {

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }

            when (state) {
                BluetoothProfile.STATE_CONNECTED -> HidServiceFacade.service?.disconnect(device)
            }
        }
    }

    private val companionDeviceListener = object : CompanionDeviceManager.Callback() {

        override fun onDeviceFound(chooserLauncher: IntentSender?) {
            val pairingRequest = IntentSenderRequest.Builder(chooserLauncher!!).build()
            sendPairingRequest(pairingRequest)
        }

        override fun onFailure(error: CharSequence?) {
        }
    }


    init {
        HidServiceFacade.registerHidDeviceListener(fragment.requireContext(), hidDeviceListener)

        val pairingContract = ActivityResultContracts.StartIntentSenderForResult()

        pairingLauncher = fragment.registerForActivityResult(pairingContract) { result ->
            if (result.resultCode != Activity.RESULT_OK)
                return@registerForActivityResult

            val device = result.data!!.getParcelableExtra<BluetoothDevice>(CompanionDeviceManager.EXTRA_DEVICE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@registerForActivityResult
            }

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