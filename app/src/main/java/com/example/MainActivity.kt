package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SetupWizardScreen
import com.example.ui.theme.GeminiBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val uiState by chatViewModel.uiState.collectAsState()

                if (!uiState.isInitialized) {
                    Box(modifier = Modifier.fillMaxSize().background(GeminiBackground))
                } else {
                    AnimatedContent(
                        targetState = uiState.shouldShowSetupWizard,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "screen_transition"
                    ) { shouldShowWizard ->
                        if (shouldShowWizard) {
                            SetupWizardScreen(
                                viewModel = chatViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            HomeScreen(
                                viewModel = chatViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
