package com.example.diary

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.example.diary.databinding.DiaryDetailPlaceRecyclerviewBinding
import com.example.diary.databinding.DiaryRecyclerviewBinding
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class DiaryAdapter(private var diaries: List<DiaryDetailResponse>) : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>()  {
    private var searchDiary = false // 일기 검색이라면 true, 내 일기 목록이라면 false

    // Amazon S3 관련 설정
    private val awsAccessKey = "1807222EE827BB41A77C"
    private val awsSecretKey = "E9DC72D2C24094CB2FE00763EF33330FB7948154"
    private val awsCredentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))// YOUR_REGION을 원하는 지역으로 변경하세요
    private lateinit var transferUtility: TransferUtility
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_recyclerview, parent, false)
        return DiaryViewHolder(view)
        val awsAccessKey = "1807222EE827BB41A77C"
        val awsSecretKey = "E9DC72D2C24094CB2FE00763EF33330FB7948154"
        val awsCredentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
        val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))
        s3Client.setEndpoint("https://kr.object.ncloudstorage.com")
        // Initialize TransferUtility with a valid context (this)
        transferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(parent.context)
            .defaultBucket("diary")
            .build()
        TransferNetworkLossHandler.getInstance(parent.context)

    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diary = diaries[position]
        holder.bind(diary)
    }

    override fun getItemCount(): Int {
        return diaries.size
    }

    // 데이터 업데이트 메서드 추가
    fun updateData(newDiaries: List<DiaryDetailResponse>, showDiaryInfo: Boolean) {
        diaries = newDiaries
        this.searchDiary = showDiaryInfo
        notifyDataSetChanged()
    }

    inner class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.diary_title)
        private val placeTextView: TextView = itemView.findViewById(R.id.diary_place)
        private val periodTextView: TextView = itemView.findViewById(R.id.diary_period)
        private val diaryLikeView: TextView = itemView.findViewById(R.id.diary_like)
        private val commentView: TextView = itemView.findViewById(R.id.diary_comment)
        private val diaryInfoLayout: LinearLayout = itemView.findViewById(R.id.diary_info)
        private val writerTextView: TextView = itemView.findViewById(R.id.diary_writer)
        private val createTextView: TextView = itemView.findViewById(R.id.diary_create)
        private val diaryLockImg : ImageView = itemView.findViewById(R.id.diary_lock)
        private val diaryImg: ImageView = itemView.findViewById(R.id.diary_img)


        init {
            itemView.setOnClickListener {
                val clickedDiary = diaries[adapterPosition]
                val diaryId = clickedDiary.diaryDto.diaryId // 클릭된 일기의 diaryId를 가져옴
                val intent = Intent(itemView.context, DiaryDetailActivity::class.java)
                intent.putExtra("diaryId", diaryId)
                itemView.context.startActivity(intent)
            }
        }

        fun bind(diary: DiaryDetailResponse) {
            titleTextView.text = diary.diaryDto.title
            placeTextView.text = diary.diaryDto.travelDest
            if (diary.diaryDto.imageUri != null) {
                s3Client.setEndpoint("https://kr.object.ncloudstorage.com")
                // Initialize TransferUtility
                TransferNetworkLossHandler.getInstance(diaryImg.context);

                transferUtility = TransferUtility.builder()
                    .s3Client(s3Client)
                    .context(diaryImg.context)
                    .defaultBucket("diary") // S3 버킷 이름을 변경하세요
                    .build()
                if (diary.diaryDto.imageUri != null) {
                    // 이미지를 여러 개 표시하기 위해 RecyclerView로 변경
                    Log.d("diaryDetailAdapter", ""+diary.diaryDto.imageData)

                    downloadAndInitializeAdapter(diary.diaryDto.imageData.toUri(), diaryImg)
//            val imageAdapter = MultiImageAdapter(uriList as ArrayList<Uri>, holder.binding.root.context)
//            holder.binding.recyclerView.adapter = imageAdapter
//            holder.binding.recyclerView.layoutManager = layoutManager
                    Log.d("detailAdapter", "이미지 추가")
                } else {
                    // 이미지가 없는 경우, RecyclerView를 숨깁니다.
                    Log.d("detailAdapter", "이미지 없음")
                }
            }







        if (searchDiary) { // 일기 검색 목록이라면
                diaryInfoLayout.visibility = View.VISIBLE
                periodTextView.visibility = View.GONE

                writerTextView.text = diary.userDto.username
                createTextView.text = diary.diaryDto.createdAt.toString()
            } else {
                periodTextView.text = "${diary.diaryDto.travelStart} ~ ${diary.diaryDto.travelEnd}"
            }

            //일기 비공개라면
            if (diary.diaryDto.public == false) {
                diaryLockImg.visibility = View.VISIBLE
            }

            // 좋아요, 댓글 수 출력
            diaryLikeView.text = "${diary.diaryDto.likes.size}"
            commentView.text = "${diary.diaryDto.comments.size}"
        }
    }

    private fun downloadAndInitializeAdapter(imageUri: Uri, binding: ImageView) {
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
                Log.e("DiaryDetailAdapter", "이미지 다운로드 오류: $ex")
            }
        })
    }
}