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

import android.bluetooth.BluetoothSocket
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.AbstractConnectionTask
import java.io.IOException
import java.io.Writer

/**
 * Created by guillaume on 1/17/18.
 */
class BluetoothConnectTask(private val connectionInterface: BluetoothConnectionInterface)
    : AbstractConnectionTask(connectionInterface) {

    @Volatile
    private var socket: BluetoothSocket? = null

    @Volatile
    private var writer: Writer? = null


    override fun doInBackground(vararg strings: String?): Void? {
        if (writer != null) {
            publishProgress(R.string.status_already_connected)
        } else {
            publishProgress(R.string.status_connecting)

            try {
                val host = connectionInterface.bluetoothAdapter.getRemoteDevice(strings[0])
                socket = host.createRfcommSocketToServiceRecord(BluetoothConnectionInterface.NUMPAD_UUID)
                socket?.connect()
                writer = socket?.outputStream?.writer()
                publishProgress(R.string.status_connected)
            } catch (e: IOException) {
                publishProgress(R.string.status_could_not_connect)

                writer = null

                socket?.close()
                socket = null
            }
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        connectionInterface.socket = socket
        connectionInterface.writer = writer
    }
}