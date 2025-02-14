package com.example.novenaappstore

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.novenaappstore.data.auth.AuthManager
import com.example.novenaappstore.data.remote.ApiService
import com.example.novenaappstore.data.repository.AppRepository
import com.example.novenaappstore.receivers.MyDeviceAdminReceiver
import com.example.novenaappstore.ui.screens.auth.AuthScreen
import com.example.novenaappstore.ui.screens.auth.AuthViewModel
import com.example.novenaappstore.ui.screens.store.StoreScreen
import com.example.novenaappstore.ui.screens.store.StoreViewModel
import com.example.novenaappstore.ui.theme.NovenaAppStoreTheme

@Composable
fun App() {
    val context = LocalContext.current
    val appRepo = AppRepository(context)
    val navController = rememberNavController()

    val storeViewModel = StoreViewModel(context, appRepo, navController)
    val authViewModel = AuthViewModel(context, appRepo, navController)

    // Determine the start screen based on token validity
    val startDestination = if (AuthManager.isTokenValid(context)) "store" else "auth"

    NovenaAppStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            NavHost(navController = navController, startDestination = startDestination) {
                composable("auth") { AuthScreen(authViewModel) }
                composable("store") { StoreScreen(storeViewModel) }
            }
        }
    }
}
