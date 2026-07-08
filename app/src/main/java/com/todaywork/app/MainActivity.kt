package com.todaywork.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.todaywork.app.ui.navigation.AppNavigation
import com.todaywork.app.ui.theme.TodayWorkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val selectedDateStr = intent.getStringExtra("selected_date")
        
        enableEdgeToEdge()
        setContent {
            TodayWorkTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                AppNavigation(windowSizeClass = windowSizeClass, initialDateStr = selectedDateStr)
            }
        }
    }
}
