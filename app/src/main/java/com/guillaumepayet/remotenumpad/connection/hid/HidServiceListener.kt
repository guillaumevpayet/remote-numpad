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

package com.guillaumepayet.remotenumpad.connection.hid

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission

/**
 * This class registers the HID app when the HID profile proxy is ready.
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidServiceListener(private val context: Context, private val listener: IHidDeviceListener) : BluetoothProfile.ServiceListener {

    companion object {

        private val KEYBOARD_DESCRIPTOR = intArrayOf(
            0x05, 0x01,     // UsagePage(Generic Desktop[1])
            0x09, 0x06,     // UsageId(Keyboard[6])
            0xA1, 0x01,     // Collection(Application)
            0x85, 0x01,     //     ReportId(1)
            0x05, 0x07,     //     UsagePage(Keyboard/Keypad[7])
            0x19, 0x01,     //     UsageIdMin(ErrorRollOver[1])
            0x29, 0x65,     //     UsageIdMax(Keyboard Application[101])
            0x15, 0x01,     //     LogicalMinimum(1)
            0x25, 0x65,     //     LogicalMaximum(101)
            0x75, 0x08,     //     ReportSize(8)
            0x95, 0x01,     //     ReportCount(1)
            0x81, 0x00,     //     Input(Data, Array, Absolute, NoWrap, Linear, PreferredState, NoNullPosition, BitField)
            0xC0            // EndCollection()
        ).map { it.toByte() }.toByteArray()

        private val SDP: BluetoothHidDeviceAppSdpSettings by lazy {
            BluetoothHidDeviceAppSdpSettings(
                "Remote Numpad",
                "Remote Numpad",
                "Guillaume Payet",
                BluetoothHidDevice.SUBCLASS1_KEYBOARD,
                KEYBOARD_DESCRIPTOR
            )
        }
    }


    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
        if (profile != BluetoothProfile.HID_DEVICE || proxy !is BluetoothHidDevice)
            return

        val callback = HidDeviceCallback(context, proxy, listener)
        proxy.registerApp(SDP, null, null, Runnable::run, callback)
    }

    override fun onServiceDisconnected(profile: Int) { }
}