package com.mk.droid.mkdroidplayer.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mk.droid.mkdroidplayer.MKPlayerManager
import com.mk.droid.mkdroidplayer.general.errors.AudioListNullPointerException

class MKPlayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val jcPlayerManager = MKPlayerManager.getInstance(context)
        var action = ""

        if (intent.hasExtra(MKNotificationPlayer.ACTION)) {
            action = intent.getStringExtra(MKNotificationPlayer.ACTION)
        }

        when (action) {
            MKNotificationPlayer.PLAY -> try {
                jcPlayerManager.get()?.continueAudio()
                jcPlayerManager.get()?.updateNotification()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            MKNotificationPlayer.PAUSE -> try {
                jcPlayerManager.get()?.pauseAudio()
                jcPlayerManager.get()?.updateNotification()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            MKNotificationPlayer.NEXT -> try {
                jcPlayerManager.get()?.nextAudio()
            } catch (e: AudioListNullPointerException) {
                try {
                    jcPlayerManager.get()?.continueAudio()
                } catch (e1: AudioListNullPointerException) {
                    e1.printStackTrace()
                }

            }

            MKNotificationPlayer.PREVIOUS -> try {
                jcPlayerManager.get()?.previousAudio()
            } catch (e: Exception) {
                try {
                    jcPlayerManager.get()?.continueAudio()
                } catch (e1: AudioListNullPointerException) {
                    e1.printStackTrace()
                }
            }

        }
    }
}
