package com.example.presentation.screens

import android.widget.Toast
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Booking
import com.example.domain.model.ChatMessage
import com.example.presentation.theme.NavySecondary
import com.example.presentation.theme.OrangePrimary
import com.example.presentation.viewmodel.HazirViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTrackerScreen(
    bookingId: Int,
    viewModel: HazirViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val booking by viewModel.activeBookingDetail.collectAsStateWithLifecycle()
    val activeWorkerProfile by viewModel.activeWorkerProfile.collectAsStateWithLifecycle()
    val chatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    val workerLat by viewModel.simulatedWorkerLat.collectAsStateWithLifecycle()
    val workerLng by viewModel.simulatedWorkerLng.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val userBalance = userProfile?.walletBalance ?: 0.0

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val isWorkerTyping by viewModel.isWorkerTyping.collectAsStateWithLifecycle()
    val isCustomerTyping by viewModel.isCustomerTyping.collectAsStateWithLifecycle()

    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var isPlayingVoiceId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            while (isRecording) {
                kotlinx.coroutines.delay(1000)
                recordingDuration++
            }
        }
    }

    var showChatRoom by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    var showShareDialog by remember { mutableStateOf(false) }
    var generatedShareLink by remember { mutableStateOf("") }
    
    var inlineRating by remember(bookingId) { mutableStateOf(5) }
    var inlineReviewText by remember(bookingId) { mutableStateOf("") }

    // Network Status State
    var isNetworkAvailable by remember { mutableStateOf(true) }
    var wasNetworkLost by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Retrieve initial network state
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        isNetworkAvailable = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Handler(Looper.getMainLooper()).post {
                    isNetworkAvailable = true
                }
            }

            override fun onLost(network: Network) {
                Handler(Looper.getMainLooper()).post {
                    isNetworkAvailable = false
                    wasNetworkLost = true
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            // Fallback if permission or API is restricted in sandbox
            isNetworkAvailable = true
        }

        onDispose {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    LaunchedEffect(isNetworkAvailable) {
        if (isNetworkAvailable && wasNetworkLost) {
            Toast.makeText(context, "Back online! Live booking updates resumed.", Toast.LENGTH_SHORT).show()
            wasNetworkLost = false
        } else if (!isNetworkAvailable) {
            Toast.makeText(context, "Internet connection lost.", Toast.LENGTH_SHORT).show()
        }
    }

    // Cancellation Flow State
    var showCancellationModal by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var otherReasonText by remember { mutableStateOf("") }
    var showFinalConfirmation by remember { mutableStateOf(false) }

    // Synchronize selected booking ID in ViewModel on creation
    LaunchedEffect(bookingId) {
        viewModel.selectBooking(bookingId)
    }

    val currentBooking = booking
    if (currentBooking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OrangePrimary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentBooking.categoryName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavySecondary
                        )
                        Text(
                            text = "Booking ID: #${currentBooking.id}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.selectBooking(null)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavySecondary)
                    }
                },
                actions = {
                    StatusBadge(currentBooking.status)
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = !isNetworkAvailable) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD32F2F))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline Indicator",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "No connection. Tracking paused.",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 1. Vector Map Area
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("booking_detail_map_view")
            ) {
                SimulatedLiveMap(
                    workerLat = workerLat,
                    workerLng = workerLng,
                    customerLat = currentBooking.latitude,
                    customerLng = currentBooking.longitude,
                    status = currentBooking.status,
                    modifier = Modifier.fillMaxSize()
                )

                // Live Tracking Overlay when technician is en route ("ACCEPTED")
                if (currentBooking.status == "ACCEPTED" && workerLat != null && workerLng != null) {
                    LiveTrackingOverlay(
                        workerLat = workerLat!!,
                        workerLng = workerLng!!,
                        customerLat = currentBooking.latitude,
                        customerLng = currentBooking.longitude,
                        workerName = currentBooking.workerName ?: "Technician",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    )
                }
            }

            // 2. Handyman detail panel
            Card(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Profile Info Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (currentBooking.workerName != null) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=200&h=200&q=80",
                                    contentDescription = "Handyman Avatar",
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, OrangePrimary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(OrangePrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = "Handyman",
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = currentBooking.workerName ?: "Assigning Worker...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = NavySecondary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                    Text("4.8 (142 completed)", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Share live status button
                            IconButton(
                                onClick = {
                                    val token = (100000..999999).random().toString()
                                    generatedShareLink = "https://hazir-app.com/track/share/${currentBooking.id}?token=SH_$token"
                                    showShareDialog = true
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(OrangePrimary.copy(alpha = 0.08f))
                                    .size(44.dp)
                                    .testTag("share_status_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share Status", tint = OrangePrimary)
                            }

                            // Call phone floating action
                            IconButton(
                                onClick = {
                                    if (currentBooking.workerPhone != null) {
                                        Toast.makeText(context, "Mock Dialing Worker: ${currentBooking.workerPhone}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Worker not assigned yet", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(NavySecondary.copy(alpha = 0.08f))
                                    .size(44.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = NavySecondary)
                            }
                        }
                    }

                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Job Address & Timings
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Room, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Text(currentBooking.address, fontSize = 12.sp, color = NavySecondary, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Text("Scheduled: ${currentBooking.date} • ${currentBooking.time}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        // Professional Profile Card Component
                        if (currentBooking.workerName != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("professional_profile_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Professional Profile",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = NavySecondary
                                        )

                                        val isVerified = activeWorkerProfile?.cnicVerified ?: true
                                        if (isVerified) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFE8F5E9))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    .testTag("professional_verified_badge"),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified Badge",
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Verified Pro",
                                                    color = Color(0xFF2E7D32),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Experience Info Block
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Engineering,
                                                contentDescription = null,
                                                tint = OrangePrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${activeWorkerProfile?.experienceYears ?: 5} Years",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = NavySecondary,
                                                modifier = Modifier.testTag("professional_experience_text")
                                            )
                                            Text(
                                                text = "Experience",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(32.dp)
                                                .background(Color.LightGray)
                                        )

                                        // Completed Jobs Info Block
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = OrangePrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${activeWorkerProfile?.completedJobs ?: 142} Jobs",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = NavySecondary,
                                                modifier = Modifier.testTag("professional_completed_jobs_text")
                                            )
                                            Text(
                                                text = "Completed",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(32.dp)
                                                .background(Color.LightGray)
                                        )

                                        // Star Rating Block
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${activeWorkerProfile?.rating ?: 4.8} ★",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = NavySecondary,
                                                modifier = Modifier.testTag("professional_rating_text")
                                            )
                                            Text(
                                                text = "Rating",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }

                            // 2.5 Direct Real-Time Messaging Component within Detail Screen
                            var inlineChatInput by remember { mutableStateOf("") }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("inline_messaging_component"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF4CAF50)) // Live green status indicator
                                            )
                                            Text(
                                                text = "Direct Service Chat",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = NavySecondary
                                            )
                                        }

                                        // Open full chat button
                                        TextButton(
                                            onClick = { showChatRoom = true },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.testTag("open_full_chat_text_button")
                                        ) {
                                            Text(
                                                text = "Open Chat Room",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = OrangePrimary
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = "Open Chat Room",
                                                tint = OrangePrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                    // Display the 3 most recent messages or placeholder if empty
                                    val recentMessages = chatMessages.takeLast(3)
                                    if (recentMessages.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(60.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No messages yet. Ask about timing or specific details!",
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                fontStyle = FontStyle.Italic,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            recentMessages.forEach { msg ->
                                                val isMe = msg.senderRole == currentRole
                                                val senderName = if (isMe) "You" else (currentBooking.workerName ?: "Technician")
                                                val bubbleColor = if (isMe) OrangePrimary.copy(alpha = 0.08f) else Color(0xFFF1F5F9)
                                                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                                
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(bubbleColor)
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = senderName,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 10.sp,
                                                                color = if (isMe) OrangePrimary else NavySecondary
                                                            )
                                                            Text(
                                                                text = sdf.format(Date(msg.timestamp)),
                                                                fontSize = 8.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = msg.message,
                                                            fontSize = 11.sp,
                                                            color = NavySecondary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Quick Input field and send button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = inlineChatInput,
                                            onValueChange = { inlineChatInput = it },
                                            placeholder = { Text("Type a message to ${currentBooking.workerName ?: "pro"}...", fontSize = 11.sp) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 40.dp)
                                                .testTag("inline_chat_input_field"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = OrangePrimary,
                                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                                cursorColor = OrangePrimary
                                            ),
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (inlineChatInput.trim().isNotEmpty()) {
                                                    viewModel.sendChatMessage(currentBooking.id, inlineChatInput)
                                                    inlineChatInput = ""
                                                }
                                            },
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(OrangePrimary)
                                                .size(36.dp)
                                                .testTag("inline_chat_send_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Send Message",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Service Proof Photo Verification Card
                        if (currentBooking.beforePhoto != null || currentBooking.afterPhoto != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Service Proof Verification",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                    }
                                    Text(
                                        text = "Photos captured by provider Sajid upon completing your repair job:",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (currentBooking.beforePhoto != null) {
                                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(100.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    AsyncImage(
                                                        model = currentBooking.beforePhoto,
                                                        contentDescription = "Before Photo",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Before Work", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            }
                                        }
                                        if (currentBooking.afterPhoto != null) {
                                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(100.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    AsyncImage(
                                                        model = currentBooking.afterPhoto,
                                                        contentDescription = "After Photo",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("After Work", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Rating Option for Completed Booking
                        if (currentBooking.status == "COMPLETED") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("booking_rating_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (currentBooking.rating != null) {
                                        // Booking is already rated: show read-only rating info
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Success",
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Service Rated & Paid",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            for (i in 1..5) {
                                                Icon(
                                                    imageVector = if (i <= currentBooking.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = null,
                                                    tint = if (i <= currentBooking.rating) Color(0xFFFFB300) else Color.Gray,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        if (!currentBooking.review.isNullOrBlank()) {
                                            Text(
                                                text = "\"${currentBooking.review}\"",
                                                fontSize = 13.sp,
                                                fontStyle = FontStyle.Italic,
                                                color = NavySecondary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                        }
                                    } else {
                                        // Booking is completed but not yet rated: show interactive star-rating and text feedback
                                        Text(
                                            text = "Rate Your Professional",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = NavySecondary
                                        )
                                        Text(
                                            text = "Your feedback helps us maintain high quality standards in Islamabad.",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )

                                        // Star Rating Row
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            for (i in 1..5) {
                                                IconButton(
                                                    onClick = { inlineRating = i },
                                                    modifier = Modifier.size(36.dp).testTag("star_button_$i")
                                                ) {
                                                    Icon(
                                                        imageVector = if (i <= inlineRating) Icons.Default.Star else Icons.Default.StarBorder,
                                                        contentDescription = "$i Stars",
                                                        tint = if (i <= inlineRating) Color(0xFFFFB300) else Color.Gray,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = inlineReviewText,
                                            onValueChange = { inlineReviewText = it },
                                            placeholder = { Text("Write your feedback here... (Optional)", fontSize = 12.sp) },
                                            modifier = Modifier.fillMaxWidth().testTag("rating_feedback_input"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = OrangePrimary,
                                                unfocusedBorderColor = Color.LightGray
                                            ),
                                            maxLines = 3
                                        )

                                        Button(
                                            onClick = {
                                                viewModel.payAndReviewBooking(
                                                    bookingId = currentBooking.id,
                                                    rating = inlineRating,
                                                    review = inlineReviewText,
                                                    tipAmount = 0.0,
                                                    finalPaymentMethod = currentBooking.paymentMethod
                                                )
                                                Toast.makeText(context, "Thank you for your rating!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("submit_rating_feedback_button")
                                        ) {
                                            Text("Submit Rating", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Collapsible FAQ Section Composable
                        BookingFAQSection()
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    // Action trigger panel
                    Box(modifier = Modifier.fillMaxWidth()) {
                        when (currentBooking.status) {
                            "PENDING" -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    LinearProgressIndicator(color = OrangePrimary, modifier = Modifier.fillMaxWidth())
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Searching for verified local workers...", fontSize = 11.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            selectedReason = ""
                                            otherReasonText = ""
                                            showCancellationModal = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("cancel_booking_request_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Cancel Booking Request", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            "ACCEPTED", "ARRIVED", "STARTED" -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Live Chat Room button
                                    Button(
                                        onClick = { showChatRoom = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("open_chat_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Chat with Worker", fontWeight = FontWeight.Bold)
                                    }

                                    // Emergency cancellation
                                    if (currentBooking.status != "STARTED") {
                                        Button(
                                            onClick = {
                                                selectedReason = ""
                                                otherReasonText = ""
                                                showCancellationModal = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                                            modifier = Modifier
                                                .height(48.dp)
                                                .testTag("emergency_cancel_button"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Cancel", color = Color.Red, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            "COMPLETED" -> {
                                if (currentBooking.rating == null) {
                                    // Complete booking pay & review button
                                    Button(
                                        onClick = { showPaymentDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("pay_review_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Pay & Review Service (PKR ${currentBooking.estimatedPrice})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                } else {
                                    // Already rated and paid
                                    Button(
                                        onClick = {
                                            viewModel.selectBooking(null)
                                            onBack()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("back_to_bookings_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Back to Bookings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                            "CANCELLED" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFFEBEE))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("This booking was cancelled.", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Sliding full overlay for real-time customer-worker chat
        AnimatedVisibility(visible = showChatRoom) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavySecondary)
                            .padding(16.paddingsFixed())
                            .statusBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showChatRoom = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentBooking.workerName ?: "Handyman", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Online • Active Job Chat", color = Color.Green, fontSize = 11.sp)
                        }
                        IconButton(onClick = {
                            if (currentBooking.workerPhone != null) {
                                Toast.makeText(context, "Dialing worker...", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                        }
                    }

                    // Message lists
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isMe = msg.senderRole == currentRole
                            val bubbleColor = if (isMe) OrangePrimary else Color(0xFFF1F5F9)
                            val textColor = if (isMe) Color.White else NavySecondary
                            val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart

                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                                val isVoice = msg.message.contains("🎙️ [Voice Note")
                                val isImage = msg.message.startsWith("📷 [Image]")

                                if (isImage) {
                                    val caption = msg.message.removePrefix("📷 [Image] ").trim()
                                    Column(
                                        modifier = Modifier
                                            .width(220.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bubbleColor)
                                            .padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isMe) OrangePrimary.copy(alpha = 0.15f)
                                                    else Color.Gray.copy(alpha = 0.1f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(
                                                    imageVector = when {
                                                        caption.contains("Receipt") -> Icons.Default.ReceiptLong
                                                        caption.contains("Part") || caption.contains("Spare") -> Icons.Default.Build
                                                        else -> Icons.Default.CameraAlt
                                                    },
                                                    contentDescription = null,
                                                    tint = textColor.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Text(
                                                    text = "High-Res Image • Simulated",
                                                    fontSize = 10.sp,
                                                    color = textColor.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                        Text(
                                            text = caption,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                } else if (isVoice) {
                                    val isPlaying = isPlayingVoiceId == msg.id
                                    Row(
                                        modifier = Modifier
                                            .width(220.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bubbleColor)
                                            .clickable {
                                                isPlayingVoiceId = if (isPlaying) null else msg.id
                                            }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                            contentDescription = "Play/Pause",
                                            tint = textColor,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.height(16.dp)
                                            ) {
                                                val barHeights = listOf(8, 14, 10, 16, 12, 6, 12, 8, 14, 10)
                                                barHeights.forEachIndexed { i, height ->
                                                    val animatedHeight = if (isPlaying) {
                                                        val infiniteTransition = rememberInfiniteTransition(label = "wave")
                                                        val scale by infiniteTransition.animateFloat(
                                                            initialValue = 0.4f,
                                                            targetValue = 1.2f,
                                                            animationSpec = infiniteRepeatable(
                                                                animation = tween(durationMillis = 400 + (i * 50), easing = LinearEasing),
                                                                repeatMode = RepeatMode.Reverse
                                                            ),
                                                            label = "wave_scale_$i"
                                                        )
                                                        height.dp * scale
                                                    } else {
                                                        height.dp
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.dp)
                                                            .height(animatedHeight)
                                                            .clip(CircleShape)
                                                            .background(textColor.copy(alpha = 0.7f))
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Voice Note • 0:08",
                                                fontSize = 9.sp,
                                                color = textColor.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 260.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 12.dp,
                                                    topEnd = 12.dp,
                                                    bottomStart = if (isMe) 12.dp else 2.dp,
                                                    bottomEnd = if (isMe) 2.dp else 12.dp
                                                )
                                            )
                                            .background(bubbleColor)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = msg.message,
                                            fontSize = 13.sp,
                                            color = textColor
                                        )
                                    }
                                }
                                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                                ) {
                                    Text(
                                        text = sdf.format(Date(msg.timestamp)),
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                    if (isMe) {
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "Read",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "Read",
                                            fontSize = 8.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }

                        // Display typing indicator inside LazyColumn item list if typing
                        val otherPartyName = if (currentRole == "CUSTOMER") (currentBooking.workerName ?: "Technician") else "Customer"
                        val isOtherPartyTyping = if (currentRole == "CUSTOMER") isWorkerTyping else isCustomerTyping
                        if (isOtherPartyTyping) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF1F5F9))
                                            .padding(8.dp)
                                    ) {
                                        TypingIndicator(otherPartyName)
                                    }
                                }
                            }
                        }
                    }

                    // Smart Quick Replies Row
                    val quickReplies = if (currentRole == "CUSTOMER") {
                        listOf(
                            "Where are you now?",
                            "What is the total estimated cost?",
                            "Do you need me to buy any material?",
                            "Okay, sure! I'll be waiting.",
                            "Please bring a drill machine."
                        )
                    } else {
                        listOf(
                            "I am on my way to your location.",
                            "I will be reaching in 5 minutes Insha'Allah.",
                            "I am starting the service work now.",
                            "What is your complete apartment/street address?",
                            "Could you please show me a photo of the problem?"
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .border(BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f)))
                    ) {
                        items(quickReplies) { reply ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.sendChatMessage(currentBooking.id, reply)
                                },
                                label = { Text(reply, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangePrimary) },
                                border = BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = OrangePrimary.copy(alpha = 0.05f)
                                )
                            )
                        }
                    }

                    // Text Composer box
                    Surface(
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { showAttachmentDialog = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.1f))
                                    .size(40.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Attachment", tint = NavySecondary)
                            }

                            if (isRecording) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFFFF1F2))
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                        val alpha by infiniteTransition.animateFloat(
                                            initialValue = 0.3f,
                                            targetValue = 1f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(500, easing = LinearEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "pulse_alpha"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .graphicsLayer(alpha = alpha)
                                                .clip(CircleShape)
                                                .background(Color.Red)
                                        )
                                        Text(
                                            text = "Recording Voice... 0:${String.format("%02d", recordingDuration)}",
                                            fontSize = 12.sp,
                                            color = Color.Red,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = chatInput,
                                    onValueChange = { chatInput = it },
                                    placeholder = { Text("Type a message...", fontSize = 13.sp) },
                                    modifier = Modifier.weight(1f).testTag("chat_input_field"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = OrangePrimary,
                                        cursorColor = OrangePrimary
                                    )
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (isRecording) {
                                        isRecording = false
                                        viewModel.sendChatMessage(currentBooking.id, "🎙️ [Voice Note - 0:0$recordingDuration] 🔊 Play")
                                    } else {
                                        isRecording = true
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isRecording) Color.Red else Color.Gray.copy(alpha = 0.1f))
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Voice Note",
                                    tint = if (isRecording) Color.White else NavySecondary
                                )
                            }

                            if (!isRecording) {
                                IconButton(
                                    onClick = {
                                        if (chatInput.trim().isNotEmpty()) {
                                            viewModel.sendChatMessage(currentBooking.id, chatInput)
                                            chatInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(OrangePrimary)
                                        .size(44.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                                }
                            }
                        }
                    }

                    if (showAttachmentDialog) {
                        AlertDialog(
                            onDismissRequest = { showAttachmentDialog = false },
                            title = { Text("Send Simulated Attachment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavySecondary) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Select a media type to simulate real-time file sharing with the other party:", fontSize = 12.sp, color = Color.Gray)
                                    
                                    listOf(
                                        Triple("📸 Capture Job Area", "📷 [Image] Work Area Photo", Icons.Default.CameraAlt),
                                        Triple("📄 Material Invoice / Receipt", "📷 [Image] Material Receipt", Icons.Default.ReceiptLong),
                                        Triple("🔧 Replacement Spare Part", "📷 [Image] Replacement Spare Part", Icons.Default.Build)
                                    ).forEach { (label, payload, icon) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    viewModel.sendChatMessage(currentBooking.id, payload)
                                                    showAttachmentDialog = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(imageVector = icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(24.dp))
                                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavySecondary)
                                        }
                                    }
                                }
                            },
                            confirmButton = {},
                            dismissButton = {
                                TextButton(onClick = { showAttachmentDialog = false }) {
                                    Text("Cancel", color = Color.Gray)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Complete Checkout Rating dialog
        if (showPaymentDialog) {
            RatingAndReviewDialog(
                estimatedPrice = currentBooking.estimatedPrice,
                initialPaymentMethod = currentBooking.paymentMethod,
                userBalance = userBalance,
                onConfirm = { rating, review, tip, method ->
                    viewModel.payAndReviewBooking(
                        bookingId = currentBooking.id,
                        rating = rating,
                        review = review,
                        tipAmount = tip,
                        finalPaymentMethod = method
                    )
                    showPaymentDialog = false
                    viewModel.selectBooking(null)
                    onBack()
                },
                onDismiss = { showPaymentDialog = false },
                onTopUp = { amount ->
                    viewModel.depositWalletFunds(amount)
                }
            )
        }

        // Share Status Dialog
        if (showShareDialog) {
            AlertDialog(
                onDismissRequest = { showShareDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Share Live Status",
                        fontWeight = FontWeight.Bold,
                        color = NavySecondary,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "A secure, temporary tracking link has been generated. This allows contacts to monitor your technician's real-time progress and job status.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = generatedShareLink,
                                fontSize = 11.sp,
                                color = NavySecondary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                val serviceName = currentBooking.categoryName
                                val statusText = currentBooking.status.replace("_", " ").uppercase()
                                val worker = currentBooking.workerName ?: "Assigning soon..."
                                val arrivalTime = currentBooking.time
                                val date = currentBooking.date
                                val address = currentBooking.address
                                val price = currentBooking.estimatedPrice
                                val messageBody = """
                                    🛠️ HAZIR SERVICE BOOKING DETAILS 🛠️
                                    
                                    Assalaamu Alaikum! Here are the tracking & booking details for my Hazir service:
                                    
                                    • Booking ID: #${currentBooking.id}
                                    • Service: $serviceName
                                    • Technician: $worker
                                    • Status: $statusText
                                    • Scheduled/Arrival Time: $date • $arrivalTime
                                    • Address: $address
                                    • Estimated Price: PKR $price
                                    
                                    📍 Live Tracking Link: $generatedShareLink
                                    
                                    Thank you for using Hazir – your instant on-demand handyman partner!
                                """.trimIndent()

                                putExtra(Intent.EXTRA_TEXT, messageBody)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share status link via")
                            context.startActivity(shareIntent)
                            showShareDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Link", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Hazir Tracking Link", generatedShareLink)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Link")
                    }
                }
            )
        }

        // 1. Cancellation Reason Selection Dialog
        if (showCancellationModal) {
            AlertDialog(
                onDismissRequest = { showCancellationModal = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Cancel Booking",
                            fontWeight = FontWeight.Bold,
                            color = NavySecondary,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Please select a reason for cancelling your booking. This helps us improve our service and provider reliability.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )

                        val reasons = listOf(
                            "Changed my mind",
                            "Found a cheaper alternative",
                            "Provider is unresponsive",
                            "Schedule/Time conflict",
                            "Accidental booking",
                            "Other reason"
                        )

                        reasons.forEach { reason ->
                            val isSelected = selectedReason == reason
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OrangePrimary.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable { selectedReason = reason }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (isSelected) OrangePrimary else Color.Gray, CircleShape)
                                        .padding(3.dp)
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(OrangePrimary)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = reason,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) OrangePrimary else NavySecondary
                                )
                            }
                        }

                        if (selectedReason == "Other reason") {
                            OutlinedTextField(
                                value = otherReasonText,
                                onValueChange = { otherReasonText = it },
                                placeholder = { Text("Please describe briefly...", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                                maxLines = 2
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCancellationModal = false
                            showFinalConfirmation = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        enabled = selectedReason.isNotEmpty() && (selectedReason != "Other reason" || otherReasonText.isNotBlank())
                    ) {
                        Text("Continue", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancellationModal = false }) {
                        Text("Go Back", color = Color.Gray)
                    }
                }
            )
        }

        // 2. Final Confirmation Dialog (Prevent accidental clicks)
        if (showFinalConfirmation) {
            AlertDialog(
                onDismissRequest = { showFinalConfirmation = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Confirm Cancellation?",
                        fontWeight = FontWeight.Bold,
                        color = NavySecondary,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Are you absolutely sure you want to cancel this booking? This action cannot be reversed and may impact your customer reliability score.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Selected Reason: " + if (selectedReason == "Other reason") otherReasonText else selectedReason,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Red,
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val finalReason = if (selectedReason == "Other reason") otherReasonText else selectedReason
                            viewModel.cancelActiveBooking(currentBooking.id, finalReason)
                            showFinalConfirmation = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Yes, Cancel Booking", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinalConfirmation = false }) {
                        Text("No, Keep Booking", fontWeight = FontWeight.SemiBold, color = NavySecondary)
                    }
                }
            )
        }
    }
}

// Inline helper for standard paddings
private fun Int.paddingsFixed() = PaddingValues(this.dp)

data class FAQItemData(val id: String, val question: String, val answer: String)

@Composable
fun BookingFAQSection() {
    var expandedItem by remember { mutableStateOf<String?>(null) }

    val faqs = listOf(
        FAQItemData(
            id = "safety",
            question = "What is Hazir’s customer safety policy?",
            answer = "Your safety is our priority. Every Hazir technician undergoes background checks, biometric registration, and strict onboarding tests. Live sharing is available so family and friends can track your booking status and location in real-time."
        ),
        FAQItemData(
            id = "cancel_refund",
            question = "How do cancellation refunds work?",
            answer = "Cancellations are completely free before a worker is assigned or accepts your job. If you cancel after a worker is en route, any online pre-payment is immediately refunded to your Hazir Wallet (within 10 minutes) or your bank card (within 3-5 business days)."
        ),
        FAQItemData(
            id = "extension",
            question = "Why would a service duration be extended?",
            answer = "For complex repairs, the technician might need more time for accurate diagnostics or assembly. Any extension requested must be explicitly approved by you on this screen before extra charges are added to your invoice."
        ),
        FAQItemData(
            id = "pricing",
            question = "Are there any hidden platform fees?",
            answer = "No! All charges, including the base fee, technician tip, and any approved time extensions are clearly transparent. You only pay what you see on your final digital receipt."
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "🙋 Common Service FAQs",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NavySecondary
            )
            Text(
                text = "Quick answers to safety policies and billing queries.",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            faqs.forEach { faq ->
                val isExpanded = expandedItem == faq.id
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedItem = if (isExpanded) null else faq.id }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = faq.question,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isExpanded) OrangePrimary else Color(0xFF334155),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = if (isExpanded) OrangePrimary else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (isExpanded) {
                        Text(
                            text = faq.answer,
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(start = 2.dp, top = 2.dp, bottom = 8.dp)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@Composable
fun LiveTrackingOverlay(
    workerLat: Double,
    workerLng: Double,
    customerLat: Double,
    customerLng: Double,
    workerName: String,
    modifier: Modifier = Modifier
) {
    val (etaMins, distanceKm) = remember(workerLat, workerLng, customerLat, customerLng) {
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(customerLat - workerLat)
        val dLng = Math.toRadians(customerLng - workerLng)
        val a = kotlin.math.sin(dLat / 2.0) * kotlin.math.sin(dLat / 2.0) +
                kotlin.math.cos(Math.toRadians(workerLat)) * kotlin.math.cos(Math.toRadians(customerLat)) *
                kotlin.math.sin(dLng / 2.0) * kotlin.math.sin(dLng / 2.0)
        val c = 2.0 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1.0 - a))
        val directKm = R * c

        val routingKm = directKm * 1.35
        val eta = kotlin.math.max(1, kotlin.math.round(routingKm / 0.5).toInt())
        Pair(eta, routingKm)
    }

    // Fluctuate speed slightly to give a real-time tracking sensation
    val simulatedSpeed = remember(workerLat, workerLng) {
        (28..34).random()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_tracking")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Card(
        modifier = modifier
            .widthIn(max = 280.dp)
            .testTag("live_tracking_overlay_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Badge Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32).copy(alpha = dotAlpha))
                    )
                    Text(
                        text = "LIVE TRACKING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2E7D32),
                        letterSpacing = 0.8.sp
                    )
                }
                Text(
                    text = "on Suzuki GD 110S",
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            // ETA and status
            Text(
                text = "$workerName is en route",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NavySecondary
            )

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            // Real-time metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "ETA",
                            tint = OrangePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (etaMins == 1) "Under 1 min" else "$etaMins mins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavySecondary
                        )
                    }
                    Text(
                        text = "Est. Arrival",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray.copy(alpha = 0.5f)))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Distance",
                            tint = NavySecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f km", distanceKm),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavySecondary
                        )
                    }
                    Text(
                        text = "Distance Left",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray.copy(alpha = 0.5f)))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed",
                            tint = Color(0xFFFF9F1C),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$simulatedSpeed km/h",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavySecondary
                        )
                    }
                    Text(
                        text = "Avg Speed",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicator(name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "dots")
        
        Text(
            text = "$name is typing",
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        
        listOf(0, 1, 2).forEach { index ->
            val delayMillis = index * 150
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 600
                        0.2f at delayMillis with FastOutSlowInEasing
                        1.2f at delayMillis + 150 with FastOutSlowInEasing
                        0.2f at delayMillis + 300 with FastOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "dot_scale_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        }
    }
}
