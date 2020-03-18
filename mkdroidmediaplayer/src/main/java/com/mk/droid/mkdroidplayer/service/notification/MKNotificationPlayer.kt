package com.mk.droid.mkdroidplayer.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import com.mk.droid.mkdroidplayer.MKPlayerManager
import com.mk.droid.mkdroidplayer.MKPlayerManagerListener
import com.mk.droid.mkdroidplayer.R
import com.mk.droid.mkdroidplayer.general.MKStatus
import com.mk.droid.mkdroidplayer.general.PlayerUtil
import java.lang.ref.WeakReference

/**
 * This class is a Android [Service] that handles notification changes on background.
 * Create by MKDroid on 12/03/20
 */
class MKNotificationPlayer private constructor(private val context: Context) : MKPlayerManagerListener {

    private var title: String? = null
    private var time = "00:00"
    private var iconResource: Int = 0
    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }
    private var notification: Notification? = null

    companion object {
        const val NEXT = "mkdroidplayer.NEXT"
        const val PREVIOUS = "mkdroidplayer.PREVIOUS"
        const val PAUSE = "mkdroidplayer.PAUSE"
        const val PLAY = "mkdroidplayer.PLAY"
        const val ACTION = "mkdroidplayer.ACTION"
        const val PLAYLIST = "mkdroidplayer.PLAYLIST"
        const val CURRENT_AUDIO = "mkdroidplayer.CURRENT_AUDIO"

        private const val NOTIFICATION_ID = 100
        private const val NOTIFICATION_CHANNEL = "mkdroidplayer.NOTIFICATION_CHANNEL"
        private const val NEXT_ID = 0
        private const val PREVIOUS_ID = 1
        private const val PLAY_ID = 2
        private const val PAUSE_ID = 3


        @Volatile
        private var INSTANCE: WeakReference<MKNotificationPlayer>? = null

        @JvmStatic
        fun getInstance(context: Context): WeakReference<MKNotificationPlayer> = INSTANCE ?: let {
            INSTANCE = WeakReference(MKNotificationPlayer(context))
            INSTANCE!!
        }
    }

    fun createNotificationPlayer(title: String?, iconResourceResource: Int) {
        this.title = title
        this.iconResource = iconResourceResource
        val openUi = Intent(context, context.javaClass)
        openUi.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                .setSmallIcon(iconResourceResource)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, iconResourceResource))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContent(createNotificationPlayerView())
                .setSound(null)
                .setContentIntent(PendingIntent.getActivity(context, NOTIFICATION_ID, openUi, PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(false)
                .build()

        @RequiresApi(Build.VERSION_CODES.O)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_LOW)
            channel.lockscreenVisibility = VISIBILITY_PUBLIC
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.setSound(null, null)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        notification?.let { notificationManager.notify(NOTIFICATION_ID, it) }
    }

    fun updateNotification() {
        createNotificationPlayer(title, iconResource)
    }

    private fun createNotificationPlayerView(): RemoteViews {
        val remoteView: RemoteViews

        if (MKPlayerManager.getInstance(context, null, null).get()?.isPaused() == true) {
            remoteView = RemoteViews(context.packageName, R.layout.notification_play)
            remoteView.setOnClickPendingIntent(R.id.btn_play_notification, buildPendingIntent(PLAY, PLAY_ID))
        } else {
            remoteView = RemoteViews(context.packageName, R.layout.notification_pause)
            remoteView.setOnClickPendingIntent(R.id.btn_pause_notification, buildPendingIntent(PAUSE, PAUSE_ID))
        }

        remoteView.setTextViewText(R.id.txt_current_music_notification, title)
        remoteView.setTextViewText(R.id.txt_duration_notification, time)
        remoteView.setImageViewResource(R.id.icon_player, iconResource)
        remoteView.setOnClickPendingIntent(R.id.btn_next_notification, buildPendingIntent(NEXT, NEXT_ID))
        remoteView.setOnClickPendingIntent(R.id.btn_prev_notification, buildPendingIntent(PREVIOUS, PREVIOUS_ID))

        return remoteView
    }

    private fun buildPendingIntent(action: String, id: Int): PendingIntent {
        val playIntent = Intent(context.applicationContext, MKPlayerNotificationReceiver::class.java)
        playIntent.putExtra(ACTION, action)

        return PendingIntent.getBroadcast(context.applicationContext, id, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onPreparedAudio(status: MKStatus) {

    }

    override fun onCompletedAudio() {

    }

    override fun onPaused(status: MKStatus) {
        createNotificationPlayer(title, iconResource)
    }

    override fun onStopped(status: MKStatus) {
        destroyNotificationIfExists()
    }

    override fun onContinueAudio(status: MKStatus) {}

    override fun onPlaying(status: MKStatus) {
        createNotificationPlayer(title, iconResource)
    }

    override fun onTimeChanged(status: MKStatus) {
        this.time = PlayerUtil.toTimeSongString(status.currentPosition.toInt())
        this.title = status.mkAudio.title
        createNotificationPlayer(title, iconResource)
    }


    fun destroyNotificationIfExists() {
        try {
            notificationManager.cancel(NOTIFICATION_ID)
            notificationManager.cancelAll()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    override fun onMKpError(throwable: Throwable) {

    }
}