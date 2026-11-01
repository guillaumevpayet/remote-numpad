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
import java.util.concurrent.Executors

/**
 * This class registers the HID app when the HID profile proxy is ready.
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidServiceListener(private val context: Context, private val listener: IHidDeviceListener) : BluetoothProfile.ServiceListener {

    companion object {

        private val KEYBOARD_DESCRIPTOR = intArrayOf(
            0x05, 0x01,     // Usage Page (Generic Desktop)
            0x09, 0x06,     // Usage (Keyboard)
            0xA1, 0x01,     // Collection (Application)
            0x85, 0x01,     //     Report ID (1)
            0x05, 0x07,     //     Usage Page (Keyboard/Keypad)
            0x19, 0xE0,     //     Usage Minimum (224)
            0x29, 0xE7,     //     Usage Maximum (231)
            0x15, 0x00,     //     Logical Minimum (0)
            0x25, 0x01,     //     Logical Maximum (1)
            0x75, 0x01,     //     Report Size (1)
            0x95, 0x08,     //     Report Count (8)
            0x81, 0x02,     //     Input (Data, Variable, Absolute) - Modifier byte
            0x95, 0x01,     //     Report Count (1)
            0x75, 0x08,     //     Report Size (8)
            0x81, 0x01,     //     Input (Constant) - Reserved byte
            0x05, 0x08,     //     Usage Page (LEDs)
            0x19, 0x01,     //     Usage Minimum (1 / Num Lock)
            0x29, 0x05,     //     Usage Maximum (5 / Kana)
            0x95, 0x05,     //     Report Count (5)
            0x75, 0x01,     //     Report Size (1)
            0x91, 0x02,     //     Output (Data, Variable, Absolute) - LED report
            0x95, 0x01,     //     Report Count (1)
            0x75, 0x03,     //     Report Size (3)
            0x91, 0x01,     //     Output (Constant) - LED padding
            0x05, 0x07,     //     Usage Page (Keyboard/Keypad)
            0x19, 0x00,     //     Usage Minimum (0)
            0x29, 0x65,     //     Usage Maximum (101)
            0x15, 0x00,     //     Logical Minimum (0)
            0x25, 0x65,     //     Logical Maximum (101)
            0x75, 0x08,     //     Report Size (8)
            0x95, 0x06,     //     Report Count (6)
            0x81, 0x00,     //     Input (Data, Array, Absolute) - Key array (6 bytes)
            0xC0            // End Collection
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
        proxy.registerApp(SDP, null, null, Executors.newCachedThreadPool(), callback)
    }

    override fun onServiceDisconnected(profile: Int) {}
}