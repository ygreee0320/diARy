package com.example.diary

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var planRecyclerView: RecyclerView
    private lateinit var planAdapter: PlanAdapter
    private var searchWord: String ?= null // 검색어
    private var searchType: String ?= "태그 검색↓" // 검색 기준
    private var searchOrder: String ?= "인기순↓"  // 정렬 기준

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        diaryRecyclerView = binding.diaryRecyclerView
        planRecyclerView = binding.planRecyclerView

        val diaryLayoutManager = LinearLayoutManager(this)
        diaryRecyclerView.layoutManager = diaryLayoutManager

        val planLayoutManager = LinearLayoutManager(this)
        planRecyclerView.layoutManager = planLayoutManager

        diaryAdapter = DiaryAdapter(emptyList()) // 초기에 빈 목록으로 어댑터 설정
        diaryRecyclerView.adapter = diaryAdapter // 리사이클러뷰에 어댑터 설정

        planAdapter = PlanAdapter(emptyList())
        planRecyclerView.adapter = planAdapter

        searchWord = intent.getStringExtra("searchWord")
        Log.d("my log", "서치 액티비티"+searchWord)

        if (!searchWord.isNullOrBlank()) {  // 초기 검색
            binding.searchView.setText(searchWord)
            searchTagDiary(searchWord!!)
            searchTagPlan(searchWord!!)
        }

        // 검색창에서 재검색
        binding.searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchView.text.toString()
                if (!query.isNullOrBlank()) {
                    searchTagDiary(query)
                    searchTagPlan(query)
                    searchWord = query

                    // 키보드를 내리는 부분 추가
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
                } else {
                    // 검색어가 비어 있을 때 Toast 메시지를 표시
                    Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        binding.diaryMore.setOnClickListener { // 일기 더보기 버튼 클릭 시
            if (binding.diaryMore.text == "더보기 >") {
                if (!searchWord.isNullOrBlank()) {
                    binding.diaryMore.setText("숨기기 >")
                    binding.planResult.visibility = View.GONE
                    binding.planRecyclerView.visibility = View.GONE

                    val layoutParams = binding.diaryRecyclerView.layoutParams
                    layoutParams.height = resources.getDimension(R.dimen.search_more_view_height).toInt()
                    binding.diaryRecyclerView.layoutParams = layoutParams
                }
            } else {
                binding.diaryMore.setText("더보기 >")
                binding.planResult.visibility = View.VISIBLE
                binding.planRecyclerView.visibility = View.VISIBLE

                val layoutParams = binding.diaryRecyclerView.layoutParams
                layoutParams.height = resources.getDimension(R.dimen.search_view_height).toInt()
                binding.diaryRecyclerView.layoutParams = layoutParams
            }
        }

        binding.planMore.setOnClickListener { // 일정 더보기 버튼 클릭 시
            if (binding.planMore.text == "더보기 >") {
                if (!searchWord.isNullOrBlank()) {
                    binding.planMore.setText("숨기기 >")
                    binding.diaryResult.visibility = View.GONE
                    binding.diaryRecyclerView.visibility = View.GONE

                    val layoutParams = binding.planRecyclerView.layoutParams
                    layoutParams.height = resources.getDimension(R.dimen.search_more_view_height).toInt()
                    binding.planRecyclerView.layoutParams = layoutParams
                }
            } else {
                binding.planMore.setText("더보기 >")
                binding.diaryResult.visibility = View.VISIBLE
                binding.diaryRecyclerView.visibility = View.VISIBLE

                val layoutParams = binding.planRecyclerView.layoutParams
                layoutParams.height = resources.getDimension(R.dimen.search_view_height).toInt()
                binding.planRecyclerView.layoutParams = layoutParams
            }
        }

        binding.searchTypeLayout.setOnClickListener {
            val selectTypeDialog = BottomSearchTypeFragment()
            selectTypeDialog.setTypeChangeListener(object : BottomSearchTypeFragment.TypeChangeListener {
                override fun onTypeChanged(type: String) {
                    // 여기에서 선택한 검색 기준(type)에 따른 동작 수행
                    // 예: 선택한 검색 기준에 따라 화면 갱신 등
                }
            })
            selectTypeDialog.show(supportFragmentManager, "select_type_dialog")
        }

        binding.searchTypeLayout.setOnClickListener { // 검색 기준 선택
            val selectTypeDialog = BottomSearchTypeFragment()
            selectTypeDialog.setTypeChangeListener(object : BottomSearchTypeFragment.TypeChangeListener {
                override fun onTypeChanged(type: String) {
                    binding.searchTypeText.text = type
                    searchType = type

                    if (type == "여행지 검색↓") {
                        if (searchOrder == "최신순↓") {
                            searchDestRecentDiary(searchWord!!)
                            searchDestRecentPlan(searchWord!!)
                        } else {
                            searchDestDiary(searchWord!!)
                            searchDestPlan(searchWord!!)
                        }
                    } else if (type == "작성자 검색↓") {
                        if (searchOrder == "최신순↓") {
                            searchWriterRecentDiary(searchWord!!)
                            searchWriterRecentPlan(searchWord!!)
                        } else {
                            searchWriterDiary(searchWord!!)
                            searchWriterPlan(searchWord!!)
                        }
                    } else {
                        if (searchOrder == "최신순↓") {
                            searchTagRecentDiary(searchWord!!)
                            searchTagRecentPlan(searchWord!!)
                        } else {
                            searchTagDiary(searchWord!!)
                            searchTagPlan(searchWord!!)
                        }
                    }
                }
            })
            selectTypeDialog.show(supportFragmentManager, "select_type_dialog")
        }

        binding.orderLayout.setOnClickListener {
            val selectOrderDialog = BottomSearchOrderFragment()
            selectOrderDialog.setOrderChangeListener(object : BottomSearchOrderFragment.OrderChangeListener {
                override fun onOrderChanged(order: String) {
                    binding.orderText.text = order
                    searchOrder = order

                    if (order == "최신순↓") {
                        when (searchType) {
                            "여행지 검색↓" -> searchDestRecentDiary(searchWord!!)
                            "작성자 검색↓" -> searchWriterRecentDiary(searchWord!!)
                            else -> searchTagRecentDiary(searchWord!!)
                        }
                    } else {
                        when (searchType) {
                            "여행지 검색↓" -> searchDestDiary(searchWord!!)
                            "작성자 검색↓" -> searchWriterDiary(searchWord!!)
                            else -> searchTagDiary(searchWord!!)
                        }
                    }
                }
            })
            selectOrderDialog.show(supportFragmentManager, "select_order_dialog")
        }

        // 뒤로가기 버튼 클릭 시
        binding.searchBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun searchTagDiary(searchWord: String) {
        SearchManager.getSearchTagDiaryData(
            searchWord = searchWord,
            onSuccess = { TagDiaryList ->
                val diary = TagDiaryList.map { it }
                Log.d("my log", "태그별 다이어리"+ diary)

                if (diary.isEmpty()) {
                    // 검색 결과가 없을 때 어댑터에 빈 목록 설정
                    diaryAdapter.updateData(emptyList(), true)
                } else {
                    diaryAdapter.updateData(diary, true)
                }
            },
            onError = { throwable ->  // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                diaryAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchTagRecentDiary(searchWord: String) {
        SearchManager.getSearchTagRecentDiaryData(
            searchWord = searchWord,
            onSuccess = { TagDiaryList ->
                val diary = TagDiaryList.map { it }
                Log.d("my log", "태그별 최신 다이어리"+ diary)

                if (diary.isEmpty()) {
                    // 검색 결과가 없을 때 어댑터에 빈 목록 설정
                    diaryAdapter.updateData(emptyList(), true)
                } else {
                    diaryAdapter.updateData(diary, true)
                }
            },
            onError = { throwable ->  // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                diaryAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchTagPlan(searchWord: String) {
        SearchManager.getSearchTagPlanData(
            searchWord = searchWord,
            onSuccess = { TagPlanList ->
                val plan = TagPlanList.map { it }
                Log.d("my log", "태그별 일정"+ plan)

                if(plan.isEmpty()) {
                    planAdapter.updateData(emptyList(), true)
                } else {
                    planAdapter.updateData(plan, true)
                }
            },
            onError = { throwable -> // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                planAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchTagRecentPlan(searchWord: String) {
        SearchManager.getSearchTagRecentPlanData(
            searchWord = searchWord,
            onSuccess = { TagPlanList ->
                val plan = TagPlanList.map { it }
                Log.d("my log", "태그별 최신 일정"+ plan)

                if(plan.isEmpty()) {
                    planAdapter.updateData(emptyList(), true)
                } else {
                    planAdapter.updateData(plan, true)
                }
            },
            onError = { throwable -> // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                planAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchDestDiary(searchWord: String) {
        SearchManager.getSearchDestDiaryData(
            searchWord = searchWord,
            onSuccess = { DestDiaryList ->
                val diary = DestDiaryList.map { it }
                Log.d("my log", "여행지별 다이어리"+ diary)

                if (diary.isEmpty()) {
                    // 검색 결과가 없을 때 어댑터에 빈 목록 설정
                    diaryAdapter.updateData(emptyList(), true)
                } else {
                    diaryAdapter.updateData(diary, true)
                }
            },
            onError = { throwable ->  // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                diaryAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchDestRecentDiary(searchWord: String) {
        SearchManager.getSearchDestRecentDiaryData(
            searchWord = searchWord,
            onSuccess = { DestDiaryList ->
                val diary = DestDiaryList.map { it }
                Log.d("my log", "여행지별 최신 다이어리"+ diary)

                if (diary.isEmpty()) {
                    // 검색 결과가 없을 때 어댑터에 빈 목록 설정
                    diaryAdapter.updateData(emptyList(), true)
                } else {
                    diaryAdapter.updateData(diary, true)
                }
            },
            onError = { throwable ->  // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                diaryAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchDestPlan(searchWord: String) {
        SearchManager.getSearchDestPlanData(
            searchWord = searchWord,
            onSuccess = { DestPlanList ->
                val plan = DestPlanList.map { it }
                Log.d("my log", "여행지별 일정"+ plan)

                if(plan.isEmpty()) {
                    planAdapter.updateData(emptyList(), true)
                } else {
                    planAdapter.updateData(plan, true)
                }
            },
            onError = { throwable -> // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                planAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchDestRecentPlan(searchWord: String) {
        SearchManager.getSearchDestRecentPlanData(
            searchWord = searchWord,
            onSuccess = { DestPlanList ->
                val plan = DestPlanList.map { it }
                Log.d("my log", "여행지별 최신 일정"+ plan)

                if(plan.isEmpty()) {
                    planAdapter.updateData(emptyList(), true)
                } else {
                    planAdapter.updateData(plan, true)
                }
            },
            onError = { throwable -> // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                planAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchWriterDiary(searchWord: String) {
        SearchManager.getSearchWriterDiaryData(
            searchWord = searchWord,
            onSuccess = { WriterDiaryList ->
                val diary = WriterDiaryList.map { it }
                Log.d("my log", "작성자별 다이어리"+ diary)

                if (diary.isEmpty()) {
                    // 검색 결과가 없을 때 어댑터에 빈 목록 설정
                    diaryAdapter.updateData(emptyList(), true)
                } else {
                    diaryAdapter.updateData(diary, true)
                }
            },
            onError = { throwable ->  // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                diaryAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchWriterRecentDiary(searchWord: String) {
        SearchManager.getSearchWriterRecentDiaryData(
            searchWord = searchWord,
            onSuccess = { WriterDiaryList ->
                val diary = WriterDiaryList.map { it }
                Log.d("my log", "작성자별 최신 다이어리"+ diary)

                if (diary.isEmpty()) {
                    // 검색 결과가 없을 때 어댑터에 빈 목록 설정
                    diaryAdapter.updateData(emptyList(), true)
                } else {
                    diaryAdapter.updateData(diary, true)
                }
            },
            onError = { throwable ->  // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                diaryAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchWriterPlan(searchWord: String) {
        SearchManager.getSearchWriterPlanData(
            searchWord = searchWord,
            onSuccess = { WriterPlanList ->
                val plan = WriterPlanList.map { it }
                Log.d("my log", "작성자별 일정"+ plan)

                if (plan.isEmpty()) {
                    // 검색 결과가 없을 때 어댑터에 빈 목록 설정
                    planAdapter.updateData(emptyList(), true)
                } else {
                    planAdapter.updateData(plan, true)
                }
            },
            onError = { throwable ->  // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                planAdapter.updateData(emptyList(), true)
            }
        )
    }

    private fun searchWriterRecentPlan(searchWord: String) {
        SearchManager.getSearchWriterRecentPlanData(
            searchWord = searchWord,
            onSuccess = { WriterPlanList ->
                val plan = WriterPlanList.map { it }
                Log.d("my log", "작성자별 일정"+ plan)

                if (plan.isEmpty()) {
                    // 검색 결과가 없을 때 어댑터에 빈 목록 설정
                    planAdapter.updateData(emptyList(), true)
                } else {
                    planAdapter.updateData(plan, true)
                }
            },
            onError = { throwable ->  // 검색어에 맞는 검색 결과가 없으면 여기로 옴
                Log.e("서버 테스트3", "오류: $throwable")
                planAdapter.updateData(emptyList(), true)
            }
        )
    }

}