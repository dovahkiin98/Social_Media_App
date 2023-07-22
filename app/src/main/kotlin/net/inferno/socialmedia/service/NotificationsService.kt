package net.inferno.socialmedia.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.inferno.socialmedia.R
import net.inferno.socialmedia.data.Repository
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsService : Service() {
    @Inject
    lateinit var repository: Repository

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()

        startForeground(
            0xAB,
            NotificationCompat.Builder(applicationContext, "service")
                .setContentText("Notifications")
                .setSilent(true)
                .setGroup("service")
                .build()
        )
    }

    private fun createNotificationChannels() {
        val notificationManager = NotificationManagerCompat.from(this)

        if (notificationManager.getNotificationChannel("service") == null) {
            notificationManager.createNotificationChannel(
                NotificationChannelCompat.Builder(
                    "service",
                    NotificationManagerCompat.IMPORTANCE_LOW,
                ).setName("service")
                    .setVibrationEnabled(false)
                    .build()
            )
        }

        if (notificationManager.getNotificationChannel("post") == null) {
            notificationManager.createNotificationChannel(
                NotificationChannelCompat.Builder(
                    "post",
                    NotificationManagerCompat.IMPORTANCE_DEFAULT,
                ).setName(getString(R.string.notification_channel_posts))
                    .build()
            )
        }

        if (notificationManager.getNotificationChannel("comment") == null) {
            notificationManager.createNotificationChannel(
                NotificationChannelCompat.Builder(
                    "comment",
                    NotificationManagerCompat.IMPORTANCE_DEFAULT,
                )
                    .setName(getString(R.string.notification_channel_comments))
                    .build()
            )
        }

        if (notificationManager.getNotificationChannel("community") == null) {
            notificationManager.createNotificationChannel(
                NotificationChannelCompat.Builder(
                    "community",
                    NotificationManagerCompat.IMPORTANCE_DEFAULT,
                )
                    .setName(getString(R.string.notification_channel_communities)).build()
            )
        }

        if (notificationManager.getNotificationChannel("user") == null) {
            notificationManager.createNotificationChannel(
                NotificationChannelCompat.Builder(
                    "user",
                    NotificationManagerCompat.IMPORTANCE_DEFAULT,
                )
                    .setName(getString(R.string.notification_channel_users)).build()
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    delay(20_000)

                    continue
                }

                try {
                    val notifications = repository.getNotifications()

                    val notificationManager = NotificationManagerCompat.from(applicationContext)

                    notifications.forEach {
                        val notificationBuilder = NotificationCompat.Builder(
                            applicationContext,
                            it.subject.model.lowercase()
                        ).setSmallIcon(R.drawable.ic_notification)

                        when (it.subject.model) {
                            "Post" -> {
                                notificationBuilder.setGroup("posts")

                                when (it.subject.action) {
                                    "likePost" -> {
                                        notificationBuilder.setContentText("User liked your post")
                                    }

                                    "dislikePost" -> {
                                        notificationBuilder.setContentText("User disliked your post")
                                    }

                                    "createComment" -> {
                                        notificationBuilder.setContentText("User commented on your post")
                                    }

                                    else -> {
                                        notificationBuilder.addExtras(bundleOf("show" to false))
                                    }
                                }
                            }

                            "Comment" -> {

                            }

                            "User" -> {

                            }

                            "Community" -> {

                            }
                        }

                        if (ContextCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val notification = notificationBuilder.build()

                            if (notification.extras.getBoolean("show", true)) {
                                notificationManager.notify(it.hashCode(), notification)
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main.immediate) {
                        Toast.makeText(
                            applicationContext,
                            e.message,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

                delay(5_000)
            }
        }

        return START_STICKY
    }
}