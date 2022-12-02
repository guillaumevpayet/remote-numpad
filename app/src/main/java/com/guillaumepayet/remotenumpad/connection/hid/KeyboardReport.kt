/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2022 Guillaume Payet
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
        const val ID = 0x01
    }

    val bytes = ByteArray(1) { 0x00 }

    init {
        if (string[0] == '+') {
            val keyString = string.substring(1, string.length - 1)
            bytes[0] = getKeyCode(keyString)
        }
    }

    private fun getKeyCode(keyString: String): Byte {
        return when (keyString) {
            context.getString(R.string.key_value_backspace) -> 0x2A
            context.getString(R.string.key_equal) -> 0x2E
            context.getString(R.string.key_value_numlock) -> 0x53
            context.getString(R.string.key_divide) -> 0x54
            context.getString(R.string.key_multiply) -> 0x55
            context.getString(R.string.key_subtract) -> 0x56
            context.getString(R.string.key_add) -> 0x57
            context.getString(R.string.key_value_enter) -> 0x58
            context.getString(R.string.key_1) -> 0x59
            context.getString(R.string.key_2) -> 0x5A
            context.getString(R.string.key_3) -> 0x5B
            context.getString(R.string.key_4) -> 0x5C
            context.getString(R.string.key_5) -> 0x5D
            context.getString(R.string.key_6) -> 0x5E
            context.getString(R.string.key_7) -> 0x5F
            context.getString(R.string.key_8) -> 0x60
            context.getString(R.string.key_9) -> 0x61
            context.getString(R.string.key_0) -> 0x62
            context.getString(R.string.key_value_decimal) -> 0x63
            else -> 0x00
        }.toByte()
    }
}