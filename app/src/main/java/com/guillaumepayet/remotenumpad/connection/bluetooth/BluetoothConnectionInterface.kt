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
import com.guillaumepayet.remotenumpad.connection.*
import java.io.Writer
import java.util.*

/**
 * Created by guillaume on 1/17/18.
 */
class BluetoothConnectionInterface
(sender: IDataSender, taskFactory: IConnectionTaskFactory)
    : AbstractConnectionInterface(sender, taskFactory) {

    val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    var socket: BluetoothSocket? = null
    var writer: Writer? = null

    companion object {
        val NUMPAD_UUID: UUID = UUID.fromString("6be5ccef-5d32-48e3-a3a0-d89e558a40f1")
    }
}