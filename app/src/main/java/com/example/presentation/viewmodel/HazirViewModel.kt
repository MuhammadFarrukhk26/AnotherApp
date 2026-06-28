package com.example.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.application.usecase.*
import com.example.infrastructure.api.GeminiClient
import com.example.infrastructure.database.HazirDatabase
import com.example.infrastructure.repository.HazirRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Intent
import com.example.infrastructure.service.WorkerTrackingService

class HazirViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "HazirViewModel"

    // Use cases
    private val getCategoriesUseCase: GetCategoriesUseCase
    private val getUserProfileUseCase: GetUserProfileUseCase
    private val getUserByIdUseCase: GetUserByIdUseCase
    private val getAllWorkersUseCase: GetAllWorkersUseCase
    private val getAvailableWorkersByCategoryUseCase: GetAvailableWorkersByCategoryUseCase
    private val updateOnlineStatusUseCase: UpdateOnlineStatusUseCase
    private val getPendingVerificationWorkersUseCase: GetPendingVerificationWorkersUseCase
    private val updateUserUseCase: UpdateUserUseCase
    private val getAllBookingsAdminUseCase: GetAllBookingsAdminUseCase
    private val getCustomerBookingsUseCase: GetCustomerBookingsUseCase
    private val getWorkerBookingsUseCase: GetWorkerBookingsUseCase
    private val getBookingByIdFlowUseCase: GetBookingByIdFlowUseCase
    private val getBookingByIdUseCase: GetBookingByIdUseCase
    private val requestBookingUseCase: RequestBookingUseCase
    private val updateBookingUseCase: UpdateBookingUseCase
    private val getChatMessagesUseCase: GetChatMessagesUseCase
    private val sendChatMessageUseCase: SendChatMessageUseCase
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase
    private val getWalletTransactionsUseCase: GetWalletTransactionsUseCase
    private val depositWalletFundsUseCase: DepositWalletFundsUseCase
    private val transferFundsUseCase: TransferFundsUseCase

    // 1. App Navigation / UI Persona State
    private val _currentRole = MutableStateFlow("CUSTOMER") // "CUSTOMER", "WORKER", "ADMIN"
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _currentUserId = MutableStateFlow("customer_1") // Or "worker_electrician", "admin_1"
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // 2. Reactive Flows
    val categories: StateFlow<List<ServiceCategory>>
    val allWorkers: StateFlow<List<User>>
    val pendingVerificationWorkers: StateFlow<List<User>>

    // User Session Profile Flow
    val currentUserProfile: StateFlow<User?>

    // Booking Flows
    private val _selectedBookingId = MutableStateFlow<Int?>(null)
    val selectedBookingId: StateFlow<Int?> = _selectedBookingId.asStateFlow()

    val customerBookings: StateFlow<List<Booking>>
    val workerBookings: StateFlow<List<Booking>>
    val allBookingsAdmin: StateFlow<List<Booking>>

    val activeBookingDetail: StateFlow<Booking?>

    // Chat Message Flow
    val activeChatMessages: StateFlow<List<ChatMessage>>

    // Wallet Transactions Flow
    val walletTransactions: StateFlow<List<WalletTransaction>>

    // AI Advisor Local Chat History
    private val _aiChatHistory = MutableStateFlow<List<AiChatMessage>>(listOf(
        AiChatMessage("Hazir AI", "Asalam-o-Alaikum! I am your Hazir AI Advisor. Tell me what issue you are facing (e.g. 'pipe leaking', 'AC is not cooling', 'switchboard sparking'). I'll suggest the right service and estimate PKR costs!", false)
    ))
    val aiChatHistory: StateFlow<List<AiChatMessage>> = _aiChatHistory.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // Live Simulated Map Coordinate (Worker current lat/lng during transit)
    private val _simulatedWorkerLat = MutableStateFlow<Double?>(null)
    val simulatedWorkerLat: StateFlow<Double?> = _simulatedWorkerLat.asStateFlow()

    private val _simulatedWorkerLng = MutableStateFlow<Double?>(null)
    val simulatedWorkerLng: StateFlow<Double?> = _simulatedWorkerLng.asStateFlow()

    init {
        // Concrete database & repository instantiation in infrastructure layer
        val db = HazirDatabase.getDatabase(application)
        val repository = HazirRepositoryImpl(db)

        // Instantiate Use Cases
        getCategoriesUseCase = GetCategoriesUseCase(repository)
        getUserProfileUseCase = GetUserProfileUseCase(repository)
        getUserByIdUseCase = GetUserByIdUseCase(repository)
        getAllWorkersUseCase = GetAllWorkersUseCase(repository)
        getAvailableWorkersByCategoryUseCase = GetAvailableWorkersByCategoryUseCase(repository)
        updateOnlineStatusUseCase = UpdateOnlineStatusUseCase(repository)
        getPendingVerificationWorkersUseCase = GetPendingVerificationWorkersUseCase(repository)
        updateUserUseCase = UpdateUserUseCase(repository)
        getAllBookingsAdminUseCase = GetAllBookingsAdminUseCase(repository)
        getCustomerBookingsUseCase = GetCustomerBookingsUseCase(repository)
        getWorkerBookingsUseCase = GetWorkerBookingsUseCase(repository)
        getBookingByIdFlowUseCase = GetBookingByIdFlowUseCase(repository)
        getBookingByIdUseCase = GetBookingByIdUseCase(repository)
        requestBookingUseCase = RequestBookingUseCase(repository)
        updateBookingUseCase = UpdateBookingUseCase(repository)
        getChatMessagesUseCase = GetChatMessagesUseCase(repository)
        sendChatMessageUseCase = SendChatMessageUseCase(repository)
        markMessagesAsReadUseCase = MarkMessagesAsReadUseCase(repository)
        getWalletTransactionsUseCase = GetWalletTransactionsUseCase(repository)
        depositWalletFundsUseCase = DepositWalletFundsUseCase(repository)
        transferFundsUseCase = TransferFundsUseCase(repository)

        // Bind Flows via Use Cases
        categories = getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allWorkers = getAllWorkersUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        pendingVerificationWorkers = getPendingVerificationWorkersUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Profile switcher bind
        currentUserProfile = _currentUserId.flatMapLatest { id ->
            getUserProfileUseCase(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        customerBookings = getCustomerBookingsUseCase("customer_1")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        workerBookings = _currentUserId.flatMapLatest { id ->
            getWorkerBookingsUseCase(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allBookingsAdmin = getAllBookingsAdminUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeBookingDetail = _selectedBookingId.flatMapLatest { id ->
            if (id != null) getBookingByIdFlowUseCase(id) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        activeChatMessages = _selectedBookingId.flatMapLatest { id ->
            if (id != null) getChatMessagesUseCase(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        walletTransactions = _currentUserId.flatMapLatest { id ->
            getWalletTransactionsUseCase(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // ==========================================
    // PROFILE SWITCHER (CUSTOMER / WORKER / ADMIN)
    // ==========================================
    fun setAppRole(role: String) {
        viewModelScope.launch {
            _currentRole.value = role
            when (role) {
                "CUSTOMER" -> _currentUserId.value = "customer_1"
                "WORKER" -> _currentUserId.value = "worker_electrician" // Sajid by default
                "ADMIN" -> _currentUserId.value = "admin_1"
            }
            Log.d(TAG, "Switched role to: $role with UserID: ${_currentUserId.value}")
        }
    }

    // Toggle Worker's online status
    fun toggleWorkerOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            val workerId = _currentUserId.value
            updateOnlineStatusUseCase(workerId, isOnline)
            Log.d(TAG, "Worker $workerId set online status: $isOnline")
        }
    }

    // ==========================================
    // BOOKING ACTIONS & LIVE SIMULATION
    // ==========================================
    fun requestBooking(
        categoryId: String,
        categoryName: String,
        address: String,
        description: String,
        estimatedPrice: Double,
        date: String,
        time: String
    ) {
        viewModelScope.launch {
            val user = currentUserProfile.value ?: return@launch

            val newBooking = Booking(
                categoryId = categoryId,
                categoryName = categoryName,
                customerId = user.id,
                customerName = user.name,
                customerPhone = user.phone,
                workerId = null,
                workerName = null,
                workerPhone = null,
                date = date,
                time = time,
                address = address,
                latitude = 33.6844, // Default Islamabad Customer position
                longitude = 73.0479,
                description = description,
                estimatedPrice = estimatedPrice,
                status = "PENDING"
            )

            val bookingId = requestBookingUseCase(newBooking)
            _selectedBookingId.value = bookingId
            Log.d(TAG, "Created booking ID: $bookingId")

            // Automatically kickstart the worker assignment and transit simulation
            runBookingSimulation(bookingId)
        }
    }

    fun selectBooking(bookingId: Int?) {
        _selectedBookingId.value = bookingId
        if (bookingId != null) {
            viewModelScope.launch {
                markMessagesAsReadUseCase(bookingId, _currentUserId.value)
            }
        }
    }

    fun cancelActiveBooking(bookingId: Int) {
        viewModelScope.launch {
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            val updated = booking.copy(status = "CANCELLED")
            updateBookingUseCase(updated)
            
            sendChatMessageUseCase(
                ChatMessage(
                    bookingId = bookingId,
                    senderId = "system",
                    senderRole = "SYSTEM",
                    message = "Booking cancelled by customer."
                )
            )
        }
    }

    fun startJobWorker(bookingId: Int) {
        viewModelScope.launch {
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            val updated = booking.copy(status = "STARTED")
            updateBookingUseCase(updated)

            sendChatMessageUseCase(
                ChatMessage(
                    bookingId = bookingId,
                    senderId = booking.workerId ?: "worker",
                    senderRole = "WORKER",
                    message = "I have started the service! I am working on resolving the issue now."
                )
            )
        }
    }

    fun completeJobWorker(bookingId: Int) {
        completeJobWorkerWithProof(
            bookingId = bookingId,
            beforePhoto = "https://images.unsplash.com/photo-1581094288338-2314dddb7eed?q=80&w=600", // Default placeholder
            afterPhoto = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?q=80&w=600"  // Default placeholder
        )
    }

    fun completeJobWorkerWithProof(bookingId: Int, beforePhoto: String, afterPhoto: String) {
        viewModelScope.launch {
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            val updated = booking.copy(
                status = "COMPLETED",
                beforePhoto = beforePhoto,
                afterPhoto = afterPhoto
            )
            updateBookingUseCase(updated)

            sendChatMessageUseCase(
                ChatMessage(
                    bookingId = bookingId,
                    senderId = booking.workerId ?: "worker",
                    senderRole = "WORKER",
                    message = "Alhamdulillah, the job is finished! I've uploaded the service proof (before/after photos) of the job site. Please review and complete the payment. JazakAllah!"
                )
            )
        }
    }

    fun payAndReviewBooking(bookingId: Int, rating: Int, review: String) {
        viewModelScope.launch {
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            if (booking.status == "COMPLETED") {
                val senderId = booking.customerId
                val receiverId = booking.workerId ?: "worker_electrician"
                
                val userProfile = getUserByIdUseCase(senderId)
                if (userProfile != null && userProfile.walletBalance >= booking.estimatedPrice) {
                    transferFundsUseCase(
                        senderId = senderId,
                        receiverId = receiverId,
                        amount = booking.estimatedPrice,
                        description = "Booking #${booking.id} - ${booking.categoryName}"
                    )

                    // Update booking rating & payment confirmation
                    val updated = booking.copy(
                        status = "COMPLETED",
                        rating = rating,
                        review = review
                    )
                    updateBookingUseCase(updated)

                    // Increment worker's completed jobs
                    val worker = getUserByIdUseCase(receiverId)
                    if (worker != null) {
                        updateUserUseCase(worker.copy(completedJobs = worker.completedJobs + 1))
                    }

                    sendChatMessageUseCase(
                        ChatMessage(
                            bookingId = bookingId,
                            senderId = "system",
                            senderRole = "SYSTEM",
                            message = "Payment of PKR ${booking.estimatedPrice} completed successfully! Thank you for choosing Hazir."
                        )
                    )
                } else {
                    Log.e(TAG, "Insufficient balance for payment!")
                }
            }
        }
    }

    // ==========================================
    // CHAT SYSTEM
    // ==========================================
    fun sendChatMessage(bookingId: Int, text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            val senderId = _currentUserId.value
            val role = _currentRole.value
            val message = ChatMessage(
                bookingId = bookingId,
                senderId = senderId,
                senderRole = role,
                message = text
            )
            sendChatMessageUseCase(message)

            // Auto-reply simulation if role is Customer and booking is active
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            if (role == "CUSTOMER" && booking.status != "COMPLETED" && booking.status != "CANCELLED") {
                simulateWorkerChatMessageReply(bookingId, text)
            }
        }
    }

    // ==========================================
    // AI ADVISOR OPERATIONS
    // ==========================================
    fun askAiAdvisor(text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            val updatedList = _aiChatHistory.value.toMutableList()
            updatedList.add(AiChatMessage("You", text, true))
            _aiChatHistory.value = updatedList

            _aiLoading.value = true
            val reply = GeminiClient.getServiceAdvice(text)
            _aiLoading.value = false

            val nextList = _aiChatHistory.value.toMutableList()
            nextList.add(AiChatMessage("Hazir AI", reply, false))
            _aiChatHistory.value = nextList
        }
    }

    fun clearAiChat() {
        _aiChatHistory.value = listOf(
            AiChatMessage("Hazir AI", "Asalam-o-Alaikum! I am your Hazir AI Advisor. Tell me what issue you are facing (e.g. 'pipe leaking', 'AC is not cooling', 'switchboard sparking'). I'll suggest the right service and estimate PKR costs!", false)
        )
    }

    // ==========================================
    // WALLET OPERATIONS
    // ==========================================
    fun depositWalletFunds(amount: Double) {
        viewModelScope.launch {
            depositWalletFundsUseCase(_currentUserId.value, amount, "Wallet Top-up Deposit via Stripe/PayPal")
            Log.d(TAG, "Deposited $amount PKR into wallet")
        }
    }

    // ==========================================
    // ADMIN ACTIONS (Worker verification)
    // ==========================================
    fun approveWorkerVerification(workerId: String, isApproved: Boolean) {
        viewModelScope.launch {
            val worker = getUserByIdUseCase(workerId) ?: return@launch
            if (isApproved) {
                updateUserUseCase(worker.copy(
                    cnicVerified = true,
                    selfieVerified = true
                ))
            } else {
                updateUserUseCase(worker.copy(
                    cnicVerified = false,
                    selfieVerified = false,
                    cnicNumber = ""
                ))
            }
        }
    }

    // ==========================================
    // SIMULATION ENGINE COROUTINES
    // ==========================================
    private fun runBookingSimulation(bookingId: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            val categoryId = booking.categoryId

            val customerLat = 33.6844
            val customerLng = 73.0479
            var workerLat = 33.7100
            var workerLng = 73.0700

            _simulatedWorkerLat.value = workerLat
            _simulatedWorkerLng.value = workerLng

            delay(4000)
            val workersOfCategory = getAvailableWorkersByCategoryUseCase(categoryId)
            val matchedWorker = workersOfCategory.firstOrNull() ?: getUserByIdUseCase("worker_electrician")!!

            val acceptedBooking = getBookingByIdUseCase(bookingId) ?: return@launch
            if (acceptedBooking.status == "PENDING") {
                val updated = acceptedBooking.copy(
                    status = "ACCEPTED",
                    workerId = matchedWorker.id,
                    workerName = matchedWorker.name,
                    workerPhone = matchedWorker.phone
                )
                updateBookingUseCase(updated)

                sendChatMessageUseCase(
                    ChatMessage(
                        bookingId = bookingId,
                        senderId = matchedWorker.id,
                        senderRole = "WORKER",
                        message = "Asalam-o-Alaikum! I have accepted your request for ${acceptedBooking.categoryName}. I'm heading over to your address immediately."
                    )
                )

                // Start background tracking service
                val intent = Intent(getApplication<Application>(), WorkerTrackingService::class.java).apply {
                    putExtra("booking_id", bookingId)
                    putExtra("customer_lat", customerLat)
                    putExtra("customer_lng", customerLng)
                    putExtra("worker_lat", workerLat)
                    putExtra("worker_lng", workerLng)
                    putExtra("worker_name", matchedWorker.name ?: "Ayaan Sheikh")
                }
                try {
                    getApplication<Application>().startService(intent)
                } catch (e: Exception) {
                    Log.e("HazirViewModel", "Failed to start background tracking service: ${e.message}")
                }

                // Collect coordinates from background service in real time
                val coordinateJob = launch {
                    WorkerTrackingService.workerCoordinates.collect { coords ->
                        if (coords != null) {
                            _simulatedWorkerLat.value = coords.first
                            _simulatedWorkerLng.value = coords.second
                        }
                    }
                }

                // Wait until the service updates the booking status to ARRIVED in the database
                var currentStatus = "ACCEPTED"
                while (currentStatus == "ACCEPTED") {
                    delay(1000)
                    currentStatus = getBookingByIdUseCase(bookingId)?.status ?: "ACCEPTED"
                }

                coordinateJob.cancel()

                val currentBooking = getBookingByIdUseCase(bookingId) ?: return@launch
                if (currentBooking.status == "ARRIVED") {
                    delay(5000)
                    val arrivedBooking = getBookingByIdUseCase(bookingId) ?: return@launch
                    if (arrivedBooking.status == "ARRIVED") {
                        updateBookingUseCase(arrivedBooking.copy(status = "STARTED"))
                        sendChatMessageUseCase(
                            ChatMessage(
                                bookingId = bookingId,
                                senderId = matchedWorker.id,
                                senderRole = "WORKER",
                                message = "I am starting the service work now."
                            )
                        )

                        delay(10000)
                        val startedBooking = getBookingByIdUseCase(bookingId) ?: return@launch
                        if (startedBooking.status == "STARTED") {
                            updateBookingUseCase(startedBooking.copy(status = "COMPLETED"))
                            sendChatMessageUseCase(
                                ChatMessage(
                                    bookingId = bookingId,
                                    senderId = matchedWorker.id,
                                    senderRole = "WORKER",
                                    message = "Alhamdulillah! The service task has been completed successfully. Please inspect the work and confirm the payment in the app."
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun simulateWorkerChatMessageReply(bookingId: Int, customerMsg: String) {
        viewModelScope.launch(Dispatchers.Default) {
            delay(2500)
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            val workerName = booking.workerName ?: "Handyman"

            val replyText = when {
                customerMsg.lowercase().contains("where") || customerMsg.lowercase().contains("time") || customerMsg.lowercase().contains("far") || customerMsg.lowercase().contains("location") -> {
                    "I am close to you. Just checking my GPS navigation, reaching in 5-10 minutes Insha'Allah."
                }
                customerMsg.lowercase().contains("price") || customerMsg.lowercase().contains("cost") || customerMsg.lowercase().contains("pkr") || customerMsg.lowercase().contains("rupee") -> {
                    "The estimated price is PKR ${booking.estimatedPrice}. I will perform the diagnosis first and explain any extra material needed."
                }
                customerMsg.lowercase().contains("ok") || customerMsg.lowercase().contains("okay") || customerMsg.lowercase().contains("sure") || customerMsg.lowercase().contains("yes") -> {
                    "JazakAllah! See you soon."
                }
                customerMsg.lowercase().contains("phone") || customerMsg.lowercase().contains("number") || customerMsg.lowercase().contains("call") -> {
                    "You can call me on my phone at ${booking.workerPhone ?: "0312-5551111"}. I'm currently driving."
                }
                else -> {
                    "Understood. Let me look at it once I arrive at your house. No need to worry, Hazir will handle it!"
                }
            }

            sendChatMessageUseCase(
                ChatMessage(
                    bookingId = bookingId,
                    senderId = booking.workerId ?: "worker",
                    senderRole = "WORKER",
                    message = replyText
                )
            )
        }
    }
}
