package com.guillaumepayet.remotenumpad.settings.hid

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission

/**
 * This class listens to the pairing process of the device previously selected via the Companion
 * Device Manager.
 * The HID profile proxy was set up and used during the pairing process and when this class receives
 * a notification of a successful pairing, the profile proxy is released and the HID app is
 * unregistered.
 */
class HidPairingStateListener(private val proxy: BluetoothHidDevice, private val device: BluetoothDevice) : BroadcastReceiver() {

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

        val state = intent?.getIntExtra(
            BluetoothDevice.EXTRA_BOND_STATE,
            BluetoothDevice.BOND_NONE)

        if (device == this.device && state == BluetoothDevice.BOND_BONDED)
            proxy.unregisterApp()
    }
}