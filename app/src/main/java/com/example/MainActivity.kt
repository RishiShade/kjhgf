package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.falcon.ui.components.FalconBottomNavigation
import com.example.falcon.ui.onboarding.OnboardingScreen
import com.example.falcon.ui.screens.*
import com.example.falcon.ui.viewmodel.FalconMainViewModel
import com.example.ui.theme.FalconDarkBg
import com.example.ui.theme.FalconTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FalconMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FalconTheme {
                val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
                val currentTab by viewModel.currentTab.collectAsState()
                val activeSecondaryScreen by viewModel.activeSecondaryScreen.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FalconDarkBg)
                ) {
                    if (!isOnboardingCompleted) {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onComplete = {
                                viewModel.completeOnboarding()
                            }
                        )
                    } else {
                        // Main Application Interface
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = FalconDarkBg,
                            bottomBar = {
                                if (activeSecondaryScreen == null) {
                                    FalconBottomNavigation(
                                        selectedTab = currentTab,
                                        onTabSelected = { index ->
                                            viewModel.selectTab(index)
                                        }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        bottom = if (activeSecondaryScreen == null) 0.dp else innerPadding.calculateBottomPadding()
                                    )
                            ) {
                                // 1. Primary Navigation Tabs
                                AnimatedVisibility(
                                    visible = activeSecondaryScreen == null,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    when (currentTab) {
                                        0 -> HomeScreen(
                                            viewModel = viewModel,
                                            onOpenConversation = { viewModel.openSecondaryScreen("conversation") },
                                            onOpenActivity = { viewModel.selectTab(1) },
                                            onOpenSettings = { viewModel.selectTab(3) }
                                        )
                                        1 -> ActivityScreen(viewModel = viewModel)
                                        2 -> MemoryScreen(viewModel = viewModel)
                                        3 -> SettingsScreen(
                                            viewModel = viewModel,
                                            onNavigateToSection = { section ->
                                                viewModel.openSecondaryScreen(section)
                                            }
                                        )
                                    }
                                }

                                // 2. Secondary Full Screens (Conversation / Settings Sub-pages)
                                AnimatedVisibility(
                                    visible = activeSecondaryScreen != null,
                                    enter = fadeIn() + slideInVertically { it / 4 },
                                    exit = fadeOut() + slideOutVertically { it / 4 }
                                ) {
                                    activeSecondaryScreen?.let { screen ->
                                        if (screen == "conversation") {
                                            ConversationScreen(
                                                viewModel = viewModel,
                                                onBack = { viewModel.closeSecondaryScreen() }
                                            )
                                        } else {
                                            SettingsDetailContainer(
                                                section = screen,
                                                viewModel = viewModel,
                                                onBack = { viewModel.closeSecondaryScreen() }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
