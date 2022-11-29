package com.guillaumepayet.remotenumpad.settings.hid

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.guillaumepayet.remotenumpad.connection.hid.IHidDeviceListener

/**
 * This class handles the pairing of the device previously selected by the user via the Companion
 * Device Manager. The [HidPairingCallback] class received the device and requested the HID profile
 * proxy. Then, this class gets notified when the app is registered and the device can be paired.
 * [HidPairingStateListener] is then registered as a [BroadcastReceiver] to listen for the
 * completion of the pairing.
 */
class HidPairingDeviceListener(private val device: BluetoothDevice, private val activity: Activity) : IHidDeviceListener {

    private lateinit var broadcastReceiver: BroadcastReceiver

    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onAppRegistered(proxy: BluetoothHidDevice?) {
        broadcastReceiver = HidPairingStateListener(proxy!!, device)
        if (device.createBond()) {
            val intentFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            activity.registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) { }
}