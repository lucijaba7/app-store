package com.example.novenaappstore.ui.screens.store

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.novenaappstore.data.auth.AuthManager
import com.example.novenaappstore.data.model.AppState
import com.example.novenaappstore.data.model.AppWithState
import com.example.novenaappstore.data.remote.RetrofitInstance
import com.example.novenaappstore.data.repository.AppRepository
import com.example.novenaappstore.receivers.AppUninstallReceiver
import com.example.novenaappstore.receivers.MyDeviceAdminReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class StoreViewModel(
    private val context: Context,
    private val repository: AppRepository,
    private val navController: NavController
) :
    ViewModel() {
    private val _apps = MutableLiveData<List<AppWithState>>(emptyList()) // Holds the list of apps
    val apps: LiveData<List<AppWithState>> get() = _apps

    private val _loading = MutableLiveData(true) // Initially loading
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>(null) // Holds any error message
    val error: LiveData<String?> get() = _error

    private val _downloadingAppId = MutableLiveData<String?>(null)
    val downloadingAppId: LiveData<String?> = _downloadingAppId

    private val _savingAppId = MutableLiveData<String?>(null)
    val savingAppId: LiveData<String?> = _savingAppId

    // LiveData to track if ANY app is downloading
    val isAnySaving = MediatorLiveData<Boolean>().apply {
        addSource(_savingAppId) { appId ->
            value = appId != null  // True if an app is downloading, False if null
        }
    }

    // LiveData to track if ANY app is downloading
    val isAnyDownloading = MediatorLiveData<Boolean>().apply {
        addSource(_downloadingAppId) { appId ->
            value = appId != null  // True if an app is downloading, False if null
        }
    }

    private val _downloadProgress = MutableLiveData(0)
    val downloadProgress: LiveData<Int> = _downloadProgress

    fun clearError() {
        _error.value = null
    }

    fun logout() {
        AuthManager.removeToken(context)
        navController.navigate("auth")
    }

    fun fetchApps() {
        _apps.value = emptyList()
        _loading.value = true // Set loading to true when the fetch starts
        _error.value = null // Clear any previous errors
        _downloadingAppId.value = null

        viewModelScope.launch {
            try {
                val installedPackages = context.packageManager.getInstalledPackages(0)
                val response = repository.getApps() // Fetch apps from the repository

                if (response.isSuccessful) {
                    val fetchedApps = response.body() ?: emptyList()
                    // Determine the state of each app
                    val appsWithState = fetchedApps.map { app ->
                        // Here, you would determine the state based on your criteria
                        val installedApp =
                            installedPackages.find { p -> p.packageName == app.packageName }

                        val state = when {
                            installedApp == null -> AppState.NOT_INSTALLED  // App is not installed
                            installedApp.versionName != app.version -> AppState.OUTDATED  // Version is outdated
                            else -> AppState.UP_TO_DATE  // Version is up to date
                        }

                        // Map the App to AppWithState
                        AppWithState(app = app, state = state)
                    }

                    // Post the list of apps with their states
                    _apps.postValue(appsWithState)
                }
                else if (response.code() == 401)
                {
                    logout()
                }
                else {
                    _error.postValue("Failed to fetch apps: ${response.message()}") // Set error message
                }
            } catch (e: Exception) {
                // Handle exceptions such as network failure
                _error.postValue("An error occurred: ${e.message}")
            } finally {
                _loading.value = false // Set loading to false after fetching is done
            }
        }
    }

    // region Download apk file, save it to downloads and start silent installation
    fun downloadFile(context: Context, fileUrl: String, appId: String) {
        val service = RetrofitInstance.api
        _downloadingAppId.value = appId
        _downloadProgress.value = 0  // Initialize progress

        service.downloadFile(fileUrl).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        viewModelScope.launch(Dispatchers.IO) {
                            // Switch to saving progress on the main thread
                            withContext(Dispatchers.Main) {
                                _downloadingAppId.value = null
                                _savingAppId.value = appId
                            }

                            // Perform file saving operation in background
                            saveFile(context, body, fileUrl)

                            // After saving completes, reset UI state on main thread
                            withContext(Dispatchers.Main) {
                                _savingAppId.value = null
                                _downloadingAppId.value = appId
                            }
                        }
                    }
                } else {
                    Log.e("Download", "Failed: ${response.errorBody()?.string()}")
                    viewModelScope.launch { _downloadingAppId.value = null }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Download", "Error: ${t.message}")
                viewModelScope.launch { _downloadingAppId.value = null }
            }
        })
    }

    suspend fun saveFile(context: Context, body: ResponseBody, fileUrl: String) {
        try {
            val fileName = fileUrl.substringAfterLast("/")
            val filePath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/$fileName"
            val file = File(filePath)

            val totalBytes = body.contentLength()
            var bytesReadSoFar = 0L

            body.byteStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    var lastProgress = 0 // Track the last progress to reduce unnecessary UI updates

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        bytesReadSoFar += bytesRead

                        if (totalBytes > 0) {
                            val progress = ((bytesReadSoFar.toFloat() / totalBytes) * 100).toInt()
                            if (progress != lastProgress) { // Only update when progress changes
                                _downloadProgress.postValue(progress)
                                lastProgress = progress
                                Log.d("Download", "Progress: $progress%")
                            }
                        }
                    }

                    outputStream.flush()
                    Log.d("Download", "File saved at: $filePath")
                }
            }

            // Switch back to the main thread to update UI
            withContext(Dispatchers.Main) {
                installApk(context, fileName) // Start installation
                _downloadProgress.value = 0
            }
        } catch (e: IOException) {
            Log.e("Download", "File save error: ${e.message}")
            withContext(Dispatchers.Main) {
                _savingAppId.value = null
                _downloadProgress.value = 0
            }
        }
    }

    private fun installApk(context: Context, fileName: String) {

        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            if (!file.exists()) {
                Log.e("ApkInstaller", "APK file not found!")
                return
            }

            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            val inputStream = file.inputStream()
            val outputStream = session.openWrite("package_install", 0, -1)

            inputStream.copyTo(outputStream)
            session.fsync(outputStream)
            outputStream.close()
            inputStream.close()

            // Commit the session to install the APK
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                Intent(Intent.ACTION_INSTALL_PACKAGE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            session.commit(pendingIntent.intentSender)

        } catch (e: Exception) {
            Log.e("ApkInstaller", "Installation failed: ${e.message}")
        }
    }
    //endregion

    //region Successful installation receiver
    // Listen for the package added event
    fun registerInstallReceiver() {
        val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
        filter.addDataScheme("package")  // The URI scheme for package is "package"

        // Register the receiver to listen for package installations
        context.registerReceiver(packageReceiver, filter)
    }

    // Unregister the receiver when it's no longer needed
    fun unregisterInstallReceiver() {
        context.unregisterReceiver(packageReceiver)
    }

    // BroadcastReceiver to handle package installation events
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val packageName = intent.data?.encodedSchemeSpecificPart
                if (packageName != null) {
                    // Update the app state for the installed app
                    onAppInstalled(packageName)
                }
            }
        }
    }

    // Trigger after successful installation to update UI
    fun onAppInstalled(packageName: String) {
        Log.d("StoreViewModel", "Handling installed app: $packageName")
        // Get the current list of apps
        _apps.value?.let { appsList ->
            // Find the app that was installed
            val updatedApps = appsList.map { appWithState ->
                if (appWithState.app.packageName == packageName) {
                    // Update the state of this app to UP_TO_DATE
                    appWithState.copy(state = AppState.UP_TO_DATE)
                } else {
                    appWithState
                }
            }

            // Update the LiveData with the updated list
            _apps.value = updatedApps
            _downloadingAppId.value = null
        }

        addAppToWhitelist(context, packageName)

        val mainActivity = getLauncherActivity(context, packageName)
        if (mainActivity != null) {
            setAppAsLauncher(context, packageName, mainActivity)
        }

    }
    //endregion

    //region Uninstall app
    // Silently uninstall app
    fun uninstallApp(context: Context, packageName: String){
        // Reset home launcher
        resetToDefaultLauncher(context, packageName)

        val packageInstaller = context.packageManager.packageInstaller

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent("ACTION_UNINSTALL_COMPLETE"),
            PendingIntent.FLAG_IMMUTABLE  // Fixes crash on Android 12+
        )

        packageInstaller.uninstall(packageName, pendingIntent.intentSender)
        Log.d("Uninstall", "Uninstall finished.")
    }

    // Trigger after successful uninstallation to update UI
    fun onAppUninstalled(packageName: String) {
        Log.d("StoreViewModel", "Handling uninstalled app: $packageName")
        // Get the current list of apps
        _apps.value?.let { appsList ->
            val updatedApps = appsList.map { appWithState ->
                if (appWithState.app.packageName == packageName) {
                    appWithState.copy(state = AppState.NOT_INSTALLED)
                } else {
                    appWithState
                }
            }

            // Update the LiveData with the updated list
            _apps.value = updatedApps
        }
        removeAppFromWhitelist(context, packageName)

    }

    // Listen for the package removed event
    fun registerUninstallReceiver() {
        val receiver = AppUninstallReceiver { packageName ->

            onAppUninstalled(packageName)  // Call your function
        }
        val intentFilter = IntentFilter(Intent.ACTION_PACKAGE_REMOVED).apply {
            addDataScheme("package")
        }
        context.registerReceiver(receiver, intentFilter)
    }

    // Unregister the receiver when it's no longer needed
    fun unregisterUninstallReceiver() {
        context.unregisterReceiver(packageUninstallReceiver)
    }

    // BroadcastReceiver to handle app uninstallation
    private val packageUninstallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val packageName =
                intent?.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME) // Get package name

            if (packageName != null) {
                Log.d("UninstallReceiver", "App uninstalled: $packageName")
                Toast.makeText(context, "Uninstalled: $packageName", Toast.LENGTH_SHORT).show()

                //  Update UI
                onAppUninstalled(packageName)
            } else {
                Log.e("UninstallReceiver", "Uninstall failed or package name missing")
            }
        }
    }
    //endregion

    //region Enable kiosk mode to installed apps
    // Get current whitelisted apps
    fun getWhitelistedApps(context: Context): List<String> {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)
        return dpm.getLockTaskPackages(adminComponent).toList()
    }

    // Adding installed apps to white list enabling them to enter kiosk mode
    fun addAppToWhitelist(context: Context, packageName: String) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)

        val currentApps = getWhitelistedApps(context).toMutableList()

        if (!currentApps.contains(packageName)) {
            currentApps.add(packageName) // Add new app
            dpm.setLockTaskPackages(adminComponent, currentApps.toTypedArray()) // Update whitelist
        }
    }

    // Remove app from white list after uninstallation
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
    //endregion

    //region Set/remove app as home launcher
    // Set installed app as home launcher
    fun setAppAsLauncher(context: Context, packageName: String, mainActivity: String) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)

        // Set the installed app as the preferred home activity
        val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        val component = ComponentName(packageName, mainActivity)

        dpm.addPersistentPreferredActivity(adminComponent, intentFilter, component)
    }

    // Check if it's set as home launcher
    fun getLauncherActivity(context: Context, packageName: String): String? {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(packageName)
        }

        val activities = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return activities.firstOrNull()?.activityInfo?.name
    }

    // Remove app as home launcher and set default one
    fun resetToDefaultLauncher(context: Context, packageName: String) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)

        // Remove any preferred home activity
        dpm.clearPackagePersistentPreferredActivities(adminComponent, packageName)
    }
    //endregion

}
