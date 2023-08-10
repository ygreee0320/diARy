package com.example.diary

import androidx.lifecycle.ViewModel

class AddDiaryViewModel : ViewModel() {
    var enteredTitle: String? = null // 일기 작성 중, 일기 제목을 담는 변수
    var enteredDest: String? = null
    var enteredStart: String? = null
    var enteredEnd: String? = null
    var enteredHash: String? = null
    var enteredClosed: Boolean = false
}