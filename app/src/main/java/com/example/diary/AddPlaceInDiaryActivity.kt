package com.example.diary

import android.app.Activity
import android.content.ClipData
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diary.databinding.ActivityAddPlaceInDiaryBinding

class AddPlaceInDiaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlaceInDiaryBinding
    private val uriList = ArrayList<Uri>()
    private lateinit var adapter: MultiImageAdapter

    // 여러 이미지 선택을 위한 ActivityResultLauncher
    private val multipleImagePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
        if (uris != null) {
            val totalSelectedImages = uriList.size + uris.size
            if (totalSelectedImages > 10) {
                Toast.makeText(applicationContext, "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show()
            } else {
                uriList.addAll(0, uris)
                adapter = MultiImageAdapter(uriList, applicationContext)
                binding.recyclerView.adapter = adapter
            }
        } else {
            Toast.makeText(applicationContext, "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceInDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  //툴바에 뒤로 가기 버튼 추가

        binding.placeImgAddBtn.setOnClickListener {
            // 이미지 선택을 위해 ActivityResultLauncher 실행
            val remainingImages = 10 - uriList.size
            if (remainingImages > 0) {
                // 최대 10장 이하의 이미지만 선택 가능하도록 합니다.
                multipleImagePicker.launch("image/*")
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
            intent.putExtra("enteredText", enteredText)
            intent.putParcelableArrayListExtra("imageUris", uriList)

            // 결과를 설정하고 현재 활동 종료
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

}