package com.example.diary

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.FragmentSearchMoreBinding

class SearchMoreFragment : Fragment() {
    private lateinit var binding: FragmentSearchMoreBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var planAdapter: PlanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchMoreBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerView

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        planAdapter = PlanAdapter(emptyList())
        diaryAdapter = DiaryAdapter(emptyList())

        val searchWord = arguments?.getString("searchWord")
        val type = arguments?.getString("type")

        if (!searchWord.isNullOrBlank()) {
            if (type == "PLAN") { // 플랜의 더보기 라면
                recyclerView.adapter = planAdapter  // 플랜 어댑터 연결됨

                searchTagPlan(searchWord)
            } else if (type == "DIARY") { // 다이어리의 더보기 라면
                binding.typeText.text = type
                recyclerView.adapter = diaryAdapter

                searchTagDiary(searchWord)
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    if (type == "PLAN") {
                        searchTagPlan(query)
                    } else if (type == "DIARY") {
                        searchTagDiary(query)
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

}