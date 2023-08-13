package com.example.diary

import android.net.Uri

data class DiaryPlaceModel(
    var content: String? = null,
    var imageUris: ArrayList<Uri>? = null
)
