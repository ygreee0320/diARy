package com.example.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommentAdapter(private var comments: List<CommentListResponse>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_recyclerview, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentAdapter.CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    // 데이터 업데이트 메서드 추가
    fun updateData(newComments: List<CommentListResponse>) {
        comments = newComments
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //private val usernameTextView: TextView = itemView.findViewById(R.id.comment_username)
        private val commentTextView: TextView = itemView.findViewById(R.id.comment_text)

        fun bind(comment: CommentListResponse) {
            commentTextView.text = comment.content
        }
    }
}