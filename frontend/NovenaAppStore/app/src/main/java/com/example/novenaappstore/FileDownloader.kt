package com.example.novenaappstore

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.novenaappstore.data.remote.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

object FileDownloader {
    fun downloadFile(context: Context, fileUrl: String, isLoading: MutableState<Boolean>) {
        val service = RetrofitInstance.api

        // Show loading screen
        isLoading.value = true

        service.downloadFile(fileUrl).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        CoroutineScope(Dispatchers.IO).launch {
                            saveFile(context, body, fileUrl, isLoading)
                        }
                    }
                } else {
                    Log.e("Download", "Failed: ${response.errorBody()?.string()}")
                    isLoading.value = false // Hide loading screen on failure
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Download", "Error: ${t.message}")
                isLoading.value = false // Hide loading screen on failure
            }
        })
    }

    suspend fun saveFile(context : Context, body: ResponseBody, fileUrl: String, isLoading: MutableState<Boolean>) {
        try {
            val fileName = fileUrl.substringAfterLast("/")
            val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/$fileName"
            val file = File(filePath)

            body.byteStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    outputStream.flush()
                    Log.d("Download", "File saved at: $filePath")
                }
            }

            // Switch back to the main thread to update UI
            withContext(Dispatchers.Main) {
                isLoading.value = false // Hide loading screen
                ApkInstaller.installApk(context, fileName) // Start installation
            }
        } catch (e: IOException) {
            Log.e("Download", "File save error: ${e.message}")
            withContext(Dispatchers.Main) {
                isLoading.value = false
            }
        }
    }
}