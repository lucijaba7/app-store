package com.example.novenaappstore.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AppInstallationReceiver: BroadcastReceiver()  {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val packageName = intent.data?.encodedSchemeSpecificPart

        if (action == Intent.ACTION_PACKAGE_ADDED) {
            Log.d("AppReceiver", "App Installed: $packageName")
        } else if (action == Intent.ACTION_PACKAGE_REMOVED) {
            Log.d("AppReceiver", "App Uninstalled: $packageName")
        }
    }
}