package com.example.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.theme.OrangePrimary

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "PENDING" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "ACCEPTED" -> Color(0xFFE3F2FD) to Color(0xFF0D47A1)
        "ARRIVED" -> Color(0xFFEDE7F6) to Color(0xFF4A148C)
        "STARTED" -> Color(0xFFE0F2F1) to Color(0xFF004D40)
        "COMPLETED" -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        "CANCELLED" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun SimulatedLiveMap(
    workerLat: Double?,
    workerLng: Double?,
    customerLat: Double = 33.6844,
    customerLng: Double = 73.0479,
    status: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Drag tracking for interactive panning of the canvas
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEAEFF5))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Coordinates mapping logic center of canvas as customer position
            val center = Offset(width / 2f + offsetX, height / 2f + offsetY)

            // 1. Draw stylized streets (grid-based and radial roads to represent Islamabad)
            val streetColor = Color(0xFFFFFFFF)
            val streetWidth = 14f
            val highwayWidth = 22f

            // F-Sector grid rows
            for (i in -4..4) {
                val y = center.y + i * 150f
                drawLine(
                    color = streetColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = streetWidth
                )
            }
            // Columns
            for (i in -4..4) {
                val x = center.x + i * 180f
                drawLine(
                    color = streetColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = streetWidth
                )
            }

            // Diagonal Expressways
            drawLine(
                color = Color(0xFFD0DCE5),
                start = Offset(center.x - 800f, center.y - 600f),
                end = Offset(center.x + 800f, center.y + 600f),
                strokeWidth = highwayWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFD0DCE5),
                start = Offset(center.x - 800f, center.y + 600f),
                end = Offset(center.x + 800f, center.y - 600f),
                strokeWidth = highwayWidth,
                cap = StrokeCap.Round
            )

            // Draw a major blue river canal (Rawal Lake stream representation)
            drawLine(
                color = Color(0xFFAFD6F5),
                start = Offset(center.x - 1000f, center.y - 350f),
                end = Offset(center.x + 1000f, center.y - 250f),
                strokeWidth = 35f,
                cap = StrokeCap.Round
            )

            // 2. Customer Pin location (Center)
            val customerPinOffset = center

            // Pulse effect for customer
            drawCircle(
                color = Color(0x33FA7D09),
                radius = 35f + pulseScale,
                center = customerPinOffset
            )

            // Draw target circle
            drawCircle(
                color = OrangePrimary,
                radius = 12f,
                center = customerPinOffset
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = customerPinOffset
            )

            // 3. Worker Pin location
            if (workerLat != null && workerLng != null) {
                // Map GPS coordinates relative to center (scaled Islamabad coordinates)
                // Customer is at (33.6844, 73.0479)
                val latScale = 8000f // Scaling factor for mapping
                val lngScale = 8000f

                val dy = (customerLat - workerLat) * latScale
                val dx = (workerLng - customerLng) * lngScale

                val workerPinOffset = Offset(center.x + dx.toFloat(), center.y + dy.toFloat())

                // Draw Transit Path
                if (status == "ACCEPTED" || status == "ARRIVED") {
                    drawLine(
                        color = OrangePrimary,
                        start = workerPinOffset,
                        end = customerPinOffset,
                        strokeWidth = 6f,
                        cap = StrokeCap.Round,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(15f, 15f), 0f
                        )
                    )
                }

                // Pulse effect for worker (if active)
                if (status == "ACCEPTED") {
                    drawCircle(
                        color = Color(0x331F4068),
                        radius = 15f + pulseScale,
                        center = workerPinOffset
                    )
                }

                // Draw worker bike marker background
                drawCircle(
                    color = Color(0xFF1F4068),
                    radius = 20f,
                    center = workerPinOffset
                )

                // White ring
                drawCircle(
                    color = Color.White,
                    radius = 16f,
                    center = workerPinOffset,
                    style = Stroke(width = 3f)
                )

                // Bike/Mechanic center core
                drawCircle(
                    color = Color(0xFFFF9F1C),
                    radius = 8f,
                    center = workerPinOffset
                )
            }
        }

        // Overlay text displaying street signs
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = when (status) {
                    "PENDING" -> "Searching for nearby workers in Islamabad..."
                    "ACCEPTED" -> "Hazir Worker is en route to F-7 Markaz..."
                    "ARRIVED" -> "Worker has arrived at your doorstep!"
                    "STARTED" -> "Task work is currently in progress."
                    "COMPLETED" -> "Service complete. Please submit payment."
                    else -> "Islamabad, Pakistan"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F4068)
            )
        }

        // UI Recenter Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            IconButton(
                onClick = {
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter",
                    tint = Color(0xFF1F4068)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingAndReviewDialog(
    estimatedPrice: Double,
    onConfirm: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var selectedTipPct by remember { mutableStateOf(0) } // preset percentages: 0, 10, 15, 20

    val tipAmount = estimatedPrice * (selectedTipPct / 100.0)
    val totalToPay = estimatedPrice + tipAmount

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = { onConfirm(rating, reviewText) },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Complete Payment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel", color = Color.Gray)
            }
        },
        title = {
            Text(
                text = "Pay Worker & Rate Service",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Payment invoice summary with tip support
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Amount to Pay", fontSize = 12.sp, color = Color.Gray)
                        Text("PKR ${String.format("%.2f", totalToPay)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        if (selectedTipPct > 0) {
                            Text(
                                "Base Price: PKR ${String.format("%.2f", estimatedPrice)} + Tip: PKR ${String.format("%.2f", tipAmount)} (${selectedTipPct}%)",
                                fontSize = 10.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text("Deducted instantly from your Hazir Wallet", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                // Gratuity/Tip Selection Component
                Text(
                    text = "Add a Tip for the Handyman:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(0, 10, 15, 20).forEach { pct ->
                        val isSelected = selectedTipPct == pct
                        val borderCol = if (isSelected) OrangePrimary else Color.LightGray
                        val bgCol = if (isSelected) Color(0xFFFFF3E0) else Color.Transparent
                        val textCol = if (isSelected) OrangePrimary else Color.DarkGray

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgCol)
                                .border(1.5.dp, borderCol, RoundedCornerShape(8.dp))
                                .clickable { selectedTipPct = pct }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (pct == 0) "No Tip" else "$pct%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textCol
                                )
                                if (pct > 0) {
                                    val currentTip = estimatedPrice * (pct / 100.0)
                                    Text(
                                        text = "+${currentTip.toInt()}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) OrangePrimary else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text("How was your service experience?", fontWeight = FontWeight.Medium, fontSize = 14.sp)

                // Five Star selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i Stars",
                                tint = if (i <= rating) Color(0xFFFFB300) else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Review input
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Write a quick feedback review (optional)...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        cursorColor = OrangePrimary
                    )
                )
            }
        }
    )
}
