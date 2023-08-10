package com.example.diary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.DiaryDetailPlaceRecyclerviewBinding

class DiaryPlaceAdapter (private val itemList: List<DiaryPlaceModel>) :
    RecyclerView.Adapter<DiaryPlaceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DiaryDetailPlaceRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    inner class ViewHolder(private val binding: DiaryDetailPlaceRecyclerviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DiaryPlaceModel) {
            // 데이터를 레이아웃의 뷰에 바인딩합니다.
            // binding.diaryDetailPlace.text = item.placeName
            // binding.diaryDetailPlaceTime.text = item.placeTime
            binding.placeContent.text = item.content

            // 이미지 리사이클러뷰 초기화 및 어댑터 연결
            val imageRecyclerView = binding.recyclerView
            imageRecyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, true)

            // 이미지 어댑터 초기화 및 연결
            val imageAdapter = MultiImageAdapter(item.imageUris ?: ArrayList(), binding.root.context)
            imageRecyclerView.adapter = imageAdapter
        }
    }
}