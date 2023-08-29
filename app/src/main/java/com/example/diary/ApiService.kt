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
    fun sendDiary(
        @Body diaryData: DiaryData,
        @Header("Authorization") authToken: String
    ): Call<Void>
}

interface MyDiaryService {
    @GET("user/diary")
    fun getDiaryData(@Header("Authorization") authToken: String): Call<List<MyDiaryList>>
}

interface DiaryDetailService {
    @GET("diary/{diaryId}")
    fun getDiaryData(@Path("diaryId") diaryId: Int): Call<DiaryDetailResponse>
}

interface DeleteDiaryService {
    @DELETE("diary/{diaryId}")
    fun deleteDiaryData(@Path("diaryId") diaryId: Int): Call<Void>
}

interface CreateDiaryLikeService {
    @POST("diary/{diaryId}/diary-like")
    fun createDiaryLikeData(@Path("diaryId") diaryId: Int): Call<Void>
}

interface DeleteDiaryLikeService {
    @DELETE("diary/{diaryId}/diary-like")
    fun deleteDiaryLikeData(@Path("diaryId") diaryId: Int): Call<Void>
}

interface CommentService { // 일기 댓글 작성
    @POST("diary/{diaryId}/comment")
    fun sendComment(
        @Path("diaryId") diaryId: Int,
        @Body commentData: CommentData): Call<Void>
}

interface CommentListService { // 일기 댓글 조회
    @GET("diary/{diaryId}/comment")
    fun getCommentListData(@Path("diaryId") diaryId: Int): Call<List<CommentListResponse>>
}