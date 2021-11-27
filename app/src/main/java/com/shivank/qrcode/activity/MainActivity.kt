package com.shivank.qrcode.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.shivank.qrcode.fragments.DashboardFragment
import com.shivank.qrcode.R
import com.shivank.qrcode.fragments.SplashScreenFragment

class MainActivity : AppCompatActivity() {


    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_scan)
        changeFragment(SplashScreenFragment())

        Handler(Looper.getMainLooper()).postDelayed({
            changeFragment(DashboardFragment())
        }, 1500)
    }
    private fun changeFragment(fragment : Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer,fragment)
        fragmentTransaction.commit()
    }
}