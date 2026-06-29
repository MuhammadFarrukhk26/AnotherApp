package com.example.domain.model

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val role: String, // "CUSTOMER", "WORKER", "ADMIN"
    val avatarUrl: String,
    val walletBalance: Double,
    val rating: Double = 5.0,
    val isOnline: Boolean = false,
    val skills: String = "", // Comma-separated for workers
    val experienceYears: Int = 0,
    val cnicVerified: Boolean = false,
    val cnicNumber: String = "",
    val selfieVerified: Boolean = false,
    val completedJobs: Int = 0,
    val latitude: Double = 33.6844,
    val longitude: Double = 73.0479
)

data class ServiceCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val description: String,
    val basePrice: Double
)

data class Booking(
    val id: Int = 0,
    val categoryId: String,
    val categoryName: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val workerId: String?,
    val workerName: String?,
    val workerPhone: String?,
    val date: String,
    val time: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val estimatedPrice: Double,
    val status: String, // "PENDING", "ACCEPTED", "ARRIVED", "STARTED", "COMPLETED", "CANCELLED"
    val rating: Int? = null,
    val review: String? = null,
    val beforePhoto: String? = null,
    val afterPhoto: String? = null,
    val paymentMethod: String = "CASH", // "CASH", "CARD", "WALLET"
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: Int = 0,
    val bookingId: Int,
    val senderId: String,
    val senderRole: String, // "CUSTOMER", "WORKER", "SYSTEM"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class WalletTransaction(
    val id: Int = 0,
    val userId: String,
    val amount: Double,
    val type: String, // "DEPOSIT", "WITHDRAW", "PAYMENT_OUT", "EARNING_IN"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiChatMessage(
    val senderName: String,
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
