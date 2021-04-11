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
 * Common parent for [IConnectionInterface] which implements a basic observable behaviour.
 *
 * @constructor Adds itself as an observer of the [IDataSender]
 * @param sender The [IDataSender] this class will listen to
 **/
abstract class AbstractConnectionInterface(private val sender: IDataSender) : IConnectionInterface {

    private val listeners: MutableCollection<IConnectionStatusListener> = HashSet()


    init { registerWithSender(sender) }


    override fun registerConnectionStatusListener(listener: IConnectionStatusListener) {
        listeners.add(listener)
    }

    override fun unregisterConnectionStatusListener(listener: IConnectionStatusListener) {
        listeners.remove(listener)
    }

    override fun onConnectionStatusChange(connectionStatus: Int) {
        listeners.forEach { it.onConnectionStatusChange(connectionStatus) }
    }


    override suspend fun close() {
        unregisterWithSender(sender)
    }


    private fun registerWithSender(sender: IDataSender) {
        sender.registerConnectionInterface(this)
    }

    private fun unregisterWithSender(sender: IDataSender) {
        sender.unregisterConnectionInterface(this)
    }
}