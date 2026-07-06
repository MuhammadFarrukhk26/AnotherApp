package com.example.infrastructure.notification

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

/**
 * Modern notification provider for the Hazir application.
 * Handles triggering high-priority push notifications for critical user journeys:
 * - When a professional accepts a job.
 * - When a professional is en route.
 * - When a professional is arriving (ETA is less than 5 minutes or they have arrived).
 * - When a job is completed.
 */
object NotificationProvider {
    private const val TAG = "NotificationProvider"
    private const val CHANNEL_ID = "hazir_push_notifications_channel"
    private const val CHANNEL_NAME = "Hazir Service Alerts"
    private const val CHANNEL_DESC = "Real-time alerts for booking status, worker arrivals, and chat messages"

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            manager.createNotificationChannel(channel)
            Log.d(TAG, "NotificationProvider channel initialized.")
        }
    }

    private fun sendPushNotification(context: Context, id: Int, title: String, message: String) {
        try {
            init(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("booking_id", id)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // System standard info icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            manager.notify(id, notification)
            Log.d(TAG, "Push alert notification sent. ID: $id, Title: $title")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send push notification: ${e.message}", e)
        }
    }

    /**
     * Triggered when a professional accepts a job request.
     */
    fun triggerJobAccepted(context: Context, bookingId: Int, workerName: String, serviceName: String) {
        val title = "✅ Job Accepted: $serviceName"
        val message = "$workerName has accepted your request. They are preparing to assist you."
        sendPushNotification(context, bookingId + 10000, title, message)
    }

    /**
     * Triggered when a professional is en route.
     */
    fun triggerWorkerEnRoute(context: Context, bookingId: Int, workerName: String, etaMins: Int) {
        val title = "🚀 Technician En Route"
        val message = "$workerName is heading to your location. Estimated arrival in $etaMins minutes."
        sendPushNotification(context, bookingId + 11000, title, message)
    }

    /**
     * Triggered when a professional is arriving (less than 5 mins away or near location).
     */
    fun triggerWorkerArriving(context: Context, bookingId: Int, workerName: String, etaMins: Int) {
        val title = "📍 Technician Arriving Soon"
        val message = "$workerName is arriving shortly! Estimated arrival in $etaMins minutes."
        sendPushNotification(context, bookingId + 12000, title, message)
    }

    /**
     * Triggered when a professional has arrived at the user's service address.
     */
    fun triggerWorkerArrived(context: Context, bookingId: Int, workerName: String) {
        val title = "⚡ Technician Arrived"
        val message = "$workerName has arrived at your address! Please meet them or guide them to the door."
        sendPushNotification(context, bookingId + 13000, title, message)
    }

    /**
     * Triggered when the job has been completed by the professional.
     */
    fun triggerJobCompleted(context: Context, bookingId: Int, workerName: String, serviceName: String) {
        val title = "🎉 Service Completed!"
        val message = "The $serviceName service by $workerName is finished. Tap to review and pay."
        sendPushNotification(context, bookingId + 14000, title, message)
    }
}
