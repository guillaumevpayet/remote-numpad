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

package com.guillaumepayet.remotenumpad.helpers

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback

/**
 * Activity that can request for permissions and/or start activities for results with an intent.
 */
interface IActivityResultManager {

    /**
     * Register a callback for permission requests.
     */
    fun registerPermissionResultCallback(callback: ActivityResultCallback<Boolean>): Boolean

    /**
     * Unregister a permission request callback.
     */
    fun unregisterPermissionResultCallback(callback: ActivityResultCallback<Boolean>): Boolean

    /**
     * Register a callback for activity results.
     */
    fun registerActivityResultCallback(callback: ActivityResultCallback<ActivityResult>): Boolean

    /**
     * Unregister an activity results callback.
     */
    fun unregisterActivityResultCallback(callback: ActivityResultCallback<ActivityResult>): Boolean

    /**
     * Start a permission request.
     *
     * @param permission The permission to request.
     */
    fun requestPermission(permission: String)

    /**
     * Start an activity for result.
     *
     * @param intent The intent on which to base the activity.
     */
    fun startActivityForResult(intent: Intent)
}