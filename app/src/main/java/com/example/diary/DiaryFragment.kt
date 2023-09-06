package com.example.diary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.FragmentDiaryBinding

class DiaryFragment : Fragment() {
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: FragmentDiaryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //일기 작성 버튼 클릭 시, bottomSheet 출력
        binding.diaryAddBtn.setOnClickListener {
//            val intent = Intent(activity, AddDiaryActivity::class.java)
//            // 새로 작성하는 것임을 알림
//            intent.putExtra("new_diary", 1)
//            startActivity(intent)
            val bottomSheetFragment = BottomAddDiaryFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        recyclerView = view.findViewById(R.id.diaryRecyclerView) // 리사이클러뷰 초기화

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        diaryAdapter = DiaryAdapter(emptyList()) // 초기에 빈 목록으로 어댑터 설정
        recyclerView.adapter = diaryAdapter // 리사이클러뷰에 어댑터 설정

        loadDiaryList()

    }

    // 서버에서 내 다이어리 리스트 불러오기
    private fun loadDiaryList() {
        val sharedPreferences = requireContext().getSharedPreferences("my_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("auth_token", null)

        Log.d("내 다이어리 리스트 토큰", ""+authToken)

        if (authToken != null) {
            MyDiaryListManager.getDiaryListData(
                authToken,
                onSuccess = { myDiaryList ->
                    val diary = myDiaryList.map { it.diaryDto }
                    Log.d("내 일기 목록 테스트", ""+diary)
                    diaryAdapter.updateData(diary)

                    // username 값을 추출하여 텍스트 뷰에 적용
                    val username = myDiaryList.firstOrNull()?.userDto?.username ?: "username"
                    binding.diaryUserName.text = username
                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()

        //다이어리 리스트 업데이트
        loadDiaryList()
    }

}