package com.example.infrastructure.database

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ==========================================
// 1. ROOM ENTITIES
// ==========================================

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
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
    val latitude: Double = 33.6844, // Islamabad central coordinate
    val longitude: Double = 73.0479
)

@Entity(tableName = "service_categories")
data class ServiceCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String,
    val description: String,
    val basePrice: Double
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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
    val paymentMethod: String = "CASH",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookingId: Int,
    val senderId: String,
    val senderRole: String, // "CUSTOMER", "WORKER", "SYSTEM"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val amount: Double,
    val type: String, // "DEPOSIT", "WITHDRAW", "PAYMENT_OUT", "EARNING_IN"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// 2. DATA ACCESS OBJECTS (DAOs)
// ==========================================

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = 'WORKER'")
    fun getAllWorkersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'WORKER' AND skills LIKE '%' || :category || '%' AND isOnline = 1")
    suspend fun getAvailableWorkersByCategory(category: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET walletBalance = walletBalance + :amount WHERE id = :userId")
    suspend fun updateWalletBalance(userId: String, amount: Double)

    @Query("UPDATE users SET isOnline = :isOnline WHERE id = :userId")
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean)

    @Query("SELECT * FROM users WHERE role = 'WORKER' AND cnicVerified = 0")
    fun getPendingVerificationWorkersFlow(): Flow<List<UserEntity>>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM service_categories")
    fun getAllCategoriesFlow(): Flow<List<ServiceCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ServiceCategoryEntity>)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookingsFlow(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getCustomerBookingsFlow(customerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE workerId = :workerId ORDER BY createdAt DESC")
    fun getWorkerBookingsFlow(workerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: Int): BookingEntity?

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun getBookingByIdFlow(id: Int): Flow<BookingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    suspend fun updateBooking(booking: BookingEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE bookingId = :bookingId ORDER BY timestamp ASC")
    fun getMessagesForBookingFlow(bookingId: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE bookingId = :bookingId AND senderId != :currentUserId")
    suspend fun markMessagesAsRead(bookingId: Int, currentUserId: String)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUserFlow(userId: String): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)
}

// ==========================================
// 3. ROOM DATABASE CLASS
// ==========================================

@Database(
    entities = [
        UserEntity::class,
        ServiceCategoryEntity::class,
        BookingEntity::class,
        ChatMessageEntity::class,
        WalletTransactionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HazirDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bookingDao(): BookingDao
    abstract fun chatDao(): ChatDao
    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var INSTANCE: HazirDatabase? = null

        fun getDatabase(context: Context): HazirDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        HazirDatabase::class.java,
                        "hazir_database"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabasePrepopulationCallback(context))
                    .build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    Log.e("HazirDatabase", "Database initialization failed, attempting automatic recovery", e)
                    try {
                        context.deleteDatabase("hazir_database")
                    } catch (ex: Exception) {
                        Log.e("HazirDatabase", "Failed to delete corrupted database", ex)
                    }
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        HazirDatabase::class.java,
                        "hazir_database"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabasePrepopulationCallback(context))
                    .build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }

    private class DatabasePrepopulationCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                seedDatabase(database)
            }
        }

        private suspend fun seedDatabase(db: HazirDatabase) {
            // 1. Seed Categories
            val categories = listOf(
                ServiceCategoryEntity("electrician", "Electrician", "bolt", "Fix wiring, replace sockets, install fans & lights safely.", 450.0),
                ServiceCategoryEntity("plumber", "Plumber", "plumbing", "Leakage repair, pipe installation, water pump maintenance.", 500.0),
                ServiceCategoryEntity("ac_technician", "AC Technician", "ac_unit", "AC cleaning, gas refilling, cooling diagnostics.", 800.0),
                ServiceCategoryEntity("cleaner", "Home Cleaner", "cleaning_services", "Full house sweeping, sanitization, deep kitchen/bath wash.", 1200.0),
                ServiceCategoryEntity("painter", "Professional Painter", "format_paint", "Wall paint touchups, full room decoration, wallpaper.", 1500.0),
                ServiceCategoryEntity("carpenter", "Carpenter", "construction", "Furniture repair, locks, wooden wardrobes, doors.", 600.0),
                ServiceCategoryEntity("mechanic", "Car/Bike Mechanic", "build", "On-road vehicle checkup, sparkplug, filter, oil change.", 1000.0),
                ServiceCategoryEntity("mover", "Mover & Packer", "local_shipping", "Luggage lifting, safe transit, furniture dismantling.", 2500.0)
            )
            db.categoryDao().insertCategories(categories)

            // 2. Seed Default Users
            // Customer (with 5000 PKR wallet)
            val customer = UserEntity(
                id = "customer_1",
                name = "Haris Mahmood",
                phone = "0300-1234567",
                role = "CUSTOMER",
                avatarUrl = "avatar_haris",
                walletBalance = 5000.0,
                latitude = 33.6844,
                longitude = 73.0479
            )
            db.userDao().insertUser(customer)

            // Admin
            val admin = UserEntity(
                id = "admin_1",
                name = "Ayesha Malik",
                phone = "0321-7654321",
                role = "ADMIN",
                avatarUrl = "avatar_ayesha",
                walletBalance = 125000.0,
                latitude = 33.6844,
                longitude = 73.0479
            )
            db.userDao().insertUser(admin)

            // Seed Local Workers
            val workers = listOf(
                UserEntity(
                    id = "worker_electrician",
                    name = "Sajid Qureshi",
                    phone = "0312-5551111",
                    role = "WORKER",
                    avatarUrl = "avatar_sajid",
                    walletBalance = 1500.0,
                    rating = 4.8,
                    isOnline = true,
                    skills = "Electrician, AC Technician",
                    experienceYears = 8,
                    cnicVerified = true,
                    cnicNumber = "37405-1234567-1",
                    selfieVerified = true,
                    completedJobs = 142,
                    latitude = 33.6905,
                    longitude = 73.0550
                ),
                UserEntity(
                    id = "worker_plumber",
                    name = "Amir Shah",
                    phone = "0333-8882222",
                    role = "WORKER",
                    avatarUrl = "avatar_amir",
                    walletBalance = 850.0,
                    rating = 4.9,
                    isOnline = true,
                    skills = "Plumber, Carpenter",
                    experienceYears = 6,
                    cnicVerified = true,
                    cnicNumber = "37405-7654321-3",
                    selfieVerified = true,
                    completedJobs = 89,
                    latitude = 33.6750,
                    longitude = 73.0400
                ),
                UserEntity(
                    id = "worker_cleaner",
                    name = "Zainab Bibi",
                    phone = "0345-3334444",
                    role = "WORKER",
                    avatarUrl = "avatar_zainab",
                    walletBalance = 2100.0,
                    rating = 4.7,
                    isOnline = true,
                    skills = "Home Cleaner",
                    experienceYears = 5,
                    cnicVerified = true,
                    cnicNumber = "37405-9998887-2",
                    selfieVerified = true,
                    completedJobs = 110,
                    latitude = 33.6810,
                    longitude = 73.0600
                ),
                UserEntity(
                    id = "worker_painter",
                    name = "Imran Khan",
                    phone = "0321-4445555",
                    role = "WORKER",
                    avatarUrl = "avatar_imran",
                    walletBalance = 4500.0,
                    rating = 4.6,
                    isOnline = false,
                    skills = "Professional Painter",
                    experienceYears = 12,
                    cnicVerified = true,
                    cnicNumber = "35201-1112223-9",
                    selfieVerified = true,
                    completedJobs = 205,
                    latitude = 33.6950,
                    longitude = 73.0300
                ),
                UserEntity(
                    id = "worker_mover",
                    name = "Bilal Ahmed",
                    phone = "0311-9990000",
                    role = "WORKER",
                    avatarUrl = "avatar_bilal",
                    walletBalance = 0.0,
                    rating = 5.0,
                    isOnline = true,
                    skills = "Mover & Packer, Carpenter",
                    experienceYears = 4,
                    cnicVerified = false,
                    cnicNumber = "33102-4444444-5",
                    selfieVerified = true,
                    completedJobs = 15,
                    latitude = 33.7000,
                    longitude = 73.0500
                )
            )

            for (worker in workers) {
                db.userDao().insertUser(worker)
            }

            // Seed initial wallet deposit transactions for Customer
            db.walletDao().insertTransaction(
                WalletTransactionEntity(
                    userId = "customer_1",
                    amount = 5000.0,
                    type = "DEPOSIT",
                    description = "Welcome Bonus Deposit"
                )
            )
        }
    }
}
