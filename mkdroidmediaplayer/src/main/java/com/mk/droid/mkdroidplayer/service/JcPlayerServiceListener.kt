package com.mk.droid.mkdroidplayer.service

import com.mk.droid.mkdroidplayer.general.MKStatus

/**
 * Create by MKDroid on 12/03/20
 */
interface JcPlayerServiceListener {

    /**
     * Notifies on prepared audio for the service listeners
     */
    fun onPreparedListener(status: MKStatus)

    /**
     * Notifies on time changed for the service listeners
     */
    fun onTimeChangedListener(status: MKStatus)

    /**
     * Notifies on continue for the service listeners
     */
    fun onContinueListener(status: MKStatus)

    /**
     * Notifies on completed audio for the service listeners
     */
    fun onCompletedListener()

    /**
     * Notifies on paused for the service listeners
     */
    fun onPausedListener(status: MKStatus)

    /**
     * Notifies on stopped for the service listeners
     */
    fun onStoppedListener(status: MKStatus)

    /**
     * Notifies an error for the service listeners
     */
    fun onError(exception: Exception)
}