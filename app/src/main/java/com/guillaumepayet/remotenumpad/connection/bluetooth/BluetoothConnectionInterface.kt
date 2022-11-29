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

package com.guillaumepayet.remotenumpad.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.annotation.Keep
import com.guillaumepayet.remotenumpad.AbstractActivity
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.IConnectionInterface
import com.guillaumepayet.remotenumpad.connection.AbstractConnectionInterface
import com.guillaumepayet.remotenumpad.connection.IDataSender
import com.guillaumepayet.remotenumpad.helpers.IBluetoothConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.Writer
import java.util.*

/**
 * This [IConnectionInterface] handles a Bluetooth connection through which an [IDataSender] object
 * can send data.
 *
 * @param sender The [IDataSender] to listen for data to send
 **/
@Keep
class BluetoothConnectionInterface(override val activity: AbstractActivity, sender: IDataSender) : AbstractConnectionInterface(sender), IBluetoothConnector {

    companion object {

        /**
         * The UUID of the "Remote Numpad" service
         */
        val NUMPAD_UUID: UUID = UUID.fromString("6be5ccef-5d32-48e3-a3a0-d89e558a40f1")
    }

    override var userHasDeclinedBluetooth: Boolean = false
        private set


    private val context = activity.applicationContext

    private val bluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter

    @Volatile
    private var socket: BluetoothSocket? = null

    @Volatile
    private var writer: Writer? = null


    override suspend fun open(host: String) = withContext(Dispatchers.IO) {
        onConnectionStatusChange(R.string.status_connecting)

        try {
            val device = bluetoothAdapter.getRemoteDevice(host)

            writer = runOrRequestPermission @SuppressLint("MissingPermission") {
                socket = device.createRfcommSocketToServiceRecord(NUMPAD_UUID)
                socket?.connect()
                socket?.outputStream?.writer()
            } as Writer?

            if (writer == null)
                throw IOException()

            onConnectionStatusChange(R.string.status_connected)
        } catch (e: IOException) {
            closeConnection()
            onConnectionStatusChange(R.string.status_could_not_connect)
        }
    }

    override suspend fun close() = withContext(Dispatchers.IO) {
        super.close()
        onConnectionStatusChange(R.string.status_disconnecting)
        closeConnection()
        onConnectionStatusChange(R.string.status_disconnected)
    }

    override suspend fun sendString(string: String): Boolean = withContext(Dispatchers.IO) {
        try {
            writer?.write(string)
            writer?.flush()
            true
        } catch (e: IOException) {
            closeConnection()
            onConnectionStatusChange(R.string.status_connection_lost)
            false
        }
    }


    override fun onUserDeclinedBluetooth() { userHasDeclinedBluetooth = true }


    private fun closeConnection() {
        writer?.close()
        writer = null

        socket?.close()
        socket = null
    }
}