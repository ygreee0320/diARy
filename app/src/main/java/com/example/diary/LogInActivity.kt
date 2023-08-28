package com.example.diary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.example.diary.databinding.ActivityLogInBinding

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding

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
                    saveAuthToken(authToken)
                    moveToHomeScreen() // 토큰 저장 후에 홈 화면으로 이동
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
    private fun saveAuthToken(token: String) {
        // 여기에 토큰 저장 로직을 추가
        // SharedPreferences, ViewModel 등을 활용하여 저장 가능
        Log.d("로그인 토큰 테스트", ""+token)
    }

    // 홈 화면으로 이동하는 메서드
    private fun moveToHomeScreen() {
        val intent = Intent(this@LogInActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}