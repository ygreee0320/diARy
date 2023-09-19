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
    private lateinit var searchWord: String
    private lateinit var childFragMang: FragmentManager

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

        childFragMang = childFragmentManager

        loadHotTopicList()

        binding.searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchView.text.toString()
                if (!query.isNullOrBlank()) {
                    navigateToSearchActivity(query)
                } else {
                    // 검색어가 비어 있을 때 Toast 메시지를 표시
                    Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        // 카테고리 클릭 시 검색
        binding.searchFragment.setOnClickListener {
            binding.searchView.requestFocus()
        }

        binding.category1.setOnClickListener {
            searchWord = binding.textView1.text.toString()
            navigateToSearchActivity(searchWord)
        }

        binding.category2.setOnClickListener {
            searchWord = binding.textView2.text.toString()
            navigateToSearchActivity(searchWord)
        }

        binding.category3.setOnClickListener {
            searchWord = binding.textView3.text.toString()
            navigateToSearchActivity(searchWord)
        }

        binding.category4.setOnClickListener {
            searchWord = binding.textView4.text.toString()
            navigateToSearchActivity(searchWord)
        }

        binding.category5.setOnClickListener {
            searchWord = binding.textView5.text.toString()
            navigateToSearchActivity(searchWord)
        }

        binding.category6.setOnClickListener {
            searchWord = binding.textView6.text.toString()
            navigateToSearchActivity(searchWord)
        }

        return binding.root
    }

    private fun loadHotTopicList() { // 핫토픽 서버에서 불러오기
        // HomeFragment가 붙어있는지 확인
//        if (getActivity()!=null && isAdded && isVisible) {
//
//        } else { }
        HotTopicManager.getHotTopicData(
            onSuccess = { topicList ->
                Log.d("핫토픽 테스트", ""+topicList)
                val adapter = HotTopicAdapter(childFragMang, lifecycle, topicList)
                viewPager.adapter = adapter

                // 데이터가 변경되었으므로 어댑터에게 알려줌
                adapter.notifyDataSetChanged()
            },
            onError = { throwable ->
                Log.e("서버 테스트3", "오류: $throwable")
            }
        )
    }

    private fun navigateToSearchActivity(searchWord: String) {
        val intent = Intent(requireContext(), SearchActivity::class.java)
        intent.putExtra("searchWord", searchWord)
        startActivity(intent)
    }

}