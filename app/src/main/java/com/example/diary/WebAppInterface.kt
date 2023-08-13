package com.example.diary

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface

class WebAppInterface(private val Context: Context) {
    var title: String? = ""
    var address: String? = ""
    var x: String? = null
    var y: String? = null

    @JavascriptInterface
    fun showToast(toast: String) {
        Log.d("WebAppInterface", toast)
    }

    @JavascriptInterface
    fun setPlaceInfo(title: String?, address: String?, x: String?, y: String?) {
        this.title = title
        this.address = address
        this.x = x
        this.y = y

        Log.d("WebAppInterface", "Save success - ${this.title} / ${this.address} / ${this.x} / ${this.y}")
    }
}