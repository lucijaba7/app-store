package com.example.novenaappstore.data.remote

import com.example.novenaappstore.data.model.App
import com.example.novenaappstore.data.model.LoginData
import com.example.novenaappstore.data.model.User
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginData): Response<User>

    
    @GET("/apps")
    suspend fun getApps(@Header("Authorization") authToken: String? = null): Response<List<App>>

    @GET("/apps/{filename}")
    fun downloadFile(
        @Path("filename") filename: String,
    ): Call<ResponseBody>

}