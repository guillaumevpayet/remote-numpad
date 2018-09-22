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

package com.guillaumepayet.remotenumpad.connection

import com.guillaumepayet.remotenumpad.controller.IKeypad
import com.guillaumepayet.remotenumpad.controller.IKeypadListener

/**
 * This class handles processing the key events for sending.
 *
 * Created by guillaume on 12/28/17.
 */
class KeyEventSender(keypad: IKeypad) : IKeypadListener, IDataSender {

    private val connectionInterfaces: MutableCollection<IConnectionInterface> = HashSet()

    init {
        keypad.registerKeypadListener(this)
    }

    override fun onKeyPress(keyValue: String) {
        sendString("+$keyValue\n")
    }

    override fun onKeyRelease(keyValue: String) {
        sendString("-$keyValue\n")
    }

    override fun registerConnectionInterface(connectionInterface: IConnectionInterface) {
        connectionInterfaces.add(connectionInterface)
    }

    override fun unregisterConnectionInterface(connectionInterface: IConnectionInterface) {
        connectionInterfaces.remove(connectionInterface)
    }

    private fun sendString(string: String) {
        for (connectionInterface in connectionInterfaces)
            connectionInterface.sendString(string)
    }
}