package com.example.diary

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.MapDiaryRecyclerviewBinding
import com.example.diary.databinding.PlanRoadMapRecyclerviewBinding
import java.net.URL
import java.util.concurrent.Executors

class PlanRoadMapAdapter(var datas: List<PlanDetailModel>): RecyclerView.Adapter<PlanRoadMapAdapter.MyViewHolder>() {

    //커스텀 리스너 인터페이스
    interface OnItemClickListener {
        fun onItemClick(v:View, position: Int)
    }

    //리스너 객체 참조 저장
    var mListener: OnItemClickListener? = null

    //리스너 객체 참조를 어댑터에 전달하는 메소드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    inner class MyViewHolder(val binding: PlanRoadMapRecyclerviewBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION && mListener != null) {
                    mListener?.onItemClick(it, pos)
                }
            }
        }

        fun bind(plan: PlanDetailModel) {
            //일정 정보 변경
            if (adapterPosition == 0 || plan.placeDate != datas[adapterPosition - 1].placeDate) {
                binding.placeDate.text = plan.placeDate.toString()
                binding.placeDate.visibility = View.VISIBLE
            } else {
                binding.placeDate.visibility = View.GONE
            }

            binding.placeTime.text = plan.placeStart + " ~ " + plan.placeEnd
            binding.placeName.text = plan.place
            imageLoader(plan.imgURL!!, binding.placeImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(PlanRoadMapRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PlanRoadMapAdapter.MyViewHolder, position: Int) {
        Log.d("mylog", "PlanRoadMap ${position}번 item binding")

        val plan = datas[position]
        holder.bind(plan)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    fun imageLoader(url : String, view : ImageView){
        val executors = Executors.newSingleThreadExecutor()
        var image : Bitmap? = null

        executors.execute {
            try {
                image = BitmapFactory.decodeStream(URL(url).openStream())
            }catch (e : Exception){
                e.printStackTrace()
            }

            // 이미지 로딩이 완료된 후 UI 업데이트를 메인(UI) 스레드에서 수행
            view.post {
                view.setImageBitmap(image)
            }
        }
    }
}