package com.example.diary

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Half
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diary.databinding.FragmentMapBinding
import com.google.android.material.color.utilities.MaterialDynamicColors.onError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceStte: Bundle?
    ): View? {
        val binding = FragmentMapBinding.inflate(inflater, container,false)

        binding.webview.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true //자바스크립트 허용
            settings.loadWithOverviewMode = true //html의 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 자동 조정
            settings.useWideViewPort = true //html의 viewport 메타 태그 지원
            settings.setSupportZoom(false)
        }

        binding.webview.addJavascriptInterface(MapInterface(requireContext()), "Android")

        binding.webview.loadUrl("https://diarymap.netlify.app/map.html") //임시 주소

        return binding.root
    }

    inner class MapInterface(context: Context) {
        var placeInfo: MutableMap<String, String?> = mutableMapOf()

        @JavascriptInterface
        fun showToast(toast: String) {
            Log.d("mylog", toast)
        }

        @JavascriptInterface
        fun showCustomOverlay() {
            val x = placeInfo.getValue("x")
            val y = placeInfo.getValue("y")

            if (x != null && y != null) {
                MapDiaryListManager.getDiaryListData(
                    x,
                    y,
                    onSuccess = { mapDiaryList -> //백그라운드 스레드
                        CoroutineScope(Dispatchers.IO).launch {
                            val diary = mapDiaryList.map { it }
                            Log.d("mylog", "x: ${x}, y: ${y}")
                            Log.d("mylog", "주소별 일기 조회 - ${diary}")
                            withContext(Dispatchers.Main) {
                                val dialog = MapDialog(this@MapFragment.requireContext())
                                dialog.myDialog(placeInfo, diary)
                            }
                        }
                    },
                    onError = {throwable ->
                        Log.e("mylog", "주소별 일기 조회 실패 - ${throwable}")
                    }
                )
            }
        }

        @JavascriptInterface
        fun setPlaceInfo(title: String?, address: String?, x: String?, y: String?, panoId: String?) {
            val search_img = ApiSearchImg()
            val img = search_img.searchImg(title!!)

            placeInfo.putAll(
                mapOf(
                    "title" to title,
                    "address" to address,
                    "x" to x,
                    "y" to y,
                    "panoId" to panoId,
                    "imgURL" to img
                )
            )
            Log.d("mylog", "placeInfo - ${placeInfo}")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}