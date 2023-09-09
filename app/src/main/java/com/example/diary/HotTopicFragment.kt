package com.example.diary

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import com.example.diary.databinding.FragmentHotTopicBinding

class HotTopicFragment : Fragment() {
    private lateinit var binding: FragmentHotTopicBinding

    companion object {
        fun newInstance(topic: Topic): HotTopicFragment {
            val fragment = HotTopicFragment()
            val args = Bundle()
            args.putString("hotTopic", topic.tagname)

            // topic의 diaryResponseDtoList에서 diaryId 값을 추출하여 리스트로 저장
            val diaryIds = topic.diaryResponseDtoList.map { it.diaryDto.diaryId }
            args.putIntegerArrayList("diaryId", ArrayList(diaryIds))

            val diaryTitle = topic.diaryResponseDtoList.map { it.diaryDto.title }
            args.putStringArrayList("diaryTitle", ArrayList(diaryTitle))

            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHotTopicBinding.inflate(inflater, container, false)

        // Bundle에서 hotTopic을 추출
        val hotTopic = arguments?.getString("hotTopic")
        binding.topicTxt.text = hotTopic

        val diaryTitles = arguments?.getStringArrayList("diaryTitle")
        val diaryIds = arguments?.getIntegerArrayList("diaryId")

        // diaryTitle을 각각의 TextView에 설정
        if (diaryIds != null) {
            if (diaryIds.size >= 1) {
                binding.diaryTitle1.text = diaryTitles?.getOrNull(0) ?: ""
            } else {
                binding.diary1.visibility = GONE
            }

            if (diaryIds.size >= 2) {
                binding.diaryTitle2.text = diaryTitles?.getOrNull(1) ?: ""
            } else {
                binding.diary2.visibility = GONE
            }

            if (diaryIds.size >= 3) {
                binding.diaryTitle3.text = diaryTitles?.getOrNull(2) ?: ""
            } else {
                binding.diary3.visibility = GONE
            }
        }
        Log.d("핫토픽 어댑터", "" + hotTopic + diaryTitles)

        // 핫토픽 클릭 시, 아이디를 가지고 다이어리 디테일 페이지로 이동
        binding.diary1.setOnClickListener {
            if (diaryIds != null && diaryIds.isNotEmpty()) {
                val diaryId = diaryIds[0]
                val intent = Intent(requireContext(), DiaryDetailActivity::class.java)
                intent.putExtra("diaryId", diaryId)
                startActivity(intent)
            }
        }

        binding.diary2.setOnClickListener {
            if (diaryIds != null && diaryIds.size >= 2) {
                val diaryId = diaryIds[1]
                val intent = Intent(requireContext(), DiaryDetailActivity::class.java)
                intent.putExtra("diaryId", diaryId)
                startActivity(intent)
            }
        }

        binding.diary3.setOnClickListener {
            if (diaryIds != null && diaryIds.size >= 3) {
                val diaryId = diaryIds[2]
                val intent = Intent(requireContext(), DiaryDetailActivity::class.java)
                intent.putExtra("diaryId", diaryId)
                startActivity(intent)
            }
        }

        return binding.root
    }

}