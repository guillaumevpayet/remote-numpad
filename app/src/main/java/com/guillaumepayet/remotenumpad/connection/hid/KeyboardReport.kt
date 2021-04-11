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

package com.guillaumepayet.remotenumpad.connection.hid

import android.content.Context
import com.guillaumepayet.remotenumpad.R

/**
 * The HID report for a key event. This is simply a container for an array of bytes to be sent
 * through the Bluetooth HID protocol.
 *
 * @param string The string representation of the key event
 **/
class KeyboardReport(private val context: Context, string: String) {

    companion object {
        const val ID = 8
    }

    val bytes = ByteArray(3) { 0 }

    init {
        if (string[0] == '+') {
            val keyString = string.substring(1, string.length - 1)
            bytes[2] = getKeyCode(keyString)
        }
    }

    private fun getKeyCode(keyString: String): Byte {
        return when (keyString) {
            context.getString(R.string.key_value_backspace) -> 42
            context.getString(R.string.key_value_numlock) -> 83
            context.getString(R.string.key_divide) -> 84
            context.getString(R.string.key_multiply) -> 85
            context.getString(R.string.key_subtract) -> 86
            context.getString(R.string.key_add) -> 87
            context.getString(R.string.key_value_enter) -> 88
            context.getString(R.string.key_1) -> 89
            context.getString(R.string.key_2) -> 90
            context.getString(R.string.key_3) -> 91
            context.getString(R.string.key_4) -> 92
            context.getString(R.string.key_5) -> 93
            context.getString(R.string.key_6) -> 94
            context.getString(R.string.key_7) -> 95
            context.getString(R.string.key_8) -> 96
            context.getString(R.string.key_9) -> 97
            context.getString(R.string.key_0) -> 98
            context.getString(R.string.key_value_decimal) -> 99
            else -> 156.toByte()    // Clear
        }.toByte()
    }
}