package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.theme.NavySecondary
import com.example.presentation.theme.OrangePrimary
import com.example.presentation.viewmodel.HazirViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: HazirViewModel
) {
    val context = LocalContext.current
    val bookings by viewModel.allBookingsAdmin.collectAsStateWithLifecycle()
    val workers by viewModel.allWorkers.collectAsStateWithLifecycle()
    val pendingVerifications by viewModel.pendingVerificationWorkers.collectAsStateWithLifecycle()

    var showRoleMenu by remember { mutableStateOf(false) }

    // Aggregate Analytics Metrics
    val totalBookings = bookings.size
    val grossRevenue = bookings.sumOf { it.estimatedPrice }
    val commissionRevenue = bookings.filter { it.status == "COMPLETED" }.sumOf { it.estimatedPrice * 0.10 }
    val activeWorkersCount = workers.count { it.isOnline }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Admin Panel",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NavySecondary
                        )
                        Text(
                            text = "System Operations & Control",
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
                                .background(Color.Red.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ayesha (Admin)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
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
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Analytics Cards Section
            item {
                Text("Platform Analytics", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavySecondary)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsCard(
                        title = "Gross Volume",
                        value = "PKR ${String.format("%.0f", grossRevenue)}",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsCard(
                        title = "10% Commission",
                        value = "PKR ${String.format("%.2f", commissionRevenue)}",
                        icon = Icons.Default.Savings,
                        color = OrangePrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsCard(
                        title = "Total Bookings",
                        value = "$totalBookings",
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        color = NavySecondary,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsCard(
                        title = "Online Workers",
                        value = "$activeWorkersCount",
                        icon = Icons.Default.Wifi,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Worker verification queue
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Worker Verification Queue (CNIC & Selfie)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavySecondary)
            }

            if (pendingVerifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No pending worker verifications", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                items(pendingVerifications) { worker ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("pending_verification_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(NavySecondary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(worker.name.take(1), fontWeight = FontWeight.Bold, color = NavySecondary)
                                    }
                                    Column {
                                        Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavySecondary)
                                        Text("Skills: ${worker.skills}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFFF3E0))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Unverified", fontSize = 10.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider()

                            // Display CNIC number
                            Column {
                                Text("CNIC Number Claim:", fontSize = 11.sp, color = Color.Gray)
                                Text(worker.cnicNumber.ifEmpty { "33102-4444444-5" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavySecondary)
                            }

                            // Review Approval controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.approveWorkerVerification(worker.id, false)
                                        Toast.makeText(context, "Documents Rejected for ${worker.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reject", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.approveWorkerVerification(worker.id, true)
                                        Toast.makeText(context, "Worker Approved Successfully!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.weight(1.2f).testTag("approve_verification_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Approve Documents", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 3. System-wide Bookings Log
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Islamabad System Bookings Log", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavySecondary)
            }

            if (bookings.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No bookings in system yet", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                items(bookings) { booking ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(booking.categoryName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavySecondary)
                            Text("Customer: ${booking.customerName} • Worker: ${booking.workerName ?: "None"}", fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Est: PKR ${booking.estimatedPrice}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                        }
                        StatusBadge(booking.status)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(title, fontSize = 11.sp, color = Color.Gray)
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = NavySecondary)
            }
        }
    }
}
