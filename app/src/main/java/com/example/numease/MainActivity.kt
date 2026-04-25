package com.example.numease

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.navigation.AppNavigation
import com.example.numease.presentation.UserPreferencesViewModel
import com.example.numease.presentation.theme.NumEaseTheme
import com.example.numease.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

val LocalSoundEnabled = compositionLocalOf { true }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Đảm bảo Supabase nhận được intent chứa token
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            val userPrefsViewModel: UserPreferencesViewModel = hiltViewModel()

            // Lắng nghe cài đặt của người dùng
            val userPrefs by userPrefsViewModel.preferences.collectAsState()

            NumEaseTheme (
                darkTheme = userPrefs.isDarkMode, // Theme đã được xử lý chuẩn ở đây!
            ) {
                // ĐÃ THÊM: Bơm trạng thái âm thanh xuống toàn bộ hệ thống UI
                CompositionLocalProvider(
                    LocalSoundEnabled provides userPrefs.isSoundEnabled
                ) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        userPrefsViewModel = userPrefsViewModel
                    )
                }
            }
        }
    }
}

