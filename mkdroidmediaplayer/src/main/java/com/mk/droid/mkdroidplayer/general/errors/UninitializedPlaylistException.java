package com.mk.droid.mkdroidplayer.general.errors;

/**
 * Create by MKDroid on 12/03/20
 */
public class UninitializedPlaylistException extends IllegalStateException {
    public UninitializedPlaylistException() {
        super("The playlist was not initialized. You must to call to initPlaylist method before.");
    }
}
