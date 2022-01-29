package com.guillaumepayet.remotenumpad.settings

import android.Manifest
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

@RequiresApi(Build.VERSION_CODES.S)
class BluetoothPermissionRationaleDialogFragment : DialogFragment() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<String>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.i("BPRDF", "created")

        val contract = ActivityResultContracts.RequestPermission()
        activityResultLauncher = registerForActivityResult(contract) {
            Log.i("BPRDF", "received result")
        }

        return activity?.let {
            AlertDialog.Builder(it)
                .setMessage("The permission to detect and connect to nearby devices is " +
                    "required in order to use the Bluetooth-based interfaces.")
                .setPositiveButton("Go to permission") { _, _ ->
                    activityResultLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
                .setNegativeButton("Deny") { _, _ -> }
                .create()
        } ?: throw IllegalStateException("Activity was not found")
    }
}