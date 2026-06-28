package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.domain.model.Booking
import com.example.presentation.theme.NavySecondary
import com.example.presentation.theme.OrangePrimary
import com.example.presentation.viewmodel.HazirViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerConsoleScreen(
    viewModel: HazirViewModel,
    onTrackBooking: (Int) -> Unit
) {
    val context = LocalContext.current
    val workerProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val bookings by viewModel.workerBookings.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    var showRoleMenu by remember { mutableStateOf(false) }

    // Service Proof Verification State
    var bookingToUploadProofId by remember { mutableStateOf<Int?>(null) }
    var selectedBeforePhoto by remember { mutableStateOf<String?>(null) }
    var selectedAfterPhoto by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Worker Console",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NavySecondary
                        )
                        Text(
                            text = workerProfile?.skills ?: "Certified Professional",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    // Persona Switcher
                    Box {
                        TextButton(
                            onClick = { showRoleMenu = true },
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(NavySecondary.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = NavySecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sajid (Worker)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavySecondary)
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Customer Dashboard") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Worker online offline state panel
            val isOnline = workerProfile?.isOnline ?: false
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFECEFF1)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color.Green else Color.Gray)
                        )
                        Column {
                            Text(
                                text = if (isOnline) "You are ONLINE" else "You are OFFLINE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = NavySecondary
                              )
                            Text(
                                text = if (isOnline) "Receiving job requests in Islamabad..." else "Toggle switch to receive customer bookings.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Switch(
                        checked = isOnline,
                        onCheckedChange = { viewModel.toggleWorkerOnlineStatus(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2E7D32)
                        )
                    )
                }
            }

            // 2. Earnings metrics cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Wallet balance
                Card(
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySecondary)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Wallet Balance", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("PKR ${String.format("%.2f", workerProfile?.walletBalance ?: 0.0)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                Toast.makeText(context, "Payout of PKR ${workerProfile?.walletBalance} initiated to EasyPaisa!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Withdraw", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Completed Jobs metric
                Card(
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Completed Jobs", color = Color.Gray, fontSize = 11.sp)
                        Text("${workerProfile?.completedJobs ?: 0}", color = NavySecondary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("Rating: ★ 4.8", color = OrangePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. Weekly earnings performance histogram custom canvas
            Text("Weekly Earnings Performance", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavySecondary)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PKR Earnings by Day (Islamabad)", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val daysData = listOf(1500f, 2500f, 0f, 3200f, 1800f) // PKR values for Mon, Tue, Wed, Thu, Fri
                        val maxPkr = 4000f

                        val barWidth = 42f
                        val gap = (canvasWidth - (daysData.size * barWidth)) / (daysData.size + 1)

                        // Base boundary axis line
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, canvasHeight),
                            end = Offset(canvasWidth, canvasHeight),
                            strokeWidth = 3f
                        )

                        // Draw bars
                        daysData.forEachIndexed { idx, pkr ->
                            val fraction = pkr / maxPkr
                            val barHeight = canvasHeight * fraction
                            val x = gap + idx * (barWidth + gap)
                            val y = canvasHeight - barHeight

                            // Gradient brush
                            val brush = Brush.verticalGradient(
                                colors = listOf(OrangePrimary, Color(0xFFFFB300))
                            )

                            drawRect(
                                brush = brush,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight)
                            )
                        }
                    }

                    // Bottom character row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri").forEach { day ->
                            Text(
                                text = day,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.width(42.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 4. Job Assignments Queue
            Text("Assigned Active Jobs", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavySecondary)

            val activeAssignedJobs = bookings.filter { it.status != "COMPLETED" && it.status != "CANCELLED" }
            if (activeAssignedJobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isOnline) "No active job bookings yet.\nKeep the app open!" else "Go ONLINE to receive booking requests.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    activeAssignedJobs.forEach { job ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(job.categoryName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavySecondary)
                                        Text("Customer: ${job.customerName}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    StatusBadge(job.status)
                                }

                                Text(job.description, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = NavySecondary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("Address: ${job.address}", fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                                        Text("Earnings: PKR ${job.estimatedPrice}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = OrangePrimary)
                                    }

                                    // Action buttons depending on state
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Open Active Track/Chat
                                        IconButton(
                                            onClick = { onTrackBooking(job.id) },
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(NavySecondary.copy(alpha = 0.1f))
                                                .size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "Chat", tint = NavySecondary, modifier = Modifier.size(18.dp))
                                        }

                                        when (job.status) {
                                            "ACCEPTED", "ARRIVED" -> {
                                                Button(
                                                    onClick = { viewModel.startJobWorker(job.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(36.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Start Work Task", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            "STARTED" -> {
                                                Button(
                                                    onClick = {
                                                        selectedBeforePhoto = null
                                                        selectedAfterPhoto = null
                                                        bookingToUploadProofId = job.id
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(36.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Complete Task", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
    }

    if (bookingToUploadProofId != null) {
        val bookingId = bookingToUploadProofId!!
        val activeJob = bookings.find { it.id == bookingId }
        
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { bookingToUploadProofId = null }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Service Proof Required",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = NavySecondary
                        )
                    }

                    Text(
                        text = "Upload 'Before' and 'After' photos of the job site to verify completed work quality for the customer.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )

                    // Before photo section
                    Text(
                        text = "Step 1: Before Work Photo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NavySecondary
                    )

                    if (selectedBeforePhoto == null) {
                        // Options for Before Photos
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val beforeOptions = listOf(
                                "Plumbing Leak" to "https://images.unsplash.com/photo-1581094288338-2314dddb7eed?q=80&w=300",
                                "Electrical Burn" to "https://images.unsplash.com/photo-1504307651254-35680f356dfd?q=80&w=300",
                                "AC Dust" to "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?q=80&w=300",
                                "Damaged Socket" to "https://images.unsplash.com/photo-1558211583-d26f610c1eb1?q=80&w=300"
                            )
                            beforeOptions.forEach { (name, url) ->
                                Card(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clickable { selectedBeforePhoto = url },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.5f))
                                                .align(Alignment.BottomCenter)
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = name,
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Show selected Before Photo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9))
                                .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.size(50.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    AsyncImage(
                                        model = selectedBeforePhoto,
                                        contentDescription = "Selected Before",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text("Before Photo Attached", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { selectedBeforePhoto = null }) {
                                Text("Change", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // After photo section
                    Text(
                        text = "Step 2: After Work Photo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NavySecondary
                    )

                    if (selectedAfterPhoto == null) {
                        // Options for After Photos
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val afterOptions = listOf(
                                "Plumbing Sealed" to "https://images.unsplash.com/photo-1605647540924-852290f6b0d5?q=80&w=300",
                                "Electrical Fitted" to "https://images.unsplash.com/photo-1544725176-7c40e5a71c5e?q=80&w=300",
                                "AC Sparkling Clean" to "https://images.unsplash.com/photo-1517646287270-a5a9ca602e5c?q=80&w=300",
                                "New Modern Socket" to "https://images.unsplash.com/photo-1540555700478-4be289fbecef?q=80&w=300"
                            )
                            afterOptions.forEach { (name, url) ->
                                Card(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clickable { selectedAfterPhoto = url },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.5f))
                                                .align(Alignment.BottomCenter)
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = name,
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Show selected After Photo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9))
                                .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.size(50.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    AsyncImage(
                                        model = selectedAfterPhoto,
                                        contentDescription = "Selected After",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text("After Photo Attached", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { selectedAfterPhoto = null }) {
                                Text("Change", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { bookingToUploadProofId = null }
                        ) {
                            Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            enabled = selectedBeforePhoto != null && selectedAfterPhoto != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            onClick = {
                                if (selectedBeforePhoto != null && selectedAfterPhoto != null) {
                                    viewModel.completeJobWorkerWithProof(
                                        bookingId = bookingId,
                                        beforePhoto = selectedBeforePhoto!!,
                                        afterPhoto = selectedAfterPhoto!!
                                    )
                                    bookingToUploadProofId = null
                                    Toast.makeText(context, "Service complete! Proof submitted.", Toast.LENGTH_LONG).show()
                                }
                            }
                        ) {
                            Text("Submit & Complete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
