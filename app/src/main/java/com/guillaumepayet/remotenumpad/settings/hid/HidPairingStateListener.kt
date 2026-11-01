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

package com.guillaumepayet.remotenumpad.settings.hid

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.guillaumepayet.remotenumpad.AbstractActivity
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.helpers.IBluetoothConnector

/**
 * This class listens to the pairing process of the device previously selected via the Companion
 * Device Manager.
 * The HID profile proxy was set up and used during the pairing process and when this class receives
 * a notification of a successful pairing, the profile proxy is released and the HID app is
 * unregistered.
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidPairingStateListener(override val activity: AbstractActivity, private val proxy: BluetoothHidDevice, private val device: BluetoothDevice) : BroadcastReceiver(), IBluetoothConnector {

    override var userHasDeclinedBluetooth = false
        private set

    private val handler = Handler(Looper.getMainLooper())
    private val progressBar: ProgressBar? = activity.findViewById(R.id.progress_bar)
    private var timeoutRunnable: Runnable? = null

    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onReceive(context: Context?, intent: Intent?) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent?.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java)
        else
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    as BluetoothDevice?

        if (device != this.device)
            return

        val state = intent?.getIntExtra(
            BluetoothDevice.EXTRA_BOND_STATE,
            BluetoothDevice.BOND_NONE)

        if (state != BluetoothDevice.BOND_BONDED)
            return

        try {
            activity.unregisterReceiver(this)
        } catch (_: IllegalArgumentException) {
            // Receiver already unregistered
        }

        val connState = proxy.getConnectionState(device)
        if (connState == BluetoothProfile.STATE_CONNECTED || connState == BluetoothProfile.STATE_CONNECTING) {
            runOrRequestPermission @SuppressLint("MissingPermission") {
                if (!proxy.disconnect(device)) {
                    proxy.unregisterApp()
                    progressBar?.isVisible = false

                    if (progressBar != null) {
                        Snackbar.make(
                            progressBar,
                            R.string.snackbar_pairing_failed,
                            Snackbar.LENGTH_LONG)
                            .show()
                    }
                } else {
                    if (progressBar != null) {
                        Snackbar.make(
                            progressBar,
                            R.string.snackbar_pairing_in_postprogress,
                            Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }
        } else if (connState == BluetoothProfile.STATE_DISCONNECTED) {
            runOrRequestPermission @SuppressLint("MissingPermission") {
                proxy.unregisterApp()
            }
            if (progressBar != null) {
                progressBar.isVisible = false
                Snackbar.make(
                    progressBar,
                    R.string.snackbar_pairing_successful,
                    Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    fun cancelTimeout() {
        timeoutRunnable?.let { handler.removeCallbacks(it) }
        timeoutRunnable = null
    }

    override fun onUserDeclinedBluetooth() { userHasDeclinedBluetooth = true }
}
