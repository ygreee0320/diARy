package com.example.diary

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApplication {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://localhost:8080/") // 서버 임시 URL(로컬)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService::class.java)
}