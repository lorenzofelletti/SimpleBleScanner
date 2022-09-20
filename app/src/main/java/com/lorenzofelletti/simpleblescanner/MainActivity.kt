package com.lorenzofelletti.simpleblescanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartScan: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        btnStartScan = findViewById(R.id.btnStartScan)
        btnStartScan.setOnClickListener {
            Log.i(TAG, "btnStartScan - onClick event")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}