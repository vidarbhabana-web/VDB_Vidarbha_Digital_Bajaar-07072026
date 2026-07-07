package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainAppScreen
import com.example.ui.VdbViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(true) } // Default to high-fidelity dark mode on first launch
            
            MyApplicationTheme(darkTheme = darkTheme, dynamicColor = false) {
                val viewModel: VdbViewModel = viewModel()
                MainAppScreen(
                    viewModel = viewModel,
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
