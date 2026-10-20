package com.example.dictionary.feature_dictionary.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dictionary.navigation.AppNavigation
import com.example.dictionary.ui.theme.DictionaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DictionaryTheme {
                AppNavigation()
            }
        }
    }
}
