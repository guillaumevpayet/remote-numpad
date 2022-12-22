package com.guillaumepayet.remotenumpad

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.guillaumepayet.remotenumpad.helpers.IActivityResultManager
import java.util.concurrent.ConcurrentSkipListSet

abstract class AbstractActivity : AppCompatActivity(), IActivityResultManager {

    /**
     * Class used to encapsulate the callbacks into subclasses of [Comparable] to work with the
     * [ConcurrentSkipListSet] collections.
     */
    private class ComparableCallback<O, C: ActivityResultCallback<O>>(private val callback: C) : ActivityResultCallback<O>, Comparable<ComparableCallback<O, C>> {

        override fun onActivityResult(result: O) = callback.onActivityResult(result)

        override fun compareTo(other: ComparableCallback<O, C>): Int =
            callback.hashCode().compareTo(other.callback.hashCode())
    }

    var isShowingDialog = false

    private val permissionResultCallbacks: MutableCollection<ActivityResultCallback<Boolean>> = ConcurrentSkipListSet()
    private val activityResultCallbacks: MutableCollection<ActivityResultCallback<ActivityResult>> = ConcurrentSkipListSet()

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
        permissionResultCallbacks.add(ComparableCallback(callback))

    override fun unregisterPermissionResultCallback(callback: ActivityResultCallback<Boolean>) =
        permissionResultCallbacks.remove(ComparableCallback(callback))

    override fun registerActivityResultCallback(callback: ActivityResultCallback<ActivityResult>) =
        activityResultCallbacks.add(ComparableCallback(callback))

    override fun unregisterActivityResultCallback(callback: ActivityResultCallback<ActivityResult>) =
        activityResultCallbacks.remove(ComparableCallback(callback))

    override fun requestPermission(permission: String) = permissionLauncher.launch(permission)

    override fun startActivityForResult(intent: Intent) = activityLauncher.launch(intent)
}