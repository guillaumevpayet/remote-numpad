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

package com.guillaumepayet.remotenumpad.controller

import android.content.Context
import android.content.SharedPreferences
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.util.HashSet

/**
 * Test suite for testing the [VirtualNumpad] controller class.
 *
 * @see VirtualNumpad
 */
@RunWith(PowerMockRunner::class)
@PrepareForTest(VibrationEffect::class, PreferenceManager::class)
class VirtualNumpadTestSuite {

    companion object {

        private const val KEY_VALUE = "TEST"
    }


    @Mock
    private val mockViewGroup: ViewGroup? = null

    @Mock
    private val mockKey: Key? = null

    @Mock
    private val mockMotionEvent: MotionEvent? = null

    @Mock
    private val mockListener: IKeypadListener? = null


    @Before
    fun mockAndroidApis() {
        val mockContext = mock(Context::class.java)
        given(mockViewGroup?.context).willReturn(mockContext)

        val mockVibrator = mock(Vibrator::class.java)
        given(mockContext?.getSystemService(Context.VIBRATOR_SERVICE)).willReturn(mockVibrator)

        val mockVibration = mock(VibrationEffect::class.java)
        mockStatic(VibrationEffect::class.java)
        given(VibrationEffect.createOneShot(anyLong(), anyInt())).willReturn(mockVibration)

        val mockPreferences = mock(SharedPreferences::class.java)
        mockStatic(PreferenceManager::class.java)
        given(PreferenceManager.getDefaultSharedPreferences(any())).willReturn(mockPreferences)
    }


    @Test
    fun constructNumpadWithNonEmptyViewGroup_numpadRegisteredItselfInEachKey() {
        val nKeys = 17

        // Given that the ViewGroup object has 17 child views and...
        given(mockViewGroup?.childCount).willReturn(nKeys)

        val mockChildViews = HashSet<View>()

        for (i in 0 until nKeys) {
            val view = mock(View::class.java)
            mockChildViews.add(view)

            // ...given that the child views are indexed in the ViewGroup,...
            given(mockViewGroup?.getChildAt(i)).willReturn(view)
        }

        // ...when the numpad is constructed with the ViewGroup...
        val numpad = VirtualNumpad(mockViewGroup!!)

        // ...then the numpad should be registered as a listener for each key.
        for (mockChildView in mockChildViews)
            then(mockChildView).should(times(1)).setOnTouchListener(numpad)
    }

    @Test
    fun constructNumpadWithEmptyViewGroup_numpadInitialisesButIsUnuseable() {
        // Given that the ViewGroup object has no child views,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...when the numpad is constructed with the ViewGroup...
        VirtualNumpad(mockViewGroup!!)

        // ...then the numpad should not have attempted to access the ViewGroup's child views.
        then(mockViewGroup).should(times(0)).getChildAt(anyInt())
    }

    @Test
    fun triggerValidKeyDownEventAndListener_numpadNotifiesKeypadListeners() {
        // Given that there is a ViewGroup object,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...given that the numpad is constructed with the ViewGroup,...
        val numpad = VirtualNumpad(mockViewGroup!!)

        // ...given that a keypad listener is registered with the numpad,...
        numpad.registerKeypadListener(mockListener!!)

        // ...given that a Key has a value,...
        given(mockKey?.value).willReturn(KEY_VALUE)

        // ...given that that Key generates a touch "down" event,...
        given(mockMotionEvent?.action).willReturn(MotionEvent.ACTION_DOWN)

        // ...when the numpad is notified of the event...
        numpad.onTouch(mockKey, mockMotionEvent)

        // ...then the message should have been passed on to the keypad listener.
        then(mockListener).should(times(1))?.onKeyPress(KEY_VALUE)
        then(mockListener).shouldHaveNoMoreInteractions()
    }

    @Test
    fun triggerValidKeyUpEventAndListener_numpadNotifiesKeypadListeners() {
        // Given that there is a ViewGroup object,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...given that the numpad is constructed with the ViewGroup and...
        val numpad = VirtualNumpad(mockViewGroup!!)

        // ...given that a keypad listener is registered with the numpad,...
        numpad.registerKeypadListener(mockListener!!)

        // ...given that a Key has a value,...
        given(mockKey?.value).willReturn(KEY_VALUE)

        // ...given that that Key generates a touch "up" event,...
        given(mockMotionEvent?.action).willReturn(MotionEvent.ACTION_UP)

        // ...when the numpad is notified of the event...
        numpad.onTouch(mockKey, mockMotionEvent)

        // ...then the message should have been passed on to the keypad listener.
        then(mockListener).should(times(1))?.onKeyRelease(KEY_VALUE)
        then(mockListener).shouldHaveNoMoreInteractions()
    }

    @Test
    fun triggerInvalidKeyTouchEventAndListener_numpadDoesNotReact() {
        // Given that there is a ViewGroup object,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...given that the numpad is constructed with the ViewGroup and...
        val numpad = VirtualNumpad(mockViewGroup!!)

        // ...given that a keypad listener is registered with the numpad,...
        numpad.registerKeypadListener(mockListener!!)

        // ...given that a Key has a value,...
        given(mockKey?.value).willReturn(KEY_VALUE)

        // ...given that that Key generates a touch "move" event,...
        given(mockMotionEvent?.action).willReturn(MotionEvent.ACTION_MOVE)

        // ...when the numpad is notified of the event...
        numpad.onTouch(mockKey, mockMotionEvent)

        // ...then the message should not have been passed on to the keypad listener.
        then(mockListener).shouldHaveZeroInteractions()
    }

    @Test
    fun triggerValidKeyTouchEventAndNoListener_numpadDoesNotReact() {
        // Given that there is a ViewGroup object,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...given that the numpad is constructed with the ViewGroup and...
        val numpad = VirtualNumpad(mockViewGroup!!)

        // ...given that a Key has a value,...
        given(mockKey?.value).willReturn(KEY_VALUE)

        // ...given that that Key generates a touch "move" event,...
        given(mockMotionEvent?.action).willReturn(MotionEvent.ACTION_MOVE)

        // ...when the numpad is notified of the event...
        numpad.onTouch(mockKey, mockMotionEvent)

        // ...then nothing should happen (and no exception should be thrown).
        then(mockListener).shouldHaveZeroInteractions()
    }

    @Test
    fun triggerValidKeyTouchEventAndTwoListeners_numpadNotifiesBothListeners() {
        // Given that there is a ViewGroup object,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...given that the numpad is constructed with the ViewGroup and...
        val numpad = VirtualNumpad(mockViewGroup!!)

        // ...given that two keypad listeners are registered with the numpad,...
        numpad.registerKeypadListener(mockListener!!)

        val mockListener2 = mock(IKeypadListener::class.java)
        numpad.registerKeypadListener(mockListener2)

        // ...given that a Key has a value,...
        given(mockKey?.value).willReturn(KEY_VALUE)

        // ...given that that Key generates a touch "down" event,...
        given(mockMotionEvent?.action).willReturn(MotionEvent.ACTION_DOWN)

        // ...when the numpad is notified of the event...
        numpad.onTouch(mockKey, mockMotionEvent)

        // ...then the message should have been passed on to both of the keypad listeners.
        then(mockListener).should(times(1))?.onKeyPress(KEY_VALUE)
        then(mockListener).shouldHaveNoMoreInteractions()
        then(mockListener2).should(times(1)).onKeyPress(KEY_VALUE)
        then(mockListener2).shouldHaveNoMoreInteractions()
    }
}