package com.example.novenaappstore.receivers
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PackageUninstallReceiver(private val callback: (String) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("PackageUninstallReceiver", "onReceive triggered")
        if (intent?.action == "ACTION_UNINSTALL_COMPLETE") {
            val packageName = intent.data?.schemeSpecificPart
            packageName?.let {
                Log.d("PackageUninstallReceiver", "Uninstalled: $it")
                callback(it)  // Notify the activity or view model
            }
        }
    }
}

