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

package com.guillaumepayet.remotenumpad.connection.socket

import android.os.AsyncTask
import com.guillaumepayet.remotenumpad.connection.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.api.support.membermodification.MemberMatcher.*
import org.powermock.api.support.membermodification.MemberModifier.suppress

/**
 * This test suite tests the [SocketConnectionInterface] class.
 * 
 * Created by guillaume on 12/29/17.
 * 
 * @see SocketConnectionInterface
 */
@RunWith(MockitoJUnitRunner::class)
class SocketConnectionInterfaceTestSuite {

    private val invalidHost = "0.0.0.0"
    private val validHost = "192.168.1.24"

    @Mock
    private val mockDataSender: IDataSender? = null

    @Mock
    private val mockTaskFactory: IConnectionTaskFactory? = null

    @Mock
    private val mockTask: AbstractConnectionTask? = null

    
    @Before
    fun setupMocks() {
        suppress(constructor(AsyncTask::class.java))
    }


    @Test
    fun creatingConnectionInterfaceWithValidDataSender_ConnectionInterfaceIsRegisteredInDataSender() {
        // Given a valid data sender...

        // ...when the connection interface is created with the data sender...
        val connectionInterface = SocketConnectionInterface(mockDataSender!!, mockTaskFactory!!)

        // ...then the connection interface registers itself in the data sender.
        verify(mockDataSender, times(1))?.registerConnectionInterface(connectionInterface)
    }

    @Test
    fun openingConnectionWithInvalidHost_ConnectingThenCouldNotConnect() {
        // Given that a working task factory was injected...
        val connectionInterface = SocketConnectionInterface(mockDataSender!!, mockTaskFactory!!)
        `when`(mockTaskFactory.createConnectTask(connectionInterface)).thenReturn(mockTask)

        // ...when the connection interface opens a connection with a valid host...
        connectionInterface.open(invalidHost)

        // ...then the connection interface creates and executes a connect task.
        verify(mockTaskFactory, times(1)).createConnectTask(connectionInterface)
        verify(mockTask, times(1))?.execute(invalidHost)
    }

    @Test
    fun openingConnectionWithValidHost_ConnectingThenConnected() {
        // Given that a working task factory was injected...
        val connectionInterface = SocketConnectionInterface(mockDataSender!!, mockTaskFactory!!)
        `when`(mockTaskFactory.createConnectTask(connectionInterface)).thenReturn(mockTask)

        // ...when the connection interface opens a connection with a valid host...
        connectionInterface.open(validHost)

        // ...then the connection interface creates and executes a connect task.
        verify(mockTaskFactory, times(1)).createConnectTask(connectionInterface)
        verify(mockTask, times(1))?.execute(validHost)
    }

    @Test
    fun openingConnectionWithValidConnectionOpen_ConnectingThenConnectedThenAlreadyConnected() {
        // Given that a working task factory was injected...
        val connectionInterface = SocketConnectionInterface(mockDataSender!!, mockTaskFactory!!)
        `when`(mockTaskFactory.createConnectTask(connectionInterface)).thenReturn(mockTask)

        // ...and a valid connection is already open...
        connectionInterface.open(validHost)

        // ...when the connection interface opens a connection with a valid host...
        connectionInterface.open(validHost)

        // TODO May need to rethink this

        // ...then the connection interface creates and executes 2 connect tasks.
        verify(mockTaskFactory, times(2)).createConnectTask(connectionInterface)
        verify(mockTask, times(2))?.execute(validHost)
    }

    @Test
    fun closingConnectionWithNoneOpen_NothingHappens() {
        // Given that a working task factory was injected...
        val connectionInterface = SocketConnectionInterface(mockDataSender!!, mockTaskFactory!!)
        `when`(mockTaskFactory.createDisconnectTask(connectionInterface)).thenReturn(mockTask)

        // ...and no connection is open,

        // ...when the connection interface attempts to close the connection...
        connectionInterface.close()

        // ...then the connection interface creates and executes a disconnect task.
        verify(mockTaskFactory, times(1)).createDisconnectTask(connectionInterface)
        verify(mockTask, times(1))?.execute()
    }

    @Test
    fun closingConnectionWithValidConnection_DisconnectingThenDisconnected() {
        // Given that a working task factory was injected...
        val connectionInterface = SocketConnectionInterface(mockDataSender!!, mockTaskFactory!!)
        `when`(mockTaskFactory.createConnectTask(connectionInterface)).thenReturn(mockTask)
        `when`(mockTaskFactory.createDisconnectTask(connectionInterface)).thenReturn(mockTask)

        // ...and a valid connection is open...
        connectionInterface.open(validHost)

        // ...when the connection interface closes the connection...
        connectionInterface.close()

        // ...then the connection interface creates and executes a connect task...
        verify(mockTaskFactory, times(1)).createConnectTask(connectionInterface)
        verify(mockTask, times(1))?.execute(validHost)

        // ...and a disconnect task.
        verify(mockTaskFactory, times(1)).createDisconnectTask(connectionInterface)
        verify(mockTask, times(1))?.execute()
    }
}