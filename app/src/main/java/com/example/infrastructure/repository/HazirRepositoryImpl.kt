package com.example.infrastructure.repository

import com.example.domain.model.*
import com.example.domain.repository.HazirRepository
import com.example.infrastructure.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HazirRepositoryImpl(private val db: HazirDatabase) : HazirRepository {

    private val userDao = db.userDao()
    private val categoryDao = db.categoryDao()
    private val bookingDao = db.bookingDao()
    private val chatDao = db.chatDao()
    private val walletDao = db.walletDao()

    // ==========================================
    // USER REPOSITORY OPERATIONS
    // ==========================================
    override suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)?.toDomain()
    }

    override fun getUserByIdFlow(id: String): Flow<User?> =
        userDao.getUserByIdFlow(id).map { it?.toDomain() }

    override fun getAllWorkersFlow(): Flow<List<User>> =
        userDao.getAllWorkersFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun getAvailableWorkersByCategory(category: String): List<User> = withContext(Dispatchers.IO) {
        userDao.getAvailableWorkersByCategory(category).map { it.toDomain() }
    }

    override suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(user.toEntity())
    }

    override suspend fun updateOnlineStatus(userId: String, isOnline: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateOnlineStatus(userId, isOnline)
    }

    override fun getPendingVerificationWorkersFlow(): Flow<List<User>> =
        userDao.getPendingVerificationWorkersFlow().map { list -> list.map { it.toDomain() } }

    // ==========================================
    // CATEGORY REPOSITORY OPERATIONS
    // ==========================================
    override fun getAllCategoriesFlow(): Flow<List<ServiceCategory>> =
        categoryDao.getAllCategoriesFlow().map { list -> list.map { it.toDomain() } }

    // ==========================================
    // BOOKING REPOSITORY OPERATIONS
    // ==========================================
    override fun getAllBookingsFlow(): Flow<List<Booking>> =
        bookingDao.getAllBookingsFlow().map { list -> list.map { it.toDomain() } }

    override fun getCustomerBookingsFlow(customerId: String): Flow<List<Booking>> =
        bookingDao.getCustomerBookingsFlow(customerId).map { list -> list.map { it.toDomain() } }

    override fun getWorkerBookingsFlow(workerId: String): Flow<List<Booking>> =
        bookingDao.getWorkerBookingsFlow(workerId).map { list -> list.map { it.toDomain() } }

    override suspend fun getBookingById(id: Int): Booking? = withContext(Dispatchers.IO) {
        bookingDao.getBookingById(id)?.toDomain()
    }

    override fun getBookingByIdFlow(id: Int): Flow<Booking?> =
        bookingDao.getBookingByIdFlow(id).map { it?.toDomain() }

    override suspend fun insertBooking(booking: Booking): Int = withContext(Dispatchers.IO) {
        bookingDao.insertBooking(booking.toEntity()).toInt()
    }

    override suspend fun updateBooking(booking: Booking) = withContext(Dispatchers.IO) {
        bookingDao.updateBooking(booking.toEntity())
    }

    // ==========================================
    // CHAT REPOSITORY OPERATIONS
    // ==========================================
    override fun getMessagesForBookingFlow(bookingId: Int): Flow<List<ChatMessage>> =
        chatDao.getMessagesForBookingFlow(bookingId).map { list -> list.map { it.toDomain() } }

    override suspend fun sendMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message.toEntity())
    }

    override suspend fun markMessagesAsRead(bookingId: Int, currentUserId: String) = withContext(Dispatchers.IO) {
        chatDao.markMessagesAsRead(bookingId, currentUserId)
    }

    // ==========================================
    // WALLET & TRANSACTION OPERATIONS
    // ==========================================
    override fun getTransactionsForUserFlow(userId: String): Flow<List<WalletTransaction>> =
        walletDao.getTransactionsForUserFlow(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun depositFunds(userId: String, amount: Double, description: String) = withContext(Dispatchers.IO) {
        // Update user balance
        userDao.updateWalletBalance(userId, amount)
        // Log transaction
        walletDao.insertTransaction(
            WalletTransactionEntity(
                userId = userId,
                amount = amount,
                type = "DEPOSIT",
                description = description
            )
        )
    }

    override suspend fun transferFunds(senderId: String, receiverId: String, amount: Double, description: String) = withContext(Dispatchers.IO) {
        // Deduct sender balance
        userDao.updateWalletBalance(senderId, -amount)
        walletDao.insertTransaction(
            WalletTransactionEntity(
                userId = senderId,
                amount = amount,
                type = "PAYMENT_OUT",
                description = "Payment to worker for Job: $description"
            )
        )

        // Add receiver balance (deducting a 10% platform fee commission)
        val platformCommission = amount * 0.10
        val workerEarning = amount - platformCommission

        userDao.updateWalletBalance(receiverId, workerEarning)
        walletDao.insertTransaction(
            WalletTransactionEntity(
                userId = receiverId,
                amount = workerEarning,
                type = "EARNING_IN",
                description = "Earning for Job (10% Fee deducted): $description"
            )
        )

        // Add platform commission to Admin account (admin_1)
        userDao.updateWalletBalance("admin_1", platformCommission)
        walletDao.insertTransaction(
            WalletTransactionEntity(
                userId = "admin_1",
                amount = platformCommission,
                type = "EARNING_IN",
                description = "Platform Commission from Job: $description"
            )
        )
    }
}
