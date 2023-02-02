/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2023 Guillaume Payet
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
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.companion.AssociationInfo
import android.companion.CompanionDeviceManager
import android.content.Context
import android.os.Build
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.android.material.snackbar.Snackbar
import com.guillaumepayet.remotenumpad.AbstractActivity
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.hid.HidServiceListener

/**
 * This class handles the result of the [android.content.Intent] launched by the
 * [HidPairingCompanionCallback] class as part of the Companion Device Manager's selection process.
 * When a device is picked, it will arrive here and the HID profile proxy will be requested to be
 * able to pair the device.
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidPairingCallback(private val activity: AbstractActivity) : ActivityResultCallback<ActivityResult> {

    private val context = activity.applicationContext

    private val bluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private val hidPairingDeviceListener = HidPairingDeviceListener(activity)


    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onActivityResult(result: ActivityResult?) {
        if (result?.resultCode != Activity.RESULT_OK)
            return

        try {
            val device = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
            else {
                val association = result.data?.getParcelableExtra(
                    CompanionDeviceManager.EXTRA_ASSOCIATION,
                    AssociationInfo::class.java
                )

                bluetoothAdapter.getRemoteDevice(association?.deviceMacAddress.toString())
            }

            hidPairingDeviceListener.device = device!!
            val hidServiceListener = HidServiceListener(context, hidPairingDeviceListener)
            bluetoothAdapter.getProfileProxy(context, hidServiceListener, BluetoothProfile.HID_DEVICE)
        } catch (e: IllegalArgumentException) {
            val view = activity.findViewById<View>(R.id.connection_interface_settings)
            Snackbar.make(view, R.string.snackbar_incompatible_device, Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Unload resources and interrupt any running pairing. Method called when the settings activity
     * is exited.
     */
    fun release() = hidPairingDeviceListener.release()
}