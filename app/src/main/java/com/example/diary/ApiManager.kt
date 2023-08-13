package com.example.diary

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ApiManager {

    // API 호출 메서드 예시
    fun getPlanData(onSuccess: (ApiResponse) -> Unit, onError: (Throwable) -> Unit) {
        val apiService = MyApplication().apiService
        val call = apiService.getPlanData()

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    apiResponse?.let {
                        onSuccess(it)
                    } ?: run {
                        onError(Throwable("Response body is null"))
                    }
                } else {
                    onError(Throwable("API call failed with response code: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                onError(t)
            }
        })
    }
}