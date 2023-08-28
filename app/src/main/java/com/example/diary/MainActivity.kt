package com.example.diary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils.replace
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.diary.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.internal.NavigationMenuItemView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityMainBinding
    lateinit var sharedPreferences: SharedPreferences //토큰을 위한 sharedPreferences
    private var authToken: String? = null    //로그인 토큰 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 저장된 토큰 읽어오기
        sharedPreferences = getSharedPreferences("my_token", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("auth_token", null)

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
}