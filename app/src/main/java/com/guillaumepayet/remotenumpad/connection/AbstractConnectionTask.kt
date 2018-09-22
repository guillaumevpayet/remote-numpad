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

import android.os.AsyncTask

/**
 * This class is the abstract parent class of all [AsyncTask] objects used by the
 * [IConnectionInterface] class.
 *
 * Created by guillaume on 12/29/17.
 *
 * @see AsyncTask
 * @see IConnectionInterface
 */
abstract class AbstractConnectionTask
(private val listener: IConnectionStatusListener)
    : AsyncTask<String, Int, Void?>() {

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

        for (status in values)
            listener.onConnectionStatusChange(status!!)
    }
}