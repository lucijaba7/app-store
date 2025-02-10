package com.example.novenaappstore

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.novenaappstore.data.remote.ApiService
import com.example.novenaappstore.data.repository.AppRepository
import com.example.novenaappstore.ui.screens.auth.AuthScreen
import com.example.novenaappstore.ui.screens.store.StoreScreen
import com.example.novenaappstore.ui.screens.store.StoreViewModel
import com.example.novenaappstore.ui.theme.NovenaAppStoreTheme

@Composable
fun App() {
    val context = LocalContext.current;
    val appRepo = AppRepository(context)
    val navController = rememberNavController()

    val storeVieModel = StoreViewModel(appRepo)

    NovenaAppStoreTheme {  // Apply your custom theme
        Surface(color = MaterialTheme.colorScheme.background) {
            NavHost(navController = navController, startDestination = "store") {
                composable("auth") { AuthScreen(navController) }
                composable("store") { StoreScreen(storeVieModel) }
            }
        }
    }
}