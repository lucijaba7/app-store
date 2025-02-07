package com.example.novenaappstore.data.remote

import com.example.novenaappstore.data.model.App
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/apps")
    suspend fun getApps(): Response<List<App>>
}