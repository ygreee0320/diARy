package com.example.diary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import com.example.diary.databinding.FragmentMapBinding

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

    inner class MapInterface(val Context: Context) {
        var title: String? = ""
        var address: String? = ""
        var x: String? = null
        var y: String? = null

        @JavascriptInterface
        fun showToast(toast: String) {
            Log.d("mylog", toast)
        }

        @JavascriptInterface
        fun setPlaceInfo(title: String?, address: String?, x: String?, y: String?) {
            this.title = title
            this.address = address
            this.x = x
            this.y = y

            Log.d("mylog", "Save successed - ${this.title} / ${this.address} / ${this.x} / ${this.y}")
        }

        @JavascriptInterface
        fun getPlaceInfo(): String {
            val info = title + " " + address + " " + x + " " + y
            return info
        }

        @JavascriptInterface
        fun moveToRoadMap() {
            val intent = Intent(Context, RoadMapActivity::class.java)

            intent.putExtra("title", title)
            intent.putExtra("address", address)
            intent.putExtra("x", x)
            intent.putExtra("y", y)

            startActivity(intent)
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