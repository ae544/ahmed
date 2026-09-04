package com.gymlock.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Draws a full-screen, non-dismissable overlay over every other app
 * (requires the "Display over other apps" permission). The overlay only
 * goes away when UnlockActivity confirms a photo was taken/uploaded and
 * calls LockOverlayService.stop().
 */
class LockOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val clockHandler = Handler(Looper.getMainLooper())

    companion object {
        const val CHANNEL_ID = "lock_service_channel"

        fun start(context: Context) {
            val intent = Intent(context, LockOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LockOverlayService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "قفل الجيم شغال", NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(getString(R.string.lock_title))
            .setContentText(getString(R.string.lock_sub))
            .setOngoing(true)
            .build()
        startForeground(2002, notification)
    }

    private fun showOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            // Deliberately NOT FLAG_NOT_FOCUSABLE: the overlay must capture all
            // touches/back-presses so the rest of the phone stays unusable.
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.overlay_lock, null)

        val clock = view.findViewById<android.widget.TextView>(R.id.overlayClock)
        updateClock(clock)

        view.findViewById<android.widget.Button>(R.id.btnUploadPhoto).setOnClickListener {
            // Hand off to a transparent activity that owns the camera intent,
            // since a Service can't launch the system camera app directly.
            val i = Intent(this, UnlockActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(i)
        }

        windowManager?.addView(view, params)
        overlayView = view
    }

    private fun updateClock(clock: android.widget.TextView) {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        clock.text = fmt.format(Date())
        clockHandler.postDelayed({ updateClock(clock) }, 15_000)
    }

    override fun onDestroy() {
        super.onDestroy()
        clockHandler.removeCallbacksAndMessages(null)
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
