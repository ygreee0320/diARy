package com.example.diary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils.replace
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.auth0.android.jwt.JWT
import com.example.diary.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.internal.NavigationMenuItemView
import com.google.android.material.navigation.NavigationView
import java.security.MessageDigest
import android.Manifest

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityMainBinding
    lateinit var sharedPreferences: SharedPreferences //토큰을 위한 sharedPreferences
    private var authToken: String? = null    //로그인 토큰 저장

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //KeyHash
        getKeyHash()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.MANAGE_MEDIA) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mainactivity", "권한 요청")
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                    Toast.makeText(this, "외부 저장소 사용을 위해 읽기/쓰기 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(
                    arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.MANAGE_MEDIA),
                    2
                );
            }
        }
        // 저장된 토큰 읽어오기
        sharedPreferences = getSharedPreferences("my_token", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("auth_token", null)


        // "Bearer" 문자열 제거한 토큰 값 추출
        if (authToken != null) {
            val tokenOnly = authToken?.substringAfter("Bearer ")

            if (tokenOnly != null) { // 토큰을 이용하여 유저 정보 저장
                val jwt = JWT(tokenOnly)
                val userId = jwt.getClaim("id")?.asInt() ?: -1
                val userEmail = jwt.getClaim("email")?.asString() ?: ""

                // userId를 SharedPreferences에 저장
                val editor = sharedPreferences.edit()
                editor.putInt("userId", userId)
                editor.putString("userEmail", userEmail)
                editor.apply()
                Log.d("메인액티비티2", ""+ tokenOnly + userId + userEmail)
            }
        }

        Log.d("메인액티비티", ""+authToken)

        //toolbar
        setSupportActionBar(binding.toolbar)

        //navigator
        binding.navView.setNavigationItemSelectedListener(this)

        //bottom navigator
        binding.bottomMenu.setOnItemSelectedListener {item ->
            changeFragment(
                when (item.itemId) {
                    R.id.home -> HomeFragment()
                    R.id.map -> MapFragment()
                    R.id.plan -> PlanFragment()
                    R.id.ranking -> RankingFragment()
                    else -> DiaryFragment()
                }
            )
            true
        }

        // 초기화면으로 HomeFragment를 보여줄 수 있도록 설정
        changeFragment(HomeFragment())
    }
    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container,fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)  //menu_actionbar 불러 와서 사용할 것임, 연결 (menu 와)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.toolbar_nav) { // 툴바의 toolbar_nav 버튼 클릭 시
            if (binding.drawer.isDrawerOpen(GravityCompat.END)) {
                binding.drawer.closeDrawer(GravityCompat.END) // 드로어 레이아웃 닫기
            } else {
                binding.drawer.openDrawer(GravityCompat.END) // 드로어 레이아웃 열기
            }
            return true
        }

        // 툴바의 로그인/마이페이지 버튼 클릭 시
        if (item.itemId == R.id.toolbar_auth) {
            if (authToken != null) {
                Log.d("MainActivity", "Auth Token: $authToken")

                val myPageFragment = MyPageFragment() // MyPageFragment 인스턴스 생성
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, myPageFragment)
                    .addToBackStack(null) // 뒤로 가기 버튼으로 이전 프래그먼트로 돌아갈 수 있도록 스택에 추가
                    .commit()
            } else {
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_1 -> {}
            R.id.menu_2 -> {}
            R.id.menu_3 -> {}
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun getKeyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signatures = info.signingInfo.apkContentsSigners
            val md = MessageDigest.getInstance("SHA")
            for (signature in signatures) {
                val md: MessageDigest
                md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val key = String(Base64.encode(md.digest(), 0))
                Log.d("mylog", "Hash key: $key")
            }
        } catch(e: Exception) {
            Log.e("name not found", e.toString())
        }
    }

    // MyPageFragment에서 auth_token이 업데이트되었을 때 호출되는 메서드
    fun onAuthTokenUpdated() {
        // SharedPreferences에서 auth_token 다시 읽어오기
        authToken = sharedPreferences.getString("auth_token", null)

        // "Bearer" 문자열 제거한 토큰 값 추출
        if (authToken != null) {
            val tokenOnly = authToken?.substringAfter("Bearer ")

            if (tokenOnly != null) { // 토큰을 이용하여 유저 정보 저장
                val jwt = JWT(tokenOnly)
                val userId = jwt.getClaim("id")?.asInt() ?: -1
                val userEmail = jwt.getClaim("email")?.asString() ?: ""

                // userId를 SharedPreferences에 저장
                val editor = sharedPreferences.edit()
                editor.putInt("userId", userId)
                editor.putString("userEmail", userEmail)
                editor.apply()
                Log.d("메인액티비티2", ""+ tokenOnly + userId + userEmail)
            }
        }
    }
}