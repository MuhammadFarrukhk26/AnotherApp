package com.example.infrastructure.database

import com.example.domain.model.*

// ==========================================
// USER MAPPER
// ==========================================
fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    phone = phone,
    role = role,
    avatarUrl = avatarUrl,
    walletBalance = walletBalance,
    rating = rating,
    isOnline = isOnline,
    skills = skills,
    experienceYears = experienceYears,
    cnicVerified = cnicVerified,
    cnicNumber = cnicNumber,
    selfieVerified = selfieVerified,
    completedJobs = completedJobs,
    latitude = latitude,
    longitude = longitude
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    phone = phone,
    role = role,
    avatarUrl = avatarUrl,
    walletBalance = walletBalance,
    rating = rating,
    isOnline = isOnline,
    skills = skills,
    experienceYears = experienceYears,
    cnicVerified = cnicVerified,
    cnicNumber = cnicNumber,
    selfieVerified = selfieVerified,
    completedJobs = completedJobs,
    latitude = latitude,
    longitude = longitude
)

// ==========================================
// SERVICE CATEGORY MAPPER
// ==========================================
fun ServiceCategoryEntity.toDomain(): ServiceCategory = ServiceCategory(
    id = id,
    name = name,
    iconName = iconName,
    description = description,
    basePrice = basePrice
)

fun ServiceCategory.toEntity(): ServiceCategoryEntity = ServiceCategoryEntity(
    id = id,
    name = name,
    iconName = iconName,
    description = description,
    basePrice = basePrice
)

// ==========================================
// BOOKING MAPPER
// ==========================================
fun BookingEntity.toDomain(): Booking = Booking(
    id = id,
    categoryId = categoryId,
    categoryName = categoryName,
    customerId = customerId,
    customerName = customerName,
    customerPhone = customerPhone,
    workerId = workerId,
    workerName = workerName,
    workerPhone = workerPhone,
    date = date,
    time = time,
    address = address,
    latitude = latitude,
    longitude = longitude,
    description = description,
    estimatedPrice = estimatedPrice,
    status = status,
    rating = rating,
    review = review,
    beforePhoto = beforePhoto,
    afterPhoto = afterPhoto,
    paymentMethod = paymentMethod,
    createdAt = createdAt
)

fun Booking.toEntity(): BookingEntity = BookingEntity(
    id = id,
    categoryId = categoryId,
    categoryName = categoryName,
    customerId = customerId,
    customerName = customerName,
    customerPhone = customerPhone,
    workerId = workerId,
    workerName = workerName,
    workerPhone = workerPhone,
    date = date,
    time = time,
    address = address,
    latitude = latitude,
    longitude = longitude,
    description = description,
    estimatedPrice = estimatedPrice,
    status = status,
    rating = rating,
    review = review,
    beforePhoto = beforePhoto,
    afterPhoto = afterPhoto,
    paymentMethod = paymentMethod,
    createdAt = createdAt
)

// ==========================================
// CHAT MESSAGE MAPPER
// ==========================================
fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    bookingId = bookingId,
    senderId = senderId,
    senderRole = senderRole,
    message = message,
    timestamp = timestamp,
    isRead = isRead
)

fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    bookingId = bookingId,
    senderId = senderId,
    senderRole = senderRole,
    message = message,
    timestamp = timestamp,
    isRead = isRead
)

// ==========================================
// WALLET TRANSACTION MAPPER
// ==========================================
fun WalletTransactionEntity.toDomain(): WalletTransaction = WalletTransaction(
    id = id,
    userId = userId,
    amount = amount,
    type = type,
    description = description,
    timestamp = timestamp
)

fun WalletTransaction.toEntity(): WalletTransactionEntity = WalletTransactionEntity(
    id = id,
    userId = userId,
    amount = amount,
    type = type,
    description = description,
    timestamp = timestamp
)

// ==========================================
// SAVED ADDRESS MAPPER
// ==========================================
fun SavedAddressEntity.toDomain(): SavedAddress = SavedAddress(
    id = id,
    userId = userId,
    label = label,
    address = address,
    latitude = latitude,
    longitude = longitude,
    isDefault = isDefault
)

fun SavedAddress.toEntity(): SavedAddressEntity = SavedAddressEntity(
    id = id,
    userId = userId,
    label = label,
    address = address,
    latitude = latitude,
    longitude = longitude,
    isDefault = isDefault
)
