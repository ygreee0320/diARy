package com.example.diary

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide.init

class DiaryAdapter(private var diaries: List<DiaryDetailResponse>) : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>()  {
    private var searchDiary = false // 일기 검색이라면 true, 내 일기 목록이라면 false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_recyclerview, parent, false)
        return DiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diary = diaries[position]
        holder.bind(diary)
    }

    override fun getItemCount(): Int {
        return diaries.size
    }

    // 데이터 업데이트 메서드 추가
    fun updateData(newDiaries: List<DiaryDetailResponse>, showDiaryInfo: Boolean) {
        diaries = newDiaries
        this.searchDiary = showDiaryInfo
        notifyDataSetChanged()
    }

    inner class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.diary_title)
        private val placeTextView: TextView = itemView.findViewById(R.id.diary_place)
        private val periodTextView: TextView = itemView.findViewById(R.id.diary_period)
        private val diaryLikeView: TextView = itemView.findViewById(R.id.diary_like)
        private val commentView: TextView = itemView.findViewById(R.id.diary_comment)
        private val diaryInfoLayout: LinearLayout = itemView.findViewById(R.id.diary_info)
        private val writerTextView: TextView = itemView.findViewById(R.id.diary_writer)
        private val createTextView: TextView = itemView.findViewById(R.id.diary_create)
        private val diaryLockImg : ImageView = itemView.findViewById(R.id.diary_lock)

        init {
            itemView.setOnClickListener {
                val clickedDiary = diaries[adapterPosition]
                val diaryId = clickedDiary.diaryDto.diaryId // 클릭된 일기의 diaryId를 가져옴
                val intent = Intent(itemView.context, DiaryDetailActivity::class.java)
                intent.putExtra("diaryId", diaryId)
                itemView.context.startActivity(intent)
            }
        }

        fun bind(diary: DiaryDetailResponse) {
            titleTextView.text = diary.diaryDto.title
            placeTextView.text = diary.diaryDto.travelDest

            if (searchDiary) { // 일기 검색 목록이라면
                diaryInfoLayout.visibility = View.VISIBLE
                periodTextView.visibility = View.GONE

                writerTextView.text = diary.userDto.username
                createTextView.text = diary.diaryDto.createdAt.toString()
            } else {
                periodTextView.text = "${diary.diaryDto.travelStart} ~ ${diary.diaryDto.travelEnd}"
            }

            //일기 비공개라면
            if (diary.diaryDto.public == false) {
                diaryLockImg.visibility = View.VISIBLE
            }

            // 좋아요, 댓글 수 출력
            diaryLikeView.text = "${diary.diaryDto.likes.size}"
            commentView.text = "${diary.diaryDto.comments.size}"
        }
    }
}