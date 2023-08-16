package com.example.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

//class DiaryAdapter(private var diaries: List<MyDiary>) : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>()  {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_recyclerview, parent, false)
//        return DiaryViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
//        val diary = diaries[position]
//        holder.bind(diary)
//    }
//
//    override fun getItemCount(): Int {
//        return diaries.size
//    }
//
//    // 데이터 업데이트 메서드 추가
//    fun updateData(newDiaries: List<MyPlan>) {
//        diaries = newDiaries
//        notifyDataSetChanged()
//    }
//
//    inner class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//    }
//}