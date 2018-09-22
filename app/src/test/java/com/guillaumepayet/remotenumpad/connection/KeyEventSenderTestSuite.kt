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

package com.guillaumepayet.remotenumpad.connection

import com.guillaumepayet.remotenumpad.controller.IKeypad
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

/**
 * Test suite for testing the [KeyEventSender] class.
 *
 * Created by guillaume on 12/28/17.
 *
 * @see KeyEventSender
 */
@RunWith(MockitoJUnitRunner::class)
class KeyEventSenderTestSuite {

    private val fakeValue = "test"

    @Mock
    private val mockKeypad: IKeypad? = null

    @Mock
    private val mockConnectionInterface: IConnectionInterface? = null

    private var keyEventSender: KeyEventSender? = null


    @Test
    fun contructKeyEventSenderWithValidKeypad_keyEventSenderRegisteredInKeypad() {
        // When the keyEventSender is created with a non-null keypad...
        keyEventSender = KeyEventSender(mockKeypad!!)

        // ...then the keyEventSender should register itself in the keypad.
        verify(mockKeypad, times(1))?.registerKeypadListener(keyEventSender!!)
    }

    @Test
    fun sendKeyPressEventWithConnectionInterface_senderSendsTheEventToTheConnectionInterface() {
        // Given that the sender has a valid connection interface registered,...
        keyEventSender = KeyEventSender(mockKeypad!!)
        keyEventSender?.registerConnectionInterface(mockConnectionInterface!!)

        // ...when an event is to be sent...
        keyEventSender?.onKeyPress(fakeValue)

        // ...then the event is sent through the connection interface.
        verify(mockConnectionInterface, times(1))?.sendString("+$fakeValue\n")
    }

    @Test
    fun sendKeyReleaseEventWithConnectionInterface_senderSendsTheEventToTheConnectionInterface() {
        // Given that the sender has a valid connection interface registered,...
        keyEventSender = KeyEventSender(mockKeypad!!)
        keyEventSender?.registerConnectionInterface(mockConnectionInterface!!)

        // ...when an event is to be sent...
        keyEventSender?.onKeyRelease(fakeValue)

        // ...then the event is sent through the connection interface.
        verify(mockConnectionInterface, times(1))?.sendString("-$fakeValue\n")
    }
}