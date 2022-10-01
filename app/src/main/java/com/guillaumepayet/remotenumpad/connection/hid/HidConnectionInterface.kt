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
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.guillaumepayet.remotenumpad.AbstractActivity
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.AbstractConnectionInterface
import com.guillaumepayet.remotenumpad.connection.IConnectionInterface
import com.guillaumepayet.remotenumpad.connection.IDataSender
import com.guillaumepayet.remotenumpad.helpers.IBluetoothConnector
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

/**
 * This [IConnectionInterface] handles a Bluetooth connection with the HID profile added to Android
 * in the API level 28 (Android P) through which an [IDataSender] object can send data.
 *
 * @param sender The [IDataSender] to listen for data to send
 */
@Keep
@RequiresApi(Build.VERSION_CODES.P)
class HidConnectionInterface(private val context: Context, sender: IDataSender) : AbstractConnectionInterface(sender), IBluetoothConnector {

    companion object {
        private const val TIMEOUT_DELAY = 3000L
    }


    override val activity = context as AbstractActivity

    override var userHasDeclinedBluetooth: Boolean = false
        private set


    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private var service: BluetoothHidDevice? = null
    private var timeoutTask: TimerTask? = null

    private lateinit var hostDevice: BluetoothDevice


    private var connectionState: Int = BluetoothProfile.STATE_DISCONNECTED
        @SuppressLint("InlinedApi")
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        set(value) {
            when (value) {
                BluetoothProfile.STATE_CONNECTING -> {
                    timeoutTask?.cancel()
                    timeoutTask = null

                    timeoutTask = Timer().schedule(TIMEOUT_DELAY) {
                        service!!.connect(hostDevice)
                    }
                }
                BluetoothProfile.STATE_CONNECTED -> {
                    timeoutTask?.cancel()
                    timeoutTask = null
                    onConnectionStatusChange(R.string.status_connected)
                }
                BluetoothProfile.STATE_DISCONNECTING ->
                    if (field == BluetoothProfile.STATE_CONNECTING || field == R.string.status_connection_lost)
                        return
                BluetoothProfile.STATE_DISCONNECTED -> {
                    timeoutTask?.cancel()
                    timeoutTask = null
                    HidServiceFacade.unregisterHidDeviceListener()

                    when (field) {
                        BluetoothProfile.STATE_CONNECTING, BluetoothProfile.STATE_DISCONNECTED ->
                            onConnectionStatusChange(R.string.status_could_not_connect)
                        BluetoothProfile.STATE_DISCONNECTING ->
                            onConnectionStatusChange(R.string.status_disconnected)
                    }
                }
                R.string.status_connection_lost -> {
                    service?.disconnect(hostDevice)
                    onConnectionStatusChange(value)
                }
            }

            field = value
        }


    private val hidDeviceListener = object: BluetoothHidDevice.Callback() {

        @SuppressLint("InlinedApi")
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)

            if (service != null) return

            service = HidServiceFacade.service

            when (val state = service!!.getConnectionState(hostDevice)) {
                BluetoothProfile.STATE_CONNECTED,
                BluetoothProfile.STATE_CONNECTING -> connectionState = state
                else -> service!!.connect(hostDevice)
            }
        }

        @SuppressLint("InlinedApi")
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            connectionState = state
        }
    }


    override suspend fun open(host: String) {
        onConnectionStatusChange(R.string.status_connecting)
        hostDevice = bluetoothAdapter.getRemoteDevice(host)

        timeoutTask = runOrRequestPermission @SuppressLint("MissingPermission") {
            HidServiceFacade.registerHidDeviceListener(activity, hidDeviceListener)

            Timer().schedule(TIMEOUT_DELAY) {
                GlobalScope.launch { close() }
            }
        } as TimerTask?

        if (timeoutTask == null)
            onConnectionStatusChange(R.string.status_could_not_connect)
    }

    override suspend fun close() {
        super.close()
        onConnectionStatusChange(R.string.status_disconnecting)

        val result = runOrRequestPermission @SuppressLint("MissingPermission") {
            if (service == null || !service!!.disconnect(hostDevice))
                connectionState = BluetoothProfile.STATE_DISCONNECTED

            true
        }

        if (result == null)
            onConnectionStatusChange(R.string.status_disconnected)
    }

    override suspend fun sendString(string: String): Boolean {
        val keyboardReport = KeyboardReport(context, string)

        val result = runOrRequestPermission @SuppressLint("MissingPermission") {
            val reportWasSent = service?.sendReport(hostDevice, KeyboardReport.ID, keyboardReport.bytes)

            if (reportWasSent != true)
                connectionState = R.string.status_connection_lost

            reportWasSent
        } as Boolean?

        if (result == null)
            onConnectionStatusChange(R.string.status_connection_lost)

        return result == true
    }


    override fun onUserDeclinedBluetooth() { userHasDeclinedBluetooth = true }
}