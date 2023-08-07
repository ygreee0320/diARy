package com.example.diary

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.example.diary.databinding.FragmentRankingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RankingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RankingFragment : Fragment() {
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
        val binding = FragmentRankingBinding.inflate(inflater, container, false)

        //검색바 검색 종류 클릭 시
        binding.searchPlaceRanking.setOnClickListener {
            binding.searchPlaceRanking.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            binding.searchTitleRanking.setTextColor(Color.parseColor("#959595"))
            binding.searchWriterRanking.setTextColor(Color.parseColor("#959595"))
        }

        binding.searchTitleRanking.setOnClickListener {
            binding.searchTitleRanking.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            binding.searchPlaceRanking.setTextColor(Color.parseColor("#959595"))
            binding.searchWriterRanking.setTextColor(Color.parseColor("#959595"))
        }

        binding.searchWriterRanking.setOnClickListener {
            binding.searchWriterRanking.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            binding.searchPlaceRanking.setTextColor(Color.parseColor("#959595"))
            binding.searchTitleRanking.setTextColor(Color.parseColor("#959595"))
        }

        // 랭킹 월별 주간 종류 클릭 시
        binding.rankingMonth.setOnClickListener {
            binding.rankingMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            binding.rankingWeek.setTextColor(Color.parseColor("#959595"))
        }

        binding.rankingWeek.setOnClickListener {
            binding.rankingWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            binding.rankingMonth.setTextColor(Color.parseColor("#959595"))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //    searchView 클릭 시, 검색 활성화
        val searchRanking = view.findViewById<SearchView>(R.id.search_ranking)

        searchRanking.setOnClickListener {
            searchRanking.isIconified = false // SearchView에 포커스 요청
            searchRanking.requestFocus()
        }

        // SearchView의 Query(검색어) 입력 이벤트 처리
        searchRanking.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색 버튼을 눌렀을 때의 동작을 여기에 구현 (검색어 사용)
                // 예를 들어, 검색어를 이용해 서버에서 데이터를 가져와 표시하거나 다른 동작을 수행할 수 있음

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어 입력이 변화할 때의 동작을 여기에 구현
                // 예를 들어, 실시간으로 검색어를 이용해 검색 결과를 갱신할 수 있음
                return false
            }
        })

        // SearchView에 포커스가 바뀔 때 키보드를 자동으로 표시
        searchRanking.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val inputMethodManager =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(searchRanking, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RankingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RankingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}