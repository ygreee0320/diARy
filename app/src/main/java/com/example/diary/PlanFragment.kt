package com.example.diary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getbase.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class PlanFragment : Fragment(){
    private lateinit var planAdapter: PlanAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.planRecyclerView) // 리사이클러뷰 초기화

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        planAdapter = PlanAdapter(emptyList()) // 초기에 빈 목록으로 어댑터 설정
        recyclerView.adapter = planAdapter // 리사이클러뷰에 어댑터 설정

        loadPlanList()

        // 검색창에서 내 플랜 검색
        val searchPlan = view.findViewById<EditText>(R.id.search_plan)
        searchPlan.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchPlan.text.toString()
                if (!query.isNullOrBlank()) {
                    searchMyPlan(query)

                    // 키보드를 내리는 부분 추가
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(searchPlan.windowToken, 0)
                } else {
                    // 검색어가 비어 있을 때 Toast 메시지를 표시
                    Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        val planCallInButton = view.findViewById<FloatingActionButton>(R.id.plan_call_in)
        planCallInButton.setOnClickListener {
            val rankingFragment = RankingFragment()
            changeFragment(rankingFragment)
        }

        val planAddNewButton = view.findViewById<FloatingActionButton>(R.id.plan_add_new)
        planAddNewButton.setOnClickListener {
            val intent = Intent(activity, AddPlanActivity::class.java)
            // 새로 작성하는 것임을 알림
            intent.putExtra("new_plan", 1)
            startActivity(intent)
        }
    }

    // 서버에서 내 플랜 리스트 불러오기
    private fun loadPlanList() {
        val sharedPreferences = requireContext().getSharedPreferences("my_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("auth_token", null)

        Log.d("내 플랜 리스트 토큰", ""+authToken)

        if (authToken != null) {
            MyPlanListManager.getPlanListData(
                authToken,
                onSuccess = { myPlanListResponse ->
                    val plan = myPlanListResponse.map { it }
                    planAdapter.updateData(plan, false)
                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }
    }

    // 검색어에 맞는 내 플랜만 표시
    private fun searchMyPlan(query: String) {
        val sharedPreferences = requireContext().getSharedPreferences("my_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("auth_token", null)

        Log.d("내 플랜 리스트 토큰", "" + authToken)

        if (authToken != null) {
            MyPlanListManager.getPlanListData(
                authToken,
                onSuccess = { myPlanListResponse ->
                    val planList = myPlanListResponse.map { it }

                    // 검색어에 해당하는 플랜만 필터링
                    val filteredPlans = if (query.isNotBlank()) {
                        planList.filter { plan ->
                            plan.plan.travelDest.contains(query, ignoreCase = true)
                        }
                    } else {
                        planList
                    }

                    planAdapter.updateData(filteredPlans, false)
                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()

        //플랜 리스트 업데이트
        //loadPlanList()
    }

    private fun changeFragment(fragment: Fragment) {
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.commit()
    }
}