package com.example.diary

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.MapDiaryRecyclerviewBinding
import org.w3c.dom.Text

class MapDiaryAdapter(var datas: List<DiaryDtoList>?): RecyclerView.Adapter<MapDiaryAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder
            = MyViewHolder(MapDiaryRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val diary = datas!![position]
        holder.bind(diary)
    }

    override fun getItemCount(): Int {
        return datas!!.size
    }

    @UiThread
    //데이터 업데이트 메서드
    fun updateData(newDiaries: List<DiaryDtoList>) {
        Log.d("mylog", "MapDiaryAdapter.updateData -> 현재 스레드: ${Thread.currentThread()}")

        datas = newDiaries
        notifyDataSetChanged()
    }

    inner class MyViewHolder(val binding: MapDiaryRecyclerviewBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val pos = datas!![adapterPosition]
                val diaryId = pos.diaryId

                val intent = Intent(itemView.context, DiaryDetailActivity::class.java)
                intent.putExtra("diaryId", diaryId)
                itemView.context.startActivity(intent)
            }
        }

        fun bind(diary: DiaryDtoList) {
            binding.userName.text = diary.userDto.username
            binding.mapDiaryText.text = diary.diaryLocationDto.content
            binding.mapDiaryDate.text =
                "여행기간: " + diary.travelStart.toString() + "~" + diary.travelEnd.toString()
        }
    }
}