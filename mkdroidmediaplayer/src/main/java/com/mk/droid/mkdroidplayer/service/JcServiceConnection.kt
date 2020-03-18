package com.mk.droid.mkdroidplayer.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.mk.droid.mkdroidplayer.model.MKAudio
import com.mk.droid.mkdroidplayer.service.notification.MKNotificationPlayer
import java.io.Serializable

/**
 * This class is an [ServiceConnection] for the [MKPlayerService] class.
 * Create by MKDroid on 12/03/20
 */
class JcServiceConnection(private val context: Context) : ServiceConnection {

    private var serviceBound = false
    private var onConnected: ((MKPlayerService.JcPlayerServiceBinder?) -> Unit)? = null
    private var onDisconnected: ((Unit) -> Unit)? = null

    override fun onServiceDisconnected(name: ComponentName?) {
        serviceBound = false
        onDisconnected?.invoke(Unit)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        serviceBound = true
        onConnected?.invoke(service as MKPlayerService.JcPlayerServiceBinder?)
    }

    /**
     * Connects with the [MKPlayerService].
     */
    fun connect(
        playlist: ArrayList<MKAudio>? = null,
        currentAudio: MKAudio? = null,
        onConnected: ((MKPlayerService.JcPlayerServiceBinder?) -> Unit)? = null,
        onDisconnected: ((Unit) -> Unit)? = null
    ) {
        this.onConnected = onConnected
        this.onDisconnected = onDisconnected

        if (serviceBound.not()) {
            val intent = Intent(context.applicationContext, MKPlayerService::class.java)
            intent.putExtra(MKNotificationPlayer.PLAYLIST, playlist as Serializable?)
            intent.putExtra(MKNotificationPlayer.CURRENT_AUDIO, currentAudio)
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Disconnects from the [MKPlayerService].
     */
    fun disconnect() {
        if (serviceBound)
            try {
                context.unbindService(this)
            } catch (e: IllegalArgumentException) {
                //TODO: Add readable exception here
            }
    }
}
