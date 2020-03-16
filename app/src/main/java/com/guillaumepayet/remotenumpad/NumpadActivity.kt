/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2018 Guillaume Payet
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

package com.guillaumepayet.remotenumpad

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.preference.PreferenceManager
import com.guillaumepayet.remotenumpad.connection.*
import com.guillaumepayet.remotenumpad.controller.VirtualNumpad
import kotlinx.android.synthetic.main.activity_numpad.*
import kotlinx.android.synthetic.main.content_numpad.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

/**
 * The app's main activity.
 * This class sets up the UI, instantiates the [IDataSender] and the correct [IConnectionInterface]
 * by reading the shared preferences. The status text is also updated by this activity through the
 * [IConnectionStatusListener] interface.
 */
class NumpadActivity : AppCompatActivity(), View.OnClickListener, IConnectionStatusListener {

    companion object {

        /**
         * The package where all the [IConnectionInterface] implementations can be found.
         */
        private val CONNECTION_INTERFACES_PACKAGE = this::class.java.`package`?.name + ".connection"
    }


    private lateinit var keyEventSender: IDataSender
    private lateinit var preferences: SharedPreferences

    private var connectionInterface: IConnectionInterface? = null
    private var task: TimerTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_numpad)
        setSupportActionBar(toolbar)

        connect_button.setOnClickListener(this)
        disconnect_button.setOnClickListener(this)

        val numpad = VirtualNumpad(numpad_keys)
        keyEventSender = KeyEventSender(numpad)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onStart() {
        super.onStart()

        if (preferences.getBoolean(getString(R.string.pref_key_backspace), false)) {
            key_numlock.visibility = View.INVISIBLE
            key_backspace.visibility = View.VISIBLE
        } else {
            key_numlock.visibility = View.VISIBLE
            key_backspace.visibility = View.INVISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_numpad, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        disconnect()
    }


    override fun onClick(view: View?) {
        when (view) {
            connect_button -> connect()
            disconnect_button -> disconnect()
        }
    }


    override fun onConnectionStatusChange(connectionStatus: Int) {
        task?.cancel()
        task = null

        val colorId = when (connectionStatus) {
            R.string.status_disconnected -> R.color.disconnected
            R.string.status_connecting, R.string.status_disconnecting -> R.color.working
            R.string.status_connection_lost, R.string.status_could_not_connect -> R.color.failed
            else -> R.color.connected
        }

        val color = ContextCompat.getColor(this, colorId)

        GlobalScope.launch(Dispatchers.Main) {
            status_text.text = getString(connectionStatus)
            status_text.setTextColor(color)
        }

        if (connectionStatus == R.string.status_could_not_connect)
            task = Timer().schedule(2000) {
                disconnect()
            }
    }


    /**
     * Attempts to connect to the host address stored in the shared preferences using the selected
     * [IConnectionInterface]. Closes any open connections before attempting a new connection.
     */
    private fun connect() {
        disconnect()

        val host = preferences.getString(getString(R.string.pref_key_host), getString(R.string.pref_no_host_entry_value))!!

        if (host == getString(R.string.pref_no_host_entry_value)) {
            Snackbar.make(status_text, getString(R.string.snackbar_no_host_selected), Snackbar.LENGTH_SHORT).show()
            return
        }

        val connectionInterfaceName = preferences.getString(getString(R.string.pref_key_connection_interface), getString(R.string.pref_socket_entry_value))
        val packageName = "$CONNECTION_INTERFACES_PACKAGE.$connectionInterfaceName"
        val prefix = "$packageName.${connectionInterfaceName?.capitalize()}"

        val validatorClass = Class.forName("${prefix}HostValidator")
        val validator = validatorClass.newInstance() as IHostValidator

        if (!validator.isHostValid(host)) {
            Snackbar.make(status_text, getString(R.string.snackbar_invalid_host), Snackbar.LENGTH_SHORT).show()
            return
        }

        connectionInterface = try {
            val clazz = Class.forName("${prefix}ConnectionInterface")
            val constructor = clazz.getConstructor(IDataSender::class.java)
            constructor.newInstance(keyEventSender) as IConnectionInterface
        } catch (e: Exception) {
            Snackbar.make(status_text, getString(R.string.snackbar_invalid_connection_interface), Snackbar.LENGTH_SHORT).show()
            null
        }

        connectionInterface?.registerConnectionStatusListener(this)
        GlobalScope.launch { connectionInterface?.open(host) }
    }

    /**
     * Closes an open connection.
     */
    private fun disconnect() {
        GlobalScope.launch {
            connectionInterface?.close()
            connectionInterface = null
        }
    }
}
