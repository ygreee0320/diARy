package com.example.diary

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.example.diary.databinding.ActivityAddPlaceInDiaryBinding
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class AddPlaceInDiaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlaceInDiaryBinding
    private var uriList = ArrayList<Uri>()
    private var imageUris = ArrayList<Uri>()
    private lateinit var adapter: MultiImageAdapter
    private val awsAccessKey = "1807222EE827BB41A77C"
    private val awsSecretKey = "E9DC72D2C24094CB2FE00763EF33330FB7948154"
    private val awsCredentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3Client = AmazonS3Client(
        awsCredentials,
        Region.getRegion(Regions.AP_NORTHEAST_2)
    )// YOUR_REGION을 원하는 지역으로 변경하세요
    private lateinit var transferUtility: TransferUtility
    // 여러 이미지 선택을 위한 ActivityResultLauncher
//    private val multipleImagePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
//        if (uris != null) {
//            val totalSelectedImages = uriList.size + uris.size
//            if (totalSelectedImages > 10) {
//                Toast.makeText(applicationContext, "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show()
//            } else {
//                uriList.addAll(0, uris)
//                adapter = MultiImageAdapter(uriList, applicationContext)
//                binding.recyclerView.adapter = adapter
//            }
//        } else {
//            Toast.makeText(applicationContext, "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
//        }
//    }


    private val multipleImagePicker =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris: List<Uri>? ->
            if (uris != null) {
                val totalSelectedImages = uriList.size + imageUris.size
                if (totalSelectedImages > 10) {
                    Toast.makeText(applicationContext, "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG)
                        .show()
                } else {
                    imageUris.addAll(0, uris)
                    Log.d("addplaceinDiaryActi", "수정 중")
                    s3Client.setEndpoint("https://kr.object.ncloudstorage.com")
                    // Initialize TransferUtility
                    TransferNetworkLossHandler.getInstance(binding.root.context);

                    transferUtility = TransferUtility.builder()
                        .s3Client(s3Client)
                        .context(binding.root.context)
                        .defaultBucket("diary") // S3 버킷 이름을 변경하세요
                        .build()
                    //기존 이미지           //새로운 이미지'
                    downloadAndInitializeAdapter(uriList, binding, imageUris)
                    //adapter = MultiImageAdapter(uriList, applicationContext)
                    //binding.recyclerView.adapter = adapter

                    // URI에 대한 지속적인 권한을 부여합니다.
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    for (uri in uris) {
                        applicationContext.contentResolver.takePersistableUriPermission(uri, flag)
                    }
                }
            } else {
                Toast.makeText(applicationContext, "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
            }
        }

    private val REQUEST_CODE_PICK_IMAGES = 1001 // 원하는 숫자로 지정

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == Activity.RESULT_OK) {
//            val selectedUris = mutableListOf<Uri>()
//
//            // 다중 이미지 선택을 처리
//            if (data?.clipData != null) {
//                val clipData = data.clipData
//                for (i in 0 until clipData!!.itemCount) {
//                    val uri = clipData.getItemAt(i).uri
//                    selectedUris.add(uri)
//                }
//            } else if (data?.data != null) {
//                // 단일 이미지 선택을 처리
//                val uri = data.data
//                selectedUris.add(uri!!)
//            }
//
//            // 이미지를 처리하고 리스트에 추가
//            if (selectedUris.isNotEmpty()) {
//                val totalSelectedImages = uriList.size + selectedUris.size
//                if (totalSelectedImages > 10) {
//                    Toast.makeText(applicationContext, "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show()
//                } else {
//                    uriList.addAll(selectedUris)
//                    adapter = MultiImageAdapter(uriList, applicationContext)
//                    binding.recyclerView.adapter = adapter
//                }
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceInDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  //툴바에 뒤로 가기 버튼 추가

        val itemPosition = intent.getIntExtra("itemPosition", -1)
        val place = intent.getStringExtra("place")
        val placeDate = intent.getStringExtra("date")
        val placeTimeS = intent.getStringExtra("timeStart")
        val placeTimeE = intent.getStringExtra("timeEnd")
        var content = intent.getStringExtra("content")
        val address = intent.getStringExtra("address")
        val x = intent.getStringExtra("x")
        val y = intent.getStringExtra("y")
        val new = intent.getIntExtra("new", 1)

        Log.d("여행지추가", "" + itemPosition + place + content)

        uriList = intent.getParcelableArrayListExtra<Uri>("imageUris") ?: ArrayList()

        if (place == "MEMO") {
            binding.placeImgAddBtn.visibility = View.GONE
            binding.placeInDiaryTime.visibility = View.GONE
        }

        if (content == "클릭하여 여행지별 일기를 기록하세요." || content == "클릭하여 메모를 작성하세요.") {
            content = null
        }

        val placeTime = "$placeDate $placeTimeS ~ $placeTimeE"

        // 여행지 정보를 텍스트뷰에 표시
        binding.placeInDiaryTitle.setText(place)
        binding.placeInDiaryTime.setText(placeTime)
        binding.placeInDiaryContent.setText(content)

        // 이미지 리사이클러뷰 초기화 및 어댑터 연결
        //새로 만든 거면
        if (new == 0) {
            Log.d("addplaceinDiaryActi", "작성 중")
            binding.recyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val imageAdapter = MultiImageAdapter(uriList, this)
            binding.recyclerView.adapter = imageAdapter
        } else if (new == 1) {
            //수정 중인 상태라면
            Log.d("addplaceinDiaryActi", "수정 중")
            // layoutManager를 설정하는 부분은 onBindViewHolder에서 이루어집니다.
            binding.recyclerView.layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.HORIZONTAL,
                true
            )
            s3Client.setEndpoint("https://kr.object.ncloudstorage.com")
            // Initialize TransferUtility
            TransferNetworkLossHandler.getInstance(binding.root.context);

            transferUtility = TransferUtility.builder()
                .s3Client(s3Client)
                .context(binding.root.context)
                .defaultBucket("diary") // S3 버킷 이름을 변경하세요
                .build()
            //기존 이미지           //새로운 이미지'
            downloadAndInitializeAdapter(uriList, binding, imageUris)
        }
        Log.d("addplaceinDiaryActi", "걍 안 들어감")

        binding.placeImgAddBtn.setOnClickListener {
            // 이미지 선택을 위해 ActivityResultLauncher 실행
            val remainingImages = 10 - uriList.size - imageUris.size
            if (remainingImages > 0) {
                // 최대 10장 이하의 이미지만 선택 가능하도록 합니다.
//                multipleImagePicker.launch("image/*")
                multipleImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
//                val intent = Intent(Intent.ACTION_GET_CONTENT)
//                intent.type = "image/*"
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGES)
            } else {
                Toast.makeText(applicationContext, "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show()
            }
        }

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)

        // 완료 버튼 클릭 시
        binding.placeInDiarySaveBtn.setOnClickListener {
            val enteredText = binding.placeInDiaryContent.text.toString() //일기 내용 저장

            // 데이터를 이전 활동으로 전달하기 위한 인텐트 생성
            val intent = Intent()
            //intent.putExtra("position", position)  // 수정 중인 아이템의 위치 정보 전달
            intent.putExtra("itemPosition", itemPosition) // position 전달
            intent.putExtra("enteredText", enteredText)
            intent.putExtra("place", place)
            intent.putExtra("date", placeDate)
            intent.putExtra("timeStart", placeTimeS)
            intent.putExtra("timeEnd", placeTimeE)
            intent.putExtra("address", address)
            intent.putExtra("x", x)
            intent.putExtra("y", y)
            intent.putParcelableArrayListExtra("imageUris", uriList)
            intent.putParcelableArrayListExtra("addimageUris", imageUris)

            Log.d("mylog", "AddPlaceInDiary에서 완료 클릭"
                    + itemPosition + title + placeDate + placeTimeS +placeTimeE + address + x + y)

            // 결과를 설정하고 현재 활동 종료
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }


    private fun downloadAndInitializeAdapter(
        imageUri: List<Uri>,
        binding: ActivityAddPlaceInDiaryBinding,
        imageUris: ArrayList<Uri>
    ) {
        // 다운로드할 파일 경로
        val downloadFile = File(binding.root.context.cacheDir, "downloaded_image")

        val transferObserverList = mutableListOf<TransferObserver>()

        val downloadFiles = ArrayList<Uri>() // 이미지 다운로드를 위한 파일 리스트

        for (uri in imageUri) {
            val fileName = uri.lastPathSegment // 파일 이름을 가져옴
            val downloadFile = File(binding.root.context.cacheDir, fileName)
            downloadFiles.add(downloadFile.toUri()) // 파일을 리스트에 추가

            val transferObserver = transferUtility.download(
                "diary",
                uri.toString(),
                downloadFile
            )

            transferObserverList.add(transferObserver)
        }
        // 모든 이미지 다운로드 완료를 기다림
        val completionCount = AtomicInteger(0)

        transferObserverList.forEach { transferObserver ->
            transferObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state == TransferState.COMPLETED) {
                        completionCount.incrementAndGet()

                        // 모든 이미지가 다운로드 완료되면 어댑터 초기화
                        if (completionCount.get() == imageUri.size) {
                            if (imageUris.isNotEmpty()) {
                                downloadFiles.addAll(imageUris)
                                Log.d("addPlaceInDiaryActi", ""+downloadFiles)
                            }
                            val imageAdapter = MultiImageAdapter(downloadFiles, binding.root.context)
                            binding.recyclerView.adapter = imageAdapter
                            //binding.recyclerView.layoutManager = layoutManager
                            Log.d("detailAdapter", "이미지 추가")
                        }
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
    }

}