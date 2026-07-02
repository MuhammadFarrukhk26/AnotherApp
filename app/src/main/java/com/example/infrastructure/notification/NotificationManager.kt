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
 * Handles posting of local notifications to alert users about key service milestones:
 * - Service request accepted
 * - Worker en route
 * - Job completed
 */
object NotificationManager {
    private const val TAG = "NotificationManager"
    private const val CHANNEL_ID = "hazir_notifications_channel"
    private const val CHANNEL_NAME = "Hazir Service Updates"
    private const val CHANNEL_DESC = "Notifications for booking status updates, worker arrivals, and completions"

    /**
     * Creates the notification channel for Android O and above.
     */
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
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel initialized.")
        }
    }

    /**
     * Sends a push alert notification using Android's system NotificationManager.
     */
    private fun sendNotification(context: Context, id: Int, title: String, message: String) {
        try {
            // Ensure channel is initialized
            init(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            manager.notify(id, notification)
            Log.d(TAG, "Notification posted successfully. ID: $id, Title: $title")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}", e)
        }
    }

    /**
     * Triggers a notification alerting the user that their request has been accepted.
     */
    fun triggerRequestAccepted(context: Context, bookingId: Int, workerName: String, categoryName: String) {
        val title = "✅ Service Request Accepted"
        val message = "Great news! $workerName has accepted your request for $categoryName and is preparing to assist you."
        sendNotification(context, bookingId + 3000, title, message)
    }

    /**
     * Triggers a notification alerting the user that the worker is en route.
     */
    fun triggerWorkerEnRoute(context: Context, bookingId: Int, workerName: String, etaMins: Int) {
        val title = "🚀 Provider En Route"
        val message = if (etaMins > 0) {
            "$workerName is heading to your location. Estimated arrival in $etaMins minutes."
        } else {
            "$workerName has started transit to your location."
        }
        sendNotification(context, bookingId + 4000, title, message)
    }

    /**
     * Triggers a notification alerting the user that the service job is completed.
     */
    fun triggerJobCompleted(context: Context, bookingId: Int, workerName: String, categoryName: String) {
        val title = "🎉 Service Job Completed"
        val message = "Alhamdulillah! The $categoryName service by $workerName is finished. Tap to review and pay."
        sendNotification(context, bookingId + 5000, title, message)
    }
}
