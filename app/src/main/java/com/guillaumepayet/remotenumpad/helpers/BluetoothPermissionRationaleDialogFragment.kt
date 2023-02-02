/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2023 Guillaume Payet
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

package com.guillaumepayet.remotenumpad.helpers

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class BluetoothPermissionRationaleDialogFragment(private val onDeclined: () -> Unit, private val onGranted: () -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(
                "" +
                        "In order to use the Bluetooth and Bluetooth (with server) connections, the " +
                        "permission to detect, connect to and derive location from nearby devices is " +
                        "required.\n" +
                        "Rest assured that no location information is derived and the permission is " +
                        "only necessary to connect to a Bluetooth device."
            )
            .setPositiveButton("Go to permission") { _, _ -> onGranted() }
            .setNegativeButton("Deny") { _, _ -> onDeclined() }
            .create()
}
