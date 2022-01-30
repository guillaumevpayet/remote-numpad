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

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.guillaumepayet.remotenumpad.R

/**
 * The VirtualNumpad receives touch events directly from the Android [View] objects and notifies
 * the [IKeypadListener] objects of the events.
 *
 * @constructor Construct the virtual numpad with the keys contained in the given ViewGroup.
 * @param viewGroup a container containing all the keys
 *
 * @see IKeypadListener
 **/
class VirtualNumpad(viewGroup: ViewGroup) : IKeypad, View.OnTouchListener {

    companion object {
        /**
         * The length of a vibration when a key is pressed (in ms)
         */
        private const val VIBRATION_LENGTH = 25L
    }

    private val listeners: MutableCollection<IKeypadListener> = HashSet()

    private val context = viewGroup.context
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val vibrator = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    } else {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    }

    init {
        // Register this object as the OnTouchListener to all the keys
        val nKeys = viewGroup.childCount

        for (i in 0 until nKeys)
            viewGroup.getChildAt(i).setOnTouchListener(this)
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
            MotionEvent.ACTION_DOWN -> {
                keyPress(keyValue)
                view.playSoundEffect(SoundEffectConstants.CLICK)

                if (preferences.getBoolean(context.getString(R.string.pref_key_vibrations), true)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val vibrationEffect = VibrationEffect.createOneShot(VIBRATION_LENGTH, VibrationEffect.DEFAULT_AMPLITUDE)
                        vibrator.vibrate(vibrationEffect)
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(VIBRATION_LENGTH)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                keyRelease(keyValue)
                view.performClick()
            }
        }

        return false
    }

    /**
     * Notify the {@link IKeypadListener} objects about a key press event.
     *
     * @param keyValue the value of the {@link Key} pressed
     */
    private fun keyPress(keyValue: String) {
        listeners.forEach { it.onKeyPress(keyValue) }
    }

    /**
     * Notify the {@link IKeypadListener} objects about a key release event.
     *
     * @param keyValue the value of the {@link Key} released
     */
    private fun keyRelease(keyValue: String) {
        listeners.forEach { it.onKeyRelease(keyValue) }
    }
}