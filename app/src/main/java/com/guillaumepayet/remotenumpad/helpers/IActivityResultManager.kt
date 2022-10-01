package com.guillaumepayet.remotenumpad.helpers

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback

interface IActivityResultManager {

    fun registerPermissionResultCallback(callback: ActivityResultCallback<Boolean>): Boolean

    fun unregisterPermissionResultCallback(callback: ActivityResultCallback<Boolean>): Boolean

    fun registerActivityResultCallback(callback: ActivityResultCallback<ActivityResult>): Boolean

    fun unregisterActivityResultCallback(callback: ActivityResultCallback<ActivityResult>): Boolean

    fun requestPermission(permission: String)

    fun startActivityForResult(intent: Intent)
}