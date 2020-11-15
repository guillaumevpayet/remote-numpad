package com.guillaumepayet.remotenumpad.settings.hid

import android.bluetooth.BluetoothAdapter
import android.os.Build
import com.guillaumepayet.remotenumpad.settings.IConnectionInterfaceValidator

class HidValidator : IConnectionInterfaceValidator {

    override val isInterfaceAvailable: Boolean
        get() {
            val adapter = BluetoothAdapter.getDefaultAdapter()

            return  adapter != null &&
                    adapter.isEnabled &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        }


}