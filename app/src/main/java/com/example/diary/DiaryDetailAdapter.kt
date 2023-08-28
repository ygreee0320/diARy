package com.example.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.DiaryDetailPlaceRecyclerviewBinding

class DiaryDetailViewHolder(val binding: DiaryDetailPlaceRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class DiaryDetailAdapter (private val itemList: MutableList<DiaryDetailModel>): RecyclerView.Adapter<DiaryDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryDetailViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DiaryDetailViewHolder(DiaryDetailPlaceRecyclerviewBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: DiaryDetailViewHolder, position: Int) {
        val data = itemList.get(position)

        holder.binding.run {
            diaryDetailPlace.text = data.place
            diaryPlaceDate.text = "${data.placeDate}"
            diaryPlaceTime.text = "${data.placeStart} ~ ${data.placeEnd}"
            placeContent.text = data.content
        }

        //이미지 불러오기 추가 필요

    }

    fun updateData(newItems: List<DiaryDetailModel>) {
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }
}