package com.example.diary

import android.net.Uri
import java.sql.Date

data class DiaryDetailModel( //다이어리 상세 페이지의 여행지 별 카드
    var diaryLocationId: Int,
    var diaryId: Int,
    var place: String? = "여행지",
    var content: String? = null,
    var address: String? = null,
    var placeDate: Date? = null,
    var placeStart: String? = null, //임시로 스트링
    var placeEnd: String? = null,
    var imageUris: Uri? = null,
)