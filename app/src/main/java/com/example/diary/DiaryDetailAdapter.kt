package com.example.diary

import android.view.LayoutInflater
import android.view.View
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
            placeContent.text = data.content

            if (data.place == "MEMO") {
                diaryPlaceDate.visibility = View.GONE
                diaryPlaceTime.visibility = View.GONE
            } else {
                diaryPlaceDate.text = "${data.placeDate}"
                diaryPlaceTime.text = "${data.placeStart} ~ ${data.placeEnd}"
            }
        }

        //이미지 불러오기 추가 필요

    }

    fun updateData(newItems: List<DiaryDetailModel>) {
        if (newItems.isNotEmpty()) { // 리스트가 비어있지 않은 경우에만 업데이트
            itemList.clear()
            itemList.addAll(newItems)
            notifyDataSetChanged()
        }
    }
}