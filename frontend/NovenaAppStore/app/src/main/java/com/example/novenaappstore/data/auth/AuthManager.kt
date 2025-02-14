package com.example.novenaappstore.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.*


object AuthManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val TOKEN_KEY = "jwt_token"

    // Initialize SharedPreferences (no encryption here, just plain SharedPreferences)
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Store JWT Token
//    suspend fun saveToken(context: Context, token: String) {
//        withContext(Dispatchers.IO) {
//            val sharedPreferences = getSharedPreferences(context)
//            sharedPreferences.edit().putString(TOKEN_KEY, token).apply()  // Asynchronous save
//        }
//    }
    suspend fun saveToken(context: Context, token: String): Boolean {
        return withContext(Dispatchers.IO) {
            val sharedPreferences = getSharedPreferences(context)
            // Using commit() to block and return result
            val result = sharedPreferences.edit().putString(TOKEN_KEY, token).commit()
            return@withContext result  // Return whether the token was saved successfully
        }
    }

    // Retrieve JWT Token
    fun getToken(context: Context): String? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    // Remove JWT Token
    fun removeToken(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit().remove(TOKEN_KEY).apply()
    }

    fun isTokenValid(context: Context): Boolean {
        val token = getToken(context) ?: return false


        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false // Invalid JWT format

            val payloadJson = String(
                Base64.getDecoder().decode(parts[1]),
                StandardCharsets.UTF_8
            )

            val json = JSONObject(payloadJson)
            val exp = json.optLong("exp", 0) * 1000 // Convert to milliseconds
            val now = Date().time

            now < exp // If current time is after expiration, token is expired
        } catch (e: Exception) {
            false // Any error means assume expired
        }
    }
}
