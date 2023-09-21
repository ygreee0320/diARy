package com.example.diary

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.FragmentRankingBinding

class RankingFragment : Fragment() {
    private lateinit var binding: FragmentRankingBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var rankingAdapter: RankingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRankingBinding.inflate(inflater, container, false)

        recyclerView = binding.rankingRecyclerView

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        rankingAdapter = RankingAdapter(emptyList())
        recyclerView.adapter = rankingAdapter

        showRanking() // 랭킹 출력(어댑터에 연결)

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

        // 랭킹 좋아요/최신순 종류 클릭 시
        binding.rankingMonth.setOnClickListener {

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//            searchView 클릭 시, 검색 활성화
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

    private fun showRanking() {
        RankingManager.getRankingData(
            onSuccess = { rankingList ->
                val ranking = rankingList.map { it }
                Log.d("my log", "랭킹 목록 출력"+ ranking)

                if (ranking.isEmpty()) { // 결과가 없을 때 어댑터에 빈 목록 설정
                    rankingAdapter.updateData(emptyList())
                    Log.d("my log", "비었음"+ ranking)
                } else {
                    rankingAdapter.updateData(ranking)
                }
            },
            onError = { throwable ->
                Log.e("서버 테스트3", "오류: $throwable")
                rankingAdapter.updateData(emptyList())
            }
        )
    }

}