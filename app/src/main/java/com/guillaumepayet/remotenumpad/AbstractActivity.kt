package com.guillaumepayet.remotenumpad

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.guillaumepayet.remotenumpad.helpers.IActivityResultManager

abstract class AbstractActivity : AppCompatActivity(), IActivityResultManager {

    var isShowingDialog = false

    private val permissionResultCallbacks = mutableSetOf<ActivityResultCallback<Boolean>>()
    private val activityResultCallbacks = mutableSetOf<ActivityResultCallback<ActivityResult>>()

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { result ->
            permissionResultCallbacks.forEach { it.onActivityResult(result) }
        }

        activityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            activityResultCallbacks.forEach { it.onActivityResult(result) }
        }
    }


    override fun registerPermissionResultCallback(callback: ActivityResultCallback<Boolean>) =
        permissionResultCallbacks.add(callback)

    override fun unregisterPermissionResultCallback(callback: ActivityResultCallback<Boolean>) =
        permissionResultCallbacks.remove(callback)

    override fun registerActivityResultCallback(callback: ActivityResultCallback<ActivityResult>) =
        activityResultCallbacks.add(callback)

    override fun unregisterActivityResultCallback(callback: ActivityResultCallback<ActivityResult>) =
        activityResultCallbacks.remove(callback)

    override fun requestPermission(permission: String) = permissionLauncher.launch(permission)

    override fun startActivityForResult(intent: Intent) = activityLauncher.launch(intent)
}