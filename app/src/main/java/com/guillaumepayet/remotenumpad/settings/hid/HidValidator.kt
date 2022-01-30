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

package com.guillaumepayet.remotenumpad.settings.hid

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import com.guillaumepayet.remotenumpad.connection.hid.HidConnectionInterface
import com.guillaumepayet.remotenumpad.settings.IConnectionInterfaceValidator

/**
 * Validator used to check whether a [BluetoothAdapter] is available and the Android version is at
 * least Android P (API 28) in which case the [HidConnectionInterface] can be used.
 */
@Keep
class HidValidator(val context: Context) : IConnectionInterfaceValidator {

    override val isInterfaceAvailable: Boolean
        get() {
            val adapter = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                @Suppress("DEPRECATION")
                BluetoothAdapter.getDefaultAdapter()
            } else {
                val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                manager.adapter
            }

            return  adapter != null &&
                    adapter.isEnabled &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        }
}