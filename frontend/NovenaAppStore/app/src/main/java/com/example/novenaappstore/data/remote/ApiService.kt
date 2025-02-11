package com.example.novenaappstore.data.remote

import com.example.novenaappstore.data.model.App
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface ApiService {
    @GET("/apps")
    suspend fun getApps(): Response<List<App>>

    @GET()
    fun downloadFile(@Url fileUrl: String): Call<ResponseBody>

    @GET("download/{filename}")
    fun downloadApk(@Path("filename") filename: String): Response<ResponseBody>
}