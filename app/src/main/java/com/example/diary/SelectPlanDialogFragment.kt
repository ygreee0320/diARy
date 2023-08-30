package com.example.diary

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SelectPlanDialogFragment : DialogFragment() {
    private lateinit var selectPlanAdapter: SelectPlanAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_plan_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.planSelectRecyclerView)

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        selectPlanAdapter = SelectPlanAdapter(emptyList()) // 초기에 빈 목록으로 어댑터 설정
        recyclerView.adapter = selectPlanAdapter // 리사이클러뷰에 어댑터 설정

        val sharedPreferences = requireContext().getSharedPreferences("my_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("auth_token", null)

        Log.d("내 플랜 리스트 토큰", ""+authToken)

        if (authToken != null) {
            MyPlanListManager.getPlanListData(
                authToken,
                onSuccess = { myPlanListResponse ->
                    val plan = myPlanListResponse.map { it.plan }
                    selectPlanAdapter.updateData(plan)
                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        return dialog
    }

}