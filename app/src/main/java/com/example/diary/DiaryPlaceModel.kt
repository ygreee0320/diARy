package com.example.diary

import android.net.Uri

data class DiaryPlaceModel(
    var place: String? = null,
    var content: String? = null,
    var imageUris: ArrayList<Uri>? = null
)
