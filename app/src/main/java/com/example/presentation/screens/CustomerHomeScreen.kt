package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Booking
import com.example.domain.model.ChatMessage
import com.example.domain.model.ServiceCategory
import com.example.domain.model.User
import com.example.domain.model.AiChatMessage
import com.example.presentation.theme.NavySecondary
import com.example.presentation.theme.OrangeLight
import com.example.presentation.theme.OrangePrimary
import com.example.presentation.viewmodel.HazirViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    viewModel: HazirViewModel,
    onTrackBooking: (Int) -> Unit
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val bookings by viewModel.customerBookings.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiChatHistory.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Home, 1: AI Advisor, 2: Bookings, 3: Wallet
    var showBookingSheetCategory by remember { mutableStateOf<ServiceCategory?>(null) }
    var showRoleMenu by remember { mutableStateOf(false) }

    val activeBooking = bookings.firstOrNull { it.status != "COMPLETED" && it.status != "CANCELLED" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "HAZIR",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = OrangePrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Islamabad • Pakistan",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    // Quick Persona Swapper Button
                    Box {
                        TextButton(
                            onClick = { showRoleMenu = true },
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(OrangePrimary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Role Swapper",
                                tint = OrangePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Switch Persona",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Customer Dashboard (Active)") },
                                onClick = {
                                    viewModel.setAppRole("CUSTOMER")
                                    showRoleMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Worker Console (Sajid)") },
                                onClick = {
                                    viewModel.setAppRole("WORKER")
                                    showRoleMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Engineering, contentDescription = null, tint = NavySecondary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Admin Panel (Ayesha)") },
                                onClick = {
                                    viewModel.setAppRole("ADMIN")
                                    showRoleMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.Red) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // User Avatar
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NavySecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile?.name?.take(1) ?: "H",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        selectedTextColor = OrangePrimary,
                        indicatorColor = OrangePrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Advisor") },
                    label = { Text("Hazir AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        selectedTextColor = OrangePrimary,
                        indicatorColor = OrangePrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("Bookings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        selectedTextColor = OrangePrimary,
                        indicatorColor = OrangePrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                    label = { Text("Wallet", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        selectedTextColor = OrangePrimary,
                        indicatorColor = OrangePrimary.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> HomeTabContent(
                    categories = categories,
                    activeBooking = activeBooking,
                    userProfile = userProfile,
                    onCategoryClick = { showBookingSheetCategory = it },
                    onNavigateToAi = { activeTab = 1 },
                    onTrackBooking = onTrackBooking
                )
                1 -> AiAdvisorTabContent(
                    messages = aiMessages,
                    isLoading = aiLoading,
                    categories = categories,
                    onSendMessage = { viewModel.askAiAdvisor(it) },
                    onPrefillBooking = { category ->
                        showBookingSheetCategory = categories.firstOrNull { it.id == category.id }
                    },
                    onClearChat = { viewModel.clearAiChat() }
                )
                2 -> BookingsHistoryTabContent(
                    bookings = bookings,
                    onTrackBooking = onTrackBooking
                )
                3 -> WalletTabContent(
                    viewModel = viewModel,
                    userProfile = userProfile
                )
            }

            // Booking dialog sheet
            showBookingSheetCategory?.let { category ->
                BookingFormDialog(
                    category = category,
                    onDismiss = { showBookingSheetCategory = null },
                    onConfirmBooking = { address, description, price, date, time ->
                        viewModel.requestBooking(
                            categoryId = category.id,
                            categoryName = category.name,
                            address = address,
                            description = description,
                            estimatedPrice = price,
                            date = date,
                            time = time
                        )
                        showBookingSheetCategory = null
                        // Switch immediately to Booking List tab so they see progress
                        activeTab = 2
                    }
                )
            }
        }
    }
}

// ==========================================
// TAB 1: HOME TAB
// ==========================================
@Composable
fun HomeTabContent(
    categories: List<ServiceCategory>,
    activeBooking: Booking?,
    userProfile: User?,
    onCategoryClick: (ServiceCategory) -> Unit,
    onNavigateToAi: () -> Unit,
    onTrackBooking: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Asalam-o-Alaikum, ${userProfile?.name ?: "Customer"}!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavySecondary
                )
                Text(
                    text = "What instant repair do you need today?",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        // Active Booking alert banner
        activeBooking?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrackBooking(it.id) },
                colors = CardDefaults.cardColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ongoing Active Booking",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = it.categoryName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Status: ${it.status}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Button(
                        onClick = { onTrackBooking(it.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Track Live", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Hero Promotional Slider Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NavySecondary, Color(0xFF162447))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(OrangePrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("AC SPECIAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text(
                        text = "Beat the Heat! AC Cleaning\nstarting from 800 PKR",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = "Get expert split diagnostics and instant gas refilling nearby.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Services Grid Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Instant Skilled Services",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NavySecondary
            )
            Text(
                text = "View All",
                fontSize = 12.sp,
                color = OrangePrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { }
            )
        }

        // Grid of services categories
        Box(modifier = Modifier.height(280.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { category ->
                    val icon = getCategoryIcon(category.iconName)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onCategoryClick(category) }
                            .testTag("category_${category.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(OrangePrimary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = category.name,
                                tint = OrangePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = category.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavySecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // AI Advisor Promotion Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToAi() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unsure what service is needed?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavySecondary
                    )
                    Text(
                        text = "Chat with Hazir AI Advisor to diagnose leaks, sparks, or errors instantly.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = NavySecondary
                )
            }
        }
    }
}

// ==========================================
// TAB 2: AI ADVISOR CHAT TAB
// ==========================================
@Composable
fun AiAdvisorTabContent(
    messages: List<AiChatMessage>,
    isLoading: Boolean,
    categories: List<ServiceCategory>,
    onSendMessage: (String) -> Unit,
    onPrefillBooking: (ServiceCategory) -> Unit,
    onClearChat: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // AI Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = OrangePrimary)
                Column {
                    Text("Hazir AI Diagnostics", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavySecondary)
                    Text("Powered by Gemini 3.5-Flash", fontSize = 10.sp, color = Color.Gray)
                }
            }
            TextButton(onClick = onClearChat) {
                Text("Clear History", color = Color.Gray, fontSize = 12.sp)
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                val bubbleColor = if (msg.isUser) OrangePrimary else Color(0xFFECEFF1)
                val textColor = if (msg.isUser) Color.White else NavySecondary

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                    bottomEnd = if (msg.isUser) 4.dp else 16.dp
                                )
                            )
                            .background(bubbleColor)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.message,
                            color = textColor,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    // Look for matches inside the AI text to display an actionable booking prefill button!
                    if (!msg.isUser) {
                        val lowerText = msg.message.lowercase()
                        val matchedCategory = categories.firstOrNull { 
                            lowerText.contains(it.name.lowercase()) || lowerText.contains(it.id.lowercase()) 
                        }
                        if (matchedCategory != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { onPrefillBooking(matchedCategory) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.1f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AddBusiness, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pre-fill ${matchedCategory.name} Booking", color = OrangePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = OrangePrimary)
                        Text("Hazir AI is analyzing diagnostic faults...", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Input Send Box
        Surface(
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("E.g., Kitchen sink leaking water...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        cursorColor = OrangePrimary
                    ),
                    maxLines = 2
                )
                IconButton(
                    onClick = {
                        if (textInput.trim().isNotEmpty()) {
                            onSendMessage(textInput)
                            textInput = ""
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
}

// ==========================================
// TAB 3: BOOKINGS HISTORY TAB
// ==========================================
@Composable
fun BookingsHistoryTabContent(
    bookings: List<Booking>,
    onTrackBooking: (Int) -> Unit
) {
    if (bookings.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(72.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No bookings yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavySecondary)
            Text("Your requested services in Islamabad will show up here.", textAlign = TextAlign.Center, fontSize = 13.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Your Service History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavySecondary)
            }

            items(bookings) { booking ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTrackBooking(booking.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(booking.categoryName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavySecondary)
                            StatusBadge(booking.status)
                        }
                        Text(booking.description, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Gray)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Date: ${booking.date} • ${booking.time}", fontSize = 11.sp, color = Color.Gray)
                                Text("Price: PKR ${booking.estimatedPrice}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                            }
                            if (booking.status == "COMPLETED" && booking.rating != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                    Text("${booking.rating}/5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavySecondary)
                                }
                            } else if (booking.status != "COMPLETED" && booking.status != "CANCELLED") {
                                Button(
                                    onClick = { onTrackBooking(booking.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.08f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Track Live", color = OrangePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 4: WALLET TAB CONTENT
// ==========================================
@Composable
fun WalletTabContent(
    viewModel: HazirViewModel,
    userProfile: User?
) {
    val transactions by viewModel.walletTransactions.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Hazir Wallet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavySecondary)

        // Premium Credit-Card Design
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(OrangePrimary, Color(0xFFE55B13))
                        )
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("HAZIR PAY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 2.sp)
                    Icon(Icons.Default.Nfc, contentDescription = "NFC", tint = Color.White)
                }

                Column {
                    Text("Available Balance", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text("PKR ${String.format("%.2f", userProfile?.walletBalance ?: 0.0)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(userProfile?.name ?: "Customer Profile", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("Verified Account", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quick Top-up buttons
        Text("Quick Top-up Balance", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavySecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(500, 1000, 2000, 5000).forEach { amount ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.depositWalletFunds(amount.toDouble()) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$amount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = OrangePrimary
                    )
                }
            }
        }

        // Recent transactions log
        Text("Transaction Log", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavySecondary)
        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("No recent transactions", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                transactions.forEach { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val (icon, color) = when (tx.type) {
                                "DEPOSIT" -> Icons.Default.ArrowDownward to Color(0xFF1B5E20)
                                "WITHDRAW" -> Icons.Default.ArrowUpward to Color(0xFFC62828)
                                "PAYMENT_OUT" -> Icons.Default.CallMade to Color(0xFFC62828)
                                "EARNING_IN" -> Icons.Default.CallReceived to Color(0xFF1B5E20)
                                else -> Icons.AutoMirrored.Filled.ReceiptLong to Color.Gray
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(tx.description, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavySecondary)
                                val date = Date(tx.timestamp)
                                val format = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                                Text(format.format(date), fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Text(
                            text = (if (tx.type == "DEPOSIT" || tx.type == "EARNING_IN") "+" else "-") + "PKR ${tx.amount}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.type == "DEPOSIT" || tx.type == "EARNING_IN") Color(0xFF1B5E20) else Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-FORM: BOOK HANDYMAN SHEET/DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormDialog(
    category: ServiceCategory,
    onDismiss: () -> Unit,
    onConfirmBooking: (String, String, Double, String, String) -> Unit
) {
    var address by remember { mutableStateOf("F-7 Markaz, Street 4, House 12A, Islamabad") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Today, June 28") }
    var time by remember { mutableStateOf("As soon as possible") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirmBooking(address, description, category.basePrice, date, time)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                enabled = description.trim().isNotEmpty()
            ) {
                Text("Confirm Instant Hiring", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OrangePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(getCategoryIcon(category.iconName), contentDescription = null, tint = OrangePrimary)
                }
                Text("Book ${category.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavySecondary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Describe the issue clearly so the worker brings the correct equipment.", fontSize = 12.sp, color = Color.Gray)

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("E.g. Shower tap leaking water, or replacing 2 burned sockets...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("booking_description_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        cursorColor = OrangePrimary
                    ),
                    maxLines = 3
                )

                // Location field
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Service Address", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        cursorColor = OrangePrimary
                    )
                )

                // Date & Time pickers (Mocked/Pre-filled)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Estimated Price Invoice
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Base Estimated Fare", fontSize = 11.sp, color = Color.Gray)
                            Text("PKR ${category.basePrice}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF33691E))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCEDC8))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Transparent", fontSize = 10.sp, color = Color(0xFF33691E), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    )
}

// ==========================================
// HELPERS
// ==========================================
fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "bolt" -> Icons.Default.Bolt
        "plumbing" -> Icons.Default.Plumbing
        "ac_unit" -> Icons.Default.AcUnit
        "cleaning_services" -> Icons.Default.CleaningServices
        "format_paint" -> Icons.Default.FormatPaint
        "construction" -> Icons.Default.Construction
        "build" -> Icons.Default.Build
        "local_shipping" -> Icons.Default.LocalShipping
        else -> Icons.Default.Handyman
    }
}
