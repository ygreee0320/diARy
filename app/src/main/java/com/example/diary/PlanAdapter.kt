package com.example.diary

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlanAdapter(private var plans: List<MyPlanListResponse>) : RecyclerView.Adapter<PlanAdapter.PlanViewHolder>() {
    private var searchPlan = false // 일정 검색이라면 true, 내 일정 목록이라면 false

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
}