package com.example.novenaappstore.ui.screens.store

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novenaappstore.data.model.App
import com.example.novenaappstore.data.repository.AppRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StoreViewModel(private val repository: AppRepository) : ViewModel() {
    private val _apps = MutableLiveData<List<App>>(emptyList()) // Holds the list of apps
    val apps: LiveData<List<App>> get() = _apps

    private val _loading = MutableLiveData<Boolean>(true) // Initially loading
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>(null) // Holds any error message
    val error: LiveData<String?> get() = _error

    init {
        fetchApps() // Start fetching apps when ViewModel is initialized
    }

    private fun fetchApps() {
        _loading.value = true // Set loading to true when the fetch starts
        _error.value = null // Clear any previous errors

        viewModelScope.launch {
            try {

                val response = repository.getApp() // Fetch apps from the repository
                if (response.isSuccessful) {
                    _apps.postValue(response.body() ?: emptyList()) // Update apps with the response
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
