package com.example.infrastructure.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.domain.model.ChatMessage
import com.example.infrastructure.database.HazirDatabase
import com.example.infrastructure.repository.HazirRepositoryImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

class WorkerTrackingService : Service() {

    private val CHANNEL_ID = "worker_tracking_channel"
    private val NOTIFICATION_ID = 1001

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bookingId = intent?.getIntExtra("booking_id", -1) ?: -1
        val customerLat = intent?.getDoubleExtra("customer_lat", 33.6844) ?: 33.6844
        val customerLng = intent?.getDoubleExtra("customer_lng", 73.0479) ?: 73.0479
        val workerLat = intent?.getDoubleExtra("worker_lat", 33.7100) ?: 33.7100
        val workerLng = intent?.getDoubleExtra("worker_lng", 73.0700) ?: 73.0700
        val workerName = intent?.getStringExtra("worker_name") ?: "Sajid"

        if (bookingId != -1) {
            // Start the service as a Foreground Service to comply with Android background execution limits
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Hazir Live Tracking")
                .setContentText("$workerName is heading over to your location.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .build()

            startForeground(NOTIFICATION_ID, notification)

            startTrackingSimulation(bookingId, customerLat, customerLng, workerLat, workerLng, workerName)
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startTrackingSimulation(
        bookingId: Int,
        customerLat: Double,
        customerLng: Double,
        startWorkerLat: Double,
        startWorkerLng: Double,
        workerName: String
    ) {
        serviceScope.launch {
            var currentLat = startWorkerLat
            var currentLng = startWorkerLng
            val ticks = 5
            val latDelta = (customerLat - startWorkerLat) / ticks
            val lngDelta = (customerLng - startWorkerLng) / ticks

            var alerted5Min = false

            val db = HazirDatabase.getDatabase(applicationContext)
            val repository = HazirRepositoryImpl(db)

            // Initial position update
            _workerCoordinates.value = Pair(currentLat, currentLng)

            // Trigger "worker en route" push notification alert via NotificationManager
            val initialEta = calculateEtaMins(currentLat, currentLng, customerLat, customerLng)
            com.example.infrastructure.notification.NotificationManager.triggerWorkerEnRoute(
                applicationContext,
                bookingId,
                workerName,
                initialEta
            )

            for (i in 1..ticks) {
                delay(3000)
                currentLat += latDelta
                currentLng += lngDelta

                // Expose coordinates live to Companion state flow for real-time app screens mapping
                _workerCoordinates.value = Pair(currentLat, currentLng)

                // Calculate geodesic route ETA
                val eta = calculateEtaMins(currentLat, currentLng, customerLat, customerLng)

                // Update persistent foreground notification with actual countdown details
                updateForegroundNotification(workerName, eta)

                // Trigger push notification alert if worker is within 5 minutes of service location
                if (eta <= 5 && !alerted5Min) {
                    alerted5Min = true
                    sendPushAlertNotification(bookingId, workerName, eta)
                }
            }

            // After ticks, worker has arrived
            _workerCoordinates.value = Pair(customerLat, customerLng)
            updateForegroundNotification(workerName, 0)

            // Mark arrived in repository
            try {
                val booking = repository.getBookingById(bookingId)
                if (booking != null && booking.status == "ACCEPTED") {
                    repository.updateBooking(booking.copy(status = "ARRIVED"))

                    // Add system arrival chat message
                    repository.sendMessage(
                        ChatMessage(
                            bookingId = bookingId,
                            senderId = booking.workerId ?: "worker",
                            senderRole = "WORKER",
                            message = "I have arrived at your address! Please come outside or guide me to the door."
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("WorkerTrackingService", "Error updating booking on arrival: ${e.message}")
            }

            // Wait a moment then stop service
            delay(2000)
            stopForeground(true)
            stopSelf()
        }
    }

    private fun updateForegroundNotification(workerName: String, etaMins: Int) {
        val message = if (etaMins == 0) {
            "$workerName has arrived!"
        } else {
            "$workerName is en route. Estimated arrival in $etaMins mins."
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hazir Live Tracking")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun sendPushAlertNotification(bookingId: Int, workerName: String, etaMins: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🚀 Provider Approaching!")
            .setContentText("$workerName is nearby and will arrive in $etaMins minutes!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use a unique notification ID (like bookingId + 2000) for the alert
        manager.notify(bookingId + 2000, notification)
    }

    private fun calculateEtaMins(
        workerLat: Double,
        workerLng: Double,
        customerLat: Double,
        customerLng: Double
    ): Int {
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(customerLat - workerLat)
        val dLng = Math.toRadians(customerLng - workerLng)
        val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
                cos(Math.toRadians(workerLat)) * cos(Math.toRadians(customerLat)) *
                sin(dLng / 2.0) * sin(dLng / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        val directKm = R * c

        // Add 35% extra for routing detours
        val routingKm = directKm * 1.35
        // Average city speed of 30 km/h (0.5 km per minute)
        return max(1, Math.round(routingKm / 0.5).toInt())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Worker Arrival Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when your provider is approaching your location"
                enableLights(true)
                enableVibration(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        private val _workerCoordinates = MutableStateFlow<Pair<Double, Double>?>(null)
        val workerCoordinates: StateFlow<Pair<Double, Double>?> = _workerCoordinates.asStateFlow()
    }
}
