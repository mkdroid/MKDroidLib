package com.mk.droid.mkdroidplayer.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RawRes
import com.mk.droid.mkdroidplayer.general.Origin


/**
 * This class is an type of audio model .
 * Create by MKDroid on 12/03/20
 * Jesus loves you.
 */
data class MKAudio constructor(
        var title: String,
        var position: Int? = -1,
        val path: String,
        val origin: Origin
) : Parcelable {

    constructor(source: Parcel) : this(
            source.readString(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readString(),
            Origin.valueOf(source.readString())
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeValue(position)
        writeString(path)
        writeString(origin.name)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<MKAudio> = object : Parcelable.Creator<MKAudio> {
            override fun createFromParcel(source: Parcel): MKAudio = MKAudio(source)
            override fun newArray(size: Int): Array<MKAudio?> = arrayOfNulls(size)
        }


        @JvmStatic
        fun createFromRaw(@RawRes rawId: Int): MKAudio {
            return MKAudio(title = rawId.toString(), path = rawId.toString(), origin = Origin.RAW)
        }

        @JvmStatic
        fun createFromRaw(title: String, @RawRes rawId: Int): MKAudio {
            return MKAudio(title = title, path = rawId.toString(), origin = Origin.RAW)
        }

        @JvmStatic
        fun createFromAssets(assetName: String): MKAudio {
            return MKAudio(title = assetName, path = assetName, origin = Origin.ASSETS)
        }

        @JvmStatic
        fun createFromAssets(title: String, assetName: String): MKAudio {
            return MKAudio(title = title, path = assetName, origin = Origin.ASSETS)
        }

        @JvmStatic
        fun createFromURL(url: String): MKAudio {
            return MKAudio(title = url, path = url, origin = Origin.URL)
        }

        @JvmStatic
        fun createFromURL(title: String, url: String): MKAudio {
            return MKAudio(title = title, path = url, origin = Origin.URL)
        }

        @JvmStatic
        fun createFromFilePath(filePath: String): MKAudio {
            return MKAudio(title = filePath, path = filePath, origin = Origin.FILE_PATH)
        }

        @JvmStatic
        fun createFromFilePath(title: String, filePath: String): MKAudio {
            return MKAudio(title = title, path = filePath, origin = Origin.FILE_PATH)
        }
    }
}