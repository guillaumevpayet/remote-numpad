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

package com.guillaumepayet.remotenumpad.connection.socket

import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.*
import java.io.IOException
import java.io.Writer
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

/**
 * This class handles the IP connection through which a [IDataSender] object sends data.
 *
 * @param sender The [IDataSender] to listen for data to send
 *
 * Created by guillaume on 12/29/17.
 *
 * @see IDataSender
 */
open class SocketConnectionInterface(sender: IDataSender) : AbstractConnectionInterface(sender) {

    companion object {

        /**
         * The port through which to connect to the server.
         */
        const val PORT = 4576
    }


    @Volatile
    private var socket: Socket? = null

    @Volatile
    private var writer: Writer? = null


    override fun open(host: String) {
        if (writer != null) {
            onConnectionStatusChange(R.string.status_already_connected)
        } else {
            onConnectionStatusChange(R.string.status_connecting)

            try {
                socket = openSocket(host)
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
        } catch (e: SocketException) {
            onConnectionStatusChange(R.string.status_connection_lost)
        }
    }


    /**
     * Instantiate and connects a socket to the given host and port.
     *
     * @param host The host to connect to
     * @param port The port through which to connect
     * @param timeout The time to wait before the host/port is considered unresponsive
     */
    protected open fun openSocket(host: String, port: Int = PORT, timeout: Int = 3000): Socket {
        val endpoint = InetSocketAddress(host, port)
        val socket = Socket()
        socket.connect(endpoint, timeout)
        return socket
    }
}