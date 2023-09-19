package com.example.diary

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.FragmentSearchBinding
import com.google.android.material.color.utilities.MaterialDynamicColors.onError

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var planRecyclerView: RecyclerView
    private lateinit var planAdapter: PlanAdapter
    private var searchWord: String ?= null // 검색어
    private var searchType: String ?= "태그 검색↓" // 검색 기준

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        diaryRecyclerView = binding.diaryRecyclerView
        planRecyclerView = binding.planRecyclerView

        val diaryLayoutManager = LinearLayoutManager(requireContext())
        diaryRecyclerView.layoutManager = diaryLayoutManager

        val planLayoutManager = LinearLayoutManager(requireContext())
        planRecyclerView.layoutManager = planLayoutManager

        diaryAdapter = DiaryAdapter(emptyList()) // 초기에 빈 목록으로 어댑터 설정
        diaryRecyclerView.adapter = diaryAdapter // 리사이클러뷰에 어댑터 설정

        planAdapter = PlanAdapter(emptyList())
        planRecyclerView.adapter = planAdapter

        searchWord = arguments?.getString("searchWord")
        val type = arguments?.getString("type") // 초기 타입(태그/작성자/여행지 검색)
        Log.d("my log", "서치뷰"+searchWord)

        if (!searchWord.isNullOrBlank()) {  // 초기 검색
            binding.searchView.setQuery(searchWord, false)
            searchTagDiary(searchWord!!)
            searchTagPlan(searchWord!!)
        }

        // 검색창에서 재검색 시
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    when (searchType) {
                        "여행지 검색↓" -> {
                            searchDestDiary(query)
                        }
                        "작성자 검색↓" -> {
                            searchWriterDiary(query)
                        }
                        else -> {
                            searchTagDiary(query)
                            searchTagPlan(query)
                        }
                    }
                    searchWord = query //검색어 저장

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

        binding.diaryMore.setOnClickListener { // 일기 더보기 버튼 클릭 시
            val searchWord = binding.searchView.query.toString()
            Log.d("my log", ""+searchWord)

            if (!searchWord.isNullOrBlank()) {
                val fragment = SearchMoreFragment()
                val args = Bundle()
                args.putString("searchWord", searchWord)
                args.putString("type", "DIARY")
                fragment.arguments = args

                val transaction = requireFragmentManager().beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null) // 이전 Fragment로 돌아가기 위해 백 스택에 추가
                transaction.commit()
            }
        }

        binding.planMore.setOnClickListener { // 일정 더보기 버튼 클릭 시
            val searchWord = binding.searchView.query.toString()
            Log.d("my log", ""+searchWord)

            if (!searchWord.isNullOrBlank()) {
                val fragment = SearchMoreFragment()
                val args = Bundle()
                args.putString("searchWord", searchWord)
                args.putString("type", "PLAN")
                fragment.arguments = args

                val transaction = requireFragmentManager().beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null) // 이전 Fragment로 돌아가기 위해 백 스택에 추가
                transaction.commit()
            }
        }

        return binding.root
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