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

package com.guillaumepayet.remotenumpad.connection.hid

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice

/**
 * This interface is used to receive feedback on the Bluetooth HID connection process. Using this
 * interface, objects can be notified when the Bluetooth HID app is registered and ready to be used
 * (e.g.: for pairing or connecting devices). Objects implementing this interface will also be
 * notified on device connection state changes.
 *
 * @see HidServiceListener
 */
interface IHidDeviceListener {

    /**
     * Event triggered when the HID profile is enabled and the app has been successfully registered.
     *
     * @param proxy the HID profile proxy to use for this session, already used to register the app
     */
    fun onAppRegistered(proxy: BluetoothHidDevice?)

    /**
     * Event triggered when connection state changes on a device connected via the HID profile.
     *
     * @param device the device for which connection state has changed
     * @param state the new connection state of the device
     *
     * @see BluetoothHidDevice.Callback.onConnectionStateChanged
     */
    fun onConnectionStateChanged(device: BluetoothDevice, state: Int)
}