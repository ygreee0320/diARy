package com.example.diary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.content.ContentProviderCompat.requireContext
import com.auth0.android.jwt.JWT
import com.example.diary.databinding.ActivityLogInBinding

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  //툴바에 뒤로 가기 버튼 추가

        binding.logInBtn.setOnClickListener { //로그인 버튼 클릭 시
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email != null && password != null) {
                val loginData = LogInData(email, password)

                Log.d("서버 테스트", ""+loginData)
                LogInManager.sendLogInToServer(loginData) { authToken ->
                    val tokenOnly = authToken?.substringAfter("Bearer ")

                    if (tokenOnly != null) {
                        val jwt = JWT(tokenOnly)
                        val useremail = jwt.getClaim("email").asString()
                        userId = jwt.getClaim("id")?.asInt() ?: -1

                        saveAuthToken(authToken, useremail ?: "", userId) //유저네임 수정 필요
                        moveToHomeScreen() // 토큰 저장 후 홈 화면으로 이동
                    }
                }
            }
            finish()
        }

        binding.joinBtn.setOnClickListener { //회원가입 버튼 클릭 시
            val intent = Intent(this@LogInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { // 뒤로 가기 버튼 클릭 시
                finish() // 현재 액티비티 종료
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // 토큰을 저장하는 메서드
    private fun saveAuthToken(token: String, email: String, userId: Int) {
        Log.d("로그인 토큰 테스트", ""+token)

        // 토큰 저장
        val sharedPreferences = this.getSharedPreferences("my_token", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.putString("user_email", email)
        editor.putInt("userId", userId)
        editor.apply()
    }

    // 홈 화면으로 이동하는 메서드
    private fun moveToHomeScreen() {
        val intent = Intent(this@LogInActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}