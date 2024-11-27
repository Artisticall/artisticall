package com.dadm.artisticall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dadm.artisticall.login.LoginScreen
import com.dadm.artisticall.ui.theme.ArtisticallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtisticallTheme {
                LoginScreen()
            }
        }
    }
}