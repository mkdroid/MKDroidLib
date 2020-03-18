package com.mk.droid.mkdroidplayer

import com.mk.droid.mkdroidplayer.general.MKStatus

/**
 * This class represents all the [JcPlayerService] callbacks.
 * Create by MKDroid on 12/03/20
 */
interface MKPlayerManagerListener {

    /**
     * Prepares the new audio.
     * @param audioName The audio name.
     * @param duration The audio duration.
     */
    fun onPreparedAudio(status: MKStatus)

    /**
     * The audio ends.
     */
    fun onCompletedAudio()

    /**
     * The audio is paused.
     */
    fun onPaused(status: MKStatus)

    /**
     * The audio was paused and user hits play again.
     */
    fun onContinueAudio(status: MKStatus)

    /**
     *  Called when there is an audio playing.
     */
    fun onPlaying(status: MKStatus)

    /**
     * Called when the time of the audio changed.
     */
    fun onTimeChanged(status: MKStatus)


    /**
     * Called when the player stops.
     */
    fun onStopped(status: MKStatus)

    /**
     * Notifies some error.
     * @param throwable The error.
     */
    fun onMKpError(throwable: Throwable)
}