package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.theme.NavySecondary
import com.example.presentation.theme.OrangeLight
import com.example.presentation.theme.OrangePrimary
import com.example.presentation.viewmodel.HazirViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterScreen(
    viewModel: HazirViewModel,
    onLoginSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var selectedRole by remember { mutableStateOf("CUSTOMER") } // CUSTOMER, WORKER, ADMIN

    // Input fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("Electrician") }
    var experienceYears by remember { mutableStateOf("2") }
    var cnicNumber by remember { mutableStateOf("") }
    var walletFunds by remember { mutableStateOf("5000") }

    // UI Feedback State
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val skillCategories = listOf(
        "Electrician", "Plumber", "AC Technician", "Home Cleaner",
        "Professional Painter", "Carpenter", "Car/Bike Mechanic", "Mover & Packer"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        val isWideScreen = maxWidth > 600.dp
        val horizontalPadding = if (isWideScreen) 32.dp else 20.dp

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Section
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Default.Handyman,
                contentDescription = "Hazir Logo Icon",
                tint = OrangePrimary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "HAZIR",
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                color = OrangePrimary,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "At your service • Islamabad & Rawalpindi",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login / Signup Selector (Tab-like row)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isLoginMode) OrangePrimary else Color.Transparent)
                            .clickable {
                                isLoginMode = true
                                errorMessage = null
                                successMessage = null
                            }
                            .testTag("tab_login_mode"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            fontWeight = FontWeight.Bold,
                            color = if (isLoginMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (!isLoginMode) OrangePrimary else Color.Transparent)
                            .clickable {
                                isLoginMode = false
                                errorMessage = null
                                successMessage = null
                            }
                            .testTag("tab_register_mode"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            fontWeight = FontWeight.Bold,
                            color = if (!isLoginMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Role / Segment Selector (Customer, Worker, Admin)
            Text(
                text = "Select Your Persona:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NavySecondary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "CUSTOMER" to "Customer",
                    "WORKER" to "Technician",
                    "ADMIN" to "Admin"
                ).forEach { (roleKey, roleLabel) ->
                    // Admins can only log in, not register
                    val isEnabled = isLoginMode || roleKey != "ADMIN"
                    if (isEnabled) {
                        val isSelected = selectedRole == roleKey
                        Button(
                            onClick = {
                                selectedRole = roleKey
                                errorMessage = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("role_select_$roleKey"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NavySecondary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = roleLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Feedback Banners
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            AnimatedVisibility(visible = successMessage != null) {
                successMessage?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Text(
                            text = it,
                            color = Color(0xFF1B5E20),
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Input Fields
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isLoginMode) {
                        // Registration: Full Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Haris Mahmood") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                cursorColor = OrangePrimary
                            ),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary) }
                        )
                    }

                    // Phone Number Input
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g. 0300-1234567") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            cursorColor = OrangePrimary
                        ),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = OrangePrimary) }
                    )

                    // Worker Specific Fields
                    if (!isLoginMode && selectedRole == "WORKER") {
                        // Specialty dropdown or row selection
                        var showSkillDropdown by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = showSkillDropdown,
                            onExpandedChange = { showSkillDropdown = !showSkillDropdown }
                        ) {
                            OutlinedTextField(
                                value = skills,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Service Specialty Skill") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSkillDropdown) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("worker_skill_dropdown"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = showSkillDropdown,
                                onDismissRequest = { showSkillDropdown = false }
                            ) {
                                skillCategories.forEach { skill ->
                                    DropdownMenuItem(
                                        text = { Text(skill) },
                                        onClick = {
                                            skills = skill
                                            showSkillDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Experience Years
                        OutlinedTextField(
                            value = experienceYears,
                            onValueChange = { experienceYears = it },
                            label = { Text("Years of Experience") },
                            placeholder = { Text("e.g. 5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("worker_experience_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                cursorColor = OrangePrimary
                            )
                        )

                        // CNIC National ID (Islamabad Security verified requirement!)
                        OutlinedTextField(
                            value = cnicNumber,
                            onValueChange = { cnicNumber = it },
                            label = { Text("CNIC Number (National ID)") },
                            placeholder = { Text("e.g. 37405-1234567-1") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("worker_cnic_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                cursorColor = OrangePrimary
                            ),
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = OrangePrimary) }
                        )
                    }

                    if (!isLoginMode) {
                        // Registration Initial Wallet Funds
                        OutlinedTextField(
                            value = walletFunds,
                            onValueChange = { walletFunds = it },
                            label = { Text("Seed Test Wallet Funds (PKR)") },
                            placeholder = { Text("e.g. 5000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_wallet_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                cursorColor = OrangePrimary
                            ),
                            leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = OrangePrimary) }
                        )
                    }

                    // Main Action Button
                    Button(
                        onClick = {
                            errorMessage = null
                            successMessage = null
                            if (isLoginMode) {
                                if (phone.trim().isEmpty()) {
                                    errorMessage = "Please enter your phone number to login."
                                    return@Button
                                }
                                viewModel.loginWithPhone(phone.trim(), selectedRole) { success, err ->
                                    if (success) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            } else {
                                // Sign Up Mode Validation
                                if (name.trim().isEmpty() || phone.trim().isEmpty()) {
                                    errorMessage = "Please fill in all general fields."
                                    return@Button
                                }
                                if (selectedRole == "WORKER" && (cnicNumber.trim().isEmpty() || skills.trim().isEmpty())) {
                                    errorMessage = "Workers must fill specialty skill and CNIC for background verification!"
                                    return@Button
                                }
                                val funds = walletFunds.toDoubleOrNull() ?: 5000.0
                                val exp = experienceYears.toIntOrNull() ?: 2

                                viewModel.signUpUser(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    role = selectedRole,
                                    skills = skills,
                                    experienceYears = exp,
                                    cnicNumber = cnicNumber.trim(),
                                    walletBalance = funds
                                ) { success, err ->
                                    if (success) {
                                        successMessage = "Account created successfully! Logging you in..."
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_auth_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isLoginMode) "Secure Login" else "Create Hazir Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Fast Testing Shortcut Logins (The "Scenario Swappers")
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fast Developer / Tester Profiles:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NavySecondary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Customer Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            phone = "0300-1234567"
                            selectedRole = "CUSTOMER"
                            isLoginMode = true
                            errorMessage = null
                            viewModel.loginWithPhone("0300-1234567", "CUSTOMER") { success, _ ->
                                if (success) onLoginSuccess()
                            }
                        }
                        .testTag("shortcut_customer"),
                    colors = CardDefaults.cardColors(containerColor = OrangePrimary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Haris Mahmood (Customer)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavySecondary)
                                Text("0300-1234567 • Test ordering services", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OrangePrimary)
                    }
                }

                // Worker Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            phone = "0312-5551111"
                            selectedRole = "WORKER"
                            isLoginMode = true
                            errorMessage = null
                            viewModel.loginWithPhone("0312-5551111", "WORKER") { success, _ ->
                                if (success) onLoginSuccess()
                            }
                        }
                        .testTag("shortcut_worker"),
                    colors = CardDefaults.cardColors(containerColor = NavySecondary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = NavySecondary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Sajid Qureshi (Technician)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavySecondary)
                                Text("0312-5551111 • Accept bookings & chat live", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NavySecondary)
                    }
                }

                // Admin Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            phone = "0321-7654321"
                            selectedRole = "ADMIN"
                            isLoginMode = true
                            errorMessage = null
                            viewModel.loginWithPhone("0321-7654321", "ADMIN") { success, _ ->
                                if (success) onLoginSuccess()
                            }
                        }
                        .testTag("shortcut_admin"),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Ayesha Malik (Admin Manager)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavySecondary)
                                Text("0321-7654321 • Verify workers & manage metrics", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Red)
                    }
                }
            }
        }
    }
}
