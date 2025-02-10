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

class StoreViewModel(private val context: Context, private val repository: AppRepository) :
    ViewModel() {
    private val _apps = MutableLiveData<List<AppWithState>>(emptyList()) // Holds the list of apps
    val apps: LiveData<List<AppWithState>> get() = _apps

    private val _loading = MutableLiveData<Boolean>(true) // Initially loading
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>(null) // Holds any error message
    val error: LiveData<String?> get() = _error

    init {
        fetchApps() // Start fetching apps when ViewModel is initialized
    }

    public fun fetchApps() {
        _loading.value = true // Set loading to true when the fetch starts
        _error.value = null // Clear any previous errors

        viewModelScope.launch {
            delay(3000)
            try {

                val response = repository.getApp() // Fetch apps from the repository
                if (response.isSuccessful) {
                    val fetchedApps = response.body() ?: emptyList();
                    getAllInstalledApps(context);
                    // Determine the state of each app
                    val appsWithState = fetchedApps.map { app ->
                        // Here, you would determine the state based on your criteria
                        val state = when {
                            !isAppInstalled(context, app.packageName) -> AppState.NOT_INSTALLED
                            getAppVersion(
                                context,
                                app.packageName
                            ) < app.version -> AppState.OUTDATED

                            else -> AppState.UP_TO_DATE
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
}

fun isAppInstalled(context: Context, packageName: String): Boolean {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    return intent != null
}

fun getAppVersion(context: Context, packageName: String): String {
    val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
    return packageInfo.versionName ?: "Unknown version"
}

fun getAllInstalledApps(context: Context): List<String> {
    val packageManager = context.packageManager
    val installedPackages = packageManager.getInstalledPackages(0)
    installedPackages.forEach { pkg ->
        Log.d("InstalledApp", "Package: ${pkg.packageName}")
    }
    return installedPackages.map { it.packageName }
}