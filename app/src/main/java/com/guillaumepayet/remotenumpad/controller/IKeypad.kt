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

package com.guillaumepayet.remotenumpad.controller

/**
 * Keypads notify [IKeypadListener] objects about key events.
 *
 * @see IKeypadListener
 */
interface IKeypad {

    /**
     * Register an [IKeypadListener] object which will be notified when a key event is triggered.
     *
     * @param listener the listener to be registered
     */
    fun registerKeypadListener(listener: IKeypadListener)

    /**
     * Unregister an [IKeypadListener] object so that it is no longer notified when a key event is
     * triggered.
     *
     * @param listener the listener to be unregistered
     */
    fun unregisterKeypadListener(listener: IKeypadListener)
}