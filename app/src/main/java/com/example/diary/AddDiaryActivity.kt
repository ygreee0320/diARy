package com.example.diary

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.DatePicker
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.example.diary.databinding.ActivityAddDiaryBinding
import java.io.File
import java.sql.Date
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddDiaryActivity : AppCompatActivity(), DiaryPlaceAdapter.ItemClickListener {
    private lateinit var binding: ActivityAddDiaryBinding
    private lateinit var viewModel: AddDiaryViewModel
    private lateinit var transferUtility: TransferUtility

    private var new: Int? = 1 // 새로 작성이면 1, 수정이면 0, 플랜 바탕 작성이면 -1
    private var diaryId: Int? = -1 // 일기 수정일 때의 해당 일기 아이디
    private var planId: Int? = -1 // 플랜 바탕 작성일 때의 해당 플랜 아이디

    // 여행지 데이터를 저장할 리스트
    private val diaryPlaceList = mutableListOf<DiaryPlaceModel>()
    private val diaryPlaceAdapter = DiaryPlaceAdapter(diaryPlaceList)

    companion object {
        lateinit var addPlaceActivityResult: ActivityResultLauncher<Intent>
        lateinit var addContentActivityResult: ActivityResultLauncher<Intent>
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
        val awsAccessKey = "1807222EE827BB41A77C"
        val awsSecretKey = "E9DC72D2C24094CB2FE00763EF33330FB7948154"
        val awsCredentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
        val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))
        s3Client.setEndpoint("https://kr.object.ncloudstorage.com")
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
                }, 2023, 9, 1)
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
                }, 2023, 9, 1)
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primary))
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
                    val editTitle =
                        Editable.Factory.getInstance().newEditable(diaryDetail.diaryDto.title)
                    val editTravelDest =
                        Editable.Factory.getInstance().newEditable(diaryDetail.diaryDto.travelDest)
                    val editHash = Editable.Factory.getInstance()
                        .newEditable(diaryDetail.diaryDto.tags.joinToString(" ") { "#${it.name}" })
                    binding.diaryAddTitle.text = editTitle
                    binding.diaryAddDest.text = editTravelDest
                    binding.diaryAddHash.text = editHash
                    binding.diaryAddLockBtn.isChecked = !diaryDetail.diaryDto.public

                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedStartDate = dateFormatter.format(diaryDetail.diaryDto.travelStart)
                    val formattedEndDate = dateFormatter.format(diaryDetail.diaryDto.travelEnd)

                    binding.diaryAddStart.text = formattedStartDate
                    binding.diaryAddEnd.text = formattedEndDate

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
                                    val imageUri = diaryImage.imageUri
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
                    val editTitle =
                        Editable.Factory.getInstance().newEditable(planDetail.plan.content)
                    val editTravelDest =
                        Editable.Factory.getInstance().newEditable(planDetail.plan.travelDest)
                    val editHash = Editable.Factory.getInstance()
                        .newEditable(planDetail.tags.joinToString(" ") { "#${it.name}" })

                    binding.diaryAddTitle.text = editTitle
                    binding.diaryAddDest.text = editTravelDest
                    binding.diaryAddHash.text = editHash

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

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

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
            content, travelDest, memo, travelStartDate, travelEndDate, tags, public
        )

        val diaryLocations = mutableListOf<DiaryLocationDto>()

        for (item in filteredDiaryPlaceList) {
            val place = item.place ?: "여행지"
            lateinit var content: String
            val imageUris = item.imageUris

            if (imageUris != null) {
                Log.d("adddiary", "imageUris" + imageUris)
                for (uri in imageUris) {
                    Log.d("adddiary", "uri" + uri)
                    val filepath = getRealPathFromURI(uri)
                    Log.d("adddiary", "filepath" + filepath)
                    val file = File(filepath)
                    Log.d("adddiary", "file 완성" + file)
                    fileListFile.add(file)
                    fileList.add(DiaryLocationImageDto(file.toString(), uri.toString()))
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

            Log.d("adddiary", "fileList 완성" + fileList)


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
                val diaryLocation = DiaryLocationDto(
                    content = content,
                    name = place,
                    address = address,
                    x = x,
                    y = y,
                    date = placeDate,
                    timeStart = placeTimeStart,
                    timeEnd = placeTimeEnd,
                    diaryLocationImageDtoList = fileList // 이미지 리스트 추가 필요
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

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun getRealPathFromURI(contentUri: Uri): String? {
        var filePath: String? = null
        if (DocumentsContract.isDocumentUri(this, contentUri)) {
            val docId = DocumentsContract.getDocumentId(contentUri)
            if ("com.android.providers.media.documents" == contentUri.authority) {
                val id = docId.split(":")[1]
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(id)
                val column = "_data"
                val projection = arrayOf(column)
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val cursor =
                    contentResolver.query(contentUri, projection, selection, selectionArgs, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnIndex = it.getColumnIndex(column)
                        filePath = it.getString(columnIndex)
                    }
                }
            } else if ("com.android.providers.downloads.documents" == contentUri.authority) {
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    docId.toLong()
                )
                filePath = getDataColumn(contentUri, null, null)
            }
        } else if ("content".equals(contentUri.scheme, ignoreCase = true)) {
            filePath = getDataColumn(contentUri, null, null)
        } else if ("file".equals(contentUri.scheme, ignoreCase = true)) {
            filePath = contentUri.path
        }
        return filePath
    }

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