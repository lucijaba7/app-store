package com.example.novenaappstore.ui.screens.store

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novenaappstore.ApkInstaller
import com.example.novenaappstore.data.model.AppState
import com.example.novenaappstore.data.model.AppWithState
import com.example.novenaappstore.data.remote.RetrofitInstance
import com.example.novenaappstore.data.repository.AppRepository
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class StoreViewModel(private val context: Context, private val repository: AppRepository) :
    ViewModel() {
    private val _apps = MutableLiveData<List<AppWithState>>(emptyList()) // Holds the list of apps
    val apps: LiveData<List<AppWithState>> get() = _apps

    private val _loading = MutableLiveData(true) // Initially loading
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>(null) // Holds any error message
    val error: LiveData<String?> get() = _error

//    private val _downloading = MutableLiveData(false) // Download loading
//    val downloading: LiveData<Boolean> get() = _downloading

    private val _downloadingAppId = MutableLiveData<String?>(null)
    val downloadingAppId: LiveData<String?> = _downloadingAppId

    // LiveData to track if ANY app is downloading
    val isAnyDownloading = MediatorLiveData<Boolean>().apply {
        addSource(_downloadingAppId) { appId ->
            value = appId != null  // True if an app is downloading, False if null
        }
    }

    fun clearError() {
        _error.value = null
    }

    init {
        fetchApps() // Start fetching apps when ViewModel is initialized
    }


    fun fetchApps() {
        _loading.value = true // Set loading to true when the fetch starts
        _error.value = null // Clear any previous errors
        _downloadingAppId.value = null

        viewModelScope.launch {
            try {

                val installedPackages = context.packageManager.getInstalledPackages(0)
//                for (pack in installedPackages) {
//                    Log.d("InstallPack", pack.packageName)
//                }
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

                } else {
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

    public fun downloadFile(context: Context, fileUrl: String, appId: String) {
        val service = RetrofitInstance.api

        // Show loading screen
        //_downloading.value = true
        _downloadingAppId.value = appId

        service.downloadFile(fileUrl).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        viewModelScope.launch (Dispatchers.IO) {
                            saveFile(context, body, fileUrl)
                        }
                    }
                } else {
                    Log.e("Download", "Failed: ${response.errorBody()?.string()}")
                    //_downloading.value = false // Hide loading screen on failure
                    _downloadingAppId.value = null
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Download", "Error: ${t.message}")
                //_downloading.value = false // Hide loading screen on failure
                _downloadingAppId.value = null
            }
        })
    }

    suspend fun saveFile(context : Context, body: ResponseBody, fileUrl: String) {
        try {
            val fileName = fileUrl.substringAfterLast("/")
            val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/$fileName"
            val file = File(filePath)

            body.byteStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    outputStream.flush()
                    Log.d("Download", "File saved at: $filePath")
                }
            }

            // Switch back to the main thread to update UI
            withContext(Dispatchers.Main) {
                ApkInstaller.installApk(context, fileName) // Start installation
            }
        } catch (e: IOException) {
            Log.e("Download", "File save error: ${e.message}")
            withContext(Dispatchers.Main) {
                //_downloading.value = false
                _downloadingAppId.value = null
            }
        }
    }

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
    }

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
}
