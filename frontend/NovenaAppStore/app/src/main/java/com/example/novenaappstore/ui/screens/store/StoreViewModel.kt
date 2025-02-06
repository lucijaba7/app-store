package com.example.novenaappstore.ui.screens.store

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novenaappstore.data.model.App
import com.example.novenaappstore.data.repository.AppRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StoreViewModel(private val appRepository: AppRepository) : ViewModel() {
    // Call the download function when you need to download the APK

    fun downloadApk(appName: String) {
        Log.d("TAG", "OVDJEE")

        viewModelScope.launch {
            appRepository.downloadApkFile(appName)
        }
    }
}