package com.example.diary

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

object PlanManager {
    fun sendPlanToServer(planData: PlanData) {
        val apiService = MyApplication().planService
        val call = apiService.sendPlan(planData)
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

object PlanLikeManager {
    fun sendPlanLikeToServer(planId: Int, authToken: String) {
        val apiService = MyApplication().planLikeService
        val call = apiService.sendPlanLike(planId,"Bearer $authToken")
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

    fun deletePlanLikeFromServer(planId: Int, authToken: String) {
        val apiService = MyApplication().deletePlanLikeService
        val call = apiService.deletePlanLike(planId, "Bearer $authToken")
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "취소-성공")
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
        val call = apiService.sendDiary(diaryData, "Bearer $authToken")
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


object DiaryLikeCreateManager {
    fun sendDiaryLikeToServer(diaryId: Int) {
        val apiService = MyApplication().creatediaryLikeService
        val call = apiService.createDiaryLikeData(diaryId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "등록-성공")
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

    fun deleteDiaryLikeFromServer(diaryId: Int) {
        val apiService = MyApplication().deleteDiaryLikeService
        val call = apiService.deleteDiaryLikeData(diaryId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "취소-성공")
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

object CommentManager {
    fun sendCommentToServer(diaryId: Int, commentData: CommentData) {
        val apiService = MyApplication().commentService
        val call = apiService.sendComment(diaryId, commentData)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "등록-성공")
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