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

interface MyPageService {
    @GET("user/mypage")
    fun getMyData(@Header("Authorization") authToken: String): Call<User>
}

interface PlanService { // 플랜 작성
    @POST("plan") // 서버 주소/plan 으로 POST
    fun sendPlan(@Body planData: PlanData, @Header("Authorization") authToken: String): Call<Void>
}

interface ModPlanService { // 플랜 수정
    @PATCH("plan/{planId}")
    fun sendModPlan(@Path("planId") planId: Int, @Body planData: PlanData,
                    @Header("Authorization") authToken: String): Call<Void>
}

interface MyPlanService {
    @GET("user/plan")
    fun getPlanData(@Header("Authorization") authToken: String): Call<List<MyPlanListResponse>>
}

interface PlanDetailService {
    @GET("plan/{planId}")
    fun getPlanData(@Path("planId") planId: Int): Call<PlanDetailResponse>
}

interface PlanLikeListService {
    @GET("plan/{planId}/plan-like")
    fun getPlanLikeData(@Path("planId") planId: Int): Call<List<PlanLikeList>>
}

interface PlanLikeService {
    @POST("plan/{planId}/plan-like")
    fun sendPlanLike(@Path("planId") planId: Int, @Header("Authorization") authToken: String): Call<Void>
}

interface DeletePlanLikeService {
    @DELETE("plan/{planId}/plan-like")
    fun deletePlanLike(@Path("planId") planId: Int, @Header("Authorization") authToken: String): Call<Void>
}

interface DeletePlanService {
    @DELETE("plan/{planId}")
    fun deletePlanData(@Path("planId") planId: Int): Call<Void>
}

interface DiaryService { // 일기 작성
    @POST("diary")
    fun sendDiary(
        @Body diaryData: DiaryData, @Header("Authorization") authToken: String
    ): Call<Void>
}

interface ModDiaryService { // 일기 수정
    @PATCH("diary/{diaryId}")
    fun sendModDiary(@Path("diaryId") diaryId: Int, @Body diaryData: DiaryData,
                    @Header("Authorization") authToken: String): Call<Void>
}

interface MapDiaryService {
    @GET("map/{address}")
    fun getDiaryData(@Path("address") address: String): Call<List<DiaryDtoList>>
}

interface MyDiaryService {
    @GET("user/diary")
    fun getDiaryData(@Header("Authorization") authToken: String): Call<List<DiaryDetailResponse>>
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
    fun createDiaryLikeData(@Path("diaryId") diaryId: Int, @Header("Authorization") authToken: String): Call<Void>
}

interface DeleteDiaryLikeService {
    @DELETE("diary/{diaryId}/diary-like")
    fun deleteDiaryLikeData(@Path("diaryId") diaryId: Int, @Header("Authorization") authToken: String): Call<Void>
}

interface CommentService {
    // 일기 댓글 작성
    @POST("diary/{diaryId}/comment")
    fun sendComment(
        @Path("diaryId") diaryId: Int,
        @Header("Authorization") authToken: String,
        @Body commentData: CommentData
    ): Call<Void>
}
interface CommentListService { // 일기 댓글 조회
    @GET("diary/{diaryId}/comment")
    fun getCommentListData(@Path("diaryId") diaryId: Int): Call<List<CommentListResponse>>
}

interface HotTopicService { // 핫토픽 리스트 조회
    @GET("home/topic")
    fun getHotTopicData(): Call<List<Topic>>
}

interface TagDiarySearchService { // 태그별 검색 조회
    @GET("search/{searchWord}/diary-tag")
    fun getTagDiarySearchData(@Path("searchWord") searchWord: String): Call<List<DiaryDetailResponse>>
}

interface TagPlanSearchService { // 태그별 검색 조회
    @GET("search/{searchword}/plan-tag")
    fun getTagPlanSearchData(): Call<List<Topic>>
}