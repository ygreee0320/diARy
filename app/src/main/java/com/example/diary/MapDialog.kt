package com.example.diary

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diary.databinding.MapDialogBinding
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors

class MapDialog(context: Context, placeInfo: MutableMap<String, String?>): Dialog(context){
    lateinit var onClickListener: ButtonClickListener

    lateinit var binding: MapDialogBinding
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: MapDiaryAdapter

    val placeInfo = placeInfo

    interface ButtonClickListener {
        fun onClicked(placeInfo: MutableMap<String, String?>)
    }

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapDialogBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)

        //초기화
        //recyclerview
        recyclerView = binding.mapRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        adapter = MapDiaryAdapter(emptyList()) //초기 빈 목록으로 어댑터 설정
        recyclerView.adapter = adapter

        loadDiaryList()

        //장소 사진
        val imgURL = placeInfo.getValue("imgURL")
        ImageLoader.load(imgURL!!, binding.img)

        //로드맵 존재 유무
        if (placeInfo.getValue("panoId") == null) {
            binding.roadmapBtn.visibility = View.GONE
        } else {
            binding.roadmapBtn.visibility = View.VISIBLE
        }

        //장소 이름
        binding.place.text = placeInfo.getValue("title")

        //로드맵 이동
        binding.roadmapBtn.setOnClickListener {
            onClickListener.onClicked(placeInfo)
            dismiss()
        }

        //Dialog 닫기
        binding.closeBtn.setOnClickListener {
            dismiss()
        }
    }

    object ImageLoader {
        fun load(url : String, view : ImageView){

            val executors = Executors.newSingleThreadExecutor()
            var image : Bitmap? = null

            executors.execute {
                try {
                    image = BitmapFactory.decodeStream(URL(url).openStream())
                    view.setImageBitmap(image)
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    fun loadDiaryList() {
        val address = placeInfo.getValue("address")

        Log.d("mylog", "일기 조회 주소 - ${address}")

        if (address != null) {
            MapDiaryListManager.getDiaryListData(
                address,
                onSuccess = { mapDiaryList ->
                    val diary = mapDiaryList.map { it.diaryDtoList }
                    Log.d("mylog", "주소별 일기 조회 테스트 - ${diary}")
                    adapter.updateData(diary)
                },
                onError = {throwable ->
                    Log.e("mylog", "주소별 일기 조회 실패 - ${throwable}")
                }
            )
        }
    }
}