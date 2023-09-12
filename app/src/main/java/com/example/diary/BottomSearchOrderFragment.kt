package com.example.diary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSearchOrderFragment : BottomSheetDialogFragment() {

    interface OrderChangeListener {
        fun onOrderChanged(order: String)
    }

    private var orderChangeListener: OrderChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_search_order, container, false)

        // BottomSheet 높이/스타일 설정
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet?.layoutParams
            bottomSheet?.setBackgroundResource(R.drawable.bottom_sheet_rounded_corner)
            layoutParams?.height = 500
            bottomSheet?.layoutParams = layoutParams
        }


        val popButton = view.findViewById<TextView>(R.id.popularity)
        popButton.setOnClickListener { // 인기순으로 선택 시
            orderChangeListener?.onOrderChanged("인기순↓")
            dismiss() // 다이얼로그 닫기
        }

        val newButton = view.findViewById<TextView>(R.id.newest)
        newButton.setOnClickListener { // 최신순으로 선택 시
            orderChangeListener?.onOrderChanged("최신순↓")
            dismiss() // 다이얼로그 닫기
        }

        return view
    }

    fun setOrderChangeListener(listener: OrderChangeListener) {
        this.orderChangeListener = listener
    }
}