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

        @JavascriptInterface
        fun showToast(toast: String) {
            Log.d("mylog", toast)
        }

        @JavascriptInterface
        fun getPlaceInfo(): String {
            val info = title + "/" + address + "/" + x + "/" + y
            return info
        }

        fun setToolbar() {
            supportActionBar?.title = title
            supportActionBar?.subtitle = address
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }
}