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

import com.guillaumepayet.remotenumpad.connection.IHostValidator

/**
 * [IHostValidator] implementation for the [SocketConnectionInterface] hosts.
 * Hosts are valid if they are made of four integers in the range [0-255] separated by points and
 * with no leading zeroes.
 */
class SocketHostValidator : IHostValidator {

    companion object {
        private const val num8bit = "(\\d|([1-9]\\d)|(1\\d{1,2})|(2[0-4]\\d)|(25[0-5]))"
    }

    override fun isHostValid(address: String): Boolean =
            address.isNotEmpty() && Regex("$num8bit(\\.$num8bit){3}").matches(address)
}