package com.example.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlanAdapter(private var plans: List<Plan>) : RecyclerView.Adapter<PlanAdapter.PlanViewHolder>() {

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
    fun updateData(newPlans: List<Plan>) {
        plans = newPlans
        notifyDataSetChanged()
    }

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.plan_title)
        private val periodTextView: TextView = itemView.findViewById(R.id.plan_period)
        // Add more views here if needed

        fun bind(plan: Plan) {
            titleTextView.text = plan.travelDest
            periodTextView.text = "${plan.travelStart} ~ ${plan.travelEnd}"
            // Bind other views here if needed
        }
    }
}