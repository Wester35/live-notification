package com.wester.tasl1.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.math.BigInteger
import kotlin.coroutines.coroutineContext
import kotlin.math.max

class FibonacciService : Service() {
    companion object {
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val CHANNEL_ID = "fibonacci_channel"
        const val NOTIFICATION_ID = 100
        var RESULT = mutableStateOf("-1")
    }
    private var currentProgress = 0
    private var calculationJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun calculateFibonacci(number: Int) {
        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification(
                "Вычисление F($number)",
                "Запуск"
            )
        )

        calculationJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                RESULT.value = calculateFibonacciWithProgress(number)
                updateNotification("Вычисление F($number)",
                    "Результат в интерфейсе")
            } catch (e: CancellationException) {
                updateNotification("Вычисление отменено", e.message.toString())
            } finally {
                delay(3000)
                stopSelf()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    @SuppressLint("DefaultLocale")
    private suspend fun calculateFibonacciWithProgress(n: Int): String {
        if (n <= 2) return "1"

        var a = BigInteger.ONE
        var b = BigInteger.ONE

        val updateStep = max(1, n / 100)
        var lastUpdateIteration = 0

        for (i in 3..n) {
            if (coroutineContext.isActive.not()) {
                throw CancellationException("Вычисление отменено")
            }

            val next = a + b
            a = b
            b = next

            if (i - lastUpdateIteration >= updateStep || i == n) {
                lastUpdateIteration = i

                currentProgress = (i * 100 / n)

                updateNotification(
                    "Вычисление F($n)",
                    "Прогресс: $currentProgress"
                )

                if (n > 10000) yield()
            }
        }

        return b.toString()
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_START -> {
                val number = intent.getIntExtra("number", 1)
                calculateFibonacci(number)
            }
            ACTION_STOP -> {
                calculationJob?.cancel()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        calculationJob?.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun updateNotification(title: String, message: String) {
        getSystemService(
            NotificationManager::class.java
        ).notify(
            NOTIFICATION_ID,
            createNotification(title, message)
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Fibonacci Live",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            enableLights(true)
            enableVibration(false)
        }

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun createNotification(title: String, message: String): Notification {
        return if (Build.VERSION.SDK_INT >= 36) {
            val builder = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setShortCriticalText("$currentProgress%")
                .setColorized(false)
                .setProgress(100, currentProgress, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.LTGRAY)

            val extras = Bundle()
            extras.putBoolean("android.extra.REQUEST_PROMOTED_ONGOING", true)
            builder.addExtras(extras)

            val notification = builder.build()
            return notification

        } else {
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setProgress(100, currentProgress, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.LTGRAY)
                .build()
        }
    }
}