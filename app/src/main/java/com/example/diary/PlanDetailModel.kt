package com.example.diary

import android.net.Uri

data class PlanDetailModel( //일정 안 여행지 카드
    var place: String? = "여행지",
    var imageUris: Uri? = null,
    var placeDate: String? = null, //우선 문자열로 (추후 Date, Time)
    var placeStart: String? = null,
    var placeEnd: String? = null
)
