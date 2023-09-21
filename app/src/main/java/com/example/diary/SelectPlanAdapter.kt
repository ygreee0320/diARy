package com.example.diary

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import java.io.File

class SelectPlanAdapter(private var plans: List<MyPlan>) : RecyclerView.Adapter<SelectPlanAdapter.SelectPlanViewHolder>() {
    private val awsAccessKey = "1807222EE827BB41A77C"
    private val awsSecretKey = "E9DC72D2C24094CB2FE00763EF33330FB7948154"
    private val awsCredentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))// YOUR_REGION을 원하는 지역으로 변경하세요
    private lateinit var transferUtility: TransferUtility
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectPlanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.select_plan_recyclerview, parent, false)
        return SelectPlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectPlanViewHolder, position: Int) {
        val plan = plans[position]
        holder.bind(plan)
    }

    override fun getItemCount(): Int {
        return plans.size
    }

    // 데이터 업데이트 메서드 추가
    fun updateData(newPlans: List<MyPlan>) {
        plans = newPlans
        notifyDataSetChanged()
    }

    inner class SelectPlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.plan_title)
        private val periodTextView: TextView = itemView.findViewById(R.id.plan_period)
        private val planLikeView: TextView = itemView.findViewById(R.id.plan_like)
        private val planLikeImeView: TextView = itemView.findViewById(R.id.plan_like_img)
        private val planImg: ImageView = itemView.findViewById(R.id.plan_img)


        init {
            itemView.setOnClickListener {
                // 클릭된 플랜을 바탕으로 AddDiaryActivity 실행 (여행지 자동 표시 필요)
                val clickedPlan = plans[adapterPosition]
                val planId = clickedPlan.planId // 클릭된 플랜의 planId를 가져옴
                val intent = Intent(itemView.context, AddDiaryActivity::class.java)
                intent.putExtra("plan_id", planId)
                intent.putExtra("new_diary", -1)
                itemView.context.startActivity(intent)
            }
        }

        fun bind(plan: MyPlan) {
            titleTextView.text = plan.travelDest
            periodTextView.text = "${plan.travelStart} ~ ${plan.travelEnd}"

            //일정 비공개라면
            if (plan.public == false) {
                planLikeImeView.visibility = View.GONE
                planLikeView.text = "비공개"
            } else {

            }

            if (plan.imageUri != null) {
                s3Client.setEndpoint("https://kr.object.ncloudstorage.com")
                // Initialize TransferUtility
                TransferNetworkLossHandler.getInstance(planImg.context);

                val transferUtility = TransferUtility.builder()
                    .s3Client(s3Client)
                    .context(planImg.context)
                    .defaultBucket("plan") // S3 버킷 이름을 변경하세요
                    .build()

                Log.d("PlanAdapter", "" + plan.imageData)

                downloadAndInitializeAdapter(plan.imageData!!.toUri(), planImg, transferUtility)
            }
        }

        private fun downloadAndInitializeAdapter(
            imageUri: Uri,
            binding: ImageView,
            transferUtility: TransferUtility
        ) {
            val fileName = imageUri.lastPathSegment // 파일 이름을 가져옴
            val downloadFile = File(binding.context.cacheDir, fileName)

            val transferObserver = transferUtility.download(
                "plan",
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
                    Log.e("PlanAdapter", "이미지 다운로드 오류: $ex")
                }
            })
        }
    }
}