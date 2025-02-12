package com.example.novenaappstore;

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.UserManager
import android.util.Log
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.novenaappstore.receivers.MyDeviceAdminReceiver

class MainActivity : ComponentActivity() {

    // Create a DevicePolicyManager instance and ComponentName for your DeviceAdminReceiver
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var deviceAdminSample: ComponentName

    private val requestDeviceAdmin: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Device Admin enabled successfully
            Toast.makeText(this, "Device Admin enabled successfully!", Toast.LENGTH_SHORT).show()
        } else {
            // Failed to enable Device Admin
            Toast.makeText(this, "Failed to enable Device Admin.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize DevicePolicyManager and ComponentName
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        deviceAdminSample = ComponentName(this, MyDeviceAdminReceiver::class.java)

        // Check if the device admin is already active
        if (!devicePolicyManager.isAdminActive(deviceAdminSample)) {
            requestDeviceAdminPermission()
        }

        enableUnknownSources(devicePolicyManager, deviceAdminSample )

        setContent {
            App() // Your Composable UI
        }
    }

    private fun requestDeviceAdminPermission() {
        // Create the intent to request device admin access
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminSample)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.add_admin_extra_app_text)) // Explanation message
        }

        // Launch the activity using the registered ActivityResultLauncher
        requestDeviceAdmin.launch(intent)
    }

    private fun enableUnknownSources(dpm: DevicePolicyManager, adminComponent: ComponentName) {
        var requestInstallPermission =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    Log.d("InstallPackagedPermission", "Permission granted!")

                } else {
                    Log.d("InstallPackagedPermission", "Permission denied")
                }
            }

//        if (!canInstallPackages(applicationContext)) {
//            requestInstallPermission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
//                data = Uri.parse("package:${packageName}")
//            })
//        }
    }

    private fun canInstallPackages(context: Context): Boolean {
        val packageManager = context.packageManager
        return packageManager.canRequestPackageInstalls()
    }
}
