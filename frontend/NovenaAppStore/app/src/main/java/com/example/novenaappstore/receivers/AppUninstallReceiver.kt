package com.example.novenaappstore.receivers
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AppUninstallReceiver(private val callback: (String) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("PackageInstallReceiver", "onReceive triggered")
        if (intent?.action == Intent.ACTION_PACKAGE_REMOVED) {
            val packageName = intent.data?.schemeSpecificPart
            packageName?.let {
                Log.d("PackageInstallReceiver", "Installed: $it")
                callback(it)  // Notify MainActivity
            }
        }
    }
}

