package com.mk.droid.mkdroidplayer.general.errors

import com.mk.droid.mkdroidplayer.model.MKAudio

/**
 * Create by MKDroid on 12/03/20
 * Jesus loves you.
 */
interface OnInvalidPathListener {

    /**
     * Audio path error MKPlayerManagerListener.
     * @param MKAudio The wrong audio.
     */
    fun onPathError(MKAudio: MKAudio)
}