package com.guillaumepayet.remotenumpad.settings.hid

import android.os.Bundle
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.settings.BasePreferenceFragment

class HidPreferenceFragment : BasePreferenceFragment() {

    override val host: String
        get() = "VANAHEIM"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.pref_hid)

//        hostPreference = findPreference(getString(R.string.pref_key_bluetooth_host))!!
//
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
//            disableBluetooth()
//        } else {
//            val devices = bluetoothAdapter.bondedDevices
//
//            val (entries, entryValues) = if (devices.isEmpty()) {
//                Pair(listOf(getString(R.string.pref_no_host_entry)), listOf(getString(R.string.pref_no_host_entry_value)))
//            } else {
//                Pair(devices.map { it.name }, devices.map { it.address })
//            }
//
//            hostPreference.entries = entries.toTypedArray()
//            hostPreference.entryValues = entryValues.toTypedArray()
//
//            if (!entryValues.contains(hostPreference.value))
//                hostPreference.value = entryValues[0]
//        }
//
//        bindPreferenceSummaryToValue(hostPreference)
    }
}