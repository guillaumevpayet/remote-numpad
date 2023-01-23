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
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.guillaumepayet.remotenumpad.AbstractActivity
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.AbstractConnectionInterface
import com.guillaumepayet.remotenumpad.connection.IConnectionInterface
import com.guillaumepayet.remotenumpad.connection.IDataSender
import com.guillaumepayet.remotenumpad.helpers.IBluetoothConnector

/**
 * This [IConnectionInterface] handles a Bluetooth connection with the HID profile added to Android
 * in the API level 28 (Android P) through which an [IDataSender] object can send data.
 *
 * @param sender The [IDataSender] to listen for data to send
 */
@Keep
@RequiresApi(Build.VERSION_CODES.P)
class HidConnectionInterface(override val activity: AbstractActivity, sender: IDataSender) : AbstractConnectionInterface(sender), IBluetoothConnector, IHidDeviceListener {

    override var userHasDeclinedBluetooth: Boolean = false
        private set


    private val context = activity.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val serviceListener = HidServiceListener(context, this)

    private val bluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter

    private lateinit var hostAddress: String

    private var service: BluetoothHidDevice? = null
    private var hostDevice: BluetoothDevice? = null


    override suspend fun open(host: String) {
        hostAddress = host

        val result = runOrRequestPermission @SuppressLint("MissingPermission") {
            getProfileProxy()
        } as Boolean?

        if (result != true) {
            onConnectionStatusChange(R.string.status_could_not_connect)
        } else {
            onConnectionStatusChange(R.string.status_connecting)
        }
    }

    override suspend fun close() {
        super.close()

        val result = runOrRequestPermission @SuppressLint("MissingPermission") {
            service?.disconnect(hostDevice)
        } as Boolean?

        if (result != true) {
            runOrRequestPermission @SuppressLint("MissingPermission") {
                service?.unregisterApp()
            }

            onConnectionStatusChange(R.string.status_connection_lost)
        }
    }

    override suspend fun sendString(string: String): Boolean {
        val keyboardReport = KeyboardReport(context, string)

        val result = runOrRequestPermission @SuppressLint("MissingPermission") {
            service?.sendReport(hostDevice, KeyboardReport.ID, keyboardReport.bytes)
        } as Boolean?

        if (result != true) {
            close()
        }

        return result == true
    }


    override fun onUserDeclinedBluetooth() {
        userHasDeclinedBluetooth = true
    }


    override fun onAppRegistered(proxy: BluetoothHidDevice?) {
        service = proxy

        val result = runOrRequestPermission @SuppressLint("MissingPermission") {
            val device = bluetoothAdapter.getRemoteDevice(hostAddress)

            handler.post {
                // Fails with Windows if another Bluetooth device is connected (even if not via HID)
                if (proxy?.connect(device) != true) {
                    getProfileProxy()
                }
            }

            true
        }

        if (result != true)
            onConnectionStatusChange(R.string.status_could_not_connect)
    }

    override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
        if (device.address != hostAddress)
            return

        when (state) {
            BluetoothProfile.STATE_CONNECTED -> {
                hostDevice = device
                onConnectionStatusChange(R.string.status_connected)
            }
            BluetoothProfile.STATE_CONNECTING -> {
                onConnectionStatusChange(R.string.status_connecting)
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                hostDevice = null

                val result = runOrRequestPermission @SuppressLint("MissingPermission") {
                    service?.unregisterApp()
                }

                if (result == true)
                    onConnectionStatusChange(R.string.status_disconnected)
                else {
                    onConnectionStatusChange(R.string.status_connection_lost)
                }

                service = null
            }
            BluetoothProfile.STATE_DISCONNECTING -> {
                onConnectionStatusChange(R.string.status_disconnecting)
            }
        }
    }


    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getProfileProxy(): Boolean =
        bluetoothAdapter.getProfileProxy(
            context,
            serviceListener,
            BluetoothProfile.HID_DEVICE
        )
}