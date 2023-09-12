package com.example.diary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSearchTypeFragment : BottomSheetDialogFragment() {

    interface TypeChangeListener {
        fun onTypeChanged(type: String)
    }

    private var typeChangeListener: TypeChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_search_type, container, false)

        // BottomSheet 높이/스타일 설정
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet?.layoutParams
            bottomSheet?.setBackgroundResource(R.drawable.bottom_sheet_rounded_corner)
            layoutParams?.height = 800
            bottomSheet?.layoutParams = layoutParams
        }

        val tagButton = view.findViewById<TextView>(R.id.search_tag)
        tagButton.setOnClickListener { //태그 검색 선택 시
            typeChangeListener?.onTypeChanged("태그 검색↓")
            dismiss() // 다이얼로그 닫기
        }

        val destButton = view.findViewById<TextView>(R.id.search_dest)
        destButton.setOnClickListener { //여행지 검색 선택 시
            typeChangeListener?.onTypeChanged("여행지 검색↓")
            dismiss() // 다이얼로그 닫기
        }

        val writerButton = view.findViewById<TextView>(R.id.search_writer)
        writerButton.setOnClickListener { //작성자 검색 선택 시
            typeChangeListener?.onTypeChanged("작성자 검색↓")
            dismiss() // 다이얼로그 닫기
        }

        return  view
    }

    fun setTypeChangeListener(listener: TypeChangeListener) {
        this.typeChangeListener = listener
    }

}