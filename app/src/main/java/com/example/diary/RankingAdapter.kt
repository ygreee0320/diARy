package com.example.diary

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RankingAdapter(private var rankings: List<MyPlanListResponse>): RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ranking_recyclerview, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val ranking = rankings[position]
        holder.bind(ranking, position + 1)
    }

    override fun getItemCount(): Int {
        return rankings.size
    }

    // 데이터 업데이트 메서드 추가
    fun updateData(newRankings: List<MyPlanListResponse>) {
        rankings = newRankings
        Log.d("my log", "어댑터 업데이트" )
        notifyDataSetChanged()
    }

    inner class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numText: TextView = itemView.findViewById(R.id.ranking_num)
        private val titleText: TextView = itemView.findViewById(R.id.ranking_title)
        private val hashText: TextView = itemView.findViewById(R.id.ranking_hash)
        private val writerText: TextView = itemView.findViewById(R.id.ranking_writer)
        private val likeCountText: TextView = itemView.findViewById(R.id.ranking_like)

        init {
            itemView.setOnClickListener {
                val clickedPlan = rankings[adapterPosition]
                val planId = clickedPlan.plan.planId // 클릭된 플랜의 planId를 가져옴
                val intent = Intent(itemView.context, PlanDetailActivity::class.java)
                intent.putExtra("planId", planId)
                itemView.context.startActivity(intent)
            }
        }

        fun bind(ranking: MyPlanListResponse, position: Int) {
            Log.d("my log", "어댑터 바인딩" )
            numText.text = position.toString() // 순서 매기기
            titleText.text = ranking.plan.travelDest
            writerText.text = ranking.user.username

            val tagNames = ranking.tags.joinToString(" ") { "#${it.name}" }
            hashText.text = tagNames

            PlanLikeListManager.getPlanLikeListData(ranking.plan.planId,
                onSuccess = { planLike ->
                    likeCountText.text = "${planLike.size}"
                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }
    }
}