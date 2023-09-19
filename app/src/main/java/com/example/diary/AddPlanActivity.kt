package com.example.diary

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.example.diary.PlanManager.sendModPlanToServer
import com.example.diary.PlanManager.sendPlanToServer
import com.example.diary.databinding.ActivityAddPlanBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.sql.Date
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

class AddPlanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlanBinding
    private lateinit var viewModel: AddPlanViewModel
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var authToken: String ?= "" // 로그인 토큰
    private var new: Int ?= 1 // 새로 작성이면 1, 수정이면 0
    private var planId: Int ?= -1 // 일정 수정일 때의 해당 플랜 아이디
    private var uriList = ArrayList<Uri>()

    // 여행지 데이터를 저장할 리스트
    private val planPlaceList = mutableListOf<PlanDetailModel>()
    private val planDetailAdapter = PlanDetailAdapter(planPlaceList)

    companion object {
        lateinit var planInMapActivityResult: ActivityResultLauncher<Intent>
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        // 저장된 토큰 읽어오기
        val sharedPreferences = getSharedPreferences("my_token", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("auth_token", null)
        val userId = sharedPreferences.getInt("userId", -1)

        // 새로 작성 or 수정 (1이면 새로 작성, 아니면 수정)
        new = intent.getIntExtra("new_plan", 1)

        binding.planDetailRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddPlanActivity)
            adapter = planDetailAdapter
        }

        viewModel = ViewModelProvider(this).get(AddPlanViewModel::class.java)

        // AddPlaceInPlanActivity(지도)를 시작하기 위한 요청 코드 정의 (이미지 추가 필요)
        planInMapActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val position = data?.getIntExtra("itemPosition", -1)
                val enteredPlace = data?.getStringExtra("enteredPlace")
                val enteredAddress = data?.getStringExtra("enteredAddress")
                val enteredTel = data?.getStringExtra("enteredTel")
                val enteredDate = data?.getStringExtra("enteredDateS")
                val enteredStart = data?.getStringExtra("enteredTimeS")
                val enteredEnd = data?.getStringExtra("enteredTimeE")
                val x = data?.getStringExtra("x")
                val y = data?.getStringExtra("y")

                //val imageUris = data?.getParcelableArrayListExtra<Uri>("imageUris")
                Log.d("리사이클러뷰", ""+position)

                val placeDate: Date = try {
                    java.sql.Date(dateFormat.parse(enteredDate).time)
                } catch (e: Exception) { java.sql.Date(System.currentTimeMillis()) }

                if (position != null && position >= 0) {
                    val item = planPlaceList[position]
                    item.place = enteredPlace
                    //item.imageUris = imageUris
                    planDetailAdapter.notifyItemChanged(position)
                } else {
                    if (!enteredPlace.isNullOrEmpty()) {
                        // planDetailModel 인스턴스를 생성하고 리스트에 추가
                        CoroutineScope(Dispatchers.IO).launch {
                            val imgURL = ApiSearchImg().searchImg(enteredPlace)
                            val newPlanPlaceModel =
                                PlanDetailModel(place = enteredPlace, address = enteredAddress, tel = enteredTel, imgURL = imgURL,
                                    placeDate = placeDate, placeStart = enteredStart, placeEnd = enteredEnd, x = x, y = y)
                            withContext(Dispatchers.Main) {
                                planPlaceList.add(newPlanPlaceModel)
                                // 어댑터에 데이터 변경을 알림
                                planDetailAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }

        if (new != 1) { // 기존의 일정 수정이라면, 기존의 일정 내용 그대로 출력
            planId = intent.getIntExtra("plan_id", -1)
            Log.d("일정 수정", "" +planId)
            PlanDetailManager.getPlanDetailData(
                planId!!,
                onSuccess = { planDetail ->
                    // 플랜 상세 정보를 UI에 적용하는 작업
                    val editTravelDest = Editable.Factory.getInstance().newEditable(planDetail.plan.travelDest)
                    val editContext = Editable.Factory.getInstance().newEditable(planDetail.plan.content)
                    val editHash = Editable.Factory.getInstance().newEditable(planDetail.tags.joinToString(" ") { "#${it.name}" })
                    binding.planTitleEdit.text = editTravelDest
                    binding.planSubtitleEdit.text = editContext
                    binding.planHashEdit.text = editHash

                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedStartDate = dateFormatter.format(planDetail.plan.travelStart)
                    val formattedEndDate = dateFormatter.format(planDetail.plan.travelEnd)

                    binding.planDateStart.text = formattedStartDate
                    binding.planDateEnd.text = formattedEndDate
                    if (planDetail.plan.imageUri != null) {
                        binding.planImgBtn.setImageURI(planDetail.plan.imageUri.toUri())
                        uriList.add(planDetail.plan.imageUri.toUri())
                    }



                    CoroutineScope(Dispatchers.IO).launch {
                        val planDetailModels: List<PlanDetailModel> = planDetail.locations.map { locationDetail ->
                            val formattedStartTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(locationDetail.timeStart)
                            val formattedEndTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(locationDetail.timeEnd)
                            val imgURL = ApiSearchImg().searchImg(locationDetail.name)

                            PlanDetailModel(
                                place = locationDetail.name,
                                address = locationDetail.address,
                                placeDate = locationDetail.date,
                                placeStart = formattedStartTime, // timeStart를 원하는 형식으로 변환
                                placeEnd = formattedEndTime,    // timeEnd를 원하는 형식으로 변환
                                x = locationDetail.x,
                                y = locationDetail.y,
                                imgURL = imgURL
                            )
                        }
                        withContext(Dispatchers.Main) {
                            planDetailAdapter.updateData(planDetailModels)
                        }
                    }
                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }

        // 현재 날짜를 가져옴
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        binding.planDateStart.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
                override fun onDateSet(view: DatePicker?, year:Int, month: Int, dayOfMonth: Int) {
                    binding.planDateStart.text = "${year}-${month+1}-${dayOfMonth}"
                }
            }, currentYear, currentMonth, currentDayOfMonth)
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
        }

        binding.planDateEnd.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
                override fun onDateSet(view: DatePicker?, year:Int, month: Int, dayOfMonth: Int) {
                    binding.planDateEnd.text = "${year}-${month+1}-${dayOfMonth}"
                }
            }, currentYear, currentMonth, currentDayOfMonth)
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
        }

        binding.planImgBtn.setOnClickListener { // 이미지 버튼 클릭 시, 갤러리로 이동해서 이미지 부착
            singleImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }

        // 저장하기 버튼 클릭 시
        binding.planSaveBtn.setOnClickListener {
            if (new == 1) {
                savePlanToServer()  // 서버로 데이터 전송
                finish() // 현재 액티비티 종료
            } else {
                modPlanToServer()  // 서버로 수정된 일정 전송
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish() // 현재 액티비티 종료
            }
        }

        // 여행지 추가 버튼 클릭 시, 지도로 연결
        binding.placeAddNew.setOnClickListener {
            // 기존의 입력을 ViewModel에 저장
            viewModel.enteredTitle = binding.planTitleEdit.text.toString()
            viewModel.enteredSubTitle = binding.planSubtitleEdit.text.toString()
            viewModel.enteredStart = binding.planDateStart.text.toString()
            viewModel.enteredEnd = binding.planDateEnd.text.toString()
            viewModel.enteredHash = binding.planHashEdit.text.toString()
            viewModel.enteredClosed = binding.planLockBtn.isChecked

            val intent = Intent(this, AddPlanMapActivity::class.java)
            //startActivity(intent)
            planInMapActivityResult.launch(intent) //지도로 이동
            //requestLauncher.launch(intent) : 인텐트를 보내어 result로 데이터를 다시 받아옴
            //->setResult(Activity.RESULT_OK, intent)
            //->finish()
        }

        // 툴바 취소 버튼 클릭 시
        binding.planCancelBtn.setOnClickListener {
            finish()
        }
    }

    private val singleImagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                // 이미 선택된 사진을 지웁니다.
                uriList.clear()

                uriList.add(uri)
                binding.planImgBtn.setImageURI(uri)

                // URI에 대한 지속적인 권한을 부여합니다.
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                applicationContext.contentResolver.takePersistableUriPermission(uri, flag)
            } else {
                Toast.makeText(applicationContext, "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
            }
        }

    // 입력된 데이터를 planData에 넣어서 전송 요청 (일정 추가)
    private fun savePlanToServer() {
        val travelDest = binding.planTitleEdit.text.toString()
        val content = binding.planSubtitleEdit.text.toString()
        val public = !binding.planLockBtn.isChecked
        val hashTagArray = binding.planHashEdit.getInsertTag() ?: emptyArray()
        val travelStart = binding.planDateStart.text.toString()
        val travelEnd = binding.planDateEnd.text.toString()
        val imageUri = uriList[0]

        val imageData = getRealPathFromURI(imageUri)
        val file = File(imageData)
        val transferUtility : TransferUtility? = null
        val uploadObserver = transferUtility?.upload("diary", file.toString(), file)
        uploadObserver!!.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                Log.d("onStateChanged: $id", "${state.toString()}")
            }

            override fun onProgressChanged(
                id: Int,
                bytesCurrent: Long,
                bytesTotal: Long
            ) {
                val percentDonef = (bytesCurrent.toFloat() / bytesTotal.toFloat()) * 100
                val percentDone = percentDonef.toInt()
                Log.d("ID:" ,"$id bytesCurrent: $bytesCurrent bytesTotal: $bytesTotal $percentDone%")
            }

            override fun onError(id: Int, ex: Exception) {
            }
        })

        val locations: List<Location> = planPlaceList.map { planDetail ->
            val timeStartUtil: java.util.Date = timeFormat.parse(planDetail.placeStart)
            val timeStartSql: Time = Time(timeStartUtil.time)

            val timeEndUtil: java.util.Date = timeFormat.parse(planDetail.placeEnd)
            val timeEndSql: Time = Time(timeEndUtil.time)
            Location(
                date = planDetail.placeDate,
                timeStart = timeStartSql,
                timeEnd = timeEndSql,
                name = planDetail.place ?: "",
                address = planDetail.address ?: "",
                x = planDetail.x ?: "",
                y = planDetail.y ?: ""
            )
        }

        val tags: List<Tag> = hashTagArray.map { Tag(it) }

        val travelStartDate: Date = try {
            java.sql.Date(dateFormat.parse(travelStart).time)
        } catch (e: Exception) { java.sql.Date(System.currentTimeMillis()) }

        val travelEndDate: Date = try {
            java.sql.Date(dateFormat.parse(travelEnd).time)
        } catch (e: Exception) { java.sql.Date(System.currentTimeMillis()) }

        val plan = Plan(travelDest, content, travelStartDate, travelEndDate, imageData.toString(), imageUri.toString(), public)
        val planData = PlanData(plan, locations, tags)

        Log.d("서버 테스트", ""+planData)

        if (authToken != null) {
            sendPlanToServer(planData, authToken!!)
        }


//        // 날짜 초기화가 되었다면
//        if (travelStartDate != null && travelEndDate != null) {
//            val plan = Plan(travelDest, content, travelStartDate, travelEndDate, public)
//            val planData = PlanData(plan, locations, tags)
//
//            Log.d("서버 테스트", "" + planData)
//            sendPlanToServer(planData)
//        } else {
//            Log.e("서버 테스트", "여행 시작일과 종료일을 선택하세요.")
//        }
    }

    // 일정 수정
    private fun modPlanToServer() {
        val travelDest = binding.planTitleEdit.text.toString()
        val content = binding.planSubtitleEdit.text.toString()
        val public = !binding.planLockBtn.isChecked
        val hashTagArray = binding.planHashEdit.getInsertTag() ?: emptyArray()
        val travelStart = binding.planDateStart.text.toString()
        val travelEnd = binding.planDateEnd.text.toString()
        var imageUri: Uri? = null
        var imageData: String? = null
        val file: File?
        val transferUtility : TransferUtility? = null
        if (uriList[0] != null) {
            imageUri = uriList[0]

            imageData = getRealPathFromURI(imageUri)
            file = File(imageData)
            val uploadObserver = transferUtility?.upload("diary", file.toString(), file)
            uploadObserver!!.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    Log.d("onStateChanged: $id", "${state.toString()}")
                }

                override fun onProgressChanged(
                    id: Int,
                    bytesCurrent: Long,
                    bytesTotal: Long
                ) {
                    val percentDonef = (bytesCurrent.toFloat() / bytesTotal.toFloat()) * 100
                    val percentDone = percentDonef.toInt()
                    Log.d("ID:" ,"$id bytesCurrent: $bytesCurrent bytesTotal: $bytesTotal $percentDone%")
                }

                override fun onError(id: Int, ex: Exception) {
                }
            })
        }


        val locations: List<Location> = planPlaceList.map { planDetail ->
            val timeStartUtil: java.util.Date = timeFormat.parse(planDetail.placeStart)
            val timeStartSql: Time = Time(timeStartUtil.time)

            val timeEndUtil: java.util.Date = timeFormat.parse(planDetail.placeEnd)
            val timeEndSql: Time = Time(timeEndUtil.time)
            Location(
                date = planDetail.placeDate,
                timeStart = timeStartSql,
                timeEnd = timeEndSql,
                name = planDetail.place ?: "",
                address = planDetail.address ?: "",
                x = planDetail.x ?: "",
                y = planDetail.y ?: ""
            )
        }

        val tags: List<Tag> = hashTagArray.map { Tag(it) }

        val travelStartDate: Date = try {
            java.sql.Date(dateFormat.parse(travelStart).time)
        } catch (e: Exception) { java.sql.Date(System.currentTimeMillis()) }

        val travelEndDate: Date = try {
            java.sql.Date(dateFormat.parse(travelEnd).time)
        } catch (e: Exception) { java.sql.Date(System.currentTimeMillis()) }

        val plan = Plan(travelDest, content, travelStartDate, travelEndDate,
            imageData, imageUri.toString(), public)
        val planData = PlanData(plan, locations, tags)

        Log.d("서버 테스트", ""+planData)

        if (authToken != null) {
            sendModPlanToServer(planId!!, planData, authToken!!)
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

    override fun onResume() {
        super.onResume()

        // 이전에 입력한 텍스트를 복원하여 보여줌
        binding.planTitleEdit.setText(viewModel.enteredTitle)
        binding.planSubtitleEdit.setText(viewModel.enteredSubTitle)

        if (viewModel.enteredStart != null || viewModel.enteredEnd != null) {
            binding.planDateStart.setText(viewModel.enteredStart)
            binding.planDateEnd.setText(viewModel.enteredEnd)
        }

        if (viewModel.enteredHash != null) {
            binding.planHashEdit.setText(viewModel.enteredHash)
        }

        binding.planLockBtn.isChecked = viewModel.enteredClosed
    }
    private fun getRealPathFromURI(uri: Uri): String? {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            val tempFile = createTempFile("temp_image", ".jpg")
            val outputStream = FileOutputStream(tempFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // 4K buffer
                var bytesRead: Int
                while (true) {
                    bytesRead = stream.read(buffer)
                    if (bytesRead == -1) break
                    output.write(buffer, 0, bytesRead)
                }
                return tempFile.absolutePath
            }
        }
        return null
    }
}
