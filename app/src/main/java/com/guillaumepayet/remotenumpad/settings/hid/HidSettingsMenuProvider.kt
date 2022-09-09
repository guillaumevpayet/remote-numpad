package com.guillaumepayet.remotenumpad.settings.hid

import android.os.Build
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.core.view.MenuProvider
import com.guillaumepayet.remotenumpad.R

@RequiresApi(Build.VERSION_CODES.P)
class HidSettingsMenuProvider(private val pairingManager: HidPairingManager?): MenuProvider {

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) =
        menuInflater.inflate(R.menu.menu_hid_settings, menu)

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
        R.id.action_hid_pair -> {
            pairingManager?.openDialog()
            true
        }
        else -> false
    }
}