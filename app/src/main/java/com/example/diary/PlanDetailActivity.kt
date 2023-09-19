package com.example.diary

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diary.databinding.ActivityPlanDetailBinding
import com.google.android.material.color.utilities.MaterialDynamicColors.onError
import java.text.SimpleDateFormat
import java.util.*

class PlanDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlanDetailBinding
    private var planId = -1 //현재 플랜ID를 담는 변수
    private var planLikeCount: Int ?= 0 // 현재 플랜의 좋아요 수
    private var isLiked:Boolean = false // 초기에는 좋아요가 되지 않은 상태로 설정
    private var isTakeIn:Boolean = false // 초기에는 담기가 되지 않은 상태로 설정
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // 여행지 데이터를 저장할 리스트
    private val planPlaceList = mutableListOf<PlanDetailModel>()
    private val planDetailAdapter = PlanDetailAdapter(planPlaceList)

    companion object {
        lateinit var planModActivityResult: ActivityResultLauncher<Intent>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  //툴바에 뒤로 가기 버튼 추가

        binding.planDetailRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlanDetailActivity)
            adapter = planDetailAdapter
        }

        planId = intent.getIntExtra("planId", -1)

        // 저장된 토큰 읽어오기
        val sharedPreferences = getSharedPreferences("my_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("auth_token", null)
        val userId = sharedPreferences.getInt("userId", -1)

        if (planId != -1) {
            // 플랜 아이디를 통해 서버에 데이터 요청
            PlanDetailManager.getPlanDetailData(
                planId,
                onSuccess = { planDetail ->
                    // 플랜 상세 정보를 UI에 적용하는 작업
                    binding.planDetailTitle.text = planDetail.plan.travelDest
                    binding.planDetailSubtitle.text = planDetail.plan.content
                    binding.planDetailWriter.text = planDetail.user.username

                    val tagNames = planDetail.tags.joinToString(" ") { "#${it.name}" }
                    binding.planDetailHash.text = tagNames

                    // 만약 작성한 유저와 현재 유저가 같다면, 수정하기/삭제하기 등등
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedStartDate = dateFormatter.format(planDetail.plan.travelStart)
                    val formattedEndDate = dateFormatter.format(planDetail.plan.travelEnd)

                    binding.planDetailMyDate.text = "$formattedStartDate ~ $formattedEndDate"

                    planLikeCount = planDetail.likes.size  //좋아요 수 저장
                    binding.planDetailLike.text = "$planLikeCount"

                    isLiked = planDetail.likes.any { it.userId == userId }

                    Log.d("기존 유저아이디", "" + userId)
                    Log.d("기존 좋아요", "" + isLiked)

                    // 좋아요 상태에 따라 UI 업데이트
                    if (isLiked) { //로그인 한 유저가 좋아요를 누른 상태라면
                        binding.planDetailLikeImg.text = "♥ "
                    } else { //로그인 한 유저가 좋아요를 누른 상태가 아니라면
                        binding.planDetailLikeImg.text = "♡ "
                    }

                    val planDetailModels: List<PlanDetailModel> = planDetail.locations.map { locationDetail ->
                        val formattedStartTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(locationDetail.timeStart)
                        val formattedEndTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(locationDetail.timeEnd)

                        PlanDetailModel(
                            place = locationDetail.name,
                            address = locationDetail.address,
                            placeDate = locationDetail.date,
                            placeStart = formattedStartTime, // timeStart를 원하는 형식으로 변환
                            placeEnd = formattedEndTime,    // timeEnd를 원하는 형식으로 변환
                            x = locationDetail.x,
                            y = locationDetail.y
                        )
                    }

                    planDetailAdapter.updateData(planDetailModels)

                    //같지 않다면, 수정/삭제 gone, 정보레이아웃 visible 필요

                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }

        binding.planDetailLikeBtn.setOnClickListener { // 좋아요 버튼 클릭 시
            if (authToken != null) {
                isLiked = !isLiked // 토글 형식으로 상태 변경
                if (isLiked) { //좋아요 요청
                    PlanLikeManager.sendPlanLikeToServer(planId, authToken) { isSuccess ->
                        if (isSuccess) {
                            updateLikeUI() // 좋아요 상태 UI 업데이트
                            // 플랜 상세 정보 다시 가져오기 -> 좋아요 수 업데이트
                            PlanDetailManager.getPlanDetailData(
                                planId,
                                onSuccess = { planDetail ->
                                    planLikeCount = planDetail.likes.size
                                    binding.planDetailLike.text = "$planLikeCount"
                                },
                                onError = { throwable ->
                                    Log.e("서버 테스트3", "오류: $throwable")
                                }
                            )
                        }
                    }
                } else { //좋아요 해제
                    PlanLikeManager.deletePlanLikeFromServer(planId, authToken) { isSuccess ->
                        if (isSuccess) {
                            updateLikeUI() // 좋아요 상태 UI 업데이트
                            // 플랜 상세 정보 다시 가져오기 -> 좋아요 수 업데이트
                            PlanDetailManager.getPlanDetailData(
                                planId,
                                onSuccess = { planDetail ->
                                    planLikeCount = planDetail.likes.size
                                    binding.planDetailLike.text = "$planLikeCount"
                                },
                                onError = { throwable ->
                                    Log.e("서버 테스트3", "오류: $throwable")
                                }
                            )
                        }
                    }
                }
            }
        }

        binding.planDetailTakeInBtn.setOnClickListener { // 일정 담기 클릭 시
            if (authToken != null) {
                PlanTakeInManager.sendPlanTakeInToServer(planId, authToken) { isSuccess ->
                    if (isSuccess) {
                        this@PlanDetailActivity.runOnUiThread {
                            Toast.makeText(this@PlanDetailActivity, "일정을 저장하였습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        planModActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 수정 후 넘어왔을 때 보여줄 내용 추가
                PlanDetailManager.getPlanDetailData(
                    planId,
                    onSuccess = { planDetail ->
                        // 플랜 상세 정보를 UI에 적용하는 작업
                        binding.planDetailTitle.text = planDetail.plan.travelDest
                        binding.planDetailSubtitle.text = planDetail.plan.content
                        binding.planDetailWriter.text = planDetail.user.username

                        val tagNames = planDetail.tags.joinToString(" ") { "#${it.name}" }
                        binding.planDetailHash.text = tagNames

                        // 만약 작성한 유저와 현재 유저가 같다면, 수정하기/삭제하기 등등
                        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedStartDate = dateFormatter.format(planDetail.plan.travelStart)
                        val formattedEndDate = dateFormatter.format(planDetail.plan.travelEnd)

                        binding.planDetailMyDate.text = "$formattedStartDate ~ $formattedEndDate"

                        planLikeCount = planDetail.likes.size  //좋아요 수 저장
                        binding.planDetailLike.text = "$planLikeCount"

                        val planDetailModels: List<PlanDetailModel> = planDetail.locations.map { locationDetail ->
                            val formattedStartTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(locationDetail.timeStart)
                            val formattedEndTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(locationDetail.timeEnd)

                            PlanDetailModel(
                                place = locationDetail.name,
                                address = locationDetail.address,
                                placeDate = locationDetail.date,
                                placeStart = formattedStartTime, // timeStart를 원하는 형식으로 변환
                                placeEnd = formattedEndTime,    // timeEnd를 원하는 형식으로 변환
                                x = locationDetail.x,
                                y = locationDetail.y
                            )
                        }

                        planDetailAdapter.updateData(planDetailModels)
                    },
                    onError = { throwable ->
                        Log.e("서버 테스트3", "오류: $throwable")
                    }
                )
            }
        }
    }

    private fun updateLikeUI() {
        if (isLiked) { //로그인 한 유저가 좋아요를 누른 상태라면
            binding.planDetailLikeImg.text = "♥ "
        } else { //로그인 한 유저가 좋아요를 누른 상태가 아니라면
            binding.planDetailLikeImg.text = "♡ "
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { // 뒤로 가기 버튼 클릭 시
                finish() // 현재 액티비티 종료
                return true
            }
            R.id.mod_menu -> { //수정하기 버튼 클릭 시
                val intent = Intent(this, AddPlanActivity::class.java)
                // 수정하는 것임을 알림
                intent.putExtra("new_plan", 0)
                intent.putExtra("plan_id", planId)
                planModActivityResult.launch(intent)
                return true
            }
            R.id.remove_menu -> { //삭제하기 버튼 클릭 시
                DeletePlanManager.deleteDataFromServer(planId)
                finish() // 현재 액티비티 종료
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}