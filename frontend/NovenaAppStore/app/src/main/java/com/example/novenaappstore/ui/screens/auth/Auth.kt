package com.example.novenaappstore.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.example.novenaappstore.data.model.AppState
import com.example.novenaappstore.ui.theme.PoppinsFontFamily
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(authViewModel: AuthViewModel) {
    val username = authViewModel.username.value
    val password = authViewModel.password.value
    val errorMessage = authViewModel.errorMessage.value
    val isLaoding = authViewModel.loading.observeAsState(false).value

    LaunchedEffect(Unit) {
        authViewModel.reset()
    }


    // Content of the AuthScreen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // Username field
        BasicTextField(
            value = username,
            onValueChange = { authViewModel.username.value = it },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    if (username.isEmpty()) Text("Username", color = Color.Gray)
                    innerTextField()
                }
            }
        )

        // Password field
        Spacer(modifier = Modifier.height(16.dp))
        BasicTextField(
            value = password,
            onValueChange = { authViewModel.password.value = it },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    if (password.isEmpty()) Text("Password", color = Color.Gray)
                    innerTextField()
                }
            }
        )

        // Error message if any
        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = Color.Red)
        }

        // Login button
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (!isLaoding) {
                    authViewModel.login()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isLaoding)
                Text("Login")
            else CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(15.dp),
                strokeWidth = 2.dp
            )
        }
    }


}
