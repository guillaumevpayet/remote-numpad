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

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * A service facade (singleton) to interface with the Bluetooth HID profile.
 */
@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.P)
object HidServiceFacade {

    private val KEYBOARD_DESCRIPTOR = byteArrayOf(
            0x05.toByte(), 0x01.toByte(),       // Usage Page (Generic Desktop)
            0x09.toByte(), 0x06.toByte(),       // Usage (Keyboard)
            0xA1.toByte(), 0x01.toByte(),       // Collection (Application)
            0x85.toByte(), 0x08.toByte(),       //     REPORT_ID (Keyboard)
            0x05.toByte(), 0x07.toByte(),       //     Usage Page (Key Codes)
            0x19.toByte(), 0xe0.toByte(),       //     Usage Minimum (224)
            0x29.toByte(), 0xe7.toByte(),       //     Usage Maximum (231)
            0x15.toByte(), 0x00.toByte(),       //     Logical Minimum (0)
            0x25.toByte(), 0x01.toByte(),       //     Logical Maximum (1)
            0x75.toByte(), 0x01.toByte(),       //     Report Size (1)
            0x95.toByte(), 0x08.toByte(),       //     Report Count (8)
            0x81.toByte(), 0x02.toByte(),       //     Input (Data, Variable, Absolute)

            0x95.toByte(), 0x01.toByte(),       //     Report Count (1)
            0x75.toByte(), 0x08.toByte(),       //     Report Size (8)
            0x81.toByte(), 0x01.toByte(),       //     Input (Constant) reserved byte(1)

            0x95.toByte(), 0x01.toByte(),       //     Report Count (1)
            0x75.toByte(), 0x08.toByte(),       //     Report Size (8)
            0x15.toByte(), 0x00.toByte(),       //     Logical Minimum (0)
            0x25.toByte(), 0x65.toByte(),       //     Logical Maximum (101)
            0x05.toByte(), 0x07.toByte(),       //     Usage Page (Key codes)
            0x19.toByte(), 0x00.toByte(),       //     Usage Minimum (0)
            0x29.toByte(), 0x65.toByte(),       //     Usage Maximum (101)
            0x81.toByte(), 0x00.toByte(),       //     Input (Data, Array) Key array(6 bytes)
            0xc0.toByte()                       // End Collection (Application)
    )

    private val SDP: BluetoothHidDeviceAppSdpSettings =
            BluetoothHidDeviceAppSdpSettings(
                    "Remote Numpad",
                    "Remote Numpad",
                    "Guillaume Payet",
                    BluetoothHidDevice.SUBCLASS1_KEYBOARD,
                    KEYBOARD_DESCRIPTOR
            )

    private val serviceListener = object: BluetoothProfile.ServiceListener {

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            service = proxy as BluetoothHidDevice
            service?.registerApp(SDP, null, null, { it.run() }, internalHidDeviceListener)
        }

        override fun onServiceDisconnected(profile: Int) {}
    }

    private val internalHidDeviceListener = object: BluetoothHidDevice.Callback() {

        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)
            hidDeviceListener?.onAppStatusChanged(pluggedDevice, registered)
        }

        @Synchronized
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            hidDeviceListener?.onConnectionStateChanged(device, state)
        }
    }

    var service: BluetoothHidDevice? = null
        private set

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private var hidDeviceListener: BluetoothHidDevice.Callback? = null


    /**
     * Registers an HID device listener for device connection events.
     *
     * @param listener The listener to register
     */
    fun registerHidDeviceListener(context: Context, listener: BluetoothHidDevice.Callback) {
        hidDeviceListener = listener

        if (!this::bluetoothAdapter.isInitialized) {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = manager.adapter
        }

        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    }

    /**
     * Unregister the previously registered HID device listener.
     */
    fun unregisterHidDeviceListener() {
        hidDeviceListener = null
        service?.unregisterApp()
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, service)
        service = null
    }
}
