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

package com.guillaumepayet.remotenumpad

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import com.google.android.material.color.MaterialColors
import com.guillaumepayet.remotenumpad.connection.*
import com.guillaumepayet.remotenumpad.controller.LeftyNumpadFragment
import com.guillaumepayet.remotenumpad.controller.NumpadFragment
import com.guillaumepayet.remotenumpad.controller.VirtualNumpad
import com.guillaumepayet.remotenumpad.databinding.ActivityNumpadBinding
import com.guillaumepayet.remotenumpad.databinding.ContentNumpadBinding
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule

/**
 * The app's main activity.
 * This class sets up the UI, instantiates the [IDataSender] and the correct [IConnectionInterface]
 * by reading the shared preferences. The status text is also updated by this activity through the
 * [IConnectionStatusListener] interface.
 */
class NumpadActivity : AbstractActivity(), View.OnClickListener, IConnectionStatusListener {

    companion object {

        /**
         * The alpha value of unchecked icons in the menu.
         */
        private const val UNCHECKED_ICON_ALPHA = 80

        private const val COULD_NOT_CONNECT_DISPLAY_TIME = 2000L

        /**
         * The package where all the [IConnectionInterface] implementations can be found.
         */
        private val CONNECTION_INTERFACES_PACKAGE = this::class.java.`package`?.name + ".connection"


        /**
         * Sets the application's night mode based on the preferences.
         *
         * @param context The context of the activity or fragment which will handle the switch
         * @param nightModeString The string from the preferences
         */
        fun setNightMode(context: Context, nightModeString: String?) {
            val nightMode = when (nightModeString) {
                context.getString(R.string.pref_light_mode_entry_value) -> AppCompatDelegate.MODE_NIGHT_NO
                context.getString(R.string.pref_dark_mode_entry_value) -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

            if (AppCompatDelegate.getDefaultNightMode() != nightMode)
                AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }


    private lateinit var activityBinding: ActivityNumpadBinding
    private lateinit var contentBinding: ContentNumpadBinding
    private lateinit var numpadFragment: NumpadFragment
    private lateinit var numpad: VirtualNumpad
    private lateinit var keyEventSender: KeyEventSender
    private lateinit var preferences: SharedPreferences

    private var connectionInterface: IConnectionInterface? = null
    private var task: TimerTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityNumpadBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        setSupportActionBar(activityBinding.toolbar)

        contentBinding = activityBinding.content
        contentBinding.connectButton.setOnClickListener(this)

        numpadFragment = contentBinding.numpadFragment.getFragment()
        numpad = VirtualNumpad(baseContext)
        numpadFragment.registerVirtualNumpad(numpad)
        keyEventSender = KeyEventSender(numpad)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (preferences.getBoolean(getString(R.string.pref_key_nosleep), false))
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStart() {
        super.onStart()
        numpadFragment.onBackspaceChanged()
        setNightMode(baseContext, preferences.getString(getString(R.string.pref_key_theme), getString(R.string.pref_system_theme_mode_entry_value)))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_numpad, menu)

        val backspaceAction = menu.findItem(R.id.action_backspace)
        val vibrationsAction = menu.findItem(R.id.action_vibrations)
        val noSleepAction = menu.findItem(R.id.action_nosleep)
        val leftyAction = menu.findItem(R.id.action_lefty)

        if (preferences.getBoolean(getString(R.string.pref_key_backspace), false))
            backspaceAction.isChecked = true
        else
            backspaceAction.icon?.alpha = UNCHECKED_ICON_ALPHA

        if (preferences.getBoolean(getString(R.string.pref_key_vibrations), true))
            vibrationsAction.isChecked = true
        else
            vibrationsAction.icon?.alpha = UNCHECKED_ICON_ALPHA

        if (preferences.getBoolean(getString(R.string.pref_key_nosleep), false))
            noSleepAction.isChecked = true
        else
            noSleepAction.icon?.alpha = UNCHECKED_ICON_ALPHA

        if (preferences.getBoolean(getString(R.string.pref_key_lefty), false)) {
            leftyAction.isChecked = true

            if (numpadFragment !is LeftyNumpadFragment)
                replaceNumpadFragment(true)
        } else
            leftyAction.icon?.alpha = UNCHECKED_ICON_ALPHA

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val wasConsumed = when (item.itemId) {
            R.id.action_backspace -> {
                preferences.edit()
                    .putBoolean(getString(R.string.pref_key_backspace), !item.isChecked)
                    .apply()

                numpadFragment.onBackspaceChanged()
                true
            }
            R.id.action_vibrations -> {
                preferences.edit()
                    .putBoolean(getString(R.string.pref_key_vibrations), !item.isChecked)
                    .apply()

                true
            }
            R.id.action_nosleep -> {
                preferences.edit()
                    .putBoolean(getString(R.string.pref_key_nosleep), !item.isChecked)
                    .apply()

                true
            }
            R.id.action_lefty -> {
                preferences.edit()
                    .putBoolean(getString(R.string.pref_key_lefty), !item.isChecked)
                    .apply()

                replaceNumpadFragment(!item.isChecked)
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> false
        }

        if (wasConsumed && item.isCheckable) {
            item.isChecked = !item.isChecked
            item.icon?.alpha = if (item.isChecked) 255 else UNCHECKED_ICON_ALPHA
        }

        return wasConsumed || super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        disconnect()
    }


    override fun onClick(view: View?) {
        when (contentBinding.connectButton.text) {
            getString(R.string.button_connect) -> connect()
            getString(R.string.button_disconnect) -> disconnect()
        }
    }


    override fun onConnectionStatusChange(connectionStatus: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            contentBinding.statusText.maxLines = 1

            val colorId = when (connectionStatus) {
                R.string.status_disconnected -> {
                    contentBinding.connectButton.text = getString(R.string.button_connect)
                    contentBinding.connectButton.isEnabled = true
                    com.google.android.material.R.attr.colorSecondary
                }
                R.string.status_disconnecting -> {
                    contentBinding.connectButton.isEnabled = false
                    com.google.android.material.R.attr.colorTertiary
                }
                R.string.status_connecting -> {
                    contentBinding.connectButton.isEnabled = false
                    com.google.android.material.R.attr.colorTertiary
                }
                R.string.status_connection_lost,
                R.string.status_could_not_connect -> {
                    task = Timer().schedule(COULD_NOT_CONNECT_DISPLAY_TIME) {
                        onConnectionStatusChange(R.string.status_disconnected)
                    }

                    connectionInterface = null
                    contentBinding.statusText.maxLines = 2
                    com.google.android.material.R.attr.colorError
                }
                else -> {
                    contentBinding.connectButton.text = getString(R.string.button_disconnect)
                    contentBinding.connectButton.isEnabled = true
                    com.google.android.material.R.attr.colorPrimary
                }
            }

            val color = MaterialColors.getColor(contentBinding.statusText, colorId)
            contentBinding.statusText.text = getString(connectionStatus)
            contentBinding.statusText.setTextColor(color)
        }
    }


    /**
     * Attempts to connect to the host address stored in the shared preferences using the selected
     * [IConnectionInterface]. Closes any open connections before attempting a new connection.
     */
    private fun connect() {
        val host = preferences.getString(getString(R.string.pref_key_host), getString(R.string.pref_no_host_entry_value))!!

        if (host == getString(R.string.pref_no_host_entry_value)) {
            Snackbar.make(contentBinding.statusText, R.string.snackbar_no_host_selected, Snackbar.LENGTH_SHORT).show()
            return
        }

        val connectionInterfaceName = preferences.getString(getString(R.string.pref_key_connection_interface), getString(R.string.pref_socket_entry_value))
        val packageName = "$CONNECTION_INTERFACES_PACKAGE.$connectionInterfaceName"
        val prefix = "$packageName.${
            connectionInterfaceName?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            }
        }"

        val validatorClass = Class.forName(prefix + "HostValidator")
        val validator = validatorClass.newInstance() as IHostValidator

        if (!validator.isHostValid(host)) {
            Snackbar.make(contentBinding.statusText, R.string.snackbar_invalid_host, Snackbar.LENGTH_SHORT).show()
            return
        }

        connectionInterface = try {
            val clazz = Class.forName(prefix + "ConnectionInterface")
            try {
                val constructor = clazz.getConstructor(AbstractActivity::class.java, IDataSender::class.java)
                constructor.newInstance(this, keyEventSender) as IConnectionInterface
            } catch (e: Exception) {
                val constructor = clazz.getConstructor(IDataSender::class.java)
                constructor.newInstance(keyEventSender) as IConnectionInterface
            }
        } catch (e: Exception) {
            Snackbar.make(contentBinding.statusText, R.string.snackbar_invalid_connection_interface, Snackbar.LENGTH_SHORT).show()
            null
        }

        if (connectionInterface != null) {
            task?.cancel()
            task = null

            connectionInterface!!.registerConnectionStatusListener(this)
            GlobalScope.launch { connectionInterface!!.open(host) }
        }

    }

    /**
     * Closes an open connection.
     */
    private fun disconnect() {
        task?.cancel()
        task = null

        runBlocking {
            connectionInterface?.close()
            connectionInterface = null
        }
    }

    private fun replaceNumpadFragment(lefty: Boolean) {
        numpadFragment = if (lefty) LeftyNumpadFragment() else NumpadFragment()
        numpadFragment.registerVirtualNumpad(numpad)

        supportFragmentManager.commit {
            replace(R.id.numpad_fragment, numpadFragment)
        }
    }
}
