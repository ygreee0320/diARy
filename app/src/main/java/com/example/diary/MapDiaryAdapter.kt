package com.example.diary

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.MapDiaryRecyclerviewBinding
import org.w3c.dom.Text

class MapDiaryAdapter(var datas: List<DiaryDtoList>?): RecyclerView.Adapter<MapDiaryAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userImg = itemView.findViewById<ImageView>(R.id.user_img)
        val userName = itemView.findViewById<TextView>(R.id.user_name)
        val content = itemView.findViewById<TextView>(R.id.map_diary_text)
        val date = itemView.findViewById<TextView>(R.id.map_diary_date)

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
            //userImg...
            userName.text = diary.userDto.username
            content.text = diary.diaryLocationDto.content
            date.text = "여행기간: " + diary.travelStart.toString() + "~" + diary.travelEnd.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.map_diary_recyclerview, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val diary = datas!![position]
        holder.bind(diary)
    }

    //데이터 업데이트 메서드
    fun updateData(newDiaries: List<DiaryDtoList>) {
        datas = newDiaries
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }
}