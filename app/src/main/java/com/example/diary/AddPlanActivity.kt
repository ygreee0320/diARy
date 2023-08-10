package com.example.diary

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aqoong.lib.hashtagedittextview.HashTagEditTextView
import com.example.diary.databinding.ActivityAddPlanBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.*

class AddPlanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlanBinding
    private lateinit var travelStartDate: Date
    private lateinit var travelEndDate: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  //툴바에 뒤로 가기 버튼 추가

        binding.planDateStart.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
                override fun onDateSet(view: DatePicker?, year:Int, month: Int, dayOfMonth: Int) {
                    //binding.planDateStart.text = "${year}-${month+1}-${dayOfMonth}"
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, dayOfMonth)
                    travelStartDate = calendar.time
                    binding.planDateStart.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                }
            }, 2023, 9, 1)
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
        }

        binding.planDateEnd.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
                override fun onDateSet(view: DatePicker?, year:Int, month: Int, dayOfMonth: Int) {
                    //binding.planDateEnd.text = "${year}-${month+1}-${dayOfMonth}"
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, dayOfMonth)
                    travelEndDate = calendar.time
                    binding.planDateEnd.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                }
            }, 2023, 9, 1)
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
        }

        // 저장하기 버튼 클릭 시
        binding.planSaveBtn.setOnClickListener {
            savePlanToServer()  // 서버로 데이터 전송
            finish() // 현재 액티비티 종료
        }

    }

    // Retrofit 인스턴스
    private val retrofit: Retrofit = Retrofit.Builder()
        //.baseUrl("http://192.168.219.140:8080/")
        .baseUrl("http://192.168.200.107:8080/") // 집ip (실제 서버 URL로 대체 필요)
        .addConverterFactory(GsonConverterFactory.create(getGson()))
        .build()

    private fun getGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd") // Date 형식 지정
            .create()
    }

    // Retrofit 서비스 인터페이스
    private val planApi: PlanApi = retrofit.create(PlanApi::class.java)

    // 서버로 plan 데이터 전송
    private fun sendPlanToServer(planData: PlanData) {
        planApi.sendPlan(planData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("서버 테스트", "성공")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("서버 테스트1", "오류: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("서버 테스트2", "오류: ${t.message}")
            }
        })
    }

    // 입력된 데이터를 planData에 넣어서 전송 요청
    private fun savePlanToServer() {
        val travelDest = binding.planTitleEdit.text.toString()
        val content = binding.planSubtitleEdit.text.toString()
        val public = !binding.planLockBtn.isChecked
        val hashTagArray = binding.planHashEdit.getInsertTag() ?: emptyArray()

        val locations: List<Location> = emptyList() // 여행지별 카드
        val tags: List<Tag> = hashTagArray.map { Tag(it) }

//        val plan = Plan(title, content, travelStartDate, travelEndDate, public)
//        val planData = PlanData(plan, locations, tags)
//
//        Log.d("서버 테스트", ""+planData)
//        sendPlanToServer(planData)
        // 날짜 초기화가 되었다면
        if (::travelStartDate.isInitialized && ::travelEndDate.isInitialized) {
            val plan = Plan(travelDest, content, travelStartDate, travelEndDate, public)
            val planData = PlanData(plan, locations, tags)

            Log.d("서버 테스트", "" + planData)
            sendPlanToServer(planData)
        } else {
            Log.e("서버 테스트", "여행 시작일과 종료일을 선택하세요.")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { // 뒤로 가기 버튼 클릭 시
                finish() // 현재 액티비티 종료
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

}

// Retrofit API 인터페이스
interface PlanApi {
    @POST("plan") // 서버 주소/plan 으로 POST
    fun sendPlan(@Body planData: PlanData): Call<Void>
}