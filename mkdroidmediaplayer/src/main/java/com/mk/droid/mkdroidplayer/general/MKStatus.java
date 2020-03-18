package com.mk.droid.mkdroidplayer.general;

import com.mk.droid.mkdroidplayer.model.MKAudio;

/**
 * Created by rio on 02 January 2017.
 */
public class MKStatus {
    private MKAudio MKAudio;
    private long duration;
    private long currentPosition;
    private PlayState playState;

    public MKStatus() {
        this(null, 0, 0, PlayState.PREPARING);
    }

    public MKStatus(MKAudio MKAudio, long duration, long currentPosition, PlayState playState) {
        this.MKAudio = MKAudio;
        this.duration = duration;
        this.currentPosition = currentPosition;
        this.playState = playState;
    }

    public MKAudio getMKAudio() {
        return MKAudio;
    }

    public void setMKAudio(MKAudio MKAudio) {
        this.MKAudio = MKAudio;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public PlayState getPlayState() {
        return playState;
    }

    public void setPlayState(PlayState playState) {
        this.playState = playState;
    }

    public enum PlayState {
        PLAY, PAUSE, STOP, CONTINUE, PREPARING, PLAYING
    }
}
