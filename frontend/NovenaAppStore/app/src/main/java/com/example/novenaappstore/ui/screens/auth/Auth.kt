package com.example.novenaappstore.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun AuthScreen(navController: NavController) {
    Column() {
        Button(onClick = { navController.navigate("store") }) {
            Text("Login")
        }
    }
}