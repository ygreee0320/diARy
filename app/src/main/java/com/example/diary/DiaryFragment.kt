package com.example.diary

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class DiaryFragment : Fragment() {
    //private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //일기 작성 버튼 클릭 시, bottomSheet 출력
        val addDiaryButton = view.findViewById<ImageView>(R.id.diary_add_btn)
        addDiaryButton.setOnClickListener {
            val bottomSheetFragment = BottomAddDiaryFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        recyclerView = view.findViewById(R.id.diaryRecyclerView) // 리사이클러뷰 초기화

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        //diaryAdapter = DiaryAdapter(emptyList()) // 초기에 빈 목록으로 어댑터 설정
        //recyclerView.adapter = diaryAdapter // 리사이클러뷰에 어댑터 설정
    }

}