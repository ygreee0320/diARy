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
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diary.databinding.MapDialogBinding
import org.w3c.dom.Text
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors

class MapDialog(context: Context){

    val dialog = Dialog(context)
    lateinit var onClickListener: ButtonClickListener

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: MapDiaryAdapter

    lateinit var placeInfo: MutableMap<String, String?>

    fun myDialog(placeInfo: MutableMap<String, String?>) {
        dialog.setContentView(R.layout.map_dialog)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()

        //변수설정
        this.placeInfo = placeInfo

        //초기화
        //recyclerview
        recyclerView = dialog.findViewById(R.id.map_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(dialog.context, LinearLayoutManager.HORIZONTAL, false)
        adapter = MapDiaryAdapter(emptyList()) //초기 빈 목록으로 어댑터 설정
        recyclerView.adapter = adapter

        //DB 읽기
        loadDiaryList()

        //장소사진
        val imgURL = placeInfo.getValue("imgURL")
        val img = dialog.findViewById<ImageView>(R.id.img)
        ImageLoader.load(imgURL!!, img)

        //장소이름
        val place = dialog.findViewById<TextView>(R.id.place)
        place.text = placeInfo.getValue("title")

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

    interface ButtonClickListener {
        fun onClicked(placeInfo: MutableMap<String, String?>)
    }

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickListener = listener
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
        val x = placeInfo.getValue("x")
        val y = placeInfo.getValue("y")

        Log.d("mylog", "일기 조회 주소 - ${x}, ${y}")

        if (x != null && y != null) {
            MapDiaryListManager.getDiaryListData(
                x,
                y,
                onSuccess = { mapDiaryList ->
                    val diary = mapDiaryList.map { it }
                    Log.d("mylog", "x: ${x}, y: ${y}")
                    Log.d("mylog", "주소별 일기 조회 - ${diary}")
                    adapter.updateData(diary)
                },
                onError = {throwable ->
                    Log.e("mylog", "주소별 일기 조회 실패 - ${throwable}")
                }
            )
        }
    }
}