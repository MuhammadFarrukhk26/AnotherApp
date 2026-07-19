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
    private val getBookingsForWorkerUseCase: GetBookingsForWorkerUseCase
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

    // Saved Address Use Cases
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase
    private val insertSavedAddressUseCase: InsertSavedAddressUseCase
    private val updateSavedAddressUseCase: UpdateSavedAddressUseCase
    private val deleteSavedAddressUseCase: DeleteSavedAddressUseCase
    private val setDefaultSavedAddressUseCase: SetDefaultSavedAddressUseCase

    private lateinit var repository: com.example.domain.repository.HazirRepository

    // 1. App Navigation / UI Persona State
    private val sharedPrefs = application.getSharedPreferences("hazir_session", android.content.Context.MODE_PRIVATE)

    private val _currentRole = MutableStateFlow(sharedPrefs.getString("current_role", "CUSTOMER") ?: "CUSTOMER") // "CUSTOMER", "WORKER", "ADMIN"
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _currentUserId = MutableStateFlow(sharedPrefs.getString("current_user_id", "") ?: "") // Empty initially representing logged out
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
    val activeWorkerProfile: StateFlow<User?>
    val activeWorkerBookingHistory: StateFlow<List<Booking>>

    // Chat Message Flow
    val activeChatMessages: StateFlow<List<ChatMessage>>

    // Wallet Transactions Flow
    val walletTransactions: StateFlow<List<WalletTransaction>>

    // Saved Addresses Flow
    val savedAddresses: StateFlow<List<SavedAddress>>

    // AI Advisor Local Chat History
    private val _aiChatHistory = MutableStateFlow<List<AiChatMessage>>(listOf(
        AiChatMessage("Hazir AI", "Asalam-o-Alaikum! I am your Hazir AI Advisor. Tell me what issue you are facing (e.g. 'pipe leaking', 'AC is not cooling', 'switchboard sparking'). I'll suggest the right service and estimate PKR costs!", false)
    ))
    val aiChatHistory: StateFlow<List<AiChatMessage>> = _aiChatHistory.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // Typing states for customer-worker real-time chat
    private val _isWorkerTyping = MutableStateFlow(false)
    val isWorkerTyping: StateFlow<Boolean> = _isWorkerTyping.asStateFlow()

    private val _isCustomerTyping = MutableStateFlow(false)
    val isCustomerTyping: StateFlow<Boolean> = _isCustomerTyping.asStateFlow()

    // Live Simulated Map Coordinate (Worker current lat/lng during transit)
    private val _simulatedWorkerLat = MutableStateFlow<Double?>(null)
    val simulatedWorkerLat: StateFlow<Double?> = _simulatedWorkerLat.asStateFlow()

    private val _simulatedWorkerLng = MutableStateFlow<Double?>(null)
    val simulatedWorkerLng: StateFlow<Double?> = _simulatedWorkerLng.asStateFlow()

    init {
        // Concrete database & repository instantiation in infrastructure layer
        val db = HazirDatabase.getDatabase(application)
        val repository = HazirRepositoryImpl(db)
        this.repository = repository

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
        getBookingsForWorkerUseCase = GetBookingsForWorkerUseCase(repository)
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

        getSavedAddressesUseCase = GetSavedAddressesUseCase(repository)
        insertSavedAddressUseCase = InsertSavedAddressUseCase(repository)
        updateSavedAddressUseCase = UpdateSavedAddressUseCase(repository)
        deleteSavedAddressUseCase = DeleteSavedAddressUseCase(repository)
        setDefaultSavedAddressUseCase = SetDefaultSavedAddressUseCase(repository)

        // Bind Flows via Use Cases
        categories = getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allWorkers = getAllWorkersUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        pendingVerificationWorkers = getPendingVerificationWorkersUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Profile switcher bind
        currentUserProfile = _currentUserId.flatMapLatest { id ->
            if (id.isNotEmpty()) getUserProfileUseCase(id) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        customerBookings = _currentUserId.flatMapLatest { id ->
            if (id.isNotEmpty()) getCustomerBookingsUseCase(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        workerBookings = _currentUserId.flatMapLatest { id ->
            if (id.isNotEmpty()) getWorkerBookingsUseCase(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allBookingsAdmin = getAllBookingsAdminUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeBookingDetail = _selectedBookingId.flatMapLatest { id ->
            if (id != null) getBookingByIdFlowUseCase(id) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        activeWorkerProfile = activeBookingDetail.flatMapLatest { booking ->
            val workerId = booking?.workerId
            if (!workerId.isNullOrEmpty()) {
                getUserProfileUseCase(workerId)
            } else {
                flowOf(null)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        activeWorkerBookingHistory = activeBookingDetail.flatMapLatest { booking ->
            val workerId = booking?.workerId
            if (!workerId.isNullOrEmpty()) {
                getWorkerBookingsUseCase(workerId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeChatMessages = _selectedBookingId.flatMapLatest { id ->
            if (id != null) getChatMessagesUseCase(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        walletTransactions = _currentUserId.flatMapLatest { id ->
            if (id.isNotEmpty()) getWalletTransactionsUseCase(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        savedAddresses = _currentUserId.flatMapLatest { id ->
            if (id.isNotEmpty()) getSavedAddressesUseCase(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // ==========================================
    // PROFILE SWITCHER (CUSTOMER / WORKER / ADMIN)
    // ==========================================
    fun setAppRole(role: String) {
        viewModelScope.launch {
            _currentRole.value = role
            val userId = when (role) {
                "CUSTOMER" -> "customer_1"
                "WORKER" -> {
                    val activeTrackId = _selectedBookingId.value
                    var matchedWorkerId: String? = null
                    if (activeTrackId != null) {
                        val booking = getBookingByIdUseCase(activeTrackId)
                        if (booking != null && booking.workerId != null) {
                            matchedWorkerId = booking.workerId
                        }
                    }
                    if (matchedWorkerId == null) {
                        val activeBooking = customerBookings.value.firstOrNull {
                            it.status != "COMPLETED" && it.status != "CANCELLED"
                        }
                        if (activeBooking != null && activeBooking.workerId != null) {
                            matchedWorkerId = activeBooking.workerId
                        }
                    }
                    matchedWorkerId ?: "worker_electrician"
                }
                "ADMIN" -> "admin_1"
                else -> ""
            }
            _currentUserId.value = userId
            sharedPrefs.edit()
                .putString("current_role", role)
                .putString("current_user_id", userId)
                .apply()
            Log.d(TAG, "Switched role to: $role with UserID: $userId")
        }
    }

    // ==========================================
    // AUTHENTICATION & REGISTRATION
    // ==========================================
    fun loginWithPhone(phone: String, role: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repository.getUserByPhone(phone)
                if (user != null) {
                    if (user.role == role) {
                        _currentUserId.value = user.id
                        _currentRole.value = user.role
                        sharedPrefs.edit()
                            .putString("current_role", user.role)
                            .putString("current_user_id", user.id)
                            .apply()
                        onResult(true, null)
                    } else {
                        onResult(false, "This phone is registered as ${user.role}, not $role.")
                    }
                } else {
                    onResult(false, "Phone number not registered. Please sign up!")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun signUpUser(
        name: String,
        phone: String,
        role: String,
        skills: String = "",
        experienceYears: Int = 0,
        cnicNumber: String = "",
        walletBalance: Double = 5000.0,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existing = repository.getUserByPhone(phone)
                if (existing != null) {
                    onResult(false, "Phone number already registered as ${existing.role}!")
                    return@launch
                }

                val newId = if (role == "WORKER") "worker_${System.currentTimeMillis()}" else "customer_${System.currentTimeMillis()}"
                val newUser = User(
                    id = newId,
                    name = name,
                    phone = phone,
                    role = role,
                    avatarUrl = if (role == "WORKER") "avatar_sajid" else "avatar_haris",
                    walletBalance = walletBalance,
                    rating = if (role == "WORKER") 5.0 else 0.0,
                    isOnline = role == "WORKER",
                    skills = skills,
                    experienceYears = experienceYears,
                    cnicVerified = role != "WORKER", // Only Workers require verification
                    cnicNumber = cnicNumber,
                    selfieVerified = role != "WORKER",
                    completedJobs = 0
                )

                repository.insertUser(newUser)
                _currentUserId.value = newId
                _currentRole.value = role
                sharedPrefs.edit()
                    .putString("current_role", role)
                    .putString("current_user_id", newId)
                    .apply()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        _currentUserId.value = ""
        sharedPrefs.edit()
            .remove("current_user_id")
            .remove("current_role")
            .apply()
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
        time: String,
        paymentMethod: String = "CASH"
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
                status = "PENDING",
                paymentMethod = paymentMethod
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
                val booking = getBookingByIdUseCase(bookingId)
                if (booking != null && booking.status != "PENDING" && booking.status != "CANCELLED") {
                    if (_simulatedWorkerLat.value == null) {
                        _simulatedWorkerLat.value = booking.latitude + 0.0256
                        _simulatedWorkerLng.value = booking.longitude + 0.0221
                    }
                } else if (booking?.status == "PENDING" || booking?.status == "CANCELLED") {
                    _simulatedWorkerLat.value = null
                    _simulatedWorkerLng.value = null
                }
            }
        }
    }

    fun cancelActiveBooking(bookingId: Int, reason: String? = null) {
        viewModelScope.launch {
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            val updated = booking.copy(status = "CANCELLED")
            updateBookingUseCase(updated)
            
            sendChatMessageUseCase(
                ChatMessage(
                    bookingId = bookingId,
                    senderId = "system",
                    senderRole = "SYSTEM",
                    message = if (reason.isNullOrBlank()) "Booking cancelled by customer." else "Booking cancelled by customer. Reason: $reason"
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

            // Trigger "completed" push notification via NotificationManager
            com.example.infrastructure.notification.NotificationManager.triggerJobCompleted(
                getApplication(),
                bookingId,
                booking.workerName ?: "Technician",
                booking.categoryName
            )
            com.example.infrastructure.notification.NotificationProvider.triggerJobCompleted(
                getApplication(),
                bookingId,
                booking.workerName ?: "Technician",
                booking.categoryName
            )

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

    fun payAndReviewBooking(bookingId: Int, rating: Int, review: String, tipAmount: Double = 0.0, finalPaymentMethod: String? = null) {
        viewModelScope.launch {
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            if (booking.status == "COMPLETED") {
                val senderId = booking.customerId
                val receiverId = booking.workerId ?: "worker_electrician"
                val method = finalPaymentMethod ?: booking.paymentMethod
                val totalAmount = booking.estimatedPrice + tipAmount
                
                val userProfile = getUserByIdUseCase(senderId)
                if (userProfile != null) {
                    var paymentSuccess = false
                    
                    if (method == "CASH") {
                        // Cash payment completed directly
                        paymentSuccess = true
                    } else if (method == "CARD" || method == "EASYPAISA" || method == "JAZZCASH") {
                        // Direct secure online checkout: simulate deposit into user's in-app wallet then transfer
                        depositWalletFundsUseCase(senderId, totalAmount, "Auto-deposit for booking checkout ($method)")
                        transferFundsUseCase(
                            senderId = senderId,
                            receiverId = receiverId,
                            amount = totalAmount,
                            description = "Booking #${booking.id} - ${booking.categoryName} (via $method)"
                        )
                        paymentSuccess = true
                    } else { // WALLET (Hazir Wallet)
                        if (userProfile.walletBalance >= totalAmount) {
                            transferFundsUseCase(
                                senderId = senderId,
                                receiverId = receiverId,
                                amount = totalAmount,
                                description = "Booking #${booking.id} - ${booking.categoryName}"
                            )
                            paymentSuccess = true
                        } else {
                            Log.e(TAG, "Insufficient balance for HAZIR wallet payment!")
                        }
                    }

                    if (paymentSuccess) {
                        // Update booking rating & payment confirmation
                        val updated = booking.copy(
                            status = "COMPLETED",
                            rating = rating,
                            review = review,
                            paymentMethod = method
                        )
                        updateBookingUseCase(updated)

                        // Increment worker's completed jobs and calculate average rating
                        val worker = getUserByIdUseCase(receiverId)
                        if (worker != null) {
                            val allWorkerBookings = getBookingsForWorkerUseCase(receiverId)
                            val ratedBookings = (allWorkerBookings.filter { it.id != booking.id } + updated)
                                .filter { it.rating != null }
                            
                            val avgRating = if (ratedBookings.isNotEmpty()) {
                                ratedBookings.mapNotNull { it.rating }.average()
                            } else {
                                rating.toDouble()
                            }

                            updateUserUseCase(worker.copy(
                                completedJobs = worker.completedJobs + 1,
                                rating = Math.round(avgRating * 10.0) / 10.0
                            ))
                        }

                        val workerDispName = booking.workerName ?: "Technician"
                        val successMsg = when (method) {
                            "CASH" -> "Alhamdulillah, Cash payment of PKR ${booking.estimatedPrice} (+ PKR ${tipAmount.toInt()} tip) handed over to $workerDispName."
                            else -> "Payment of PKR ${totalAmount} (Base: ${booking.estimatedPrice} + Tip: ${tipAmount.toInt()}) completed securely via $method! Thank you for choosing Hazir."
                        }

                        sendChatMessageUseCase(
                            ChatMessage(
                                bookingId = bookingId,
                                senderId = "system",
                                senderRole = "SYSTEM",
                                message = successMsg
                            )
                        )
                    }
                } else {
                    Log.e(TAG, "User profile not found for payment!")
                }
            }
        }
    }

    fun submitPostServiceRating(bookingId: Int, rating: Int, review: String) {
        viewModelScope.launch {
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            val updated = booking.copy(
                rating = rating,
                review = review
            )
            updateBookingUseCase(updated)

            val workerId = booking.workerId
            if (workerId != null) {
                val worker = getUserByIdUseCase(workerId)
                if (worker != null) {
                    val allWorkerBookings = getBookingsForWorkerUseCase(workerId)
                    val ratedBookings = (allWorkerBookings.filter { it.id != booking.id } + updated)
                        .filter { it.rating != null }
                    
                    val avgRating = if (ratedBookings.isNotEmpty()) {
                        ratedBookings.mapNotNull { it.rating }.average()
                    } else {
                        rating.toDouble()
                    }

                    updateUserUseCase(worker.copy(
                        rating = Math.round(avgRating * 10.0) / 10.0
                    ))
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

            // Auto-reply simulation if booking is active
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch
            if (booking.status != "COMPLETED" && booking.status != "CANCELLED") {
                if (role == "CUSTOMER") {
                    simulateWorkerChatMessageReply(bookingId, text)
                } else if (role == "WORKER") {
                    simulateCustomerChatMessageReply(bookingId, text)
                }
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

                // Trigger "accepted" push notification via NotificationManager
                com.example.infrastructure.notification.NotificationManager.triggerRequestAccepted(
                    getApplication(),
                    bookingId,
                    matchedWorker.name ?: "Technician",
                    acceptedBooking.categoryName
                )
                com.example.infrastructure.notification.NotificationProvider.triggerJobAccepted(
                    getApplication(),
                    bookingId,
                    matchedWorker.name ?: "Technician",
                    acceptedBooking.categoryName
                )

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
                        if (_currentRole.value == "WORKER" && _currentUserId.value == arrivedBooking.workerId) {
                            // Yield to manual "Start Work Task" button click by the worker
                            while (getBookingByIdUseCase(bookingId)?.status == "ARRIVED") {
                                delay(1000)
                            }
                        } else {
                            // Automatically start work after delay
                            updateBookingUseCase(arrivedBooking.copy(status = "STARTED"))
                            sendChatMessageUseCase(
                                ChatMessage(
                                    bookingId = bookingId,
                                    senderId = matchedWorker.id,
                                    senderRole = "WORKER",
                                    message = "I am starting the service work now."
                                )
                            )
                        }
                    }

                    // Wait until status transitions out of ARRIVED (either manually or automatically)
                    while ((getBookingByIdUseCase(bookingId)?.status ?: "") == "ARRIVED") {
                        delay(1000)
                    }

                    delay(10000)
                    val startedBooking = getBookingByIdUseCase(bookingId) ?: return@launch
                    if (startedBooking.status == "STARTED") {
                        if (_currentRole.value == "WORKER" && _currentUserId.value == startedBooking.workerId) {
                            // Yield to manual "Complete Task" button click by the worker
                            while (getBookingByIdUseCase(bookingId)?.status == "STARTED") {
                                delay(1000)
                            }
                        } else {
                            // Automatically complete job after delay
                            updateBookingUseCase(startedBooking.copy(status = "COMPLETED"))

                            // Trigger "completed" push notification via NotificationManager
                            com.example.infrastructure.notification.NotificationManager.triggerJobCompleted(
                                getApplication(),
                                bookingId,
                                matchedWorker.name ?: "Technician",
                                startedBooking.categoryName
                            )
                            com.example.infrastructure.notification.NotificationProvider.triggerJobCompleted(
                                getApplication(),
                                bookingId,
                                matchedWorker.name ?: "Technician",
                                startedBooking.categoryName
                            )

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
            _isWorkerTyping.value = true
            delay(2500)
            _isWorkerTyping.value = false
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

    private fun simulateCustomerChatMessageReply(bookingId: Int, workerMsg: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _isCustomerTyping.value = true
            delay(2500)
            _isCustomerTyping.value = false
            val booking = getBookingByIdUseCase(bookingId) ?: return@launch

            val replyText = when {
                workerMsg.lowercase().contains("start") || workerMsg.lowercase().contains("starting") || workerMsg.lowercase().contains("work") -> {
                    "Perfect, please go ahead. Let me know if you need access to any sockets or a ladder."
                }
                workerMsg.lowercase().contains("arrive") || workerMsg.lowercase().contains("arrived") || workerMsg.lowercase().contains("outside") || workerMsg.lowercase().contains("reached") -> {
                    "Okay great, I am coming downstairs to open the door."
                }
                workerMsg.lowercase().contains("price") || workerMsg.lowercase().contains("cost") || workerMsg.lowercase().contains("pkr") || workerMsg.lowercase().contains("material") -> {
                    "Sure, please let me know how much the materials cost, and I can pay you directly in the app or cash."
                }
                workerMsg.lowercase().contains("completed") || workerMsg.lowercase().contains("finish") || workerMsg.lowercase().contains("done") -> {
                    "Alhamdulillah! Thank you. I am checking the work now and will complete the payment."
                }
                else -> {
                    "Sounds good, thank you for clarifying!"
                }
            }

            sendChatMessageUseCase(
                ChatMessage(
                    bookingId = bookingId,
                    senderId = booking.customerId,
                    senderRole = "CUSTOMER",
                    message = replyText
                )
            )
        }
    }

    fun refreshBookings(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            delay(1200)
            val currentId = _currentUserId.value
            if (currentId.isNotEmpty()) {
                _currentUserId.value = ""
                _currentUserId.value = currentId
            }
            onComplete()
        }
    }

    // ==========================================
    // SAVED ADDRESS MANAGEMENT METHODS
    // ==========================================
    fun saveAddress(label: String, address: String, isDefault: Boolean) {
        val userId = _currentUserId.value
        if (userId.isEmpty()) return
        viewModelScope.launch {
            val newAddress = SavedAddress(
                userId = userId,
                label = label,
                address = address,
                isDefault = isDefault
            )
            val id = insertSavedAddressUseCase(newAddress)
            if (isDefault) {
                setDefaultSavedAddressUseCase(userId, id)
            }
        }
    }

    fun updateAddress(savedAddress: SavedAddress) {
        viewModelScope.launch {
            if (savedAddress.isDefault) {
                setDefaultSavedAddressUseCase(savedAddress.userId, savedAddress.id)
            }
            updateSavedAddressUseCase(savedAddress)
        }
    }

    fun deleteAddress(savedAddress: SavedAddress) {
        viewModelScope.launch {
            deleteSavedAddressUseCase(savedAddress)
        }
    }

    fun setAsDefaultAddress(addressId: Int) {
        val userId = _currentUserId.value
        if (userId.isEmpty()) return
        viewModelScope.launch {
            setDefaultSavedAddressUseCase(userId, addressId)
        }
    }
}
