package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.screens.*
import com.example.presentation.theme.MyApplicationTheme
import com.example.presentation.viewmodel.HazirViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: HazirViewModel = viewModel()
                val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
                val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

                var showOnboarding by remember { mutableStateOf(true) }
                var trackedBookingId by remember { mutableStateOf<Int?>(null) }

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedContent(
                        targetState = Triple(showOnboarding, currentUserId.isEmpty(), trackedBookingId),
                        label = "app_navigation"
                    ) { (onboardingActive, isLoggedOut, activeTrackId) ->
                        if (onboardingActive) {
                            OnboardingScreen(
                                onGetStarted = { showOnboarding = false }
                            )
                        } else if (isLoggedOut) {
                            LoginRegisterScreen(
                                viewModel = viewModel,
                                onLoginSuccess = { /* State transitions automatically */ }
                            )
                        } else if (activeTrackId != null) {
                            BookingTrackerScreen(
                                bookingId = activeTrackId,
                                viewModel = viewModel,
                                onBack = { trackedBookingId = null }
                            )
                        } else {
                            when (currentRole) {
                                "CUSTOMER" -> CustomerHomeScreen(
                                    viewModel = viewModel,
                                    onTrackBooking = { trackedBookingId = it }
                                )
                                "WORKER" -> WorkerConsoleScreen(
                                    viewModel = viewModel,
                                    onTrackBooking = { trackedBookingId = it }
                                )
                                "ADMIN" -> AdminPanelScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

