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

package com.guillaumepayet.remotenumpad.connection.hid

import androidx.annotation.Keep
import com.guillaumepayet.remotenumpad.connection.IHostValidator

/**
 * [IHostValidator] implementation for the [HidConnectionInterface] hosts.
 * Hosts are valid if they are valid Bluetooth addresses.
 */
@Keep
class HidHostValidator : IHostValidator {

    companion object {

        /**
         * The regex pattern for a byte in a Bluetooth address
         */
        private const val BYTE = "(\\d|[A-F]){2}"
    }

    override fun isHostValid(address: String): Boolean =
            address.isNotEmpty() && Regex("$BYTE(:$BYTE){5}").matches(address)
}