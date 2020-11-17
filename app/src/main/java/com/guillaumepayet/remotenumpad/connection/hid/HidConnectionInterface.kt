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
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.AbstractConnectionInterface
import com.guillaumepayet.remotenumpad.connection.IConnectionInterface
import com.guillaumepayet.remotenumpad.connection.IDataSender
import java.lang.IllegalStateException
import java.util.*

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

    private var hostDevice: BluetoothDevice? = null

    private var timer: Timer? = null

    private val timeout = object: TimerTask() {
        override fun run() { connectionState = BluetoothProfile.STATE_DISCONNECTED }
    }

    private var connectionState: Int = BluetoothProfile.STATE_DISCONNECTED
        set(value) {
            Log.i(TAG, "connectionState($value), $field")
            when (value) {
                BluetoothProfile.STATE_CONNECTED -> {
                    timer?.cancel()
                    timer = null
                    onConnectionStatusChange(R.string.status_connected)
                }
                BluetoothProfile.STATE_DISCONNECTED ->
                    if (field == BluetoothProfile.STATE_CONNECTING)
                        onConnectionStatusChange(R.string.status_could_not_connect)
                    else {
                        hostDevice = null
                        onConnectionStatusChange(R.string.status_disconnected)
                        HidServiceFacade.unregisterHidDeviceListener(hidDeviceListener)
                    }
            }

            field = value
        }

    private val hidDeviceListener = object: BluetoothHidDevice.Callback() {

        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)
            service = HidServiceFacade.service

            if (service!!.getConnectionState(hostDevice) == BluetoothProfile.STATE_DISCONNECTED) {
                service!!.connect(hostDevice)
                timer = Timer()
                timer!!.schedule(timeout, TIMEOUT_DELAY)
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            Log.i(TAG, "BluetoothHidDevice.Callback.onConnectionStateChanged(<${device?.name}>, $state) on ${Thread.currentThread().name}")

            if (device == hostDevice)
                connectionState = state
        }
    }


    override suspend fun open(host: String) {
        Log.i(TAG, "HidConnectionInterface.open(\"$host\") on ${Thread.currentThread().name}")
        onConnectionStatusChange(R.string.status_connecting)
        hostDevice = bluetoothAdapter.getRemoteDevice(host)
        HidServiceFacade.registerHidDeviceListener(hidDeviceListener)
    }

    override suspend fun close() {
        super.close()
        Log.i(TAG, "HidConnectionInterface.close() on ${Thread.currentThread().name}")
        onConnectionStatusChange(R.string.status_disconnecting)
        service?.disconnect(hostDevice)
    }

    override suspend fun sendString(string: String): Boolean {
        val keyboardReport = KeyboardReport(string)

        return if (hostDevice == null) {
            Log.e(TAG, "No host device")
            false
        } else if (!service!!.sendReport(hostDevice, KeyboardReport.ID, keyboardReport.bytes)) {
            Log.e(TAG, "Failed to send the HID report")
            false
        } else true
    }


    companion object {
        private const val TAG = "HidConnectionInterface"

        private const val TIMEOUT_DELAY = 3000L

        private val bluetoothAdapter: BluetoothAdapter by lazy {
            BluetoothAdapter.getDefaultAdapter()
        }

        private var service: BluetoothHidDevice? = null
    }
}