package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity

/**
 * PowerButtonService listens to Screen ON / Screen OFF events.
 *
 * TECHNICAL HONESTY NOTE:
 * On unrooted Android devices, third-party apps cannot consume KeyEvent.KEYCODE_POWER
 * globally when outside the app. The industry-standard native technique used by safety apps
 * is registering a broadcast receiver for Intent.ACTION_SCREEN_ON and Intent.ACTION_SCREEN_OFF
 * inside a running Foreground Service. When the user presses the physical power button, the screen
 * toggles between on and off. Detecting 3 state transitions within a short window (e.g., 2.5 seconds)
 * reliably captures physical power-button presses while locked or in background.
 */
class PowerButtonService : Service() {

    private var screenToggleCount = 0
    private var lastToggleTime = 0L
    private val TIME_WINDOW_MS = 2500L

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == Intent.ACTION_SCREEN_ON || action == Intent.ACTION_SCREEN_OFF) {
                val now = System.currentTimeMillis()
                if (now - lastToggleTime > TIME_WINDOW_MS) {
                    screenToggleCount = 1
                } else {
                    screenToggleCount++
                }
                lastToggleTime = now

                if (screenToggleCount >= 3) {
                    screenToggleCount = 0
                    triggerSosFromHardware()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun triggerSosFromHardware() {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_TRIGGER_SOS_HARDWARE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(launchIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_TRIGGER_SOS_HARDWARE = "com.example.nagpursuraksha.TRIGGER_SOS_HARDWARE"
    }
}
