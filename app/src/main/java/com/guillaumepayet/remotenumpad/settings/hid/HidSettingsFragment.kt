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
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.guillaumepayet.remotenumpad.R
import com.guillaumepayet.remotenumpad.connection.hid.HidConnectionInterface
import com.guillaumepayet.remotenumpad.settings.AbstractSettingsFragment
import com.guillaumepayet.remotenumpad.settings.BluetoothPermissionRationaleDialogFragment

/**
 * This settings page provides a way to list and select a paired device as the host for a
 * [HidConnectionInterface].
 **/
@Keep
@RequiresApi(Build.VERSION_CODES.P)
class HidSettingsFragment : AbstractSettingsFragment() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        } else {
            val manager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
            manager?.adapter
        }
    }


    private val hostPreference: ListPreference by lazy {
        val preference = findPreference<ListPreference>(getString(R.string.pref_key_hid_host))!!

        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            host = value.toString()
            updateSummary(preference, host)
            true
        }

        preference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED
            ) updateDeviceList()

            true
        }

        preference
    }

    private var pairingManager: HidPairingManager? = null


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setHasOptionsMenu(true)
        setPreferencesFromResource(R.xml.pref_hid, rootKey)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S
            || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED
        ) pairingManager = HidPairingManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_hid_settings, menu)
    }

    override fun onResume() {
        super.onResume()

        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                    || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED
            -> updateDeviceList()
            shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT) ->
                BluetoothPermissionRationaleDialogFragment().show(parentFragmentManager, "HID_PERMISSION")
            else -> {
                // TODO Let the user know that the permission is missing
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_hid_pair -> {
            pairingManager?.openDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        pairingManager?.release()
    }


    /**
     * Updates the [ListPreference] entries for devices.
     */
    @SuppressLint("MissingPermission")
    private fun updateDeviceList() {
        val devices = bluetoothAdapter!!.bondedDevices

        val (entries, entryValues) = if (devices.isEmpty())
            Pair(listOf(getString(R.string.pref_no_host_entry)), listOf(getString(R.string.pref_no_host_entry_value)))
        else
            Pair(devices.map { it.name }, devices.map { it.address })

        hostPreference.entries = entries.toTypedArray()
        hostPreference.entryValues = entryValues.toTypedArray()

        if (hostPreference.value !in entryValues)
            hostPreference.value = entryValues.last()

        hostPreference.callChangeListener(hostPreference.value)
    }
}