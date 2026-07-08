package com.todaywork.app.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.todaywork.app.MainActivity

class WidgetConfirmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val dateStr = intent.getStringExtra("selected_date")
        
        AlertDialog.Builder(this)
            .setTitle("마님 일정")
            .setMessage("앱의 달력으로 이동하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                val mainIntent = Intent(this, MainActivity::class.java).apply {
                    putExtra("selected_date", dateStr)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(mainIntent)
                finish()
            }
            .setNegativeButton("취소") { _, _ ->
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }
}
