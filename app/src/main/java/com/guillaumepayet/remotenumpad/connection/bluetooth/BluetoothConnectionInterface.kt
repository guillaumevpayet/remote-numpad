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

package com.guillaumepayet.remotenumpad.connection.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import androidx.annotation.Keep
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.IConnectionInterface
import com.guillaumepayet.remotenumpad.connection.AbstractConnectionInterface
import com.guillaumepayet.remotenumpad.connection.IDataSender
import java.io.IOException
import java.io.Writer
import java.util.*

/**
 * This [IConnectionInterface] handles a Bluetooth connection through which an [IDataSender] object
 * can send data.
 *
 * @param sender The [IDataSender] to listen for data to send
 *
 * Created by guillaume on 1/17/18.
 */
@Keep
class BluetoothConnectionInterface(sender: IDataSender) : AbstractConnectionInterface(sender) {

    companion object {

        /**
         * The UUID of the "Remote Numpad" service
         */
        val NUMPAD_UUID: UUID = UUID.fromString("6be5ccef-5d32-48e3-a3a0-d89e558a40f1")
    }


    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    @Volatile
    private var socket: BluetoothSocket? = null

    @Volatile
    private var writer: Writer? = null


    override fun open(host: String) {
        if (writer != null) {
            onConnectionStatusChange(R.string.status_already_connected)
        } else {
            onConnectionStatusChange(R.string.status_connecting)

            try {
                val device = bluetoothAdapter.getRemoteDevice(host)
                socket = device.createRfcommSocketToServiceRecord(NUMPAD_UUID)
                socket?.connect()
                writer = socket?.outputStream?.writer()
                onConnectionStatusChange(R.string.status_connected)
            } catch (e: IOException) {
                onConnectionStatusChange(R.string.status_could_not_connect)

                writer = null

                socket?.close()
                socket = null
            }
        }
    }

    override fun close() {
        onConnectionStatusChange(R.string.status_disconnecting)

        writer?.close()
        writer = null

        socket?.close()
        socket = null

        onConnectionStatusChange(R.string.status_disconnected)
    }

    override fun sendString(string: String) {
        try {
            writer?.write(string)
            writer?.flush()
        } catch (e: IOException) {
            onConnectionStatusChange(R.string.status_connection_lost)
        }
    }
}