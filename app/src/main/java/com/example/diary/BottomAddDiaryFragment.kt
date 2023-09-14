package com.example.diary

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomAddDiaryFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_add_diary, container, false)

        // BottomSheet 높이/스타일 설정
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet?.layoutParams
            bottomSheet?.setBackgroundResource(R.drawable.bottom_sheet_rounded_corner)
            layoutParams?.height = 600
            bottomSheet?.layoutParams = layoutParams
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 새로 작성하기 버튼 클릭 시
        val newDiaryButton = view.findViewById<LinearLayout>(R.id.diary_new_btn)
        newDiaryButton.setOnClickListener {
            dismiss() // 다이얼로그 닫기
            val intent = Intent(activity, AddDiaryActivity::class.java)
            // 새로 작성하는 것임을 알림
            intent.putExtra("new_diary", 1)
            startActivity(intent)
        }

        // 내 일정 불러오기 버튼 클릭 시
        val planSelectButton = view.findViewById<LinearLayout>(R.id.diary_from_plan_btn)
        planSelectButton.setOnClickListener {
            val selectPlanDialog = SelectPlanDialogFragment()
            selectPlanDialog.show(childFragmentManager, "select_plan_dialog")
        }
    }
}