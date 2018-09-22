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

package com.guillaumepayet.remotenumpad.controller

/**
 * Keypad listeners receive and handle key events (generated from [IKeypad]).
 *
 * Created by guillaume on 12/27/17.
 *
 * @see IKeypad
 */
interface IKeypadListener {

    /**
     * Method called when a [Key] is pressed.
     *
     * @param keyValue the [Key.value] of the [Key] pressed
     */
    fun onKeyPress(keyValue: String)

    /**
     * Method called when a [Key] is released.
     *
     * @param keyValue the [Key.value] of the [Key] released
     */
    fun onKeyRelease(keyValue: String)
}