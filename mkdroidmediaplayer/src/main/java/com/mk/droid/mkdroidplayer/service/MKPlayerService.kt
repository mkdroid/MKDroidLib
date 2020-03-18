package com.mk.droid.mkdroidplayer.service

import android.app.Service
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.mk.droid.mkdroidplayer.general.MKStatus
import com.mk.droid.mkdroidplayer.general.Origin
import com.mk.droid.mkdroidplayer.general.errors.AudioAssetsInvalidException
import com.mk.droid.mkdroidplayer.general.errors.AudioFilePathInvalidException
import com.mk.droid.mkdroidplayer.general.errors.AudioRawInvalidException
import com.mk.droid.mkdroidplayer.general.errors.AudioUrlInvalidException
import com.mk.droid.mkdroidplayer.model.MKAudio
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * This class is an Android [Service] that handles all player changes on background.
 * Create by MKDroid on 12/03/20
 */
class MKPlayerService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener {

    private val binder = JcPlayerServiceBinder()

    private var mediaPlayer: MediaPlayer? = null

    var isPlaying: Boolean = false
        private set

    var isPaused: Boolean = true
        private set

    var currentAudio: MKAudio? = null
        private set

    private val jcStatus = MKStatus()

    private var assetFileDescriptor: AssetFileDescriptor? = null // For Asset and Raw file.

    var serviceListener: JcPlayerServiceListener? = null


    inner class JcPlayerServiceBinder : Binder() {
        val service: MKPlayerService
            get() = this@MKPlayerService
    }

    override fun onBind(intent: Intent): IBinder? = binder


    fun play(MKAudio: MKAudio): MKStatus {
        val tempJcAudio = currentAudio
        currentAudio = MKAudio
        var status = MKStatus()

        if (isAudioFileValid(MKAudio.path, MKAudio.origin)) {
            try {
                mediaPlayer?.let {
                    if (isPlaying) {
                        stop()
                        play(MKAudio)
                    } else {
                        if (tempJcAudio !== MKAudio) {
                            stop()
                            play(MKAudio)
                        } else {
                            status = updateStatus(MKAudio, MKStatus.PlayState.CONTINUE)
                            updateTime()
                            serviceListener?.onContinueListener(status)
                        }
                    }
                } ?: let {
                    mediaPlayer = MediaPlayer().also {
                        when {
                            MKAudio.origin == Origin.URL -> it.setDataSource(MKAudio.path)
                            MKAudio.origin == Origin.RAW -> assetFileDescriptor =
                                    applicationContext.resources.openRawResourceFd(
                                            Integer.parseInt(MKAudio.path)
                                    ).also { descriptor ->
                                        it.setDataSource(
                                                descriptor.fileDescriptor,
                                                descriptor.startOffset,
                                                descriptor.length
                                        )
                                        descriptor.close()
                                        assetFileDescriptor = null
                                    }


                            MKAudio.origin == Origin.ASSETS -> {
                                assetFileDescriptor = applicationContext.assets.openFd(MKAudio.path)
                                        .also { descriptor ->
                                            it.setDataSource(
                                                    descriptor.fileDescriptor,
                                                    descriptor.startOffset,
                                                    descriptor.length
                                            )

                                            descriptor.close()
                                            assetFileDescriptor = null
                                        }
                            }

                            MKAudio.origin == Origin.FILE_PATH ->
                                it.setDataSource(applicationContext, Uri.parse(MKAudio.path))
                        }

                        it.prepareAsync()
                        it.setOnPreparedListener(this)
                        it.setOnBufferingUpdateListener(this)
                        it.setOnCompletionListener(this)
                        it.setOnErrorListener(this)

                        status = updateStatus(MKAudio, MKStatus.PlayState.PREPARING)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            throwError(MKAudio.path, MKAudio.origin)
        }

        return status
    }

    fun pause(MKAudio: MKAudio): MKStatus {
        val status = updateStatus(MKAudio, MKStatus.PlayState.PAUSE)
        serviceListener?.onPausedListener(status)
        return status
    }

    fun stop(): MKStatus {
        val status = updateStatus(status = MKStatus.PlayState.STOP)
        serviceListener?.onStoppedListener(status)
        return status
    }


    fun seekTo(time: Int) {
        mediaPlayer?.seekTo(time)
    }

    override fun onBufferingUpdate(mediaPlayer: MediaPlayer, i: Int) {}

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        serviceListener?.onCompletedListener()
    }

    override fun onError(mediaPlayer: MediaPlayer, i: Int, i1: Int): Boolean {
        return false
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        this.mediaPlayer = mediaPlayer
        val status = updateStatus(currentAudio, MKStatus.PlayState.PLAY)

        updateTime()
        serviceListener?.onPreparedListener(status)
    }

    private fun updateStatus(MKAudio: MKAudio? = null, status: MKStatus.PlayState): MKStatus {
        currentAudio = MKAudio
        jcStatus.mkAudio = MKAudio
        jcStatus.playState = status

        mediaPlayer?.let {
            jcStatus.duration = it.duration.toLong()
            jcStatus.currentPosition = it.currentPosition.toLong()
        }

        when (status) {
            MKStatus.PlayState.PLAY -> {
                try {
                    mediaPlayer?.start()
                    isPlaying = true
                    isPaused = false

                } catch (exception: Exception) {
                    serviceListener?.onError(exception)
                }
            }

            MKStatus.PlayState.STOP -> {
                mediaPlayer?.let {
                    it.stop()
                    it.reset()
                    it.release()
                    mediaPlayer = null
                }

                isPlaying = false
                isPaused = true
            }

            MKStatus.PlayState.PAUSE -> {
                mediaPlayer?.pause()
                isPlaying = false
                isPaused = true
            }

            MKStatus.PlayState.PREPARING -> {
                isPlaying = false
                isPaused = true
            }

            MKStatus.PlayState.PLAYING -> {
                isPlaying = true
                isPaused = false
            }

            else -> { // CONTINUE case
                mediaPlayer?.start()
                isPlaying = true
                isPaused = false
            }
        }

        return jcStatus
    }

    private fun updateTime() {
        object : Thread() {
            override fun run() {
                while (isPlaying) {
                    try {
                        val status = updateStatus(currentAudio, MKStatus.PlayState.PLAYING)
                        serviceListener?.onTimeChangedListener(status)
                        Thread.sleep(TimeUnit.SECONDS.toMillis(1))
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }

    private fun isAudioFileValid(path: String, origin: Origin): Boolean {
        when (origin) {
            Origin.URL -> return path.startsWith("http") || path.startsWith("https")

            Origin.RAW -> {
                assetFileDescriptor = null
                assetFileDescriptor =
                        applicationContext.resources.openRawResourceFd(Integer.parseInt(path))
                return assetFileDescriptor != null
            }

            Origin.ASSETS -> return try {
                assetFileDescriptor = null
                assetFileDescriptor = applicationContext.assets.openFd(path)
                assetFileDescriptor != null
            } catch (e: IOException) {
                e.printStackTrace() //TODO: need to give user more readable error.
                false
            }

            Origin.FILE_PATH -> {
                val file = File(path)
                //TODO: find an alternative to checking if file is exist, this code is slower on average.
                //read more: http://stackoverflow.com/a/8868140
                return file.exists()
            }

            else -> // We should never arrive here.
                return false // We don't know what the origin of the Audio File
        }
    }

    private fun throwError(path: String, origin: Origin) {
        when (origin) {
            Origin.URL -> throw AudioUrlInvalidException(path)

            Origin.RAW -> try {
                throw AudioRawInvalidException(path)
            } catch (e: AudioRawInvalidException) {
                e.printStackTrace()
            }

            Origin.ASSETS -> try {
                throw AudioAssetsInvalidException(path)
            } catch (e: AudioAssetsInvalidException) {
                e.printStackTrace()
            }

            Origin.FILE_PATH -> try {
                throw AudioFilePathInvalidException(path)
            } catch (e: AudioFilePathInvalidException) {
                e.printStackTrace()
            }
        }
    }

    fun getMediaPlayer(): MediaPlayer? {
        return mediaPlayer
    }

    fun finalize() {
        onDestroy()
        stopSelf()
    }
}
