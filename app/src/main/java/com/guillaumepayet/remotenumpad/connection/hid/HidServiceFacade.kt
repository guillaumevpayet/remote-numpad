package com.guillaumepayet.remotenumpad.connection.hid

import android.bluetooth.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.guillaumepayet.remotenumpad.NumpadActivity

@RequiresApi(Build.VERSION_CODES.P)
object HidServiceFacade : BluetoothHidDevice.Callback() {

    private const val TAG = "HidServiceFacade"

    private val KEYBOARD_DESCRIPTOR = byteArrayOf(
            0x05.toByte(), 0x01.toByte(),       // Usage Page (Generic Desktop)
            0x09.toByte(), 0x06.toByte(),       // Usage (Keyboard)
            0xA1.toByte(), 0x01.toByte(),       // Collection (Application)
            0x85.toByte(), 0x08.toByte(),       //     REPORT_ID (Keyboard)
            0x05.toByte(), 0x07.toByte(),       //     Usage Page (Key Codes)
            0x19.toByte(), 0xe0.toByte(),       //     Usage Minimum (224)
            0x29.toByte(), 0xe7.toByte(),       //     Usage Maximum (231)
            0x15.toByte(), 0x00.toByte(),       //     Logical Minimum (0)
            0x25.toByte(), 0x01.toByte(),       //     Logical Maximum (1)
            0x75.toByte(), 0x01.toByte(),       //     Report Size (1)
            0x95.toByte(), 0x08.toByte(),       //     Report Count (8)
            0x81.toByte(), 0x02.toByte(),       //     Input (Data, Variable, Absolute)

            0x95.toByte(), 0x01.toByte(),       //     Report Count (1)
            0x75.toByte(), 0x08.toByte(),       //     Report Size (8)
            0x81.toByte(), 0x01.toByte(),       //     Input (Constant) reserved byte(1)

            0x95.toByte(), 0x01.toByte(),       //     Report Count (1)
            0x75.toByte(), 0x08.toByte(),       //     Report Size (8)
            0x15.toByte(), 0x00.toByte(),       //     Logical Minimum (0)
            0x25.toByte(), 0x65.toByte(),       //     Logical Maximum (101)
            0x05.toByte(), 0x07.toByte(),       //     Usage Page (Key codes)
            0x19.toByte(), 0x00.toByte(),       //     Usage Minimum (0)
            0x29.toByte(), 0x65.toByte(),       //     Usage Maximum (101)
            0x81.toByte(), 0x00.toByte(),       //     Input (Data, Array) Key array(6 bytes)
            0xc0.toByte()                       // End Collection (Application)
    )

    private val SDP: BluetoothHidDeviceAppSdpSettings =
            BluetoothHidDeviceAppSdpSettings(
                    "Remote Numpad",
                    "Remote Numpad",
                    "Guillaume Payet",
                    BluetoothHidDevice.SUBCLASS1_KEYBOARD,
                    KEYBOARD_DESCRIPTOR
            )

    private val SERVICE_LISTENER = object: BluetoothProfile.ServiceListener {

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            service = proxy as BluetoothHidDevice
            service.registerApp(SDP, null, null, { it.run() }, HidServiceFacade)
        }

        override fun onServiceDisconnected(profile: Int) {
            Log.i(TAG, "onServiceDisconnected($profile)")
            service.unregisterApp()
        }
    }

    lateinit var service: BluetoothHidDevice
        private set

    private val bluetoothAdapter: BluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val listeners: MutableCollection<BluetoothHidDevice.Callback> = HashSet()
    private var lastAppStatus: Pair<BluetoothDevice?, Boolean>? = null


    init {
        bluetoothAdapter.getProfileProxy(
                NumpadActivity.context,
                SERVICE_LISTENER,
                BluetoothProfile.HID_DEVICE)
    }

    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
        super.onAppStatusChanged(pluggedDevice, registered)
        Log.i(TAG, "onAppStatusChanged(<${pluggedDevice?.name}>, $registered)")
        listeners.forEach { it.onAppStatusChanged(pluggedDevice, registered) }
        lastAppStatus = Pair(pluggedDevice, registered)
    }

    override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
        super.onConnectionStateChanged(device, state)
        Log.i(TAG, "onConnectionStateChanged(<${device?.name}>, $state)")
        listeners.forEach { it.onConnectionStateChanged(device, state) }
    }


    fun registerHidDeviceListener(listener: BluetoothHidDevice.Callback) {
        listeners.add(listener)

        if (lastAppStatus != null)
            listener.onAppStatusChanged(lastAppStatus!!.first, lastAppStatus!!.second)
    }

    fun unregisterHidDeviceListener(listener: BluetoothHidDevice.Callback) =
        listeners.remove(listener)
}