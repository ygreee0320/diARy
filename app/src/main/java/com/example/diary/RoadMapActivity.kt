package com.example.diary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.diary.databinding.ActivityMainBinding
import com.example.diary.databinding.ActivityRoadMapBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoadMapActivity : AppCompatActivity() {
    lateinit var binding: ActivityRoadMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoadMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent()

        //toolbar
        setSupportActionBar(binding.toolbar)
        RoadMapInterface(this).setToolbar()

        //webview
        binding.webview.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true //자바스크립트 허용
            settings.loadWithOverviewMode = true //html의 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 자동 조정
            settings.useWideViewPort = true //html의 viewport 메타 태그 지원
            settings.setSupportZoom(false)
        }

        binding.webview.addJavascriptInterface(RoadMapInterface(this), "Android")

        binding.webview.loadUrl("https://diarymap.netlify.app/roadmap.html") //임시 주소
    }

    //toolbar 뒤로가기 버튼
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class RoadMapInterface(val Context: Context) {
        var title: String? = intent.getStringExtra("title")
        var address: String? = intent.getStringExtra("address")
        var x: String? = intent.getStringExtra("x")
        var y: String? = intent.getStringExtra("y")
        var imgURL: String? = null

        @JavascriptInterface
        fun showToast(toast: String) {
            Log.d("mylog", toast)
        }

        @JavascriptInterface
        fun getPlaceInfo(): String {
            val info = title + "/" + address + "/" + x + "/" + y
            return info
        }

        @JavascriptInterface
        fun showCustomOverlay() {
            val imgURL = ApiSearchImg().searchImg(title!!)

            val placeInfo = mutableMapOf(
                "x" to x,
                "y" to y,
                "title" to title,
                "address" to address,
                "imgURL" to imgURL,
                "panoId" to null, //임시
            )

            if (x != null && y != null) {
                MapDiaryListManager.getDiaryListData(
                    x!!,
                    y!!,
                    onSuccess = { mapDiaryList -> //백그라운드 스레드
                        CoroutineScope(Dispatchers.IO).launch {
                            val diary = mapDiaryList.map { it }
                            Log.d("mylog", "x: ${x}, y: ${y}")
                            Log.d("mylog", "주소별 일기 조회 - ${diary}")
                            withContext(Dispatchers.Main) {
                                val dialog = MapDialog(this@RoadMapActivity)
                                dialog.myDialog(placeInfo, diary)
                            }
                        }
                    },
                    onError = {throwable ->
                        Log.e("mylog", "주소별 일기 조회 실패 - ${throwable}")
                    }
                )
            }
        }

        @JavascriptInterface
        fun getSearchImg(title: String): String {
            Log.d("mylog", "$title")
            imgURL = ApiSearchImg().searchImg(title)
            return imgURL as String
        }

        fun setToolbar() {
            supportActionBar?.title = title
            supportActionBar?.subtitle = address
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }
}