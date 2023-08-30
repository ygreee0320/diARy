package com.example.diary

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.FragmentCommentBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentFragment : BottomSheetDialogFragment() {
    private var diaryId: Int = -1 //해당 다이어리 아이디, 기본값 -1로 초기화
    private lateinit var binding: FragmentCommentBinding
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentBinding.inflate(inflater, container, false)

        diaryId = arguments?.getInt("diaryId", -1) ?: -1

        recyclerView = binding.commentRecyclerView

        val layoutManager = LinearLayoutManager(requireContext())
        binding.commentRecyclerView.layoutManager = layoutManager

        commentAdapter = CommentAdapter(emptyList()) // 초기에 빈 목록으로 어댑터 설정
        recyclerView.adapter = commentAdapter // 리사이클러뷰에 어댑터 설정

        loadCommentList()

        // BottomSheet 높이/스타일 설정
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet?.layoutParams
            bottomSheet?.setBackgroundResource(R.drawable.bottom_sheet_rounded_corner)
            layoutParams?.height = 1200
            bottomSheet?.layoutParams = layoutParams
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = requireContext().getSharedPreferences("my_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("auth_token", null)


        //댓글 전송하기 클릭 시
        binding.commentBtn.setOnClickListener {
            val commentText = binding.commentText.text.toString() //댓글 텍스트
            val commentData = CommentData(commentText)
            if (authToken != null) {
                CommentManager.sendCommentToServer(authToken, diaryId, commentData)
            }

            binding.commentText.text.clear() // 댓글 전송 후 텍스트 초기화

            loadCommentList()
        }
    }

    fun setDiaryId(diaryId: Int) {
        val args = Bundle()
        args.putInt("diaryId", diaryId)
        arguments = args
    }

    override fun onResume() {
        super.onResume()

        //댓글 리스트 업데이트
        loadCommentList()
    }

    // 서버에서 댓글 리스트 불러오기
    private fun loadCommentList() {
        CommentListManager.getCommentListData(
            diaryId,
            onSuccess = { commentListResponse ->
                val comment = commentListResponse.map { it }
                commentAdapter.updateData(comment)
            },
            onError = { throwable ->
                Log.e("서버 테스트3", "오류: $throwable")
            }
        )
    }

}