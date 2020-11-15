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

package com.guillaumepayet.remotenumpad.connection.hid

import android.bluetooth.*
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import com.guillaumepayet.remotenumpad.NumpadActivity
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.AbstractConnectionInterface
import com.guillaumepayet.remotenumpad.connection.IConnectionInterface
import com.guillaumepayet.remotenumpad.connection.IDataSender

/**
 * This [IConnectionInterface] handles a Bluetooth connection with the HID profile added to Android
 * in the API level 28 (Android P) through which an [IDataSender] object can send data.
 *
 * @param sender The [IDataSender] to listen for data to send
 *
 * Created by guillaume on 11/15/20.
 */
@Keep
@RequiresApi(Build.VERSION_CODES.P)
class HidConnectionInterface(sender: IDataSender) : AbstractConnectionInterface(sender) {

    private var bluetoothHidDevice: BluetoothHidDevice? = null
    private var host: BluetoothDevice? = null

    private val callback = object: BluetoothHidDevice.Callback() {

        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)
            Log.i(TAG, "BluetoothHidDevice.Callback.onAppStatusChanged(${pluggedDevice?.name}, $registered)")

            if (pluggedDevice == null) return

            if (bluetoothHidDevice?.getConnectionState(pluggedDevice) == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Attempting to connect to ${pluggedDevice.name}")
                bluetoothHidDevice!!.connect(pluggedDevice)
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            Log.i(TAG, "BluetoothHidDevice.Callback.onConnectionStateChanged(${device?.name}, $state)")

            when (state) {
                BluetoothProfile.STATE_DISCONNECTED ->
                    onConnectionStatusChange(R.string.status_could_not_connect)
                BluetoothProfile.STATE_CONNECTED -> {
                    host = device
                    Log.i(TAG, "Now connected to ${host?.name}")
                    onConnectionStatusChange(R.string.status_connected)
                }
            }
        }
    }

    private val profileListener = object: BluetoothProfile.ServiceListener {

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            Log.i(TAG, "BluetoothProfile.ServiceListener.onServiceConnected")
            bluetoothHidDevice = proxy as BluetoothHidDevice
            bluetoothHidDevice!!.registerApp(SDP, null, null, { it.run() }, callback)
        }

        override fun onServiceDisconnected(profile: Int) {
            Log.i(TAG, "BluetoothProfile.ServiceListener.onServiceDisconnected")
            bluetoothHidDevice = null
        }
    }

    override suspend fun open(host: String) {
        Log.i(TAG, "HidConnectionInterface.open(\"${host}\")")
        onConnectionStatusChange(R.string.status_connecting)
        bluetoothAdapter.getProfileProxy(NumpadActivity.context, profileListener, BluetoothProfile.HID_DEVICE)
    }

    override suspend fun close() {
        Log.i(TAG, "HidConnectionInterface.close()")
        onConnectionStatusChange(R.string.status_disconnecting)
        bluetoothHidDevice?.unregisterApp()
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, bluetoothHidDevice)
        onConnectionStatusChange(R.string.status_disconnected)
    }

    override suspend fun sendString(string: String): Boolean {
        Log.i(TAG, "HidConnectionInterface.sendString(\"${string}\")")
        val keyboardReport = KeyboardReport(string)

        if (!bluetoothHidDevice?.sendReport(host, KeyboardReport.ID, keyboardReport.bytes)!!) {
            Log.e(TAG, "Failed to send the report")
            return false
        }

        return true
    }

    companion object {

        private const val TAG = "HidConnectionInterface"

        private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        private val SDP: BluetoothHidDeviceAppSdpSettings? by lazy {
            BluetoothHidDeviceAppSdpSettings(
                    "Remote Numpad",
                    "Remote Numpad",
                    "Guillaume Payet",
                    BluetoothHidDevice.SUBCLASS1_KEYBOARD,
                    KEYBOARD_DESCRIPTOR
            )
        }

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
    }
}