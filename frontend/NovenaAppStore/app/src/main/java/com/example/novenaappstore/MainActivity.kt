package com.example.novenaappstore

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val activeAdmins = dpm.activeAdmins

        if (activeAdmins != null && activeAdmins.isNotEmpty()) {
            // Loop through the active admins and display them
            for (admin in activeAdmins) {
                Log.d("DeviceAdmin", "Active Admin: ${admin.packageName}")
            }
        } else {
            Log.d("DeviceAdmin", "No active admins found.")
        }

        setContent {
            App()
        }
    }
}