package com.guillaumepayet.remotenumpad.settings.hid

import android.app.Activity
import android.companion.CompanionDeviceManager
import android.content.IntentSender
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

/**
 * This class handles the result of the Companion Device Manager. When a user selects a device in
 * the list shown on-screen, this class receives the [IntentSender] and launches an
 * [android.content.Intent].
 */
@RequiresApi(Build.VERSION_CODES.P)
class HidPairingCompanionCallback(fragment: Fragment) : CompanionDeviceManager.Callback() {

    private val hidPairingCallback = HidPairingCallback(fragment.activity as Activity)

    private val pairingLauncher: ActivityResultLauncher<IntentSenderRequest> =
        fragment.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
            hidPairingCallback)


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
        // TODO Display a Toast or SnackBar informing the user that the scan failed.
        return
    }
}