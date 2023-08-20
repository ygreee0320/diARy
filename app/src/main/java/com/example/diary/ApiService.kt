package com.example.diary

import retrofit2.Call
import retrofit2.http.*

interface PlanService {
    @POST("plan") // 서버 주소/plan 으로 POST
    fun sendPlan(@Body planData: PlanData): Call<Void>
}

interface MyPlanService {
    @GET("user/1/plan")
    fun getPlanData(): Call<List<MyPlanListResponse>>
}

interface PlanDetailService {
    @GET("plan/{planId}")
    fun getPlanData(@Path("planId") planId: Int): Call<PlanDetailResponse>
}

interface DeletePlanService {
    @DELETE("plan/{planId}")
    fun deletePlanData(@Path("planId") planId: Int): Call<Void>
}

interface DiaryService {
    @POST("diary")
    fun sendDiary(@Body diaryData: DiaryData): Call<Void>
}