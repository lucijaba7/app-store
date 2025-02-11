package com.example.novenaappstore.ui.screens.store

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novenaappstore.data.model.App
import com.example.novenaappstore.data.model.AppState
import com.example.novenaappstore.data.model.AppWithState
import com.example.novenaappstore.data.repository.AppRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.Manifest
import android.app.Activity
import android.os.Environment.DIRECTORY_DOWNLOADS
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class StoreViewModel(private val context: Context, private val repository: AppRepository) :
    ViewModel() {
    private val _apps = MutableLiveData<List<AppWithState>>(emptyList()) // Holds the list of apps
    val apps: LiveData<List<AppWithState>> get() = _apps

    private val _loading = MutableLiveData<Boolean>(true) // Initially loading
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>(null) // Holds any error message
    val error: LiveData<String?> get() = _error

    fun clearError() {
        _error.value = null
    }

    init {
        fetchApps() // Start fetching apps when ViewModel is initialized
    }

    fun fetchApps() {
        _loading.value = true // Set loading to true when the fetch starts
        _error.value = null // Clear any previous errors

        viewModelScope.launch {
            delay(3000)
            try {

                val installedPackages = context.packageManager.getInstalledPackages(0)
                val response = repository.getApps() // Fetch apps from the repository
                if (response.isSuccessful) {
                    val fetchedApps = response.body() ?: emptyList();
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

    // Download APK from backend
    fun downloadApk(filename: String) {
        _loading.value = true // Set loading to true when download starts
        _error.value = null // Clear any previous errors

        viewModelScope.launch {
            try {
                // Call the repository to download the APK
                val response = repository.downloadApk(filename)
                if (response.isSuccessful) {
                    // If the download is successful, get the input stream
                    val inputStream = response.body()?.byteStream()

                    // Save the file to device storage
                    if (inputStream != null) {
                        saveFileToStorage(inputStream, filename)
                        _error.postValue("Download completed successfully.")
                    } else {
                        _error.postValue("Failed to download APK.")
                    }
                } else {
                    _error.postValue("Failed to fetch the APK: ${response.message()}")
                }
            } catch (e: Exception) {
                // Handle errors such as network failure
                _error.postValue("An error occurred: ${e.message}")
            } finally {
                _loading.value = false // Set loading to false after download is done
            }
        }
    }

    // Save the file to device storage
    private fun saveFileToStorage(inputStream: InputStream, filename: String) {
        val file = File(context.getExternalFilesDir(null), filename)
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }
}
