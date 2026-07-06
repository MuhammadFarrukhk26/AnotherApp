package com.example.domain.repository

import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

interface HazirRepository {
    // User Operations
    suspend fun getUserById(id: String): User?
    suspend fun getUserByPhone(phone: String): User?
    fun getUserByIdFlow(id: String): Flow<User?>
    fun getAllWorkersFlow(): Flow<List<User>>
    suspend fun getAvailableWorkersByCategory(category: String): List<User>
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean)
    fun getPendingVerificationWorkersFlow(): Flow<List<User>>

    // Category Operations
    fun getAllCategoriesFlow(): Flow<List<ServiceCategory>>

    // Booking Operations
    fun getAllBookingsFlow(): Flow<List<Booking>>
    fun getCustomerBookingsFlow(customerId: String): Flow<List<Booking>>
    fun getWorkerBookingsFlow(workerId: String): Flow<List<Booking>>
    suspend fun getBookingsForWorker(workerId: String): List<Booking>
    suspend fun getBookingById(id: Int): Booking?
    fun getBookingByIdFlow(id: Int): Flow<Booking?>
    suspend fun insertBooking(booking: Booking): Int
    suspend fun updateBooking(booking: Booking)

    // Chat Operations
    fun getMessagesForBookingFlow(bookingId: Int): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: ChatMessage)
    suspend fun markMessagesAsRead(bookingId: Int, currentUserId: String)

    // Wallet & Transaction Operations
    fun getTransactionsForUserFlow(userId: String): Flow<List<WalletTransaction>>
    suspend fun depositFunds(userId: String, amount: Double, description: String)
    suspend fun transferFunds(senderId: String, receiverId: String, amount: Double, description: String)

    // Saved Address Operations
    fun getSavedAddressesFlow(userId: String): Flow<List<SavedAddress>>
    suspend fun getSavedAddresses(userId: String): List<SavedAddress>
    suspend fun insertSavedAddress(address: SavedAddress): Int
    suspend fun updateSavedAddress(address: SavedAddress)
    suspend fun deleteSavedAddress(address: SavedAddress)
    suspend fun setDefaultSavedAddress(userId: String, addressId: Int)
}
