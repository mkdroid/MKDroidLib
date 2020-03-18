package com.mk.droid.mkdroidplayer.general.errors;

/**
 * Create by MKDroid on 12/03/20
 */
public class AudioUrlInvalidException extends IllegalStateException {
    public AudioUrlInvalidException(String url) {
        super("The url does not appear valid: " + url);
    }
}
