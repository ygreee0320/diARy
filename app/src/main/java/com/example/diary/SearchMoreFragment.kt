package com.example.diary

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.FragmentSearchMoreBinding

class SearchMoreFragment : Fragment(),
    BottomSearchOrderFragment.OrderChangeListener, BottomSearchTypeFragment.TypeChangeListener {
    private lateinit var binding: FragmentSearchMoreBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var planAdapter: PlanAdapter
    private var searchWord: String ?= null // 검색어
    private var searchType: String ?= "태그 검색↓" // 검색 기준
    private var searchOrder: String ?= "popular" //정렬 기준

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchMoreBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerView

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        planAdapter = PlanAdapter(emptyList())
        diaryAdapter = DiaryAdapter(emptyList())

        searchWord = arguments?.getString("searchWord")
        val type = arguments?.getString("type")



        if (!searchWord.isNullOrBlank()) {
            if (type == "PLAN") { // 플랜의 더보기 라면
                recyclerView.adapter = planAdapter  // 플랜 어댑터 연결됨

                searchTagPlan(searchWord!!)
            } else if (type == "DIARY") { // 다이어리의 더보기 라면
                binding.typeText.text = type
                recyclerView.adapter = diaryAdapter

                searchTagDiary(searchWord!!)
            }
        }

        // 검색창에서 재검색 시
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    if (type == "PLAN") {
                        when (searchType) {
                            else -> searchTagPlan(query)
                        }
                        searchWord = query

                    } else if (type == "DIARY") {
                        when (searchType) {
                            "여행지 검색↓" -> searchDestDiary(query)
                            "작성자 검색↓" -> searchWriterDiary(query)
                            else -> searchTagDiary(query)
                        }
                        searchWord = query
                    }

                    // SearchView 포커스 제거
                    binding.searchView.clearFocus()
                } else {
                    // 검색어가 비어 있을 때 Toast 메시지를 표시
                    Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어 입력이 변경될 때의 동작 (필요하면 구현)
                return false
            }
        })

        binding.searchTypeLayout.setOnClickListener { // 검색 기준 선택
            val selectTypeDialog = BottomSearchTypeFragment()
            selectTypeDialog.setTypeChangeListener(this)
            selectTypeDialog.show(childFragmentManager, "select_type_dialog")
        }

        binding.orderLayout.setOnClickListener { // 정렬 기준 선택
            val selectOrderDialog = BottomSearchOrderFragment()
            selectOrderDialog.setOrderChangeListener(this)
            selectOrderDialog.show(childFragmentManager, "select_order_dialog")
        }

        return binding.root
    }

    override fun onTypeChanged(type: String) { // 검색 기준이 변경되었을 때 호출됨 -> 텍스트 변경
        binding.searchTypeText.text = type
        searchType = type

        if (type == "여행지 검색↓") {
            searchDestDiary(searchWord!!)
        } else if (type == "작성자 검색↓") {
            searchWriterDiary(searchWord!!)
        } else {
            searchTagDiary(searchWord!!)
        }

    }

    override fun onOrderChanged(order: String) { // 정렬 기준이 변경되었을 때 호출됨 -> 텍스트 변경
        binding.orderText.text = order
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

}