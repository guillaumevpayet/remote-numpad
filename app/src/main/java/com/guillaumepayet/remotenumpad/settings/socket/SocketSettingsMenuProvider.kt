package com.guillaumepayet.remotenumpad.settings.socket

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.guillaumepayet.remotenumpad.R

class SocketSettingsMenuProvider(private val socketHostScanner: SocketHostScanner): MenuProvider {

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) =
        menuInflater.inflate(R.menu.menu_socket_settings, menu)

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
        R.id.action_socket_refresh -> {
            socketHostScanner.scan()
            true
        }
        else -> false
    }
}