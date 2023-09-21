package com.example.diary

import android.content.Intent
import android.media.Image
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
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import java.io.File

class PlanAdapter(private var plans: List<MyPlanListResponse>) : RecyclerView.Adapter<PlanAdapter.PlanViewHolder>() {
    private var searchPlan = false // 일정 검색이라면 true, 내 일정 목록이라면 false
    // Amazon S3 관련 설정
    private val awsAccessKey = "1807222EE827BB41A77C"
    private val awsSecretKey = "E9DC72D2C24094CB2FE00763EF33330FB7948154"
    private val awsCredentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))// YOUR_REGION을 원하는 지역으로 변경하세요
    private lateinit var transferUtility: TransferUtility
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plan_recyclerview, parent, false)
        return PlanViewHolder(view)

    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = plans[position]
        holder.bind(plan)
    }

    override fun getItemCount(): Int {
        return plans.size
    }

    // 데이터 업데이트 메서드 추가
    fun updateData(newPlans: List<MyPlanListResponse>, showPlanInfo: Boolean) {
        plans = newPlans
        this.searchPlan = showPlanInfo
        notifyDataSetChanged()
    }

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.plan_title)
        private val periodTextView: TextView = itemView.findViewById(R.id.plan_period)
        private val planLikeView: TextView = itemView.findViewById(R.id.plan_like)
        private val planLikeImeView: TextView = itemView.findViewById(R.id.plan_like_img)
        private val planInfoLayout: LinearLayout = itemView.findViewById(R.id.plan_info)
        private val myPlanInfoLayout: LinearLayout = itemView.findViewById(R.id.my_plan_info)
        private val hashTextView: TextView = itemView.findViewById(R.id.plan_hash)
        private val writerTextView: TextView = itemView.findViewById(R.id.plan_writer)
        private val planImg: ImageView = itemView.findViewById(R.id.plan_img)

        init {
            itemView.setOnClickListener {
                val clickedPlan = plans[adapterPosition]
                val planId = clickedPlan.plan.planId // 클릭된 플랜의 planId를 가져옴
                val intent = Intent(itemView.context, PlanDetailActivity::class.java)
                intent.putExtra("planId", planId)
                itemView.context.startActivity(intent)
            }
        }

        fun bind(planList: MyPlanListResponse) {
            titleTextView.text = planList.plan.travelDest
            if (planList.plan.imageUri != null) {
                s3Client.setEndpoint("https://kr.object.ncloudstorage.com")
                // Initialize TransferUtility
                TransferNetworkLossHandler.getInstance(planImg.context);

                val transferUtility = TransferUtility.builder()
                    .s3Client(s3Client)
                    .context(planImg.context)
                    .defaultBucket("plan") // S3 버킷 이름을 변경하세요
                    .build()

                Log.d("PlanAdapter", ""+planList.plan.imageData)

                downloadAndInitializeAdapter(planList.plan.imageData!!.toUri(), planImg, transferUtility)


//            val imageAdapter = MultiImageAdapter(uriList as ArrayList<Uri>, holder.binding.root.context)
//            holder.binding.recyclerView.adapter = imageAdapter
////            holder.binding.recyclerView.layoutManager = layoutManager
//                    Log.d("detailAdapter", "이미지 추가")
//                } else {
//                    // 이미지가 없는 경우, RecyclerView를 숨깁니다.
//                    Log.d("detailAdapter", "이미지 없음")
//                }
            } else {
                Log.d("detailAdapter", "이미지 없음")
            }
            if (searchPlan) { // 일정 검색 목록이라면
                planInfoLayout.visibility = View.VISIBLE
                myPlanInfoLayout.visibility = View.GONE

                writerTextView.text = planList.user.username
                // 해시태그 출력
            } else {
                periodTextView.text = "${planList.plan.travelStart} ~ ${planList.plan.travelEnd}"
            }

            //일정 비공개라면
            if (planList.plan.public == false) {
                planLikeImeView.visibility = View.GONE
                planLikeView.text = "비공개"
            } else {
                planLikeImeView.visibility = View.VISIBLE

                PlanLikeListManager.getPlanLikeListData(planList.plan.planId,
                    onSuccess = { planLike ->
                        planLikeView.text = "${planLike.size}"
                    },
                    onError = { throwable ->
                        Log.e("서버 테스트3", "오류: $throwable")
                    }
                )
            }
        }
    }
    private fun downloadAndInitializeAdapter(imageUri: Uri, binding: ImageView, transferUtility: TransferUtility) {
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