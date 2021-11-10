package com.shivank.qrcode

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var btnScan :Button

    private val TAG = "mytag"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        btnScan = findViewById(R.id.btnQR)
        btnScan.setOnClickListener{
            val intent=Intent(this,ScanActivity::class.java)
            startActivity(intent)
        }

    }


}