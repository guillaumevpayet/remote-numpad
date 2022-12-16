/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2022 Guillaume Payet
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
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.guillaumepayet.remotenumpad.AbstractActivity
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.hid.HidConnectionInterface
import com.guillaumepayet.remotenumpad.helpers.IBluetoothConnector
import com.guillaumepayet.remotenumpad.settings.AbstractSettingsFragment

/**
 * This settings page provides a way to list and select a paired device as the host for a
 * [HidConnectionInterface].
 **/
@Keep
@RequiresApi(Build.VERSION_CODES.P)
class HidSettingsFragment : AbstractSettingsFragment(), IBluetoothConnector {

    override val activity: AbstractActivity
        get() = requireActivity() as AbstractActivity

    override var userHasDeclinedBluetooth: Boolean = false
        private set


    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        manager?.adapter
    }


    private val hostPreference: ListPreference by lazy {
        val preference = findPreference<ListPreference>(getString(R.string.pref_key_hid_host))!!

        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            host = value.toString()
            updateSummary(preference, host)
            true
        }

        preference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            runOrRequestPermission @SuppressLint("MissingPermission") {
                updateDeviceList()
                true
            } as Boolean? == true
        }

        preference
    }

    private var pairingManager: HidPairingManager? = null


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_hid, rootKey)
        pairingManager = HidPairingManager(this)
    }

    override fun onResume() {
        super.onResume()

        runOrRequestPermission @SuppressLint("MissingPermission") {
            updateDeviceList()
        }
    }

    override fun onDestroy() {
        pairingManager?.release()
        super.onDestroy()
    }


    override fun onUserDeclinedBluetooth() { userHasDeclinedBluetooth = true }


    /**
     * Updates the [ListPreference] entries for devices.
     */
    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun updateDeviceList() {
        val devices = bluetoothAdapter!!.bondedDevices

        val (entries, entryValues) = if (devices.isEmpty())
            Pair(
                listOf(getString(R.string.pref_no_host_entry)),
                listOf(getString(R.string.pref_no_host_entry_value))
            )
        else
            Pair(devices.map { it.name }, devices.map { it.address })

        hostPreference.entries = entries.toTypedArray()
        hostPreference.entryValues = entryValues.toTypedArray()

        if (hostPreference.value !in entryValues)
            hostPreference.value = entryValues.last()

        hostPreference.callChangeListener(hostPreference.value)
    }
}