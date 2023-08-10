package com.example.diary

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.DatePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diary.databinding.ActivityAddDiaryBinding

class AddDiaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddDiaryBinding
    private lateinit var viewModel: AddDiaryViewModel

    // 여행지 데이터를 저장할 리스트
    private val diaryPlaceList = mutableListOf<DiaryPlaceModel>()

    private val diaryPlaceAdapter = DiaryPlaceAdapter(diaryPlaceList)

    // AddPlaceInDiaryActivity를 시작하기 위한 요청 코드 정의
    private val addPlaceActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val enteredText = data?.getStringExtra("enteredText")
            val imageUris = data?.getParcelableArrayListExtra<Uri>("imageUris")

            if (!enteredText.isNullOrEmpty() || imageUris != null) {
                // DiaryPlaceModel 인스턴스를 생성하고 리스트에 추가
                val newDiaryPlaceModel = DiaryPlaceModel(content = enteredText, imageUris = imageUris)
                diaryPlaceList.add(newDiaryPlaceModel)

                // 어댑터에 데이터 변경을 알림
                diaryPlaceAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.diaryAddPlaceRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddDiaryActivity)
            adapter = diaryPlaceAdapter
        }

        viewModel = ViewModelProvider(this).get(AddDiaryViewModel::class.java)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        // 툴바 취소 버튼 클릭 시
        binding.diaryCancelBtn.setOnClickListener {
            finish()
        }

        // 툴바 완료 버튼 클릭 시
        binding.diarySaveBtn.setOnClickListener {
            // 일기 저장 처리 필요

            finish()
        }

        binding.diaryAddStart.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
                override fun onDateSet(view: DatePicker?, year:Int, month: Int, dayOfMonth: Int) {
                    binding.diaryAddStart.text = "${year}.${month+1}.${dayOfMonth}"
                }
            }, 2023, 9, 1)
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
        }

        binding.diaryAddEnd.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
                override fun onDateSet(view: DatePicker?, year:Int, month: Int, dayOfMonth: Int) {
                    binding.diaryAddEnd.text = "${year}.${month+1}.${dayOfMonth}"
                }
            }, 2023, 9, 1)
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.primary))
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

            val intent = Intent(this, AddPlaceInDiaryActivity::class.java)
            //startActivity(intent)
            //startActivityForResult(intent, ADD_PLACE_REQUEST_CODE)
            addPlaceActivityResult.launch(intent)
        }

        //메모 추가 버튼 클릭 시
        binding.diaryAddMemoBtn.setOnClickListener {

        }

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


}