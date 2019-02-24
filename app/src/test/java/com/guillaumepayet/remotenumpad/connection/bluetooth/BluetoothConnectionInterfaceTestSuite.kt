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

package com.guillaumepayet.remotenumpad.connection.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.IConnectionStatusListener
import com.guillaumepayet.remotenumpad.connection.IDataSender
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.api.mockito.PowerMockito.whenNew
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * This test suite tests the [BluetoothConnectionInterface] class.
 *
 * Created by guillaume on 12/29/17.
 *
 * @see BluetoothConnectionInterface
 */
@RunWith(PowerMockRunner::class)
@PrepareForTest(BluetoothAdapter::class)
class BluetoothConnectionInterfaceTestSuite {

    companion object {

        private const val INVALID_HOST = "00:00:00:00:00:00"
        private const val VALID_HOST = "00:11:22:33:AA:BB"
    }


    @Mock
    private val mockSocket: BluetoothSocket? = null

    @Mock
    private val mockDataSender: IDataSender? = null

    @Mock
    private val mockListener: IConnectionStatusListener? = null


    @Before
    fun setupMocks() {
        val mockAdapter = mock(BluetoothAdapter::class.java)
        mockStatic(BluetoothAdapter::class.java)
        given(BluetoothAdapter.getDefaultAdapter()).willReturn(mockAdapter)

        val mockInvalidDevice = mock(BluetoothDevice::class.java)
        given(mockAdapter?.getRemoteDevice(INVALID_HOST)).willReturn(mockInvalidDevice)
        given(mockInvalidDevice.createRfcommSocketToServiceRecord(any())).willThrow(IOException())

        val mockValidDevice = mock(BluetoothDevice::class.java)
        given(mockAdapter?.getRemoteDevice(VALID_HOST)).willReturn(mockValidDevice)
        given(mockValidDevice?.createRfcommSocketToServiceRecord(any())).willReturn(mockSocket)

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
        val connectionInterface = BluetoothConnectionInterface(mockDataSender!!)

        // ...then the connection interface registers itself in the data sender.
        then(mockDataSender).should(times(1))?.registerConnectionInterface(connectionInterface)
    }

    @Test
    fun openingConnectionWithInvalidHost_ConnectingThenCouldNotConnect() {
        // Given that a working listener was injected...
        val connectionInterface = BluetoothConnectionInterface(mockDataSender!!)
        connectionInterface.registerConnectionStatusListener(mockListener!!)

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
        val connectionInterface = BluetoothConnectionInterface(mockDataSender!!)
        connectionInterface.registerConnectionStatusListener(mockListener!!)

        // ...when the connection interface opens a connection with a valid host...
        runBlocking { connectionInterface.open(VALID_HOST) }

        // ...then the connection interface attempts to connect but fails.
        val inOrder = inOrder(mockListener, mockSocket)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connecting)
        then(mockSocket).should(inOrder, times(1))?.outputStream
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connected)
    }

    @Test
    fun openingConnectionWithValidConnectionOpen_ConnectingThenConnectedThenAlreadyConnected() {
        // Given that a working listener was injected...
        val connectionInterface = BluetoothConnectionInterface(mockDataSender!!)
        connectionInterface.registerConnectionStatusListener(mockListener!!)

        // ...and a valid connection is already open...
        connectionInterface.open(VALID_HOST)

        // ...when the connection interface opens a connection with a valid host...
        connectionInterface.open(VALID_HOST)

        // ...then the connection interface does not attempt a second connection.
        val inOrder = inOrder(mockListener, mockSocket)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connecting)
        then(mockSocket).should(inOrder, times(1))?.outputStream
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connected)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_already_connected)
    }

    @Test
    fun closingConnectionWithNoneOpen_NothingHappens() {
        // Given that a working listener was injected...
        val connectionInterface = BluetoothConnectionInterface(mockDataSender!!)
        connectionInterface.registerConnectionStatusListener(mockListener!!)

        // ...and no connection is open,

        // ...when the connection interface attempts to close the connection...
        connectionInterface.close()

        // ...then the connection interface closes no connection.
        val inOrder = inOrder(mockListener)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_disconnecting)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_disconnected)
        then(mockSocket).shouldHaveZeroInteractions()
    }

    @Test
    fun closingConnectionWithValidConnection_DisconnectingThenDisconnected() {
        // Given that a working listener was injected...
        val connectionInterface = BluetoothConnectionInterface(mockDataSender!!)
        connectionInterface.registerConnectionStatusListener(mockListener!!)

        // ...and a valid connection is open...
        connectionInterface.open(VALID_HOST)

        // ...when the connection interface closes the connection...
        connectionInterface.close()

        // ...then the connection interface connects to the host...
        val inOrder = inOrder(mockListener, mockSocket)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connecting)
        then(mockSocket).should(inOrder, times(1))?.outputStream
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_connected)

        // ...then disconnects.
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_disconnecting)
        then(mockListener).should(inOrder, times(1))?.onConnectionStatusChange(R.string.status_disconnected)
    }
}