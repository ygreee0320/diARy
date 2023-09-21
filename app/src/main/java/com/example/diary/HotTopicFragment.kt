package com.example.diary

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import com.example.diary.databinding.FragmentHotTopicBinding
import java.io.File

class HotTopicFragment : Fragment() {
    private lateinit var binding: FragmentHotTopicBinding
    private val awsAccessKey = "1807222EE827BB41A77C"
    private val awsSecretKey = "E9DC72D2C24094CB2FE00763EF33330FB7948154"
    private val awsCredentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))// YOUR_REGION을 원하는 지역으로 변경하세요
    private lateinit var transferUtility: TransferUtility
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

            val diaryImages = topic.diaryResponseDtoList.map {it.diaryDto.imageData}
            args.putStringArrayList("diaryImg", ArrayList(diaryImages))

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
        val diaryImages = arguments?.getStringArrayList("diaryImg")

        s3Client.setEndpoint("https://kr.object.ncloudstorage.com")

        // diaryTitle을 각각의 TextView에 설정
        if (diaryIds != null) {
            if (diaryIds.size >= 1) {
                binding.diaryTitle1.text = diaryTitles?.getOrNull(0) ?: ""
                if (diaryImages?.getOrNull(0) != "null") {
//                if (diaryImages!![0] != null) {
                    TransferNetworkLossHandler.getInstance(binding.img1.context);

                    val transferUtility = TransferUtility.builder()
                        .s3Client(s3Client)
                        .context(binding.img1.context)
                        .defaultBucket("diary") // S3 버킷 이름을 변경하세요
                        .build()

                    Log.d("diaryAdapter", "" + diaryImages!![0])

                    downloadAndInitializeAdapter(diaryImages[0].toUri(), binding.img1, transferUtility)
                }
            } else {
                binding.diary1.visibility = GONE
            }

            if (diaryIds.size >= 2) {
                binding.diaryTitle2.text = diaryTitles?.getOrNull(1) ?: ""
                if (diaryImages?.getOrNull(1) != null) {
                    TransferNetworkLossHandler.getInstance(binding.img2.context);

                    val transferUtility = TransferUtility.builder()
                        .s3Client(s3Client)
                        .context(binding.img2.context)
                        .defaultBucket("diary") // S3 버킷 이름을 변경하세요
                        .build()

                    Log.d("diaryAdapter", "" + diaryImages[1])

                    downloadAndInitializeAdapter(diaryImages[1].toUri(), binding.img2, transferUtility)
                }
            } else {
                binding.diary2.visibility = GONE
            }

            if (diaryIds.size >= 3) {
                binding.diaryTitle3.text = diaryTitles?.getOrNull(2) ?: ""
                if (diaryImages?.getOrNull(2) != null) {
                    TransferNetworkLossHandler.getInstance(binding.img3.context);

                    val transferUtility = TransferUtility.builder()
                        .s3Client(s3Client)
                        .context(binding.img3.context)
                        .defaultBucket("diary") // S3 버킷 이름을 변경하세요
                        .build()

                    Log.d("diaryAdapter", "" + diaryImages[2])

                    downloadAndInitializeAdapter(diaryImages[2].toUri(), binding.img3, transferUtility)
                }
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

    private fun downloadAndInitializeAdapter(
        imageUri: Uri,
        binding: ImageView,
        transferUtility: TransferUtility
    ) {
        val fileName = imageUri.lastPathSegment // 파일 이름을 가져옴
        val downloadFile = File(binding.context.cacheDir, fileName)

        val transferObserver = transferUtility.download(
            "diary",
            imageUri.toString(),
            downloadFile
        )

        transferObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    // 이미지 다운로드가 완료되었습니다. 이제 ImageView에 이미지를 표시하세요.
                    val downloadedImageUri = Uri.fromFile(downloadFile)
                    Glide.with(binding.context)
                        .load(downloadedImageUri)
                        .into(binding)
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                // 진행 상태 업데이트
            }

            override fun onError(id: Int, ex: Exception) {
                Log.e("diaryAdapter", "이미지 다운로드 오류: $ex")
            }
        })
    }

}