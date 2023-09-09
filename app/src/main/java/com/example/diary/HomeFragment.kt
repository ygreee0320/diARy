package com.example.diary

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.diary.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewPager: ViewPager2

    class HotTopicAdapter(
        fragmentManager: FragmentManager, lifecycle: Lifecycle, private val topicList: List<Topic>
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return topicList.size
        }

        override fun createFragment(position: Int): Fragment {
            val topic = topicList[position]
            return HotTopicFragment.newInstance(topic)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewPager = binding.viewPager

        loadHotTopicList()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색어를 입력하고 엔터 또는 검색 버튼을 누르면 SearchFragment로 전환
                if (!query.isNullOrBlank()) {
                    navigateToSearchFragment(query)
                } else {
                    // 검색어가 비어 있을 때 Toast 메시지를 표시
                    Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어 입력이 변경될 때의 동작 (필요하면 구현)
                return false
            }
        })

        binding.searchFragment.setOnClickListener {
            binding.searchView.requestFocus()
        }

        return binding.root
    }

    private fun loadHotTopicList() { // 핫토픽 서버에서 불러오기
        HotTopicManager.getHotTopicData(
            onSuccess = { topicList ->
                Log.d("핫토픽 테스트", ""+topicList)
                val adapter = HotTopicAdapter(childFragmentManager, lifecycle, topicList)
                viewPager.adapter = adapter

                // 데이터가 변경되었으므로 어댑터에게 알려줌
                adapter.notifyDataSetChanged()
            },
            onError = { throwable ->
                Log.e("서버 테스트3", "오류: $throwable")
            }
        )
    }

    private fun navigateToSearchFragment(searchWord: String) {
        val bundle = Bundle()
        bundle.putString("searchWord", searchWord)

        val searchFragment = SearchFragment()
        searchFragment.arguments = bundle

        // FragmentTransaction을 사용하여 SearchFragment로 이동
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(R.id.fragment_container, searchFragment)
        transaction?.addToBackStack(null)
        transaction?.commit()
    }

}