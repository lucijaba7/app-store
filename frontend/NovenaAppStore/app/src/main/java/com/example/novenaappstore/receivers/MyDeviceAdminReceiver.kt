package com.example.novenaappstore.receivers

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DeviceAdminService
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.novenaappstore.MainActivity

class MyDeviceAdminReceiver : DeviceAdminReceiver() {

//    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
//        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//
//        val componentName = ComponentName(context.applicationContext, MyDeviceAdminReceiver::class.java)
//        manager.setProfileName(componentName, "Administrator")
//
//        val launch = Intent(context, MainActivity::class.java)
//        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        context.startActivity(launch)
//    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d("DeviceAdmin", "Device admin enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d("DeviceAdmin", "Device admin disabled")
    }
}
