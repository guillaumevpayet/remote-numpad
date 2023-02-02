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
                            requestPermission(permission)
                        }
                    ).show(activity.supportFragmentManager, null)

                    activity.isShowingDialog = true
                }
            }
            !userHasDeclinedBluetooth -> {
                requestPermission(permission)
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

                    try {
                        activity.startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    } catch (e: IllegalStateException) {
                        activity.unregisterActivityResultCallback(activityResultCallback)
                    }

                    activity.isShowingDialog = true
                }
            }
        }

        return null
    }

    private fun requestPermission(permission: String) {
        activity.registerPermissionResultCallback(permissionResultCallback)

        try {
            activity.requestPermission(permission)
        } catch (e: IllegalStateException) {
            activity.unregisterPermissionResultCallback(permissionResultCallback)
        }
    }
}