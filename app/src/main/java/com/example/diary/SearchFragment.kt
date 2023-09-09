package com.example.diary

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.FragmentHomeBinding
import com.example.diary.databinding.FragmentSearchBinding
import com.google.android.material.color.utilities.MaterialDynamicColors.onError

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        recyclerView = binding.diaryRecyclerView

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        diaryAdapter = DiaryAdapter(emptyList()) // 초기에 빈 목록으로 어댑터 설정
        recyclerView.adapter = diaryAdapter // 리사이클러뷰에 어댑터 설정

        val searchWord = arguments?.getString("searchWord")
        if (searchWord != null) {
            searchTagDiary(searchWord)
        }

        return binding.root
    }

    private fun searchTagDiary(searchWord: String) {
        SearchManager.getSearchTagDiaryData(
            searchWord = searchWord,
            onSuccess = { TagDiaryList ->
                val diary = TagDiaryList.map { it }
                Log.d("my log", "태그별 다이어리"+ diary)

                diaryAdapter.updateData(diary, true)
            },
            onError = { throwable ->
                Log.e("서버 테스트3", "오류: $throwable")
            }
        )
    }

}