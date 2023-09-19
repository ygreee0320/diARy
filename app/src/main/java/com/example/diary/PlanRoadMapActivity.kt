package com.example.diary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.ActivityPlanRoadMapBinding
import com.google.android.material.color.utilities.MaterialDynamicColors.onError
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PlanRoadMapActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlanRoadMapBinding

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: PlanRoadMapAdapter

    lateinit var locationInfo: String
    lateinit var planPlaceList: List<PlanDetailModel>

    var planId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlanRoadMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //초기화
        locationInfo = intent.getStringExtra("place")!! //title, x, y, phone, address, placeDate, placeStart, placeEnd, imgURL -> String

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()
        planPlaceList = gson.fromJson(locationInfo, object: TypeToken<List<PlanDetailModel>>() {}.type) //JSON - 여행지 목록 리스트

        //화면설정
        //toolbar
        setSupportActionBar(binding.toolbar)
        PlanRoadMapInterface(this).setToolbar()

        //webview
        binding.webview.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true //자바스크립트 허용
            settings.loadWithOverviewMode = true //html의 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 자동 조정
            settings.useWideViewPort = true //html의 viewport 메타 태그 지원
            settings.setSupportZoom(false)
        }

        binding.webview.addJavascriptInterface(PlanRoadMapInterface(this), "Android")

        binding.webview.loadUrl("https://diarymap.netlify.app/planRoadmap.html") //임시 주소

        //recyclerview
        recyclerView = binding.planRoadmapRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        )

        adapter = PlanRoadMapAdapter(planPlaceList)
        recyclerView.adapter = adapter

        //recyclerview 아이템 클릭 이벤트 처리
        adapter.setOnItemClickListener(object: PlanRoadMapAdapter.OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                //toolbar 설정
                PlanRoadMapInterface(this@PlanRoadMapActivity).setToolbar(position)

                //(x,y)값 변경하여 로드맵 표시
                val place = planPlaceList[position]

                PlanRoadMapInterface(this@PlanRoadMapActivity).setRoadmap(place.x!!, place.y!!)
            }
        })

        //일정 작성자 정보 조회
        planId = intent.getIntExtra("planId", -1)

        if(planId != -1) {
            PlanDetailManager.getPlanDetailData(
                planId,
                onSuccess = { planDetail ->
                    binding.title.text = planDetail.plan.travelDest //일정 이름
                    //binding.userImg
                    binding.userName.text = planDetail.user.username //작성자 이름
                },
                onError = { throwable ->
                    Log.e("mylog", "PlanRoadMapActivity -> plan 조회 실해 : $throwable")
                }
            )
        }
    }

    //toolbar 뒤로가기 버튼
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class PlanRoadMapInterface(val Context: Context) {

        @JavascriptInterface
        fun showToast(toast: String) {
            Log.d("mylog", toast)
        }

        @JavascriptInterface
        fun getCoords(index: Int = 0): String {
            return planPlaceList[index].x + "," + planPlaceList[index].y
        }

        fun setToolbar(index: Int = 0) {
            Log.d("mylog", "toolbar 설정: ${planPlaceList[index].place} / ${planPlaceList[index].address}")

            supportActionBar?.title = planPlaceList[index].place
            supportActionBar?.subtitle = planPlaceList[index].address
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        fun setRoadmap(x: String, y: String) {
            binding.webview.loadUrl("javascript:planRoadmap.setRoadmap($x, $y)")
        }
    }
}