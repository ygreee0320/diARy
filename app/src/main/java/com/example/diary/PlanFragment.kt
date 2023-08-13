package com.example.diary

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getbase.floatingactionbutton.FloatingActionButton

class PlanFragment : Fragment() {
    private lateinit var planAdapter: PlanAdapter
    private lateinit var recyclerView: RecyclerView // 리사이클러뷰 추가

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

        // Call the API to get actual plan data
        ApiManager.getPlanData(
            onSuccess = { apiResponse ->
                val plans = apiResponse.plans
                planAdapter.updateData(plans)
            },
            onError = { throwable ->
                // Handle error
            }
        )

        val searchPlan = view.findViewById<SearchView>(R.id.search_plan)
        searchPlan.setOnClickListener {
            searchPlan.isIconified = false
        }

        val planCallInButton = view.findViewById<FloatingActionButton>(R.id.plan_call_in)
        planCallInButton.setOnClickListener {
            val rankingFragment = RankingFragment()
            changeFragment(rankingFragment)
        }

        val planAddNewButton = view.findViewById<FloatingActionButton>(R.id.plan_add_new)
        planAddNewButton.setOnClickListener {
            val intent = Intent(activity, AddPlanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun changeFragment(fragment: Fragment) {
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.commit()
    }
}