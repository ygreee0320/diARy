package com.example.diary

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

}