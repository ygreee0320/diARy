package com.example.diary

import retrofit2.Call
import retrofit2.http.*

interface LogInService {
    @POST("login")
    fun sendLogInRequest(@Body loginData: LogInData): Call<Void>
}

interface JoinService {
    @POST("auth/join")
    fun sendJoinRequest(@Body joinData: JoinData): Call<Void>
}

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

interface MyDiaryService {
    @GET("user/1/diary")
    fun getDiaryData(): Call<List<MyDiaryList>>
}

interface DiaryDetailService {
    @GET("diary/{diaryId}")
    fun getDiaryData(@Path("diaryId") diaryId: Int): Call<DiaryDetailResponse>
}

interface DeleteDiaryService {
    @DELETE("diary/{diaryId}")
    fun deleteDiaryData(@Path("diaryId") diaryId: Int): Call<Void>
}