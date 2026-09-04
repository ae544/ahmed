package com.gymlock.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Fires every day at 13:00. Doesn't lock anything by itself — it just asks
 * the person to open the app and confirm/adjust which 3 days a week and
 * what time the phone should lock for a gym photo.
 */
class DailyPromptReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "daily_prompt"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "تأكيد ميعاد الجيم", NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("openFromDailyPrompt", true)
        }
        val pi = PendingIntent.getActivity(
            context, 1, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("حدد أيام الجيم لِلأسبوع ده")
            .setContentText("اختار الـ3 أيام ومعاد التذكير اللي هيقفل موبايلك لحد ما ترفع صورة")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        nm.notify(3003, notification)

        // Re-arm itself for tomorrow at 13:00.
        AlarmScheduler.scheduleDailyPrompt(context)
    }
}
