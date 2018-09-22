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

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

/**
 * The VirtualNumpad receives touch events directly from the Android [View] objects and notifies
 * the [IKeypadListener] objects of the events.
 *
 * Created by guillaume on 12/27/17.
 *
 * @see IKeypadListener
 */
class VirtualNumpad
/**
 * Construct the virtual numpad with the keys contained in the given ViewGroup.
 *
 * @param viewGroup a container containing all the keys
 */
(viewGroup: ViewGroup) : IKeypad, View.OnTouchListener {

    private val listeners: MutableCollection<IKeypadListener> = HashSet()

    init {
        // Register this object as the OnTouchListener to all the keys
        val nKeys = viewGroup.childCount

        for (i in 0..(nKeys - 1)) {
            viewGroup.getChildAt(i).setOnTouchListener(this)
        }
    }

    override fun registerKeypadListener(listener: IKeypadListener) {
        listeners.add(listener)
    }

    override fun unregisterKeypadListener(listener: IKeypadListener) {
        listeners.remove(listener)
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        val keyValue: String = (view as Key).value

        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN -> keyPress(keyValue)
            MotionEvent.ACTION_UP -> {
                keyRelease(keyValue)
                view.performClick()
            }
            else -> return false
        }

        return true
    }

    /**
     * Notify the {@link IKeypadListener} objects about a key press event.
     *
     * @param keyValue the value of the {@link Key} pressed
     */
    private fun keyPress(keyValue: String) {
        for (listener in listeners)
            listener.onKeyPress(keyValue)
    }

    /**
     * Notify the {@link IKeypadListener} objects about a key release event.
     *
     * @param keyValue the value of the {@link Key} released
     */
    private fun keyRelease(keyValue: String) {
        for (listener in listeners)
            listener.onKeyRelease(keyValue)
    }
}