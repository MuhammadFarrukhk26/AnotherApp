package com.example.application.usecase

import com.example.domain.model.*
import com.example.domain.repository.HazirRepository
import kotlinx.coroutines.flow.Flow

// ==========================================
// CATEGORY USE CASES
// ==========================================
class GetCategoriesUseCase(private val repository: HazirRepository) {
    operator fun invoke(): Flow<List<ServiceCategory>> = repository.getAllCategoriesFlow()
}

// ==========================================
// USER USE CASES
// ==========================================
class GetUserProfileUseCase(private val repository: HazirRepository) {
    operator fun invoke(userId: String): Flow<User?> = repository.getUserByIdFlow(userId)
}

class GetUserByIdUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(id: String): User? = repository.getUserById(id)
}

class GetAllWorkersUseCase(private val repository: HazirRepository) {
    operator fun invoke(): Flow<List<User>> = repository.getAllWorkersFlow()
}

class GetAvailableWorkersByCategoryUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(category: String): List<User> = repository.getAvailableWorkersByCategory(category)
}

class UpdateOnlineStatusUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(userId: String, isOnline: Boolean) = repository.updateOnlineStatus(userId, isOnline)
}

class GetPendingVerificationWorkersUseCase(private val repository: HazirRepository) {
    operator fun invoke(): Flow<List<User>> = repository.getPendingVerificationWorkersFlow()
}

class UpdateUserUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(user: User) = repository.updateUser(user)
}

// ==========================================
// BOOKING USE CASES
// ==========================================
class GetAllBookingsAdminUseCase(private val repository: HazirRepository) {
    operator fun invoke(): Flow<List<Booking>> = repository.getAllBookingsFlow()
}

class GetCustomerBookingsUseCase(private val repository: HazirRepository) {
    operator fun invoke(customerId: String): Flow<List<Booking>> = repository.getCustomerBookingsFlow(customerId)
}

class GetWorkerBookingsUseCase(private val repository: HazirRepository) {
    operator fun invoke(workerId: String): Flow<List<Booking>> = repository.getWorkerBookingsFlow(workerId)
}

class GetBookingByIdFlowUseCase(private val repository: HazirRepository) {
    operator fun invoke(id: Int): Flow<Booking?> = repository.getBookingByIdFlow(id)
}

class GetBookingByIdUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(id: Int): Booking? = repository.getBookingById(id)
}

class RequestBookingUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(booking: Booking): Int = repository.insertBooking(booking)
}

class UpdateBookingUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(booking: Booking) = repository.updateBooking(booking)
}

// ==========================================
// CHAT USE CASES
// ==========================================
class GetChatMessagesUseCase(private val repository: HazirRepository) {
    operator fun invoke(bookingId: Int): Flow<List<ChatMessage>> = repository.getMessagesForBookingFlow(bookingId)
}

class SendChatMessageUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(message: ChatMessage) = repository.sendMessage(message)
}

class MarkMessagesAsReadUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(bookingId: Int, currentUserId: String) = repository.markMessagesAsRead(bookingId, currentUserId)
}

// ==========================================
// WALLET USE CASES
// ==========================================
class GetWalletTransactionsUseCase(private val repository: HazirRepository) {
    operator fun invoke(userId: String): Flow<List<WalletTransaction>> = repository.getTransactionsForUserFlow(userId)
}

class DepositWalletFundsUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(userId: String, amount: Double, description: String) = repository.depositFunds(userId, amount, description)
}

class TransferFundsUseCase(private val repository: HazirRepository) {
    suspend operator fun invoke(senderId: String, receiverId: String, amount: Double, description: String) =
        repository.transferFunds(senderId, receiverId, amount, description)
}
