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

package com.guillaumepayet.remotenumpad.connection.socket

import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.*
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito.whenNew
import org.powermock.modules.junit4.PowerMockRunner
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.Socket

/**
 * This test suite tests the [SocketConnectionInterface] class.
 * 
 * @see SocketConnectionInterface
 */
@RunWith(PowerMockRunner::class)
class SocketConnectionInterfaceTestSuite {

    companion object {

        private const val INVALID_HOST = "0.0.0.0"
        private const val VALID_HOST = "192.168.1.24"
    }


    private class TestableSocketConnectionInterface(dataSender: IDataSender): SocketConnectionInterface(dataSender) {

        var mockSocket: Socket? = null

        override suspend fun openSocket(host: String, port: Int, timeout: Int): Socket {
            return if (host == INVALID_HOST)
                throw IOException()
            else
                mockSocket!!
        }
    }


    @Mock
    private val mockSocket: Socket? = null

    @Mock
    private val mockDataSender: IDataSender? = null

    @Mock
    private val mockListener: IConnectionStatusListener? = null

    
    @Before
    fun setupMocks() {
        val mockStream = mock(OutputStream::class.java)
        given(mockSocket?.outputStream).willReturn(mockStream)

        val mockWriter = mock(OutputStreamWriter::class.java)
        whenNew(OutputStreamWriter::class.java)
                .withArguments(mockStream, Charsets.UTF_8)
                .thenReturn(mockWriter)
    }


    @Test
    fun creatingConnectionInterfaceWithValidDataSender_ConnectionInterfaceIsRegisteredInDataSender() {
        // Given a valid data sender...

        // ...when the connection interface is created with the data sender...
        val connectionInterface = SocketConnectionInterface(mockDataSender!!)

        // ...then the connection interface registers itself in the data sender.
        then(mockDataSender).should(times(1))?.registerConnectionInterface(connectionInterface)
    }

    @Test
    fun openingConnectionWithInvalidHost_ConnectingThenCouldNotConnect() {
        // Given that a working listener was injected...
        val connectionInterface = createConnectionInterface()

        // ...when the connection interface opens a connection with a valid host...
        runBlocking { connectionInterface.open(INVALID_HOST) }

        // ...then the connection interface attempts to connect but fails.
        val inOrder = inOrder(mockListener)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connecting)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_could_not_connect)
        then(mockSocket).shouldHaveZeroInteractions()
    }

    @Test
    fun openingConnectionWithValidHost_ConnectingThenConnected() {
        // Given that a working listener was injected...
        val connectionInterface = createConnectionInterface()

        // ...when the connection interface opens a connection with a valid host...
        runBlocking { connectionInterface.open(VALID_HOST) }

        // ...then the connection interface connects to the host.
        val inOrder = inOrder(mockListener, mockSocket)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connecting)
        then(mockSocket).should(inOrder, times(1))?.outputStream
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connected)
    }

    @Test
    fun closingConnectionWithNoneOpen_NothingHappens() {
        // Given that a working listener was injected...
        val connectionInterface = createConnectionInterface()

        // ...and no connection is open,

        // ...when the connection interface attempts to close the connection...
        runBlocking { connectionInterface.close() }

        // ...then the connection interface closes no connection.
        val inOrder = inOrder(mockListener)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_disconnecting)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_disconnected)
        then(mockSocket).shouldHaveZeroInteractions()
    }

    @Test
    fun closingConnectionWithValidConnection_DisconnectingThenDisconnected() {
        // Given that a working listener was injected...
        val connectionInterface = createConnectionInterface()

        runBlocking {
            // ...and a valid connection is open...
            connectionInterface.open(VALID_HOST)

            // ...when the connection interface closes the connection...
            connectionInterface.close()
        }

        // ...then the connection interface connects to the host...
        val inOrder = inOrder(mockListener, mockSocket)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connecting)
        then(mockSocket).should(inOrder, times(1))?.outputStream
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connected)

        // ...and disconnects.
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_disconnecting)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_disconnected)
    }


    private fun createConnectionInterface(): SocketConnectionInterface {
        val connectionInterface = TestableSocketConnectionInterface(mockDataSender!!)
        connectionInterface.mockSocket = mockSocket
        connectionInterface.registerConnectionStatusListener(mockListener!!)
        return connectionInterface
    }
}