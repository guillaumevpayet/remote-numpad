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

/**
 * Interface for [AbstractConnectionTask] objects factory.
 * These factories should be injected into and used by a [IConnectionInterface] object.
 *
 * Created by guillaume on 1/2/18.
 *
 * @see AbstractConnectionTask
 * @see IConnectionInterface
 */
interface IConnectionTaskFactory {

    fun createConnectTask(connectionInterface: IConnectionInterface) : AbstractConnectionTask
    fun createDisconnectTask(connectionInterface: IConnectionInterface) : AbstractConnectionTask
    fun createSendTask(connectionInterface: IConnectionInterface) : AbstractConnectionTask
}