package com.example.diary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import com.example.diary.databinding.ActivityPlanMapBinding
import com.google.gson.Gson

class PlanMapActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlanMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlanMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //webview
        binding.webview.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true //자바스크립트 허용
            settings.loadWithOverviewMode = true //html의 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 자동 조정
            settings.useWideViewPort = true //html의 viewport 메타 태그 지원
            settings.setSupportZoom(false)
        }

        binding.webview.addJavascriptInterface(PlanMapInterface(this), "Android")

        binding.webview.loadUrl("https://diarymap.netlify.app/planMap.html") //임시 주소
    }

    //toolbar 뒤로가기 버튼
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    //자바스크립트 인터페이스
    inner class PlanMapInterface(val context: Context) {
        val gson = Gson()
        val apiKey = "KakaoAK 00cd17143e8814def8b0c526fdfe59bf"
        val planId = intent.getIntExtra("planId", -1)

        var locationInfo = ""
        //title, x, y, phone, address(지번주소), imgURL

        @JavascriptInterface
        fun showToast(toast: String) {
            Log.d("mylog", toast)
        }


        @JavascriptInterface
        fun moveToRoadmap() {
            val intent = Intent(this@PlanMapActivity, PlanRoadMapActivity::class.java)
            intent.putExtra("place", locationInfo)
            intent.putExtra("planId", planId)
            startActivity(intent)
        }

        @JavascriptInterface
        fun getPlacesInfo(): String {
            val cnt = intent.getIntExtra("placeCnt", 0)
            if (cnt != 0) {
                val locationInfoArray = mutableListOf<Map<String, String>>()
                for (i in 0 until cnt) {
                    val place = intent.getStringArrayListExtra("place${i}")!! //place, x, y, placeDate, placeStart, placeEnd
                    Log.d("mylog", "PlanMapActivity -> place${i}의 정보: ${place}")

                    val searchResponse = SearchPlaceManager.getList(apiKey, place[0], "", place[1], place[2])
                    val foundPlace = findPlaceByCoordinates(searchResponse!!, place[0], place[1], place[2]) ///place -> phone, address를 찾는 함수

                    if (foundPlace != null) {
                        val imgURL = ApiSearchImg().searchImg(foundPlace.place_name) //이미지URL

                        locationInfoArray.add(mapOf(
                            "place" to foundPlace.place_name,
                            "x" to foundPlace.x,
                            "y" to foundPlace.y,
                            "tel" to foundPlace.phone,
                            "address" to foundPlace.address_name,
                            "placeDate" to place[3],
                            "placeStart" to place[4],
                            "placeEnd" to place[5],
                            "imgURL" to imgURL
                        ))

                        Log.d("mylog", "장소 정보 설정 완료 - ${locationInfoArray}")
                    } else {
                        Log.d("mylog", "일치 장소 없음")
                    }
                }
                locationInfo = gson.toJson(locationInfoArray)
                Log.d("mylog", "전달 정보 - ${locationInfo}")
            }
            return locationInfo
        }

        fun findPlaceByCoordinates(searchResponse: SearchResponse, title: String, x: String, y: String): Place? {
            for (place in searchResponse.documents) {
                if (place.place_name == title && place.x == x && place.y == y) {
                    return place
                }
            }
            return null
        }
    }
}