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
            0x05, 0x01,     // UsagePage(Generic Desktop)
            0x09, 0x02,     // UsageId(TouchScreen)
            0xa1, 0x01,     // Collection(Application)
            0x09, 0x01,     //     UsageId(Pointer)
            0xa1, 0x00,     //     Collection(Physical)
            0x85, 0x01,     //         ReportId(1)
            0x05, 0x09,     //         UsagePage(Button)
            0x19, 0x01,     //         UsageIdMin(1)
            0x29, 0x03,     //         UsageIdMax(3)
            0x15, 0x00,     //         LogicalMinimum(0)
            0x25, 0x01,     //         LogicalMaximum(1)
            0x95, 0x03,     //         ReportCount(3)
            0x75, 0x01,     //         ReportSize(1)
            0x81, 0x02,     //         Input(Data, Variable, Absolute)
            0x95, 0x01,     //         ReportCount(1)
            0x75, 0x05,     //         ReportSize(5)
            0x81, 0x03,     //         Input(Constant, Variable, Absolute)
            0x05, 0x01,     //         UsagePage(Generic Desktop)
            0x09, 0x30,     //         UsageId(X)
            0x09, 0x31,     //         UsageId(Y)
            0x09, 0x38,     //         UsageId(Wheel)
            0x15, 0x81,     //         LogicalMinimum(-127)
            0x25, 0x7f,     //         LogicalMaximum(127)
            0x75, 0x08,     //         ReportSize(8)
            0x95, 0x03,     //         ReportCount(3)
            0x81, 0x06,     //         Input(Data, Variable, Relative)
            0xc0,           //     EndCollection
            0xc0,           // EndCollection

            0x05, 0x01,     // UsagePage(Generic Desktop)
            0x09, 0x06,     // UsageId(Keyboard)
            0xa1, 0x01,     // Collection(Application)
            0x85, 0x02,     //     ReportId(2)
            0x05, 0x07,     //     UsagePage(Keyboard/Keypad)
            0x19, 0xE0,     //     UsageIdMin(224)
            0x29, 0xE7,     //     UsageIdMax(231)
            0x15, 0x00,     //     LogicalMinimum(0)
            0x25, 0x01,     //     LogicalMaximum(1)
            0x75, 0x01,     //     ReportSize(1)
            0x95, 0x08,     //     ReportCount(8)
            0x81, 0x02,     //     Input(Data, Variable, Absolute) - Modifier byte
            0x95, 0x01,     //     ReportCount(1)
            0x75, 0x08,     //     ReportSize(8)
            0x15, 0x00,     //     LogicalMinimum(0)
            0x25, 0x65,     //     LogicalMaximum(101)
            0x19, 0x00,     //     UsageIdMin(0)
            0x29, 0x65,     //     UsageIdMax(101)
            0x81, 0x00,     //     Input(Data, Array, Absolute) - Keycode byte
            0x05, 0x08,     //     UsagePage(LEDs)
            0x95, 0x05,     //     ReportCount(5)
            0x75, 0x01,     //     ReportSize(1)
            0x19, 0x01,     //     UsageIdMin(1)
            0x29, 0x05,     //     UsageIdMax(5)
            0x91, 0x02,     //     Output(Data, Variable, Absolute) - LED report
            0x95, 0x01,     //     ReportCount(1)
            0x75, 0x03,     //     ReportSize(3)
            0x91, 0x03,     //     Output(Constant, Variable, Absolute) - LED padding
            0xc0            // End Collection
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