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
import android.content.IntentFilter
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
import com.guillaumepayet.remotenumpad.connection.hid.IHidDeviceListener
import com.guillaumepayet.remotenumpad.helpers.IBluetoothConnector

/**
 * This class handles the pairing of the device previously selected by the user via the Companion
 * Device Manager. The [HidPairingCallback] class received the device and requested the HID profile
 * proxy. Then, this class gets notified when the app is registered and the device can be paired.
 * [HidPairingStateListener] is then registered as a [BroadcastReceiver] to listen for the
 * completion of the pairing.
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidPairingDeviceListener(override val activity: AbstractActivity) : IHidDeviceListener, IBluetoothConnector {

    override var userHasDeclinedBluetooth: Boolean = false
        private set

    lateinit var device: BluetoothDevice

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var progressBar: ProgressBar
    private lateinit var proxy: BluetoothHidDevice
    private lateinit var broadcastReceiver: HidPairingStateListener

    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onAppRegistered(proxy: BluetoothHidDevice?) {
        runOrRequestPermission @SuppressLint("MissingPermission") {
            handler.post {
                progressBar = activity.findViewById(R.id.progress_bar)
                this.proxy = proxy!!
                broadcastReceiver = HidPairingStateListener(activity, proxy, device)

                if (proxy.connect(device)) {
                    val intentFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                    activity.registerReceiver(broadcastReceiver, intentFilter)
                    progressBar.isVisible = true

                    Snackbar.make(
                        progressBar,
                        R.string.snackbar_pairing_in_progress,
                        Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
        runOrRequestPermission @SuppressLint("MissingPermission") {
            handler.post {
                if (device == this.device) {
                    when (state) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            proxy.disconnect(device)

                            if (this::progressBar.isInitialized) {
                                Snackbar.make(
                                    progressBar,
                                    R.string.snackbar_pairing_in_postprogress,
                                    Snackbar.LENGTH_LONG)
                                    .show()
                            }
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                                proxy.unregisterApp()

                                if (this::progressBar.isInitialized) {
                                    progressBar.isVisible = false

                                    Snackbar.make(
                                        progressBar,
                                        R.string.snackbar_pairing_successful,
                                        Snackbar.LENGTH_LONG)
                                        .show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onUserDeclinedBluetooth() { userHasDeclinedBluetooth = true }

    /**
     * Unregister the HID app. Method called when the settings activity is exited.
     */
    fun release() {
        runOrRequestPermission @SuppressLint("MissingPermission") {
            if (this::proxy.isInitialized)
                proxy.unregisterApp()
        }

        if (this::progressBar.isInitialized) {
            progressBar.isVisible = false

            Snackbar.make(
                progressBar,
                R.string.snackbar_pairing_interrupted,
                Snackbar.LENGTH_LONG)
                .show()
        }
    }
}