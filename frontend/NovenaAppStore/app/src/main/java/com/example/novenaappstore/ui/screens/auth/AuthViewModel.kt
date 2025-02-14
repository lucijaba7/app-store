package com.example.novenaappstore.ui.screens.auth


import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.novenaappstore.data.auth.AuthManager
import com.example.novenaappstore.data.remote.ApiService
import com.example.novenaappstore.data.remote.RetrofitInstance
import com.example.novenaappstore.data.repository.AppRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val context: Context, private val appRepo: AppRepository, private val navController: NavController) :
    ViewModel() {

    var username = mutableStateOf("")
    var password = mutableStateOf("")
    var errorMessage = mutableStateOf<String?>(null)

    private val _isLoggedIn = MutableLiveData<Boolean>(false)
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading


    fun reset()
    {
        username.value = ""
        password.value = ""
        _isLoggedIn.value = false
        errorMessage.value = null
        _loading.postValue(false)
    }

    // Method to handle login
    fun login() {
        _loading.value = true
        if (username.value.isBlank() || password.value.isBlank()) {
            errorMessage.value = "Please enter both username and password"
            _loading.value = false
            return
        }

        viewModelScope.launch {
            try {
                // Make the API call to log in and get the JWT token
                val response = appRepo.login(username.value, password.value)
                if (response.isSuccessful) {
                    // Store the token in shared preferences or a secure storage
                    val token = response.body()?.token

                    if (token != null) {

                        // Save the token using AuthManager and wait for it to finish saving
                        val tokenSaved = AuthManager.saveToken(
                            context,
                            token
                        )  // Return true if the token is saved

                        if (tokenSaved) {
                            // After confirming the token is saved, update the login state
                            navController.navigate("store")
                        } else {
                            // Handle failure to save the token if needed
                            Log.e("TOKEN SAVE", "Failed to save the token.")
                        }
                    }
                } else {
                    errorMessage.value = "Invalid credentials"
                    _loading.value = false
                }

            } catch (e: Exception) {
                errorMessage.value = "An error occurred. Please try again."
                _loading.value = false
            }
        }
    }
}