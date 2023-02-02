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

import android.companion.CompanionDeviceManager
import android.content.IntentSender
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.guillaumepayet.remotenumpad.AbstractActivity
import com.guillaumepayet.remotenumpad.R

/**
 * This class handles the result of the Companion Device Manager. When a user selects a device in
 * the list shown on-screen, this class receives the [IntentSender] and launches an
 * [android.content.Intent].
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidPairingCompanionCallback(private val activity: AbstractActivity, private val pairingLauncher: ActivityResultLauncher<IntentSenderRequest>) : CompanionDeviceManager.Callback() {

    // NOTE: This method is required for older versions of Android
    @Deprecated("Deprecated in Java", ReplaceWith("onAssociationPending(intentSender)"))
    override fun onDeviceFound(intentSender: IntentSender) {
        onAssociationPending(intentSender)
    }

    override fun onAssociationPending(intentSender: IntentSender) {
        try {
            val pairingRequest = IntentSenderRequest.Builder(intentSender).build()
            pairingLauncher.launch(pairingRequest)
        } catch (e: IllegalStateException) {
            // If user closes the activity before the Companion Device Manager returns, ignore
        }
    }

    override fun onFailure(error: CharSequence?) {
        Snackbar.make(
            activity.findViewById(R.id.common_settings),
            R.string.snackbar_scan_failed,
            Snackbar.LENGTH_SHORT)
            .show()
        return
    }
}