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
import androidx.annotation.RequiresPermission
import com.guillaumepayet.remotenumpad.AbstractActivity
import com.guillaumepayet.remotenumpad.helpers.IBluetoothConnector

/**
 * This class listens to the pairing process of the device previously selected via the Companion
 * Device Manager.
 * The HID profile proxy was set up and used during the pairing process and when this class receives
 * a notification of a successful pairing, the profile proxy is released and the HID app is
 * unregistered.
 */
class HidPairingStateListener(override val activity: AbstractActivity, private val proxy: BluetoothHidDevice, private val device: BluetoothDevice) : BroadcastReceiver(), IBluetoothConnector {

    override var userHasDeclinedBluetooth = false
        private set

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

        activity.unregisterReceiver(this)

        if (proxy.getConnectionState(device) == BluetoothProfile.STATE_CONNECTING) {
            runOrRequestPermission @SuppressLint("MissingPermission") {
                if (!proxy.disconnect(device))
                    proxy.unregisterApp()
            }
        }
    }

    override fun onUserDeclinedBluetooth() { userHasDeclinedBluetooth = true }
}
