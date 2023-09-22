package com.example.diary

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import com.example.diary.databinding.ActivityAddDiaryBinding
import com.google.android.material.color.utilities.MaterialDynamicColors.onError
import java.io.File
import java.io.FileOutputStream
import java.sql.Date
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddDiaryActivity : AppCompatActivity(), DiaryPlaceAdapter.ItemClickListener {
    private lateinit var binding: ActivityAddDiaryBinding
    private lateinit var viewModel: AddDiaryViewModel
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    // Amazon S3 관련 설정
    private val awsAccessKey = "1807222EE827BB41A77C"
    private val awsSecretKey = "E9DC72D2C24094CB2FE00763EF33330FB7948154"
    private val awsCredentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))// YOUR_REGION을 원하는 지역으로 변경하세요
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var transferUtility: TransferUtility
    private val REQUEST_CODE = 123

    //대표이미지 추가를 위한
    private var uriList = ArrayList<Uri>()

    private var new: Int? = 1 // 새로 작성이면 1, 수정이면 0, 플랜 바탕 작성이면 -1
    private var diaryId: Int? = -1 // 일기 수정일 때의 해당 일기 아이디
    private var planId: Int? = -1 // 플랜 바탕 작성일 때의 해당 플랜 아이디

    // 여행지 데이터를 저장할 리스트
    private val diaryPlaceList = mutableListOf<DiaryPlaceModel>()
    private val diaryPlaceAdapter = DiaryPlaceAdapter(diaryPlaceList, new!!)

    companion object {
        lateinit var addPlaceActivityResult: ActivityResultLauncher<Intent>
        lateinit var addContentActivityResult: ActivityResultLauncher<Intent>
    }

    private val singleImagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                // 이미 선택된 사진을 지웁니다.
                uriList.clear()

                uriList.add(uri)
                binding.diaryImgBtn.setImageURI(uri)

                // URI에 대한 지속적인 권한을 부여합니다.
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                applicationContext.contentResolver.takePersistableUriPermission(uri, flag)
            } else {
                Toast.makeText(applicationContext, "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
            }
        }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 새로 작성 or 수정 (1이면 새로 작성, 아니면 수정)
        new = intent.getIntExtra("new_diary", 1)

        binding.diaryAddPlaceRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddDiaryActivity)
            adapter = diaryPlaceAdapter
        }
        // Initialize TransferUtility with a valid context (this)
        transferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(this)
            .defaultBucket("diary")
            .build()
        TransferNetworkLossHandler.getInstance(applicationContext)

        viewModel = ViewModelProvider(this).get(AddDiaryViewModel::class.java)

        diaryPlaceAdapter.setItemClickListener(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        } else {
        }

        // AddPlaceInDiaryActivity를 시작하기 위한 요청 코드 정의
        addContentActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val position = data?.getIntExtra("itemPosition", -1)
                    val enteredText = data?.getStringExtra("enteredText")
                    val place = data?.getStringExtra("place")
                    val placeDate = data?.getStringExtra("date")
                    val placeTimeS = data?.getStringExtra("timeStart")
                    val placeTimeE = data?.getStringExtra("timeEnd")
                    val placeAddress = data?.getStringExtra("address")
                    val placeX = data?.getStringExtra("x")
                    val placeY = data?.getStringExtra("y")
                    val imageUris = data?.getParcelableArrayListExtra<Uri>("imageUris")
                    Log.d(
                        "리사이클러뷰", "" + position + enteredText + place +
                                placeDate + placeTimeS + placeTimeE + placeAddress + placeX + placeY
                    )

                    if (position != null && position >= 0) {
                        val item = diaryPlaceList[position]
                        item.content = enteredText
                        item.imageUris = imageUris
                        item.place = place
                        item.placeDate = placeDate
                        item.placeTimeS = placeTimeS
                        item.placeTimeE = placeTimeE
                        item.address = placeAddress
                        item.x = placeX
                        item.y = placeY
                        diaryPlaceAdapter.notifyItemChanged(position)
                    } else {
                        if (!enteredText.isNullOrEmpty() || imageUris != null) {
                            // DiaryPlaceModel 인스턴스를 생성하고 리스트에 추가
                            val newDiaryPlaceModel =
                                DiaryPlaceModel(
                                    content = enteredText,
                                    imageUris = imageUris,
                                    place = place,
                                    placeDate = placeDate,
                                    placeTimeS = placeTimeS,
                                    placeTimeE = placeTimeE,
                                    address = placeAddress,
                                    x = placeX,
                                    y = placeY
                                )
                            diaryPlaceList.add(newDiaryPlaceModel)

                            // 특정 아이템을 리스트의 맨 마지막으로 이동시키는 함수 호출
                            diaryPlaceAdapter.moveMemoItemToLast()

                            // 어댑터에 데이터 변경을 알림
                            diaryPlaceAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

        // AddDiaryMapActivity를 시작하기 위한 요청 코드 정의
        addPlaceActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val position = data?.getIntExtra("itemPosition", -1)
                    val enteredPlace = data?.getStringExtra("enteredPlace")
                    val enteredDate = data?.getStringExtra("enteredDate")
                    val enteredTimeS = data?.getStringExtra("enteredStart")
                    val enteredTimeE = data?.getStringExtra("enteredEnd")
                    val enteredAddress = data?.getStringExtra("enteredAddress")
                    val enteredX = data?.getStringExtra("enteredX")
                    val enteredY = data?.getStringExtra("enteredY")

                    Log.d(
                        "지도 이후 AddDiary에서 추가", "" + position + enteredPlace +
                                enteredDate + enteredTimeS + enteredTimeE + enteredAddress + enteredX + enteredY
                    )

                    if (position != null && position >= 0) {
                        val item = diaryPlaceList[position]
                        item.place = enteredPlace
                        item.placeDate = enteredDate
                        item.placeTimeS = enteredTimeS
                        item.placeTimeE = enteredTimeE
                        item.address = enteredAddress
                        item.x = enteredX
                        item.y = enteredY

                        diaryPlaceAdapter.notifyItemChanged(position)
                    } else {
                        if (!enteredPlace.isNullOrEmpty()) {
                            // DiaryPlaceModel 인스턴스를 생성하고 리스트에 추가
                            val newDiaryPlaceModel =
                                DiaryPlaceModel(
                                    place = enteredPlace,
                                    placeDate = enteredDate,
                                    placeTimeS = enteredTimeS,
                                    placeTimeE = enteredTimeE,
                                    address = enteredAddress,
                                    x = enteredX,
                                    y = enteredY
                                )
                            diaryPlaceList.add(newDiaryPlaceModel)

                            // 특정 아이템을 리스트의 맨 마지막으로 이동시키는 함수 호출
                            diaryPlaceAdapter.moveMemoItemToLast()

                            // 어댑터에 데이터 변경을 알림
                            diaryPlaceAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

        // "MEMO" 항목 추가
        val initialMemo = DiaryPlaceModel(place = "MEMO", content = "클릭하여 메모를 작성하세요.")
        diaryPlaceList.add(initialMemo)
        diaryPlaceAdapter.notifyDataSetChanged()

        // 툴바 취소 버튼 클릭 시
        binding.diaryCancelBtn.setOnClickListener {
            finish()
        }

        // 툴바 완료 버튼 클릭 시
        binding.diarySaveBtn.setOnClickListener {
            new = intent.getIntExtra("new_diary", 1)
            // 일기 저장 처리
            Log.d("일기 저장or수정", "" + new)
            saveDiaryToServer(new!!)
            finish()
        }

        // 현재 날짜를 가져옴
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        binding.diaryAddStart.setOnClickListener {
            val datePickerDialog =
                DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(
                        view: DatePicker?,
                        year: Int,
                        month: Int,
                        dayOfMonth: Int
                    ) {
                        binding.diaryAddStart.text = "${year}-${month + 1}-${dayOfMonth}"
                    }
                }, currentYear, currentMonth, currentDayOfMonth)
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primary))
        }

        binding.diaryAddEnd.setOnClickListener {
            val datePickerDialog =
                DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(
                        view: DatePicker?,
                        year: Int,
                        month: Int,
                        dayOfMonth: Int
                    ) {
                        binding.diaryAddEnd.text = "${year}-${month + 1}-${dayOfMonth}"
                    }
                }, currentYear, currentMonth, currentDayOfMonth)
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primary))
        }


        binding.diaryImgBtn.setOnClickListener {
            singleImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }


        // 여행지 추가 버튼 클릭 시
        binding.diaryAddPlaceBtn.setOnClickListener {
            // 기존의 입력을 ViewModel에 저장
            viewModel.enteredTitle = binding.diaryAddTitle.text.toString()
            viewModel.enteredDest = binding.diaryAddDest.text.toString()
            viewModel.enteredStart = binding.diaryAddStart.text.toString()
            viewModel.enteredEnd = binding.diaryAddEnd.text.toString()
            viewModel.enteredHash = binding.diaryAddHash.text.toString()
            viewModel.enteredClosed = binding.diaryAddLockBtn.isChecked

            val intent = Intent(this, AddDiaryMapActivity::class.java)
            intent.putExtra("itemPosition", -1)
            addPlaceActivityResult.launch(intent)
        }

        //메모 추가 버튼 클릭 시
        binding.diaryAddMemoBtn.setOnClickListener {

        }

        if (new == 0) { // 기존의 일기 수정이라면, 기존의 일기 내용 그대로 출력
            diaryId = intent.getIntExtra("diary_id", -1)
            DiaryDetailManager.getDiaryDetailData(
                diaryId!!,
                onSuccess = { diaryDetail ->
                    Log.d("adddiaryAc", "수정하러 들어옴" + diaryDetail)
                    val editTitle = Editable.Factory.getInstance().newEditable(diaryDetail.diaryDto.title)
                    val editTravelDest = Editable.Factory.getInstance().newEditable(diaryDetail.diaryDto.travelDest)
                    val editHash = Editable.Factory.getInstance()
                        .newEditable(diaryDetail.diaryDto.tags.joinToString(" ") { "#${it.name}" })
                    binding.diaryAddTitle.text = editTitle
                    binding.diaryAddDest.text = editTravelDest
                    binding.diaryAddHash.text = editHash
                    binding.diaryAddLockBtn.isChecked = !diaryDetail.diaryDto.public


                    Log.d("adddiaryadpater", ""+ diaryDetail.diaryDto.imageUri)
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedStartDate = dateFormatter.format(diaryDetail.diaryDto.travelStart)
                    val formattedEndDate = dateFormatter.format(diaryDetail.diaryDto.travelEnd)

                    binding.diaryAddStart.text = formattedStartDate
                    binding.diaryAddEnd.text = formattedEndDate

                    if (diaryDetail.diaryDto.imageData != "null") {
                        s3Client.setEndpoint("https://kr.object.ncloudstorage.com")
                        TransferNetworkLossHandler.getInstance(binding.diaryImgBtn.context);
                        downloadAndInitializeAdapter(diaryDetail.diaryDto.imageData.toUri(), binding.diaryImgBtn)
                        Log.d("detailAdapter", "이미지 추가")
                    }


                    val diaryPlaceModels: List<DiaryPlaceModel> =
                        if (diaryDetail.diaryLocationDtoList != null && diaryDetail.diaryLocationDtoList.isNotEmpty()) {
                            diaryDetail.diaryLocationDtoList.map { locationDetail ->
                                Log.d("adddiaryAc", "수정하러 들어옴" + locationDetail)
                                val formattedStartTime =
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                        locationDetail.timeStart
                                    )
                                val formattedEndTime =
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                        locationDetail.timeEnd
                                    )

                                //uri list 만들기
                                val diaryUris: ArrayList<Uri> = ArrayList()
                                for (diaryImage in locationDetail.diaryLocationImageDtoList) {
                                    val imageUri = diaryImage.imageData
                                    if (imageUri != null) {
                                        diaryUris.add(imageUri.toUri())
                                    }
                                }
                                Log.d("adddiaryAc", "수정하러 들어옴" + diaryUris)
                                //locationDetail.diaryLocationImageDtoList.
                                DiaryPlaceModel(
                                    place = locationDetail.name,
                                    content = locationDetail.content,
                                    address = locationDetail.address,
                                    imageUris = diaryUris,
                                    x = locationDetail.x,
                                    y = locationDetail.y,
                                    placeDate = dateFormatter.format(locationDetail.date),
                                    placeTimeS = formattedStartTime, // timeStart를 원하는 형식으로 변환
                                    placeTimeE = formattedEndTime    // timeEnd를 원하는 형식으로 변환
                                )
                            }

                        } else {
                            emptyList()
                        }

                    if (diaryDetail.diaryDto.memo != null && diaryDetail.diaryDto.memo.isNotEmpty()) {
                        val memoItem =
                            DiaryPlaceModel(place = "MEMO", content = diaryDetail.diaryDto.memo)
                        diaryPlaceAdapter.updateData(diaryPlaceModels + listOf(memoItem))
                    } else {
                        diaryPlaceAdapter.updateData(diaryPlaceModels)
                    }
                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }

        if (new == -1) { // 내 플랜에서 작성이라면, 플랜 내용 출력
            planId = intent.getIntExtra("plan_id", -1)
            PlanDetailManager.getPlanDetailData(
                planId!!,
                onSuccess = { planDetail ->
                    Log.d("my log", ""+ planDetail.plan.content + planDetail.plan.travelDest)
                    val editTitle = Editable.Factory.getInstance().newEditable(planDetail.plan.content)
                    val editTravelDest = Editable.Factory.getInstance().newEditable(planDetail.plan.travelDest)
                    val editHash = Editable.Factory.getInstance()
                        .newEditable(planDetail.tags.joinToString(" ") { "#${it.name}" })

                    binding.diaryAddTitle.text = editTitle
                    binding.diaryAddDest.text = editTravelDest
                    binding.diaryAddHash.text = editHash

                    Log.d("my log", ""+ editTitle + binding.diaryAddDest.text.toString() + editHash)

                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedStartDate = dateFormatter.format(planDetail.plan.travelStart)
                    val formattedEndDate = dateFormatter.format(planDetail.plan.travelEnd)

                    binding.diaryAddStart.text = formattedStartDate
                    binding.diaryAddEnd.text = formattedEndDate

                    val diaryPlaceModels: List<DiaryPlaceModel> =
                        if (planDetail.locations != null && planDetail.locations.isNotEmpty()) {
                            planDetail.locations.map { locationDetail ->
                                val formattedStartTime =
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                        locationDetail.timeStart
                                    )
                                val formattedEndTime =
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                        locationDetail.timeEnd
                                    )

                                DiaryPlaceModel(
                                    place = locationDetail.name,
                                    address = locationDetail.address,
                                    x = locationDetail.x,
                                    y = locationDetail.y,
                                    placeDate = dateFormatter.format(locationDetail.date),
                                    placeTimeS = formattedStartTime, // timeStart를 원하는 형식으로 변환
                                    placeTimeE = formattedEndTime    // timeEnd를 원하는 형식으로 변환
                                )
                            }
                        } else {
                            emptyList()
                        }
                    diaryPlaceAdapter.updateData(diaryPlaceModels)

                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }


    }

    override fun itemClicked() {
        // 기존의 입력을 ViewModel에 저장
        viewModel.enteredTitle = binding.diaryAddTitle.text.toString()
        viewModel.enteredDest = binding.diaryAddDest.text.toString()
        viewModel.enteredStart = binding.diaryAddStart.text.toString()
        viewModel.enteredEnd = binding.diaryAddEnd.text.toString()
        viewModel.enteredHash = binding.diaryAddHash.text.toString()
        viewModel.enteredClosed = binding.diaryAddLockBtn.isChecked
        Log.d("my log", "뷰모델"+viewModel.enteredDest)
    }

    override fun onResume() {
        super.onResume()

        // 이전에 입력한 텍스트를 복원하여 보여줌
        binding.diaryAddTitle.setText(viewModel.enteredTitle)
        binding.diaryAddDest.setText(viewModel.enteredDest)

        if (viewModel.enteredStart != null || viewModel.enteredEnd != null) {
            binding.diaryAddStart.setText(viewModel.enteredStart)
            binding.diaryAddEnd.setText(viewModel.enteredEnd)
        }

        if (viewModel.enteredHash != null) {
            binding.diaryAddHash.setText(viewModel.enteredHash)
        }

        binding.diaryAddLockBtn.isChecked = viewModel.enteredClosed
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveDiaryToServer(isNew: Int) { // 일기 서버에 추가
        val travelDest = binding.diaryAddDest.text.toString()
        val content = binding.diaryAddTitle.text.toString()
        val public = !binding.diaryAddLockBtn.isChecked
        val hashTagArray = binding.diaryAddHash.getInsertTag() ?: emptyArray()
        val tags: List<TagName> = hashTagArray.map { TagName(it) }
        val travelStart = binding.diaryAddStart.text.toString()
        val travelEnd = binding.diaryAddEnd.text.toString()
        val fileList: MutableList<DiaryLocationImageDto> = mutableListOf()
        val fileListFile: MutableList<File> = mutableListOf()
        var imageuri : Uri? = null
        var imagedata : String? = null
        Log.d("adddiaryActi", "" + uriList.size)
        if (uriList.size != 0) {
            imageuri = uriList[0]
            imagedata = getRealPathFromURI(imageuri)
            val imagefile = File(imagedata)

            val uploadObserver = transferUtility.upload("diary", imagefile.toString(), imagefile)
            uploadObserver.setTransferListener(object : TransferListener {
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


        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val travelStartDate: Date = try {
            java.sql.Date(dateFormat.parse(travelStart).time)
        } catch (e: Exception) {
            java.sql.Date(System.currentTimeMillis())
        }

        val travelEndDate: Date = try {
            java.sql.Date(dateFormat.parse(travelEnd).time)
        } catch (e: Exception) {
            java.sql.Date(System.currentTimeMillis())
        }

        // 여행지 목록에 "MEMO" 제외
        val filteredDiaryPlaceList = diaryPlaceList.filter { it.place != "MEMO" }

        // "MEMO" 항목을 처리할 수 있도록 따로 처리 코드 추가
        val memoItem = diaryPlaceList.find { it.place == "MEMO" }
        lateinit var memo: String

        if (memoItem?.content == "클릭하여 메모를 작성하세요.") {
            memo = ""
        } else {
            memo = memoItem?.content ?: ""
        }

        val diaryDto = DiaryDto(
            content, travelDest, memo, travelStartDate, travelEndDate, tags, public, imagedata.toString(), imageuri.toString()
        )

        val diaryLocations = mutableListOf<DiaryLocationDto>()

        for (item in filteredDiaryPlaceList) {
            val place = item.place ?: "여행지"
            lateinit var content: String
            val imageUris = item.imageUris

            // 이미지 리스트를 따로 관리하기 위한 리스트
            val placeImageList: MutableList<DiaryLocationImageDto> = mutableListOf()

            if (imageUris != null) {
                Log.d("adddiary", "imageUris" + imageUris)
                for (uri in imageUris) {
                    Log.d("adddiary", "uri" + uri)
                    val filepath = getRealPathFromURI(uri)
                    Log.d("adddiary", "filepath" + filepath)
                    val file = File(filepath)
                    Log.d("adddiary", "file 완성" + file)

                    // 파일을 FileProvider를 사용하여 Uri로 변환 (새로 추가한 부분)
//                    val fileUri = FileProvider.getUriForFile(this, "com.example.diary.fileprovider", file)

                    fileListFile.add(file)
//                    fileList.add(DiaryLocationImageDto(file.toString(), uri.toString()))
                    // 여행지 별로 이미지 리스트에 추가
                    placeImageList.add(DiaryLocationImageDto(file.toString(), uri.toString()))

//                    fileList.add(DiaryLocationImageDto(file.toString(), fileUri.toString()))
//                    val uploadObserver = transferUtility.upload("diary", file.toString(), file)
                    val uploadObserver = transferUtility.upload("diary", file.toString(), file)
                    uploadObserver.setTransferListener(object : TransferListener {
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
            }

            Log.d("adddiary", "fileList 완성" + placeImageList)


            val address = item.address ?: ""
            val x = item.x ?: ""
            val y = item.y ?: ""

            if (item.content == "클릭하여 여행지별 일기를 기록하세요.") {
                content = ""
            } else {
                content = item.content ?: ""
            }

            val placeDate: Date = try {
                java.sql.Date(dateFormat.parse(item.placeDate).time)
            } catch (e: Exception) {
                java.sql.Date(System.currentTimeMillis())
            }

            val placeTimeStart: Time = try {
                java.sql.Time(timeFormat.parse(item.placeTimeS).time)
            } catch (e: Exception) {
                java.sql.Time(System.currentTimeMillis())
            }

            val placeTimeEnd: Time = try {
                java.sql.Time(timeFormat.parse(item.placeTimeE).time)
            } catch (e: Exception) {
                java.sql.Time(System.currentTimeMillis())
            }



            Log.d("서버 테스트 content", "" + content)
            if (!place.isNullOrEmpty()) {
                val timeStartUtil: java.util.Date = timeFormat.parse(item.placeTimeS)
                val timeStartSql: Time = Time(timeStartUtil.time)

                val timeEndUtil: java.util.Date = timeFormat.parse(item.placeTimeE)
                val timeEndSql: Time = Time(timeEndUtil.time)
                val diaryLocation = DiaryLocationDto(
                    content = content,
                    name = place,
                    address = address,
                    x = x,
                    y = y,
                    date = placeDate,
                    timeStart = timeStartSql,
                    timeEnd = timeEndSql,
                    diaryLocationImageDtoList = placeImageList // 이미지 리스트 추가 필요
                )
                diaryLocations.add(diaryLocation)
            }
        }

        val diaryData = DiaryData(diaryDto, diaryLocations)

        // 저장된 토큰 읽어오기
        val sharedPreferences = getSharedPreferences("my_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("auth_token", null)

        Log.d("서버 테스트", "" + diaryData)
        if (authToken != null) {
            if (isNew == 1 || isNew == -1) {
                DiaryManager.sendDiaryToServer(diaryData, authToken)
            } else {
                DiaryManager.sendModDiaryToServer(diaryId!!, diaryData, authToken)
                Log.d("adddiaryAc", "수정")
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
            }

        }
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
    private fun downloadAndInitializeAdapter(imageUri: Uri, binding: ImageView) {
        val fileName = imageUri.lastPathSegment // 파일 이름을 가져옴
        val downloadFile = File(binding.context.cacheDir, fileName)

        val transferObserver = transferUtility.download(
            "diary",
            imageUri.toString(),
            downloadFile
        )

        transferObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    // 이미지 다운로드가 완료되었습니다. 이제 ImageView에 이미지를 표시하세요.
                    val downloadedImageUri = Uri.fromFile(downloadFile)
                    Glide.with(binding.context)
                        .load(downloadedImageUri)
                        .into(binding)
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                // 진행 상태 업데이트
            }

            override fun onError(id: Int, ex: Exception) {
                Log.e("DiaryDetailAdapter", "이미지 다운로드 오류: $ex")
            }
        })
    }




//    private fun getRealPathFromURI(contentUri: Uri): String? {
//        val projection = arrayOf(MediaStore.Images.Media.DATA)
//        val cursor = contentResolver.query(contentUri, projection, null, null, null)
//        cursor?.use {
//            it.moveToFirst()
//            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            return it.getString(columnIndex)
//        }
//        return null
//    }

//    @RequiresApi(Build.VERSION_CODES.KITKAT)
//    private fun getRealPathFromURI(contentUri: Uri): String? {
//        var filePath: String? = null
//        if (DocumentsContract.isDocumentUri(this, contentUri)) {
//            val docId = DocumentsContract.getDocumentId(contentUri)
//            if ("com.android.providers.media.documents" == contentUri.authority) {
//                val id = docId.split(":")[1]
//                val selection = MediaStore.Images.Media._ID + "=?"
//                val selectionArgs = arrayOf(id)
//                val column = "_data"
//                val projection = arrayOf(column)
//                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                val cursor =
//                    contentResolver.query(contentUri, projection, selection, selectionArgs, null)
//                cursor?.use {
//                    if (it.moveToFirst()) {
//                        val columnIndex = it.getColumnIndex(column)
//                        filePath = it.getString(columnIndex)
//                    }
//                }
//            } else if ("com.android.providers.downloads.documents" == contentUri.authority) {
//                val contentUri = ContentUris.withAppendedId(
//                    Uri.parse("content://downloads/public_downloads"),
//                    docId.toLong()
//                )
//                filePath = getDataColumn(contentUri, null, null)
//            }
//        } else if ("content".equals(contentUri.scheme, ignoreCase = true)) {
//            filePath = getDataColumn(contentUri, null, null)
//        } else if ("file".equals(contentUri.scheme, ignoreCase = true)) {
//            filePath = contentUri.path
//        }
//        return filePath
//    }

    private fun getDataColumn(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }
}


//        val id = DocumentsContract.getDocumentId(contentUri).split(":".toRegex())
//            .dropLastWhile { it.isEmpty() }
//            .toTypedArray()[1]
//        val columns = arrayOf(MediaStore.Files.FileColumns.DATA)
//        val selection = MediaStore.Files.FileColumns._ID + " = " + id
//        val cursor: Cursor? = contentResolver.query(
//            MediaStore.Files.getContentUri("external"),
//            columns,
//            selection,
//            null,
//            null
//        )
//        try {
//            val columnIndex: Int = cursor!!.getColumnIndex(columns[0])
//            if (cursor.moveToFirst()) {
//                Log.d("getRealpath", "cursor??" + cursor.getString(columnIndex))
//                return cursor.getString(columnIndex)
//            }
//        } finally {
//            cursor!!.close()
//        }
//        return null