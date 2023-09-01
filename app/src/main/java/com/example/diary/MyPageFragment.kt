package com.example.diary

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.diary.databinding.FragmentMyPageBinding

class MyPageFragment : Fragment() {
    private lateinit var binding: FragmentMyPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // sharedPreferences에서 토큰 가져오기
        val sharedPreferences = requireContext().getSharedPreferences("my_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("auth_token", "")
        var userEmail = sharedPreferences.getString("user_email", "abc123@example.com")
        var username :String = "username"

        if (authToken != null) {
            MyPageManager.getMyData(
                authToken,
                onSuccess = { myData ->
                    username = myData.username
                    userEmail = myData.email
                    Log.d("마이페이지", "" +username + authToken + userEmail)
                    binding.email.text = userEmail
                    binding.username.text = username
                },
                onError = { throwable ->
                    Log.e("서버 테스트3", "오류: $throwable")
                }
            )
        }
    }

}