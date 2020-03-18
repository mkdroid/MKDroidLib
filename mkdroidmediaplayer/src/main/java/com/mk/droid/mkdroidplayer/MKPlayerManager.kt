package com.mk.droid.mkdroidplayer

import android.content.Context
import com.mk.droid.mkdroidplayer.general.MKStatus
import com.mk.droid.mkdroidplayer.general.errors.AudioListNullPointerException
import com.mk.droid.mkdroidplayer.general.errors.MKPServiceDisconnectedError
import com.mk.droid.mkdroidplayer.model.MKAudio
import com.mk.droid.mkdroidplayer.service.MKPlayerService
import com.mk.droid.mkdroidplayer.service.JcPlayerServiceListener
import com.mk.droid.mkdroidplayer.service.JcServiceConnection
import com.mk.droid.mkdroidplayer.service.notification.MKNotificationPlayer
import java.lang.ref.WeakReference
import java.util.*

/**
 * This class is the player manager. Handles all interactions and communicates with [MKPlayerService].
 * Create by MKDroid on 12/03/20
 */
class MKPlayerManager
private constructor(private val serviceConnection: JcServiceConnection) : JcPlayerServiceListener {

    lateinit var context: Context
    private var mkNotificationPlayer: MKNotificationPlayer? = null
    private var MKPlayerService: MKPlayerService? = null
    private var serviceBound = false
    var playlist: ArrayList<MKAudio> = ArrayList()
    private var currentPositionList: Int = 0
    private val managerListeners: ArrayList<MKPlayerManagerListener> = ArrayList()

    var MKPlayerManagerListener: MKPlayerManagerListener? = null
        set(value) {
            value?.let { managerListeners.add(it) }
        }

    val currentAudio: MKAudio?
        get() = MKPlayerService?.currentAudio

    var onShuffleMode: Boolean = false

    var repeatPlaylist: Boolean = false
        private set

    var repeatCurrAudio: Boolean = false
        private set

    private var repeatCount = 0

    init {
        initService()
    }

    companion object {

        @Volatile
        private var INSTANCE: WeakReference<MKPlayerManager>? = null

        @JvmStatic
        fun getInstance(
            context: Context,
            playlist: ArrayList<MKAudio>? = null,
            listener: MKPlayerManagerListener? = null
        ): WeakReference<MKPlayerManager> = INSTANCE ?: let {
            INSTANCE = WeakReference(
                    MKPlayerManager(JcServiceConnection(context)).also {
                        it.context = context
                        it.playlist = playlist ?: ArrayList()
                        it.MKPlayerManagerListener = listener
                    }
            )
            INSTANCE!!
        }
    }

    /**
     * Connects with audio service.
     */
    private fun initService(connectionListener: ((service: MKPlayerService?) -> Unit)? = null) =
            serviceConnection.connect(
                    playlist = playlist,
                    onConnected = { binder ->
                        MKPlayerService = binder?.service.also { service ->
                            serviceBound = true
                            connectionListener?.invoke(service)
                        } ?: throw MKPServiceDisconnectedError
                    },
                    onDisconnected = {
                        serviceBound = false
                        throw  MKPServiceDisconnectedError
                    }
            )

    /**
     * Plays the given [MKAudio].
     * @param MKAudio The audio to be played.
     */
    @Throws(AudioListNullPointerException::class)
    fun playAudio(MKAudio: MKAudio) {
        if (playlist.isEmpty()) {
            notifyError(AudioListNullPointerException())
        } else {
            MKPlayerService?.let { service ->
                service.serviceListener = this
                service.play(MKAudio)
            } ?: initService { service ->
                MKPlayerService = service
                playAudio(MKAudio)
            }
        }
    }


    /**
     * Goes to next audio.
     */
    @Throws(AudioListNullPointerException::class)
    fun nextAudio() {
        if (playlist.isEmpty()) {
            throw AudioListNullPointerException()
        } else {
            MKPlayerService?.let { service ->
                if (repeatCurrAudio) {
                    currentAudio?.let {
                        service.seekTo(0)
                        service.onPrepared(service.getMediaPlayer()!!)
                    }
                } else {
                    service.stop()
                    getNextAudio()?.let { service.play(it) } ?: service.finalize()
                }
            }
        }
    }

    /**
     * Goes to previous audio.
     */
    @Throws(AudioListNullPointerException::class)
    fun previousAudio() {
        if (playlist.isEmpty()) {
            throw AudioListNullPointerException()
        } else {
            MKPlayerService?.let { service ->
                if (repeatCurrAudio) {
                    currentAudio?.let {
                        service.seekTo(0)
                        service.onPrepared(service.getMediaPlayer()!!)
                    }
                } else {
                    service.stop()
                    getPreviousAudio()?.let { service.play(it) }
                }
            }
        }
    }

    /**
     * Pauses the current audio.
     */
    fun pauseAudio() {
        MKPlayerService?.let { service -> currentAudio?.let { service.pause(it) } }
    }

    /**
     * Continues the stopped audio.
     */
    @Throws(AudioListNullPointerException::class)
    fun continueAudio() {
        if (playlist.isEmpty()) {
            throw AudioListNullPointerException()
        } else {
            val audio = MKPlayerService?.currentAudio?.let { it } ?: let { playlist.first() }
            playAudio(audio)
        }
    }

    /**
     * Creates a new notification with icon resource.
     * @param iconResource The icon resource path.
     */
    fun createNewNotification(iconResource: Int) {
        mkNotificationPlayer
                ?.createNotificationPlayer(currentAudio?.title, iconResource)
                ?: let {
                    mkNotificationPlayer = MKNotificationPlayer
                            .getInstance(context)
                            .get()
                            .also { notification ->
                                MKPlayerManagerListener = notification
                            }

                    createNewNotification(iconResource)
                }
    }

    /**
     * Updates the current notification
     */
    fun updateNotification() {
        mkNotificationPlayer
                ?.updateNotification()
                ?: let {
                    mkNotificationPlayer = MKNotificationPlayer
                            .getInstance(context)
                            .get()
                            .also { MKPlayerManagerListener = it }

                    updateNotification()
                }
    }

    /**
     * Jumps audio to the specific time.
     */
    fun seekTo(time: Int) {
        MKPlayerService?.seekTo(time)
    }


    private fun getNextAudio(): MKAudio? {
        return if (onShuffleMode) {
            playlist[Random().nextInt(playlist.size)]
        } else {
            try {
                playlist[currentPositionList.inc()]
            } catch (e: IndexOutOfBoundsException) {
                if (repeatPlaylist) {
                    return playlist.first()
                }

                null
            }
        }
    }

    private fun getPreviousAudio(): MKAudio? {
        return if (onShuffleMode) {
            playlist[Random().nextInt(playlist.size)]
        } else {
            try {
                playlist[currentPositionList.dec()]

            } catch (e: IndexOutOfBoundsException) {
                return playlist.first()
            }
        }
    }


    override fun onPreparedListener(status: MKStatus) {
        updatePositionAudioList()

        for (listener in managerListeners) {
            listener.onPreparedAudio(status)
        }
    }

    override fun onTimeChangedListener(status: MKStatus) {
        for (listener in managerListeners) {
            listener.onTimeChanged(status)

            if (status.currentPosition in 1..2 /* Not to call this every second */) {
                listener.onPlaying(status)
            }
        }
    }

    override fun onContinueListener(status: MKStatus) {
        for (listener in managerListeners) {
            listener.onContinueAudio(status)
        }
    }

    override fun onCompletedListener() {
        for (listener in managerListeners) {
            listener.onCompletedAudio()
        }
    }

    override fun onPausedListener(status: MKStatus) {
        for (listener in managerListeners) {
            listener.onPaused(status)
        }
    }

    override fun onStoppedListener(status: MKStatus) {
        for (listener in managerListeners) {
            listener.onStopped(status)
        }
    }

    override fun onError(exception: Exception) {
        notifyError(exception)
    }

    /**
     * Notifies errors for the service listeners
     */
    private fun notifyError(throwable: Throwable) {
        for (listener in managerListeners) {
            listener.onMKpError(throwable)
        }
    }

    /**
     * Handles the repeat mode.
     */
    fun activeRepeat() {
        if (repeatCount == 0) {
            repeatPlaylist = true
            repeatCurrAudio = false
            repeatCount++
            return
        }

        if (repeatCount == 1) {
            repeatCurrAudio = true
            repeatPlaylist = false
            repeatCount++
            return
        }

        if (repeatCount == 2) {
            repeatCurrAudio = false
            repeatPlaylist = false
            repeatCount = 0
        }
    }

    /**
     * Updates the current position of the audio list.
     */
    private fun updatePositionAudioList() {
        playlist.indices
                .singleOrNull { playlist[it] == currentAudio }
                ?.let { this.currentPositionList = it }
                ?: let { this.currentPositionList = 0 }
    }

    fun isPlaying(): Boolean {
        return MKPlayerService?.isPlaying ?: false
    }

    fun isPaused(): Boolean {
        return MKPlayerService?.isPaused ?: true
    }

    /**
     * Kills the JcPlayer, including Notification and service.
     */
    fun kill() {
        MKPlayerService?.let {
            it.stop()
            it.onDestroy()
        }

        serviceConnection.disconnect()
        mkNotificationPlayer?.destroyNotificationIfExists()
        managerListeners.clear()
        INSTANCE = null
    }
}
