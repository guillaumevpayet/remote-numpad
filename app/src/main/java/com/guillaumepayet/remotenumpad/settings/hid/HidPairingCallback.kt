package com.guillaumepayet.remotenumpad.settings.hid

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.companion.AssociationInfo
import android.companion.CompanionDeviceManager
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.guillaumepayet.remotenumpad.connection.hid.HidServiceListener

/**
 * This class handles the result of the [android.content.Intent] launched by the
 * [HidPairingCompanionCallback] class as part of the Companion Device Manager's selection process.
 * When a device is picked, it will arrive here and the HID profile proxy will be requested to be
 * able to pair the device.
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidPairingCallback(private val activity: Activity) : ActivityResultCallback<ActivityResult> {

    private val context = activity.applicationContext

    private val bluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter


    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onActivityResult(result: ActivityResult?) {
        if (result?.resultCode != Activity.RESULT_OK)
            return

        val device = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            @Suppress("DEPRECATION")
            result.data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
        else {
            val association = result.data?.getParcelableExtra(
                CompanionDeviceManager.EXTRA_ASSOCIATION,
                AssociationInfo::class.java
            )

            bluetoothAdapter.getRemoteDevice(association?.deviceMacAddress.toString())
        }

        val hidPairingDeviceListener = HidPairingDeviceListener(device!!, activity)
        val hidServiceListener = HidServiceListener(context, hidPairingDeviceListener)
        bluetoothAdapter.getProfileProxy(context, hidServiceListener, BluetoothProfile.HID_DEVICE)
    }
}