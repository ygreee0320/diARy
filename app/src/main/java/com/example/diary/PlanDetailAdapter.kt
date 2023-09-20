package com.example.diary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.PlanDetailRecyclerviewBinding
import java.net.URL
import java.util.concurrent.Executors

class PlanDetailViewHolder(val binding: PlanDetailRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class PlanDetailAdapter(private val itemList: MutableList<PlanDetailModel>): RecyclerView.Adapter<PlanDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanDetailViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PlanDetailViewHolder(PlanDetailRecyclerviewBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: PlanDetailViewHolder, position: Int) {
        val data = itemList.get(position)

        holder.binding.run {
            placeTime.text = "${data.placeStart} ~ ${data.placeEnd}"
            placeName.text = data.place
//            imageLoader(data.imgURL!!, placeImageView)
            if (data.imgURL != null) {
                imageLoader(data.imgURL!!, placeImageView)
            } else {}
        }
    }

    fun updateData(newItems: List<PlanDetailModel>) {
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
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