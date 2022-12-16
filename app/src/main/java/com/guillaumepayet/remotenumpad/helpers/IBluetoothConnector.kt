package com.guillaumepayet.remotenumpad.helpers

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.guillaumepayet.remotenumpad.AbstractActivity

interface IBluetoothConnector {

    val activity: AbstractActivity

    val userHasDeclinedBluetooth: Boolean

    private val permissionResultCallback
        get() = object : ActivityResultCallback<Boolean> {

            override fun onActivityResult(isGranted: Boolean?) {
                try {
                    activity.isShowingDialog = false
                    activity.unregisterPermissionResultCallback(this)
                } catch (e: Exception) {
                    // There is no activity with which to unregister this so do nothing
                }

                if (isGranted != true)
                    onUserDeclinedBluetooth()
            }
        }

    private val activityResultCallback
        get() = object : ActivityResultCallback<ActivityResult> {

            override fun onActivityResult(result: ActivityResult?) {
                try {
                    activity.isShowingDialog = false
                    activity.unregisterActivityResultCallback(this)
                } catch (e: IllegalStateException) {
                    // There is no Activity with which to unregister this so do nothing
                }

                if (result?.resultCode != RESULT_OK)
                    onUserDeclinedBluetooth()
            }
        }


    fun onUserDeclinedBluetooth()

    fun IBluetoothConnector.runOrRequestPermission(callback: () -> Any?): Any? {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            Manifest.permission.BLUETOOTH_CONNECT
        else
            Manifest.permission.BLUETOOTH

        when {
            ContextCompat.checkSelfPermission(activity, permission) ==
                    PackageManager.PERMISSION_GRANTED ->
                return runOrRequestEnable(callback)
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> {
                if (!activity.isShowingDialog) {
                    BluetoothPermissionRationaleDialogFragment(
                        onDeclined = {
                            activity.isShowingDialog = false
                            onUserDeclinedBluetooth()
                        },
                        onGranted = {
                            activity.registerPermissionResultCallback(permissionResultCallback)
                            activity.requestPermission(permission)
                        }
                    ).show(activity.supportFragmentManager, null)

                    activity.isShowingDialog = true
                }
            }
            !userHasDeclinedBluetooth -> {
                activity.registerPermissionResultCallback(permissionResultCallback)
                activity.requestPermission(permission)
            }
        }

        return null
    }

    private fun runOrRequestEnable(callback: () -> Any?): Any? {
        val context = activity.applicationContext
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        when {
            manager.adapter.isEnabled ->
                return try {
                    callback()
                } catch (e: Exception) {
                    null
                }
            !userHasDeclinedBluetooth -> {
                if (!activity.isShowingDialog) {
                    activity.registerActivityResultCallback(activityResultCallback)
                    activity.startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    activity.isShowingDialog = true
                }
            }
        }

        return null
    }
}