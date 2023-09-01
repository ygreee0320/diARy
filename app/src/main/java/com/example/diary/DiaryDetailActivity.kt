package com.example.diary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diary.databinding.ActivityDiaryDetailBinding
import com.google.android.material.color.utilities.MaterialDynamicColors.onError
import java.text.SimpleDateFormat
import java.util.*

class DiaryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiaryDetailBinding
    private var diaryId = -1 //현재 다이어리ID를 담는 변수

    // 여행지 데이터를 저장할 리스트
    private val diaryPlaceList = mutableListOf<DiaryDetailModel>()
    private val diaryDetailAdapter = DiaryDetailAdapter(diaryPlaceList)

    override fun onCreate(savedInstanceState: Bundle?) {
        var isLiked:Boolean = false // 초기에는 좋아요가 되지 않은 상태로 설정

        //임시 유저

        super.onCreate(savedInstanceState)
        binding = ActivityDiaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  //툴바에 뒤로 가기 버튼 추가

        binding.diaryDetailRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiaryDetailActivity)
            adapter = diaryDetailAdapter
        }

        // 저장된 토큰 읽어오기
        val sharedPreferences = getSharedPreferences("my_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("auth_token", null)
        val userId = sharedPreferences.getInt("userId", -1)

        diaryId = intent.getIntExtra("diaryId", -1)
        if (diaryId != -1) {
            // 다이어리 아이디를 통해 서버에 데이터 요청
            DiaryDetailManager.getDiaryDetailData(
                diaryId,
                onSuccess = { diaryDetail ->
                    // 다이어리 상세 정보를 UI에 적용하는 작업
                    binding.diaryDetailTitle.text = diaryDetail.diaryDto.title
                    binding.diaryDetailSubtitle.text = diaryDetail.diaryDto.travelDest
                    binding.diaryDetailWriter.text = diaryDetail.userDto.username
                    binding.diaryDetailCreateDate.text = "${diaryDetail.diaryDto.createdAt}"
                    binding.diaryDetailComment.text = "댓글 ${diaryDetail.diaryDto.comments.size}개 >"
                    binding.diaryDetailLike.text = diaryDetail.diaryDto.likes.size.toString()

                    isLiked = diaryDetail.diaryDto.likes.any { it.userId == userId }

                    // 좋아요 상태에 따라 UI 업데이트
                    if (isLiked) { //로그인 한 유저가 좋아요를 누른 상태라면
                        binding.diaryDetailLikeImg.text = "♥ "
                    } else {
                        binding.diaryDetailLikeImg.text = "♡ "
                    }

                    val tagNames = diaryDetail.diaryDto.tags.joinToString(" ") { "#${it.name}" }
                    binding.diaryDetailHash.text = tagNames

                    // 만약 작성한 유저와 현재 유저가 같다면, 수정하기/삭제하기 등등
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedStartDate = dateFormatter.format(diaryDetail.diaryDto.travelStart)
                    val formattedEndDate = dateFormatter.format(diaryDetail.diaryDto.travelEnd)

                    binding.diaryDetailDate.text = "$formattedStartDate ~ $formattedEndDate"

                    val planDetailModels: List<DiaryDetailModel> =
                        diaryDetail.diaryLocationDtoList.map { locationDetail ->
                            val formattedStartTime = SimpleDateFormat(
                                "HH:mm",
                                Locale.getDefault()
                            ).format(locationDetail.timeStart)
                            val formattedEndTime = SimpleDateFormat(
                                "HH:mm",
                                Locale.getDefault()
                            ).format(locationDetail.timeEnd)

                            DiaryDetailModel(
                                diaryLocationId = locationDetail.diaryLocationId,
                                diaryId = locationDetail.diaryId,
                                place = locationDetail.name,
                                content = locationDetail.content,
                                address = locationDetail.address,
                                placeDate = locationDetail.date,
                                placeStart = formattedStartTime, // timeStart를 원하는 형식으로 변환
                                placeEnd = formattedEndTime    // timeEnd를 원하는 형식으로 변환
                            )
                        }

                    diaryDetailAdapter.updateData(planDetailModels)

                    //같지 않다면, 수정/삭제 gone, 정보레이아웃 visible 필요

                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }

        binding.diaryDetailLikeBtn.setOnClickListener { // 좋아요 버튼 클릭 시
            if (authToken != null) {
                isLiked = !isLiked // 토글 형식으로 상태 변경
                if (isLiked) { //좋아요 추가 시
                    DiaryLikeManager.sendDiaryLikeToServer(diaryId, authToken) { isSuccess ->
                        if (isSuccess) {
                            binding.diaryDetailLikeImg.text = "♥ "
                            // 일기 상세 정보 다시 가져오기 -> 좋아요 수 업데이트
                            DiaryDetailManager.getDiaryDetailData(
                                diaryId,
                                onSuccess = { diaryDetail ->
                                    binding.diaryDetailLike.text = diaryDetail.diaryDto.likes.size.toString()},
                                onError = { throwable ->
                                    Log.e("서버 테스트3", "오류: $throwable")
                                }
                            )
                        }
                    }
                } else { //좋아요 해제 시
                    DiaryLikeManager.deleteDiaryLikeFromServer(diaryId, authToken) { isSuccess ->
                        if (isSuccess) {
                            binding.diaryDetailLikeImg.text = "♡ "
                            // 일기 상세 정보 다시 가져오기 -> 좋아요 수 업데이트
                            DiaryDetailManager.getDiaryDetailData(
                                diaryId,
                                onSuccess = { diaryDetail ->
                                    binding.diaryDetailLike.text = diaryDetail.diaryDto.likes.size.toString()},
                                onError = { throwable ->
                                    Log.e("서버 테스트3", "오류: $throwable")
                                }
                            )
                        }
                    }
                }
            }
        }

        binding.diaryDetailCommentBtn.setOnClickListener { //댓글 버튼 클릭 시,  CommentActivity로 이동
            val bottomSheetFragment = CommentFragment()
            bottomSheetFragment.setDiaryId(diaryId) // diaryId를 Fragment에 전달
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
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

                return true
            }
            R.id.remove_menu -> { //삭제하기 버튼 클릭 시
                DeleteDiaryManager.deleteDataFromServer(diaryId)
                finish() // 현재 일기 삭제 후, 액티비티 종료
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}