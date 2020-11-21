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
 * Connection status listeners are notified by [IConnectionInterface] objects for any change in the
 * connection's status (e.g.: connection opened, closed, lost, etc.).
 *
 * @see IConnectionInterface
 */
interface IConnectionStatusListener {

    /**
     * Method called when the connection has changed state.
     *
     * @param connectionStatus the new state of the connection
     */
    fun onConnectionStatusChange(connectionStatus: Int)
}