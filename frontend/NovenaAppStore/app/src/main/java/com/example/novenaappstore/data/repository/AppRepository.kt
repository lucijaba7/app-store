package com.example.novenaappstore.data.repository

import android.content.Context
import android.util.Log
import com.example.novenaappstore.data.model.App
import com.example.novenaappstore.data.remote.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AppRepository(private val context: Context) {

    private val api = RetrofitInstance.api

    suspend fun getApps(): Response<List<App>> {
        return api.getApps()
    }

    suspend fun downloadApk(filename: String): Response<ResponseBody> {
        return api.downloadApk(filename)
    }

    /*
    // Function to download APK
    suspend fun downloadApkFile(appName: String) {
        val response = api.downloadApk(appName)

        if (response.isSuccessful) {
            val body = response.body()
            body?.let { saveFile(it) }
        } else {
            Log.e("Download", "Failed to download file: ${response.message()}")
        }
    }

    // Save file to local storage
    private fun saveFile(body: ResponseBody) {
        val file = File(context.cacheDir, "downloaded_apk.apk")
        try {
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            inputStream.close()
            outputStream.close()
            Log.d("Download", "File saved to: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e("Download", "Error saving file", e)
        }
    }

     */
}
