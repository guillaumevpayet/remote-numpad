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

import android.bluetooth.*
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.*
import android.os.Build
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.MenuProvider
import com.guillaumepayet.remotenumpad.AbstractActivity
import com.guillaumepayet.remotenumpad.R
import java.util.regex.Pattern

/**
 * Manager class to handle the process of pairing a new device ready for the Bluetooth HID profile.
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidPairingManager(fragment: HidSettingsFragment): MenuProvider {

    private val activity = fragment.requireActivity() as AbstractActivity
    private val hidPairingCallback = HidPairingCallback(activity)

    private val pairingLauncher: ActivityResultLauncher<IntentSenderRequest> =
        fragment.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
            hidPairingCallback)

    private val companionDeviceManager =
        fragment.requireActivity().getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) =
        menuInflater.inflate(R.menu.menu_hid_settings, menu)

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
        R.id.action_hid_pair -> {
            val deviceFilter = BluetoothDeviceFilter.Builder()
                .setNamePattern(Pattern.compile("([a-fA-F\\d]:){0}"))
                .build()

            val pairingRequest = AssociationRequest.Builder().addDeviceFilter(deviceFilter).build()

            val companionCallback = HidPairingCompanionCallback(activity, pairingLauncher)
            companionDeviceManager.associate(pairingRequest, companionCallback, null)
            true
        }
        else -> false
    }


    init { activity.addMenuProvider(this) }


    /**
     * Unload the menu provider and other listeners. Method called when the settings activity i
     * exited.
     */
    fun release() {
        activity.removeMenuProvider(this)
        hidPairingCallback.release()
    }
}