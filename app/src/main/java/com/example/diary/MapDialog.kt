package com.example.diary

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.utilities.MaterialDynamicColors.onError
import kotlinx.coroutines.*
import java.net.URL
import java.util.concurrent.Executors

class MapDialog(context: Context) {
    val dialog = Dialog(context)
    lateinit var onClickListener: ButtonClickListener

    interface ButtonClickListener {
        fun onClicked(placeInfo: MutableMap<String, String?>)
    }

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickListener = listener
    }


    fun myDialog(placeInfo: MutableMap<String, String?>, diary: List<DiaryDtoList>) {
        dialog.setContentView(R.layout.map_dialog)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.show()

        //recyclerview
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.map_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(dialog.context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = MapDiaryAdapter(diary) //초기 빈 목록으로 어댑터 설정
        recyclerView.adapter = adapter

        //DB 읽기
        val x = placeInfo.getValue("x")
        val y = placeInfo.getValue("y")

        Log.d("mylog", "일기 조회 주소 - ${x}, ${y}")

        //장소사진
        val imgURL = placeInfo.getValue("imgURL")
        val img = dialog.findViewById<ImageView>(R.id.img)
        imageLoader(imgURL!!, img)

        //장소이름
        val place = dialog.findViewById<TextView>(R.id.place)
        place.text = placeInfo.getValue("title")

        //만족도(임시)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.diary_progress)
        val data = diary.map { it.satisfaction }
        val avg = if (data.size > 0) data.sum() / data.size else 0.0
        progressBar.progress = avg.toInt()

        val satisfaction = dialog.findViewById<TextView>(R.id.diary_sat)
        satisfaction.text = avg.toString() + "%"

        //로드맵 존재 유무
        val roadmapBtn = dialog.findViewById<Button>(R.id.roadmapBtn)
        if (placeInfo.getValue("panoId") == null) {
            roadmapBtn.visibility = View.GONE
        } else {
            roadmapBtn.visibility = View.VISIBLE
        }

        //로드맵 이동
        roadmapBtn.setOnClickListener {
            onClickListener.onClicked(placeInfo)
            dialog.dismiss()
        }

        //Dialog 닫기
        val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)
        closeBtn.setOnClickListener {
            dialog.dismiss()
        }
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
            view.post {
                view.setImageBitmap(image)
            }
        }
    }
}