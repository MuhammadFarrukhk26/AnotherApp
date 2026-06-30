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
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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

                    // User Avatar with Logout Menu
                    var showUserMenu by remember { mutableStateOf(false) }
                    Box {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NavySecondary)
                                .clickable { showUserMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile?.name?.take(1) ?: "H",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Log Out Account", color = Color.Red) },
                                onClick = {
                                    showUserMenu = false
                                    viewModel.logout()
                                },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red) }
                            )
                        }
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
                    userBalance = userProfile?.walletBalance ?: 5000.0,
                    onDismiss = { showBookingSheetCategory = null },
                    onConfirmBooking = { address, description, price, date, time, paymentMethod ->
                        viewModel.requestBooking(
                            categoryId = category.id,
                            categoryName = category.name,
                            address = address,
                            description = description,
                            estimatedPrice = price,
                            date = date,
                            time = time,
                            paymentMethod = paymentMethod
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 600.dp)
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
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = if (maxWidth > 600.dp) 6 else 4
            val chunkedCategories = categories.chunked(columns)

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                chunkedCategories.forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowCategories.forEach { category ->
                            val icon = getCategoryIcon(category.iconName)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
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
                        // Pad out empty spaces if the row isn't fully filled
                        val emptySpaces = columns - rowCategories.size
                        if (emptySpaces > 0) {
                            repeat(emptySpaces) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
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
    var selectedTab by remember { mutableStateOf(0) } // 0: Pending & Active, 1: Past Bookings

    val pendingBookings = bookings.filter { it.status != "COMPLETED" && it.status != "CANCELLED" }
    val pastBookings = bookings.filter { it.status == "COMPLETED" || it.status == "CANCELLED" }
    val currentBookingsList = if (selectedTab == 0) pendingBookings else pastBookings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Header
        Text(
            text = "Your Bookings",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = NavySecondary
        )

        // Custom Segmented Pill Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("Pending & Active (${pendingBookings.size})", "Past Bookings (${pastBookings.size})")
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val background = if (isSelected) OrangePrimary else Color.Transparent
                val textColor = if (isSelected) Color.White else NavySecondary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(background)
                        .clickable { selectedTab = index }
                        .padding(vertical = 12.dp)
                        .testTag("bookings_segment_tab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textColor
                    )
                }
            }
        }

        if (currentBookingsList.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val icon = if (selectedTab == 0) Icons.Default.EventNote else Icons.Default.HistoryToggleOff
                val title = if (selectedTab == 0) "No Active Bookings" else "No Past Bookings"
                val description = if (selectedTab == 0) {
                    "You have no pending or active service requests. Need something repaired? Select a category on the Home tab."
                } else {
                    "You have not completed any bookings yet. Your finished service history in Islamabad will appear here."
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color.LightGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavySecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentBookingsList, key = { it.id }) { booking ->
                    val categoryIcon = getCategoryIconByCategoryId(booking.categoryId)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackBooking(booking.id) }
                            .testTag("booking_card_${booking.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Row 1: Category Icon, Name, and StatusBadge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(OrangePrimary.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = categoryIcon,
                                        contentDescription = booking.categoryName,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = booking.categoryName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = NavySecondary
                                    )
                                    Text(
                                        text = "Booking #${booking.id}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                StatusBadge(booking.status)
                            }

                            // Row 2: Description
                            Text(
                                text = booking.description,
                                fontSize = 13.sp,
                                color = NavySecondary.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )

                            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                            // Row 3: Meta info and Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "${booking.date} • ${booking.time}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        text = "PKR ${booking.estimatedPrice}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangePrimary
                                    )
                                }

                                if (booking.status == "COMPLETED") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (booking.rating != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                repeat(booking.rating) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "Star Rating",
                                                        tint = Color(0xFFFFB300),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${booking.rating}/5",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NavySecondary
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "Completed",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B5E20)
                                            )
                                        }
                                    }
                                } else if (booking.status == "CANCELLED") {
                                    Text(
                                        text = "Cancelled",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                } else {
                                    Button(
                                        onClick = { onTrackBooking(booking.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.08f)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("track_button_${booking.id}"),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CompassCalibration,
                                                contentDescription = null,
                                                tint = OrangePrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Track Live",
                                                color = OrangePrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
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

// Resolver for booking category icon
fun getCategoryIconByCategoryId(categoryId: String): ImageVector {
    return when (categoryId) {
        "electrician" -> Icons.Default.Bolt
        "plumber" -> Icons.Default.Plumbing
        "ac_technician" -> Icons.Default.AcUnit
        "cleaner" -> Icons.Default.CleaningServices
        "painter" -> Icons.Default.FormatPaint
        "carpenter" -> Icons.Default.Construction
        "mechanic" -> Icons.Default.Build
        "mover" -> Icons.Default.LocalShipping
        else -> Icons.Default.Handyman
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
// SUB-FORM: BOOK HANDYMAN SHEET/DIALOG WITH SECURE PAYMENT SELECTOR
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormDialog(
    category: ServiceCategory,
    userBalance: Double,
    onDismiss: () -> Unit,
    onConfirmBooking: (String, String, Double, String, String, String) -> Unit
) {
    var address by remember { mutableStateOf("F-7 Markaz, Street 4, House 12A, Islamabad") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Today, June 28") }
    var time by remember { mutableStateOf("As soon as possible") }

    // Payment selection states
    var paymentMethod by remember { mutableStateOf("CASH") } // "CASH", "CARD", "WALLET"
    var walletType by remember { mutableStateOf("HAZIR") } // "HAZIR", "EASYPAISA", "JAZZCASH"

    // Card Details
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }

    // External Wallet details
    var walletPhone by remember { mutableStateOf("0300-1234567") }
    var isOtpRequested by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var isOtpVerified by remember { mutableStateOf(false) }

    // Validate checkout form before enabling booking submission
    val isFormValid = description.trim().isNotEmpty() && when (paymentMethod) {
        "CASH" -> true
        "CARD" -> cardNumber.length >= 16 && cardExpiry.length >= 4 && cardCvv.length >= 3 && cardName.isNotBlank()
        "WALLET" -> {
            if (walletType == "HAZIR") {
                userBalance >= category.basePrice
            } else {
                isOtpVerified
            }
        }
        else -> false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        confirmButton = {
            Button(
                onClick = {
                    val finalPaymentMethod = when (paymentMethod) {
                        "CARD" -> "CARD"
                        "WALLET" -> if (walletType == "HAZIR") "WALLET" else walletType
                        else -> "CASH"
                    }
                    onConfirmBooking(address, description, category.basePrice, date, time, finalPaymentMethod)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                enabled = isFormValid
            ) {
                Text("Confirm Booking & Pay", fontWeight = FontWeight.Bold)
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
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
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

                Spacer(modifier = Modifier.height(2.dp))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(2.dp))

                // Payment Method Selector
                Text("Secure Payment Method", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavySecondary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // COD option
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.5.dp,
                                color = if (paymentMethod == "CASH") OrangePrimary else Color.LightGray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { paymentMethod = "CASH" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (paymentMethod == "CASH") OrangePrimary.copy(alpha = 0.08f) else Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = if (paymentMethod == "CASH") OrangePrimary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Cash / COD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (paymentMethod == "CASH") OrangePrimary else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Card option
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.5.dp,
                                color = if (paymentMethod == "CARD") OrangePrimary else Color.LightGray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { paymentMethod = "CARD" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (paymentMethod == "CARD") OrangePrimary.copy(alpha = 0.08f) else Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = if (paymentMethod == "CARD") OrangePrimary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Credit Card",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (paymentMethod == "CARD") OrangePrimary else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Wallet option
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.5.dp,
                                color = if (paymentMethod == "WALLET") OrangePrimary else Color.LightGray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { paymentMethod = "WALLET" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (paymentMethod == "WALLET") OrangePrimary.copy(alpha = 0.08f) else Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = if (paymentMethod == "WALLET") OrangePrimary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Wallet",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (paymentMethod == "WALLET") OrangePrimary else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Dynamic detail views based on selection
                when (paymentMethod) {
                    "CASH" -> {
                        Text(
                            "Pay PKR ${category.basePrice} in cash to the service provider upon completion of work. No online pre-payment required.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    "CARD" -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Secure 256-Bit SSL Checkout", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }

                            OutlinedTextField(
                                value = cardName,
                                onValueChange = { cardName = it },
                                label = { Text("Cardholder Name", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { if (it.length <= 16 && it.all { char -> char.isDigit() }) cardNumber = it },
                                label = { Text("Card Number", fontSize = 11.sp) },
                                placeholder = { Text("4111 2222 3333 4444") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = OrangePrimary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                                singleLine = true
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { if (it.length <= 5) cardExpiry = it },
                                    label = { Text("Expiry (MM/YY)", fontSize = 11.sp) },
                                    placeholder = { Text("12/28") },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) cardCvv = it },
                                    label = { Text("CVV", fontSize = 11.sp) },
                                    placeholder = { Text("123") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                                    singleLine = true
                                )
                            }
                        }
                    }
                    "WALLET" -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("Choose Wallet Provider", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavySecondary)

                            // 3 Wallet choices with custom radio button indicators
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Hazir Wallet
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (walletType == "HAZIR") OrangePrimary.copy(alpha = 0.08f) else Color.Transparent)
                                        .clickable { walletType = "HAZIR" }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isSelected = walletType == "HAZIR"
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, if (isSelected) OrangePrimary else Color.Gray, CircleShape)
                                            .padding(3.dp)
                                    ) {
                                        if (isSelected) {
                                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(OrangePrimary))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Hazir In-App Balance", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavySecondary)
                                        Text(
                                            "Current Balance: PKR $userBalance", 
                                            fontSize = 11.sp, 
                                            color = if (userBalance >= category.basePrice) Color(0xFF33691E) else Color.Red,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // EasyPaisa
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (walletType == "EASYPAISA") OrangePrimary.copy(alpha = 0.08f) else Color.Transparent)
                                        .clickable { walletType = "EASYPAISA" }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isSelected = walletType == "EASYPAISA"
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, if (isSelected) OrangePrimary else Color.Gray, CircleShape)
                                            .padding(3.dp)
                                    ) {
                                        if (isSelected) {
                                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(OrangePrimary))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("EasyPaisa Mobile Wallet", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavySecondary)
                                        Text("Direct secure handshake via EasyPaisa OTP", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }

                                // JazzCash
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (walletType == "JAZZCASH") OrangePrimary.copy(alpha = 0.08f) else Color.Transparent)
                                        .clickable { walletType = "JAZZCASH" }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isSelected = walletType == "JAZZCASH"
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, if (isSelected) OrangePrimary else Color.Gray, CircleShape)
                                            .padding(3.dp)
                                    ) {
                                        if (isSelected) {
                                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(OrangePrimary))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("JazzCash Mobile Wallet", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavySecondary)
                                        Text("Checkout safely via JazzCash SMS authentication", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }

                            if (walletType == "HAZIR") {
                                if (userBalance < category.basePrice) {
                                    Text(
                                        "⚠️ Insufficient Wallet Balance! Please top-up from your Wallet tab or select a different payment option.",
                                        fontSize = 11.sp,
                                        color = Color.Red,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                } else {
                                    Text(
                                        "✓ Funds secured! PKR ${category.basePrice} will be released to the worker upon job completion.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF33691E),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            } else {
                                // EasyPaisa/JazzCash SMS verification sub-flow
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    OutlinedTextField(
                                        value = walletPhone,
                                        onValueChange = { walletPhone = it },
                                        label = { Text("${if (walletType == "EASYPAISA") "EasyPaisa" else "JazzCash"} Phone Number", fontSize = 11.sp) },
                                        placeholder = { Text("03001234567") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                                        singleLine = true,
                                        enabled = !isOtpVerified
                                    )

                                    if (!isOtpRequested && !isOtpVerified) {
                                        Button(
                                            onClick = { isOtpRequested = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Link Wallet & Send OTP", fontSize = 12.sp)
                                        }
                                    } else if (isOtpRequested && !isOtpVerified) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("A 4-digit verification code has been sent via SMS to $walletPhone. Enter '1234' to verify.", fontSize = 11.sp, color = Color.Gray)
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = otpCode,
                                                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) otpCode = it },
                                                    label = { Text("OTP Code", fontSize = 10.sp) },
                                                    placeholder = { Text("1234") },
                                                    modifier = Modifier.weight(1.5f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                                                    singleLine = true
                                                )
                                                Button(
                                                    onClick = {
                                                        if (otpCode == "1234" || otpCode.length >= 4) {
                                                            isOtpVerified = true
                                                            isOtpRequested = false
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33691E)),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    enabled = otpCode.length >= 4
                                                ) {
                                                    Text("Verify", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    } else if (isOtpVerified) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text("Wallet verified securely!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        }
                                    }
                                }
                            }
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
