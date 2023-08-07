package com.example.diary

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import com.getbase.floatingactionbutton.FloatingActionButton

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlanFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //    searchView 클릭 시, 검색 활성화
        val searchPlan = view.findViewById<SearchView>(R.id.search_plan)

        searchPlan.setOnClickListener {
            searchPlan.isIconified = false // SearchView에 포커스 요청
        }

        // 일정 불러오기(plan_call_in) 버튼 클릭 시, 랭킹 페이지로 이동
        // 하단 메뉴바에는 플랜으로 그대로 표시됨
        val planCallInButton = view.findViewById<FloatingActionButton>(R.id.plan_call_in)

        planCallInButton.setOnClickListener {
            val rankingFragment = RankingFragment()
            changeFragment(rankingFragment)
        }

        // 일정 새로 추가하기(plan_add_new) 버튼 클릭 시, DetailPlanActivity 로 이동
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlanFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}