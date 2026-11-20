
package com.example.waterme.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.waterme.MainActivity
import com.example.waterme.R
import android.annotation.SuppressLint
import kotlin.jvm.java

class WaterReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {

        val plantName = inputData.getString(nameKey)
            ?: return Result.failure()

        createNotificationChannel()

        // Kiểm tra quyền thông báo trên Android 13 trở lên
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        // Mở ứng dụng khi nhấn vào thông báo
        val intent = Intent(
            applicationContext,
            MainActivity::class.java
        )

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            plantName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // Tạo thông báo
        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Water Me!")
            .setContentText("It's time to water your $plantName!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Don't forget to water your $plantName today!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(plantName.hashCode(), notification)

        return Result.success()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Water Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for watering plants"
            }

            val manager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val nameKey = "PLANT_NAME"
        private const val CHANNEL_ID = "WATER_REMINDER_CHANNEL"
    }
}

