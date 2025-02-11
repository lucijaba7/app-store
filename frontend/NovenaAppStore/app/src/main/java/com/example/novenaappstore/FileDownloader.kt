package com.example.novenaappstore

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.novenaappstore.data.remote.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

object FileDownloader {
    fun downloadFile(context: Context, fileUrl: String) {
        val service = RetrofitInstance.api

        service.downloadFile(fileUrl).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        saveFile(context, body, fileUrl)
                    }
                } else {
                    Log.e("Download", "Failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Download", "Error: ${t.message}")
            }
        })
    }

    fun saveFile(context : Context, body: ResponseBody, fileUrl: String) {
        try {
            val fileName = fileUrl.substringAfterLast("/")
            val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/$fileName"

            val file = File(filePath)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
                Log.d("Download", "File saved at: $filePath")
            } catch (e: IOException) {
                Log.e("Download", "File save error: ${e.message}")
            } finally {
                inputStream?.close()
                outputStream?.close()
                ApkInstaller.installApk(context, fileName)
            }
        } catch (e: Exception) {
            Log.e("Download", "Error: ${e.message}")
        }

    }
}