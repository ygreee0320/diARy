package com.example.diary

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

//카카오 맵 REST API Manager
object SearchManager {
    fun getList(apiKey: String, query: String, category: String, x: String, y: String): SearchResponse? {
        val apiService = MyApplication().searchService
        val call = apiService.getList(apiKey, query, category, x, y)
        val response = call.execute()

        if (response.isSuccessful) {
            return response.body()
        } else {
            return null
        }
    }

//    fun getList(apiKey: String, query: String, category: String, x: String, y: String, onSuccess: (SearchResponse) -> Unit) {
//        val apiService = MyApplication().searchService
//        val call = apiService.getList(apiKey, query, category, x, y)
//
//        call.enqueue(object : Callback<SearchResponse> {
//            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
//                if(response.isSuccessful) {
//                    val json = response.body()
//                    onSuccess(json!!)
//                    Log.d("mylog", "검색 결과 - ${json}")
//                } else {
//                    Log.d("mylog", "API 호출 실패. 응답 코드: ${response.code()}")
//                }
//            }
//
//            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
//                Log.d("mylog", "${t}")
//            }
//        })
//    }
}

object LogInManager {
    fun sendLogInToServer(loginData: LogInData, onSuccess: (String) -> Unit) {
        val apiService = MyApplication().loginService
        val call = apiService.sendLogInRequest(loginData)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val authToken = response.headers()["Authorization"]
                    if (authToken != null) {
                        // 토큰을 onSuccess 콜백으로 전달
                        onSuccess(authToken)
                    } else {
                        Log.e("서버 테스트", "토큰이 없습니다.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
            }
        })
    }
}

object JoinManager {
    fun sendJoinToServer(joinData: JoinData) {
        val apiService = MyApplication().joinService
        val call = apiService.sendJoinRequest(joinData)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "성공")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
            }
        })
    }
}

object MyPageManager {
    fun getMyData(authToken: String, onSuccess: (User) -> Unit, onError: (Throwable) -> Unit) {
        val apiService = MyApplication().myPageService
        val call = apiService.getMyData(authToken)

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
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

            override fun onFailure(call: Call<User>, t: Throwable) {
                onError(t)
            }
        })
    }
}

object PlanManager {
    fun sendPlanToServer(planData: PlanData, authToken: String) { // 일정 새로 추가
        val apiService = MyApplication().planService
        val call = apiService.sendPlan(planData, authToken)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "성공")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
            }
        })
    }

    fun sendModPlanToServer(planId: Int, planData: PlanData, authToken: String) { // 일정 수정
        val apiService = MyApplication().modPlanService
        val call = apiService.sendModPlan(planId, planData, authToken)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "성공")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
            }
        })
    }
}

object MyPlanListManager {
    fun getPlanListData(authToken: String, onSuccess: (List<MyPlanListResponse>) -> Unit, onError: (Throwable) -> Unit) {
        val apiService = MyApplication().myPlanService
        val call = apiService.getPlanData(authToken)

        call.enqueue(object : Callback<List<MyPlanListResponse>> {
            override fun onResponse(call: Call<List<MyPlanListResponse>>, response: Response<List<MyPlanListResponse>>) {
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

            override fun onFailure(call: Call<List<MyPlanListResponse>>, t: Throwable) {
                onError(t)
            }
        })
    }
}

object PlanLikeListManager {
    fun getPlanLikeListData(planId: Int, onSuccess: (List<PlanLikeList>) -> Unit, onError: (Throwable) -> Unit) {
        val apiService = MyApplication().planLikeListService
        val call = apiService.getPlanLikeData(planId)

        call.enqueue(object : Callback<List<PlanLikeList>> {
            override fun onResponse(call: Call<List<PlanLikeList>>, response: Response<List<PlanLikeList>>) {
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

            override fun onFailure(call: Call<List<PlanLikeList>>, t: Throwable) {
                onError(t)
            }
        })
    }
}

object PlanLikeManager { //좋아요 등록 & 취소
    fun sendPlanLikeToServer(planId: Int, authToken: String, callback: (Boolean) -> Unit) {
        val apiService = MyApplication().planLikeService
        val call = apiService.sendPlanLike(planId, authToken)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "성공")
                    callback(true) // 성공 시 true 전달
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                    callback(false) // 실패 시 false 전달
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
                callback(false) // 실패 시 false 전달
            }
        })
    }

    fun deletePlanLikeFromServer(planId: Int, authToken: String, callback: (Boolean) -> Unit) {
        val apiService = MyApplication().deletePlanLikeService
        val call = apiService.deletePlanLike(planId, authToken)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "취소-성공")
                    callback(true) // 성공 시 true 전달
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                    callback(false) // 실패 시 false 전달
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
                callback(false) // 실패 시 false 전달
            }
        })
    }
}

object PlanDetailManager {
    fun getPlanDetailData(planId: Int, onSuccess: (PlanDetailResponse) -> Unit, onError: (Throwable) -> Unit) {
        val apiService = MyApplication().planDetailService
        val call = apiService.getPlanData(planId)

        call.enqueue(object : Callback<PlanDetailResponse> {
            override fun onResponse(call: Call<PlanDetailResponse>, response: Response<PlanDetailResponse>) {
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

            override fun onFailure(call: Call<PlanDetailResponse>, t: Throwable) {
                onError(t)
            }
        })
    }
}

object DeletePlanManager {
    fun deleteDataFromServer(planId: Int) {
        val apiService = MyApplication().deletePlanService
        val call = apiService.deletePlanData(planId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "삭제 성공")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
            }
        })
    }
}

// 일기 추가
object DiaryManager {
    fun sendDiaryToServer(diaryData: DiaryData, authToken: String) {
        val apiService = MyApplication().diaryService
        val call = apiService.sendDiary(diaryData, authToken)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "성공")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
            }
        })
    }

    fun sendModDiaryToServer(diaryId: Int, diaryData: DiaryData, authToken: String) { // 일기 수정
        val apiService = MyApplication().modDiaryService
        val call = apiService.sendModDiary(diaryId, diaryData, authToken)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "성공")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
            }
        })
    }
}

//장소별 일기 목록 불러오기
object MapDiaryListManager {
    fun getDiaryListData(x: String, y: String, onSuccess: (List<DiaryDtoList>) -> Unit, onError: (Throwable) -> Unit) {
        val apiService = MyApplication().mapDiaryService
        val call = apiService.getDiaryData(x, y)

        call.enqueue(object : Callback<List<DiaryDtoList>> {
            override fun onResponse(call: Call<List<DiaryDtoList>>, response: Response<List<DiaryDtoList>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    apiResponse?.let {
                        onSuccess(it)
                    }?: run {
                        onError(Throwable("Response body is null"))
                    }
                } else {
                    onError(Throwable("API call failed with response code: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<List<DiaryDtoList>>, t: Throwable) {
                onError(t)
            }

        })
    }
}

// 유저별 일기 목록 불러오기
object MyDiaryListManager {
    fun getDiaryListData(authToken: String, onSuccess: (List<MyDiaryList>) -> Unit, onError: (Throwable) -> Unit) {
        val apiService = MyApplication().myDiaryService
        val call = apiService.getDiaryData(authToken)

        call.enqueue(object : Callback<List<MyDiaryList>> {
            override fun onResponse(call: Call<List<MyDiaryList>>, response: Response<List<MyDiaryList>>) {
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

            override fun onFailure(call: Call<List<MyDiaryList>>, t: Throwable) {
                onError(t)
            }
        })
    }
}

object DiaryDetailManager {
    fun getDiaryDetailData(diaryId: Int, onSuccess: (DiaryDetailResponse) -> Unit, onError: (Throwable) -> Unit) {
        val apiService = MyApplication().diaryDetailService
        val call = apiService.getDiaryData(diaryId)

        call.enqueue(object : Callback<DiaryDetailResponse> {
            override fun onResponse(call: Call<DiaryDetailResponse>, response: Response<DiaryDetailResponse>) {
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

            override fun onFailure(call: Call<DiaryDetailResponse>, t: Throwable) {
                onError(t)
            }
        })
    }
}

object DeleteDiaryManager {
    fun deleteDataFromServer(diaryId: Int) {
        val apiService = MyApplication().deleteDiaryService
        val call = apiService.deleteDiaryData(diaryId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "삭제 성공")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
            }
        })
    }
}


object DiaryLikeManager { // 일기 좋아요 & 취소
    fun sendDiaryLikeToServer(diaryId: Int, authToken: String, callback: (Boolean) -> Unit) {
        val apiService = MyApplication().creatediaryLikeService
        val call = apiService.createDiaryLikeData(diaryId, authToken)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "등록-성공")
                    callback(true) // 성공 시 true 전달
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                    callback(true) // 성공 시 true 전달
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
                callback(false)
            }
        })
    }

    fun deleteDiaryLikeFromServer(diaryId: Int, authToken: String, callback: (Boolean) -> Unit) {
        val apiService = MyApplication().deleteDiaryLikeService
        val call = apiService.deleteDiaryLikeData(diaryId, authToken)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "취소-성공")
                    callback(true) // 성공 시 true 전달
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                    callback(true) // 성공 시 true 전달
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
                callback(false)
            }
        })
    }
}

object CommentManager {
    fun sendCommentToServer(diaryId: Int, authToken: String, commentData: CommentData, callback: (Boolean) -> Unit) {
        val apiService = MyApplication().commentService
        val call = apiService.sendComment(diaryId, authToken, commentData)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "등록-성공")
                    callback(true) // 성공 시 true 전달
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                    callback(true) // 성공 시 true 전달
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
                callback(false)
            }
        })
    }
}

object CommentListManager {
    fun getCommentListData(diaryId: Int, onSuccess: (List<CommentListResponse>) -> Unit, onError: (Throwable) -> Unit) {
        val apiService = MyApplication().commentListService
        val call = apiService.getCommentListData(diaryId)

        call.enqueue(object : Callback<List<CommentListResponse>> {
            override fun onResponse(call: Call<List<CommentListResponse>>, response: Response<List<CommentListResponse>>) {
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

            override fun onFailure(call: Call<List<CommentListResponse>>, t: Throwable) {
                onError(t)
            }
        })
    }
}