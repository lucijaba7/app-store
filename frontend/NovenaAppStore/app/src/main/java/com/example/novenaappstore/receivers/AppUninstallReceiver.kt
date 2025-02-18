package com.example.novenaappstore.receivers

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.novenaappstore.ui.screens.store.StoreViewModel

class AppUninstallReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_PACKAGE_REMOVED) {
            val packageName = intent.data?.schemeSpecificPart ?: return

            // Remove the app from the kiosk whitelist
            removeAppFromWhitelist(context, packageName)
            // Reset home launcher
            resetToDefaultLauncher(context, packageName)
        }
    }

    private fun removeAppFromWhitelist(context: Context, packageName: String) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)

        // Get current whitelisted apps
        val currentApps = dpm.getLockTaskPackages(adminComponent).toMutableList()

        if (currentApps.contains(packageName)) {
            currentApps.remove(packageName) // Remove app from whitelist
            dpm.setLockTaskPackages(adminComponent, currentApps.toTypedArray()) // Update whitelist
        }
    }

    fun resetToDefaultLauncher(context: Context, packageName: String) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)

        // Remove any preferred home activity
        dpm.clearPackagePersistentPreferredActivities(adminComponent, packageName)
    }
}