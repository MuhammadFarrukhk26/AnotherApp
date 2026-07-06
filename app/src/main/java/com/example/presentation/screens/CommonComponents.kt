package com.example.presentation.screens

import android.util.Log
import androidx.compose.animation.core.*
import com.example.infrastructure.api.StripeClient
import com.example.infrastructure.api.StripeTokenResult
import com.example.infrastructure.api.StripePaymentResult
import kotlinx.coroutines.launch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
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
    var zoomScale by remember { mutableStateOf(1.0f) }

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
            .testTag("simulated_live_map_box")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Coordinates mapping logic center of canvas as customer position
            val center = Offset(width / 2f + offsetX, height / 2f + offsetY)

            // 1. Draw stylized streets (grid-based and radial roads to represent Islamabad)
            val streetColor = Color(0xFFFFFFFF)
            val streetWidth = 14f * zoomScale
            val highwayWidth = 22f * zoomScale

            // F-Sector grid rows
            for (i in -4..4) {
                val y = center.y + i * 150f * zoomScale
                drawLine(
                    color = streetColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = streetWidth
                )
            }
            // Columns
            for (i in -4..4) {
                val x = center.x + i * 180f * zoomScale
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
                start = Offset(center.x - 800f * zoomScale, center.y - 600f * zoomScale),
                end = Offset(center.x + 800f * zoomScale, center.y + 600f * zoomScale),
                strokeWidth = highwayWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFD0DCE5),
                start = Offset(center.x - 800f * zoomScale, center.y + 600f * zoomScale),
                end = Offset(center.x + 800f * zoomScale, center.y - 600f * zoomScale),
                strokeWidth = highwayWidth,
                cap = StrokeCap.Round
            )

            // Draw a major blue river canal (Rawal Lake stream representation)
            drawLine(
                color = Color(0xFFAFD6F5),
                start = Offset(center.x - 1000f * zoomScale, center.y - 350f * zoomScale),
                end = Offset(center.x + 1000f * zoomScale, center.y - 250f * zoomScale),
                strokeWidth = 35f * zoomScale,
                cap = StrokeCap.Round
            )

            // Draw real landmarks using native Canvas Text rendering for Islamabad details
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 24f * zoomScale
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
                alpha = 140
            }

            drawContext.canvas.nativeCanvas.drawText(
                "Faisal Mosque",
                center.x - 300f * zoomScale,
                center.y - 200f * zoomScale,
                paint
            )

            drawContext.canvas.nativeCanvas.drawText(
                "F-7 Markaz",
                center.x,
                center.y - 120f * zoomScale,
                paint
            )

            drawContext.canvas.nativeCanvas.drawText(
                "Rawal Lake Reserve",
                center.x + 400f * zoomScale,
                center.y + 350f * zoomScale,
                paint
            )

            // 2. Customer Pin location (Center)
            val customerPinOffset = center

            // Pulse effect for customer
            drawCircle(
                color = Color(0x33FA7D09),
                radius = (35f + pulseScale) * zoomScale,
                center = customerPinOffset
            )

            // Draw target circle
            drawCircle(
                color = OrangePrimary,
                radius = 12f * zoomScale,
                center = customerPinOffset
            )
            drawCircle(
                color = Color.White,
                radius = 5f * zoomScale,
                center = customerPinOffset
            )

            // 3. Worker Pin location
            if (workerLat != null && workerLng != null) {
                // Map GPS coordinates relative to center (scaled Islamabad coordinates)
                // Customer is at (33.6844, 73.0479)
                val latScale = 8000f // Scaling factor for mapping
                val lngScale = 8000f

                val dy = (customerLat - workerLat) * latScale * zoomScale
                val dx = (workerLng - customerLng) * lngScale * zoomScale

                val workerPinOffset = Offset(center.x + dx.toFloat(), center.y + dy.toFloat())

                // Draw Transit Path
                if (status == "ACCEPTED" || status == "ARRIVED") {
                    drawLine(
                        color = OrangePrimary,
                        start = workerPinOffset,
                        end = customerPinOffset,
                        strokeWidth = 6f * zoomScale,
                        cap = StrokeCap.Round,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(15f * zoomScale, 15f * zoomScale), 0f
                        )
                    )
                }

                // Pulse effect for worker (if active)
                if (status == "ACCEPTED") {
                    drawCircle(
                        color = Color(0x331F4068),
                        radius = (15f + pulseScale) * zoomScale,
                        center = workerPinOffset
                    )
                }

                // Draw worker bike marker background
                drawCircle(
                    color = Color(0xFF1F4068),
                    radius = 20f * zoomScale,
                    center = workerPinOffset
                )

                // White ring
                drawCircle(
                    color = Color.White,
                    radius = 16f * zoomScale,
                    center = workerPinOffset,
                    style = Stroke(width = 3f * zoomScale)
                )

                // Bike/Mechanic center core
                drawCircle(
                    color = Color(0xFFFF9F1C),
                    radius = 8f * zoomScale,
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

        // UI Recenter and Zoom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom In button
            IconButton(
                onClick = {
                    if (zoomScale < 3.0f) zoomScale += 0.25f
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .size(40.dp)
                    .testTag("map_zoom_in_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = Color(0xFF1F4068)
                )
            }

            // Zoom Out button
            IconButton(
                onClick = {
                    if (zoomScale > 0.5f) zoomScale -= 0.25f
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .size(40.dp)
                    .testTag("map_zoom_out_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = Color(0xFF1F4068)
                )
            }

            // Recenter button
            IconButton(
                onClick = {
                    offsetX = 0f
                    offsetY = 0f
                    zoomScale = 1.0f
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .size(40.dp)
                    .testTag("map_recenter_button")
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
    initialPaymentMethod: String = "CASH",
    userBalance: Double = 5000.0,
    onConfirm: (Int, String, Double, String) -> Unit, // rating, review, tip, paymentMethod
    onDismiss: () -> Unit,
    onTopUp: (Double) -> Unit = {}
) {
    var rating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var selectedTipPct by remember { mutableStateOf(0) } // preset percentages: 0, 10, 15, 20
    var currentStep by remember { mutableStateOf(1) } // 1 = Review & Tip, 2 = Payment method selection, 3 = Gateway Authentication

    val tipAmount = estimatedPrice * (selectedTipPct / 100.0)
    val totalToPay = estimatedPrice + tipAmount

    // Payment Selection
    var selectedPaymentMethod by remember { mutableStateOf(initialPaymentMethod) }
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    // Wallet details
    var walletPhone by remember { mutableStateOf("03001234567") }
    var walletPin by remember { mutableStateOf("") }

    // Gateway Simulation States
    var paymentProcessingPhase by remember { mutableStateOf(0) } // 0 = Idle, 1 = Secure Session, 2 = SMS OTP sent, 3 = Authenticated
    var otpCode by remember { mutableStateOf("") }
    var simulatedOtp = "582104"
    var showOtpToast by remember { mutableStateOf(false) }

    // Instant top-up sub-flow
    var showTopUpCardForm by remember { mutableStateOf(false) }
    var topUpAmount by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var stripeIsProcessing by remember { mutableStateOf(false) }
    var stripeProcessingError by remember { mutableStateOf<String?>(null) }


    AlertDialog(
        onDismissRequest = { if (paymentProcessingPhase == 0) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = paymentProcessingPhase == 0,
            dismissOnClickOutside = paymentProcessingPhase == 0
        ),
        confirmButton = {
            if (currentStep == 1) {
                Button(
                    onClick = { currentStep = 2 },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Proceed to Pay", fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp).size(16.dp)
                    )
                }
            } else if (currentStep == 2 && !showTopUpCardForm) {
                val canSubmit = when (selectedPaymentMethod) {
                    "CASH" -> true
                    "CARD" -> cardNumber.replace(" ", "").length >= 15 && cardExpiry.length >= 4 && cardCvv.length >= 3 && cardName.isNotBlank()
                    "EASYPAISA", "JAZZCASH" -> walletPhone.length >= 10
                    "WALLET" -> userBalance >= totalToPay
                    else -> false
                }

                Button(
                    onClick = {
                        if (selectedPaymentMethod == "CASH") {
                            onConfirm(rating, reviewText, tipAmount, "CASH")
                        } else if (selectedPaymentMethod == "CARD") {
                            currentStep = 3
                            paymentProcessingPhase = 1
                            stripeIsProcessing = true
                            stripeProcessingError = null
                            
                            val expiryParts = cardExpiry.split("/")
                            val expMonth = expiryParts.getOrNull(0)?.trim() ?: "12"
                            val expYear = expiryParts.getOrNull(1)?.trim() ?: "28"
                            
                            coroutineScope.launch {
                                Log.d("StripePayment", "Tokenizing card expMonth: $expMonth, expYear: $expYear")
                                val tokenResult = StripeClient.createCardToken(
                                    cardNumber = cardNumber,
                                    expMonth = expMonth,
                                    expYear = expYear,
                                    cvc = cardCvv,
                                    name = cardName
                                )
                                
                                when (tokenResult) {
                                    is StripeTokenResult.Success -> {
                                        Log.d("StripePayment", "Stripe Token generated: ${tokenResult.tokenId}")
                                        if (StripeClient.isSandboxMode()) {
                                            // Simulated 3D Secure
                                            paymentProcessingPhase = 2
                                            showOtpToast = true
                                            stripeIsProcessing = false
                                        } else {
                                            // Real API Mode: process payment directly!
                                            Log.d("StripePayment", "Stripe in Real mode. Charging...")
                                            val payResult = StripeClient.processPayment(
                                                amountPkr = totalToPay,
                                                token = tokenResult.tokenId,
                                                description = "Hazir Service Payment - Booking"
                                            )
                                            when (payResult) {
                                                is StripePaymentResult.Success -> {
                                                    Log.d("StripePayment", "Stripe Payment Success: ${payResult.paymentIntentId}")
                                                    paymentProcessingPhase = 3
                                                    stripeIsProcessing = false
                                                }
                                                is StripePaymentResult.Failure -> {
                                                    Log.e("StripePayment", "Stripe Payment Failed: ${payResult.errorMessage}")
                                                    stripeProcessingError = payResult.errorMessage
                                                    stripeIsProcessing = false
                                                }
                                            }
                                        }
                                    }
                                    is StripeTokenResult.Failure -> {
                                        Log.e("StripePayment", "Stripe Tokenization Failed: ${tokenResult.errorMessage}")
                                        stripeProcessingError = tokenResult.errorMessage
                                        stripeIsProcessing = false
                                    }
                                }
                            }
                        } else {
                            currentStep = 3
                            paymentProcessingPhase = 1
                            // Simulate 3D Secure SMS generation after 1.5 seconds
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                paymentProcessingPhase = 2
                                showOtpToast = true
                            }, 1800)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    enabled = canSubmit && !stripeIsProcessing
                ) {
                    Text(
                        text = if (selectedPaymentMethod == "CASH") "Confirm Direct Cash" else if (stripeIsProcessing) "Processing..." else "Pay PKR ${String.format("%.0f", totalToPay)}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            if (paymentProcessingPhase == 0) {
                TextButton(
                    onClick = {
                        if (currentStep > 1) {
                            if (showTopUpCardForm) {
                                showTopUpCardForm = false
                            } else {
                                currentStep -= 1
                            }
                        } else {
                            onDismiss()
                        }
                    }
                ) {
                    Text(if (currentStep == 1) "Cancel" else "Back", color = Color.Gray)
                }
            }
        },
        title = {
            Text(
                text = when (currentStep) {
                    1 -> "Rate Service & Review"
                    2 -> if (showTopUpCardForm) "Wallet Quick Top-up" else "Secure Payment Checkout"
                    else -> "Secure Gateway Checkout"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A)
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // STEP 1: REVIEW, TIP & TOTAL INVOICE
                    if (currentStep == 1) {
                        // Invoice total banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3E0))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Invoice Sum", fontSize = 11.sp, color = Color.Gray)
                                Text("PKR ${String.format("%.2f", totalToPay)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                                if (selectedTipPct > 0) {
                                    Text(
                                        "Base Price: PKR ${String.format("%.2f", estimatedPrice)} + Tip: PKR ${String.format("%.2f", tipAmount)} (${selectedTipPct}%)",
                                        fontSize = 10.sp,
                                        color = Color(0xFFE65100),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Tip selector
                        Text(
                            text = "Add a Tip for the Technician:",
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
                                            val tip = estimatedPrice * (pct / 100.0)
                                            Text(
                                                text = "+${tip.toInt()}",
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
                        Text("How was your experience?", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))

                        // Star Rating Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
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

                        // Review Text
                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            placeholder = { Text("How can we improve? (Optional)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                            maxLines = 3
                        )
                    }

                    // STEP 2: PAYMENT METHOD & GATEWAY INPUT
                    else if (currentStep == 2) {
                        if (showTopUpCardForm) {
                            // Quick wallet top-up using card
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Current Balance: PKR $userBalance", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                Text("Missing Amount: PKR ${String.format("%.0f", totalToPay - userBalance)}", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)

                                OutlinedTextField(
                                    value = topUpAmount,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) topUpAmount = it },
                                    label = { Text("Top-up Amount (PKR)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = cardName,
                                    onValueChange = { cardName = it },
                                    label = { Text("Cardholder Name", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { if (it.length <= 16 && it.all { char -> char.isDigit() }) cardNumber = it },
                                    label = { Text("Card Number", fontSize = 11.sp) },
                                    placeholder = { Text("4111 2222 3333 4444") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                                    singleLine = true
                                )

                                Button(
                                    onClick = {
                                        val amt = topUpAmount.toDoubleOrNull() ?: (totalToPay - userBalance)
                                        if (amt > 0) {
                                            onTopUp(amt)
                                            showTopUpCardForm = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    enabled = cardNumber.length >= 16 && cardName.isNotBlank()
                                ) {
                                    Text("Top-up Wallet Instantly", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Interactive checkout summary
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount Due:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text("PKR ${String.format("%.2f", totalToPay)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            }

                            // Horizontal payment selections
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    Triple("CARD", "Stripe Credit/Debit Card", Icons.Default.CreditCard),
                                    Triple("WALLET", "Hazir Wallet (Bal: PKR $userBalance)", Icons.Default.AccountBalanceWallet),
                                    Triple("EASYPAISA", "EasyPaisa Wallet", Icons.Default.AccountBalance),
                                    Triple("JAZZCASH", "JazzCash Wallet", Icons.Default.AccountBalance),
                                    Triple("CASH", "Cash / COD Payment", Icons.Default.Payments)
                                ).forEach { (method, label, icon) ->
                                    val isSelected = selectedPaymentMethod == method
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.5.dp, if (isSelected) OrangePrimary else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .background(if (isSelected) OrangePrimary.copy(alpha = 0.05f) else Color.White)
                                            .clickable { selectedPaymentMethod = method }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) OrangePrimary else Color.Gray, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) OrangePrimary else Color.DarkGray)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, if (isSelected) OrangePrimary else Color.Gray, CircleShape)
                                                .padding(2.dp)
                                        ) {
                                            if (isSelected) Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(OrangePrimary))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Dynamic sub-form fields
                            when (selectedPaymentMethod) {
                                "CARD" -> {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
                                            Text("Stripe Secure Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                                            Spacer(modifier = Modifier.weight(1f))
                                            if (StripeClient.isSandboxMode()) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFFFFBEB))
                                                        .border(0.5.dp, Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Sandbox Mode", fontSize = 8.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFECFDF5))
                                                        .border(0.5.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Live Mode", fontSize = 8.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = cardName,
                                            onValueChange = { cardName = it },
                                            label = { Text("Cardholder Name", fontSize = 10.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary)
                                        )

                                        val isCardValid = cardNumber.isEmpty() || StripeClient.validateCardNumber(cardNumber)
                                        OutlinedTextField(
                                            value = cardNumber,
                                            onValueChange = { if (it.length <= 16 && it.all { char -> char.isDigit() }) cardNumber = it },
                                            label = { Text("Card Number", fontSize = 10.sp) },
                                            placeholder = { Text("4111 2222 3333 4444") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            isError = !isCardValid,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary)
                                        )
                                        if (!isCardValid) {
                                            Text("Invalid card format (Luhn check failed)", color = Color.Red, fontSize = 9.sp)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = cardExpiry,
                                                onValueChange = { if (it.length <= 5) cardExpiry = it },
                                                label = { Text("Expiry (MM/YY)", fontSize = 10.sp) },
                                                placeholder = { Text("12/28") },
                                                modifier = Modifier.weight(1.5f),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary)
                                            )
                                            OutlinedTextField(
                                                value = cardCvv,
                                                onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) cardCvv = it },
                                                label = { Text("CVV", fontSize = 10.sp) },
                                                placeholder = { Text("123") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary)
                                            )
                                        }
                                    }
                                }
                                "EASYPAISA", "JAZZCASH" -> {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text("Direct handoff to ${selectedPaymentMethod.lowercase().capitalize()} API", fontSize = 11.sp, color = Color.Gray)
                                        OutlinedTextField(
                                            value = walletPhone,
                                            onValueChange = { walletPhone = it },
                                            label = { Text("Mobile Wallet Account Phone", fontSize = 10.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary)
                                        )
                                        OutlinedTextField(
                                            value = walletPin,
                                            onValueChange = { walletPin = it },
                                            label = { Text("Mobile PIN (Optional checkout bypass)", fontSize = 10.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary)
                                        )
                                    }
                                }
                                "WALLET" -> {
                                    if (userBalance < totalToPay) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFFFF3F3), RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                Text("Insufficient Wallet Balance!", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                            }
                                            Text("You have PKR $userBalance but you need PKR ${String.format("%.0f", totalToPay)}.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                            Button(
                                                onClick = {
                                                    topUpAmount = (totalToPay - userBalance).toInt().toString()
                                                    showTopUpCardForm = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                modifier = Modifier.fillMaxWidth().height(38.dp)
                                            ) {
                                                Text("Top-up Instantly via Card", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Text(
                                            "Payment of PKR ${String.format("%.2f", totalToPay)} will be securely debited from your Hazir Balance instantly.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                                "CASH" -> {
                                    Text(
                                        "Please hand over physical Cash / COD of PKR ${String.format("%.0f", totalToPay)} to the service provider directly. Rating & reviews will be logged instantly.",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // STEP 3: SECURE 3D SECURE GATEWAY OTP PROCESSING
                    else if (currentStep == 3) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (stripeProcessingError != null) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(44.dp))
                                Text("Payment Failed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                Text(
                                    text = stripeProcessingError ?: "An unknown error occurred.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Button(
                                    onClick = {
                                        stripeProcessingError = null
                                        currentStep = 2
                                        paymentProcessingPhase = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                ) {
                                    Text("Go Back & Retry", fontWeight = FontWeight.Bold)
                                }
                            } else if (paymentProcessingPhase == 1) {
                                CircularProgressIndicator(color = OrangePrimary, modifier = Modifier.size(44.dp))
                                Text(
                                    text = if (selectedPaymentMethod == "CARD") "Contacting Stripe Server..." else "Contacting payment server...",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (selectedPaymentMethod == "CARD") "Executing secure PCI-compliant card token handshake" else "Initializing secure 256-bit handshake with bank partner",
                                    fontSize = 10.sp,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center
                                )
                            } else if (paymentProcessingPhase == 2) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(44.dp))
                                Text(
                                    text = if (selectedPaymentMethod == "CARD") "Stripe 3D-Secure Verification" else "Two-Factor 3D Secure verification",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = "We have simulated a secure Stripe message token for this payment. Please enter simulated code: '$simulatedOtp'",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                                    label = { Text("Secure OTP Code", fontSize = 11.sp) },
                                    placeholder = { Text("582104") },
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary)
                                )

                                Button(
                                    onClick = {
                                        if (otpCode == simulatedOtp) {
                                            paymentProcessingPhase = 1 // Show loader while completing the sandbox payment
                                            stripeIsProcessing = true
                                            coroutineScope.launch {
                                                Log.d("StripePayment", "Processing Sandbox charge of PKR $totalToPay...")
                                                val payResult = StripeClient.processPayment(
                                                    amountPkr = totalToPay,
                                                    token = "tok_sandbox_${System.currentTimeMillis()}",
                                                    description = "Hazir Service Payment - Booking"
                                                )
                                                when (payResult) {
                                                    is StripePaymentResult.Success -> {
                                                        Log.d("StripePayment", "Sandbox Payment succeeded!")
                                                        paymentProcessingPhase = 3
                                                        stripeIsProcessing = false
                                                    }
                                                    is StripePaymentResult.Failure -> {
                                                        Log.e("StripePayment", "Sandbox Payment failed: ${payResult.errorMessage}")
                                                        stripeProcessingError = payResult.errorMessage
                                                        stripeIsProcessing = false
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.fillMaxWidth(0.8f).height(42.dp),
                                    enabled = otpCode == simulatedOtp && !stripeIsProcessing
                                ) {
                                    Text("Authorize Transaction", fontWeight = FontWeight.Bold)
                                }
                            } else if (paymentProcessingPhase == 3) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(64.dp))
                                Text(
                                    text = if (selectedPaymentMethod == "CARD") "Stripe Charge Succeeded!" else "Payment Authorized!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = if (selectedPaymentMethod == "CARD") "Secure Stripe PaymentIntent succeeded successfully." else "Transaction token generated successfully.",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )

                                Button(
                                    onClick = {
                                        onConfirm(rating, reviewText, tipAmount, selectedPaymentMethod)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    modifier = Modifier.fillMaxWidth(0.8f).height(44.dp)
                                ) {
                                    Text("Finish", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // In-App Simulated Toast message for SMS OTP
                if (showOtpToast && paymentProcessingPhase == 2) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Sms, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(14.dp))
                                Text("SIMULATED SMS NOTIFICATION", fontSize = 9.sp, fontWeight = FontWeight.Black, color = OrangePrimary)
                            }
                            Text(
                                text = "Hazir Pay Secure Auth Code: $simulatedOtp for PKR ${String.format("%.0f", totalToPay)} transaction.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostServiceRatingDialog(
    workerName: String,
    categoryName: String,
    onConfirm: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    
    val quickFeedbackChips = listOf(
        "Punctual & Professional",
        "Excellent Quality of Work",
        "Polite & Respectful",
        "Highly Skilled Technician",
        "Fair & Reasonable Price",
        "Cleaned Up Afterward"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Rate & Review Service",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "For $workerName • $categoryName",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "How would you rate the experience?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )

                // Interactive Star Rating Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i Stars",
                                tint = if (i <= rating) Color(0xFFFFB300) else Color.Gray,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Suggestion Tags
                Text(
                    text = "Tap quick tags to add to your review:",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Standard scrollable row for tag chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickFeedbackChips.forEach { chip ->
                        val isSelected = reviewText.contains(chip)
                        val borderCol = if (isSelected) OrangePrimary else Color.LightGray.copy(alpha = 0.5f)
                        val bgCol = if (isSelected) OrangePrimary.copy(alpha = 0.08f) else Color.Transparent
                        val textCol = if (isSelected) OrangePrimary else Color.DarkGray

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgCol)
                                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (reviewText.contains(chip)) {
                                        // Remove chip safely
                                        val cleaned = reviewText.replace(chip, "").replace(", ,", ",").trim()
                                        reviewText = if (cleaned.startsWith(",")) cleaned.substring(1).trim()
                                        else if (cleaned.endsWith(",")) cleaned.substring(0, cleaned.length - 1).trim()
                                        else cleaned
                                    } else {
                                        // Add chip safely
                                        reviewText = if (reviewText.isBlank()) chip else "$reviewText, $chip"
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chip,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textCol
                            )
                        }
                    }
                }

                // Review Text Field
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Share details about your experience (optional)...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        cursorColor = OrangePrimary
                    ),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rating, reviewText) },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Submit Review", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now", color = Color.Gray)
            }
        }
    )
}

