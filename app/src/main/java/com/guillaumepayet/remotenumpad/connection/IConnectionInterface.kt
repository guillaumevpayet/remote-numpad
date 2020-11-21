/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2020 Guillaume Payet
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

package com.guillaumepayet.remotenumpad.connection

/**
 * This interface describes how an object can send data to a network or other interface for cross-
 * -application communication. When the connection status of the connection changes,
 * [IConnectionStatusListener] objects are notified.
 *
 * @see IConnectionStatusListener
 */
interface IConnectionInterface : IConnectionStatusListener {

    /**
     * Register an [IConnectionStatusListener] object to be notified when the connection status
     * changes.
     *
     * @param listener the connection status listener to register
     */
    fun registerConnectionStatusListener(listener: IConnectionStatusListener)

    /**
     * Unregister an [IConnectionStatusListener] object that it is no longer notified of connection
     * status changes.
     *
     * @param listener the connection status listener to unregister
     */
    fun unregisterConnectionStatusListener(listener: IConnectionStatusListener)

    /**
     * Open a connection with the given host (or host address).
     *
     * @param host the name or address of the host (the format depends on the connection interface)
     */
    suspend fun open(host: String)

    /**
     * Close the connection (if one is open).
     */
    suspend fun close()

    /**
     * Send a string through the interface.
     *
     * @param string the string to be sent
     * @return A boolean that is true if the string was sent successfully, false otherwise.
     */
    suspend fun sendString(string: String): Boolean
}