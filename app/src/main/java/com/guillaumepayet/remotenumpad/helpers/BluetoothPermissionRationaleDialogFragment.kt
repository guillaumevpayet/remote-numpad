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
