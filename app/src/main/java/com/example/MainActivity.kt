package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppNavDestination
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MyraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MyraViewModel = viewModel()
                MyraAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MyraAppContent(viewModel: MyraViewModel) {
    val isWelcomeCompleted by viewModel.isWelcomeCompleted.collectAsState()
    val currentDestination by viewModel.currentDestination.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Back handling: if on sub-screen, return to HOME
    BackHandler(enabled = isWelcomeCompleted && currentDestination != AppNavDestination.HOME) {
        viewModel.navigateTo(AppNavDestination.HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (!isWelcomeCompleted) {
            WelcomeScreen(viewModel = viewModel)
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = BackgroundDark,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    BottomNavBar(
                        currentDestination = currentDestination,
                        onNavigate = { destination ->
                            viewModel.navigateTo(destination)
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { target ->
                        when (target) {
                            AppNavDestination.HOME -> HomeScreen(viewModel = viewModel)
                            AppNavDestination.CHAT -> ChatScreen(viewModel = viewModel)
                            AppNavDestination.VOICE -> VoiceScreen(viewModel = viewModel)
                            AppNavDestination.HISTORY -> HistoryScreen(viewModel = viewModel)
                            AppNavDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
