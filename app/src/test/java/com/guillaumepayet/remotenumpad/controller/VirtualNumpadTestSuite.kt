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
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.HashSet

/**
 * Test suite for testing the [VirtualNumpad] controller class.
 *
 * Created by guillaume on 12/27/17.
 *
 * @see VirtualNumpad
 */
@RunWith(MockitoJUnitRunner::class)
class VirtualNumpadTestSuite {

    private val fakeValue = "test"

    @Mock
    private val mockViewGroup: ViewGroup? = null

    @Mock
    private val mockKey: Key? = null

    @Mock
    private val mockMotionEvent: MotionEvent? = null

    @Mock
    private val mockListener: IKeypadListener? = null

    private var numpad: VirtualNumpad? = null

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
        numpad = VirtualNumpad(mockViewGroup!!)

        // ...then the numpad should be registered as a listener for each key.
        for (mockChildView in mockChildViews)
            verify(mockChildView, times(1)).setOnTouchListener(numpad)
    }

    @Test
    fun constructNumpadWithEmptyViewGroup_numpadInitialisesButIsUnuseable() {
        // Given that the ViewGroup object has no child views,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...when the numpad is constructed with the ViewGroup...
        numpad = VirtualNumpad(mockViewGroup!!)

        // ...then the numpad should not have attempted to access the ViewGroup's child views.
        verify(mockViewGroup, times(0)).getChildAt(0)
    }

    @Test
    fun triggerValidKeyDownEventAndListener_numpadNotifiesKeypadListeners() {
        // Given that there is a ViewGroup object,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...given that the numpad is constructed with the ViewGroup,...
        numpad = VirtualNumpad(mockViewGroup!!)

        // ...given that a keypad listener is registered with the numpad,...
        numpad?.registerKeypadListener(mockListener!!)

        // ...given that a Key has a value,...
        given(mockKey?.value).willReturn(fakeValue)

        // ...given that that Key generates a touch "down" event,...
        given(mockMotionEvent?.action).willReturn(MotionEvent.ACTION_DOWN)

        // ...when the numpad is notified of the event...
        numpad?.onTouch(mockKey, mockMotionEvent)

        // ...then the message should have been passed on to the keypad listener.
        verify(mockListener, times(1))?.onKeyPress(fakeValue)
        verify(mockListener, times(0))?.onKeyRelease(fakeValue)
    }

    @Test
    fun triggerValidKeyUpEventAndListener_numpadNotifiesKeypadListeners() {
        // Given that there is a ViewGroup object,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...given that the numpad is constructed with the ViewGroup and...
        numpad = VirtualNumpad(mockViewGroup!!)

        // ...given that a keypad listener is registered with the numpad,...
        numpad?.registerKeypadListener(mockListener!!)

        // ...given that a Key has a value,...
        given(mockKey?.value).willReturn(fakeValue)

        // ...given that that Key generates a touch "up" event,...
        given(mockMotionEvent?.action).willReturn(MotionEvent.ACTION_UP)

        // ...when the numpad is notified of the event...
        numpad?.onTouch(mockKey, mockMotionEvent)

        // ...then the message should have been passed on to the keypad listener.
        verify(mockListener, times(0))?.onKeyPress(fakeValue)
        verify(mockListener, times(1))?.onKeyRelease(fakeValue)
    }

    @Test
    fun triggerInvalidKeyTouchEventAndListener_numpadDoesNotReact() {
        // Given that there is a ViewGroup object,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...given that the numpad is constructed with the ViewGroup and...
        numpad = VirtualNumpad(mockViewGroup!!)

        // ...given that a keypad listener is registered with the numpad,...
        numpad?.registerKeypadListener(mockListener!!)

        // ...given that a Key has a value,...
        given(mockKey?.value).willReturn(fakeValue)

        // ...given that that Key generates a touch "move" event,...
        given(mockMotionEvent?.action).willReturn(MotionEvent.ACTION_MOVE)

        // ...when the numpad is notified of the event...
        numpad?.onTouch(mockKey, mockMotionEvent)

        // ...then the message should not have been passed on to the keypad listener.
        verify(mockListener, times(0))?.onKeyPress(fakeValue)
        verify(mockListener, times(0))?.onKeyRelease(fakeValue)
    }

    @Test
    fun triggerValidKeyTouchEventAndNoListener_numpadDoesNotReact() {
        // Given that there is a ViewGroup object,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...given that the numpad is constructed with the ViewGroup and...
        numpad = VirtualNumpad(mockViewGroup!!)

        // ...given that a Key has a value,...
        given(mockKey?.value).willReturn(fakeValue)

        // ...given that that Key generates a touch "move" event,...
        given(mockMotionEvent?.action).willReturn(MotionEvent.ACTION_MOVE)

        // ...when the numpad is notified of the event...
        numpad?.onTouch(mockKey, mockMotionEvent)

        // ...then nothing should happen (and no exception should be thrown).
    }

    @Test
    fun triggerValidKeyTouchEventAndTwoListeners_numpadNotifiesBothListeners() {
        // Given that there is a ViewGroup object,...
        given(mockViewGroup?.childCount).willReturn(0)

        // ...given that the numpad is constructed with the ViewGroup and...
        numpad = VirtualNumpad(mockViewGroup!!)

        // ...given that two keypad listeners are registered with the numpad,...
        numpad?.registerKeypadListener(mockListener!!)

        val mockListener2 = mock(IKeypadListener::class.java)
        numpad?.registerKeypadListener(mockListener2)

        // ...given that a Key has a value,...
        given(mockKey?.value).willReturn(fakeValue)

        // ...given that that Key generates a touch "down" event,...
        given(mockMotionEvent?.action).willReturn(MotionEvent.ACTION_DOWN)

        // ...when the numpad is notified of the event...
        numpad?.onTouch(mockKey, mockMotionEvent)

        // ...then the message should have been passed on to both of the keypad listeners.
        verify(mockListener, times(1))?.onKeyPress(fakeValue)
        verify(mockListener, times(0))?.onKeyRelease(fakeValue)
        verify(mockListener2, times(1)).onKeyPress(fakeValue)
        verify(mockListener2, times(0)).onKeyRelease(fakeValue)
    }
}