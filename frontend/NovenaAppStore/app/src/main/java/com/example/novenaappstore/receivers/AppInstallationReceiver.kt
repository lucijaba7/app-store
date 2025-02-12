package com.example.novenaappstore.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log

class AppInstallationReceiver(private val onStatusChanged: (Int) -> Unit): BroadcastReceiver()  {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        onStatusChanged(status)
        Log.d("StatusReceiver", "Received status update: $status")
    }
}