package com.example.diary

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("plan")
    fun getPlanData(
    ): Call<ApiResponse>
}