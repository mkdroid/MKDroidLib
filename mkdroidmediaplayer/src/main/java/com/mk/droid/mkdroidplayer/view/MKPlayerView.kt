package com.mk.droid.mkdroidplayer.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.mk.droid.mkdroidplayer.MKPlayerManager
import com.mk.droid.mkdroidplayer.MKPlayerManagerListener
import com.mk.droid.mkdroidplayer.R
import com.mk.droid.mkdroidplayer.general.MKStatus
import com.mk.droid.mkdroidplayer.general.PlayerUtil.toTimeSongString
import com.mk.droid.mkdroidplayer.general.errors.AudioListNullPointerException
import com.mk.droid.mkdroidplayer.general.errors.OnInvalidPathListener
import com.mk.droid.mkdroidplayer.model.MKAudio
import kotlinx.android.synthetic.main.view_jcplayer.view.*


/**
 * This class is the MKAudio View. Handles user interactions and communicate with [mkPlayerManager].
 * Create by MKDroid on 12/03/20

 */
class MKPlayerView : LinearLayout, View.OnClickListener, SeekBar.OnSeekBarChangeListener, MKPlayerManagerListener {

    private val mkPlayerManager: MKPlayerManager by lazy {
        MKPlayerManager.getInstance(context).get()!!
    }

    val myPlaylist: List<MKAudio>?
        get() = mkPlayerManager.playlist

    val isPlaying: Boolean
        get() = mkPlayerManager.isPlaying()

    val isPaused: Boolean
        get() = mkPlayerManager.isPaused()

    val currentAudio: MKAudio?
        get() = mkPlayerManager.currentAudio

    var onInvalidPathListener: OnInvalidPathListener? = null

    var mkPlayerManagerListener: MKPlayerManagerListener? = null
        set(value) {
            mkPlayerManager.MKPlayerManagerListener = value
        }


    companion object {
        private const val PULSE_ANIMATION_DURATION = 200L
        private const val TITLE_ANIMATION_DURATION = 600
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()

        context.theme
                .obtainStyledAttributes(attrs, R.styleable.MKPlayerView, 0, 0)
                .also { setAttributes(it) }
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()

        context.theme
                .obtainStyledAttributes(attrs, R.styleable.MKPlayerView, defStyle, 0)
                .also { setAttributes(it) }
    }

    private fun init() {
        View.inflate(context, R.layout.view_jcplayer, this)

        btnNext?.setOnClickListener(this)
        btnPrev?.setOnClickListener(this)
        btnPlay?.setOnClickListener(this)
        btnPause?.setOnClickListener(this)
        btnRandom?.setOnClickListener(this)
        btnRepeat?.setOnClickListener(this)
        btnRepeatOne?.setOnClickListener(this)
        seekBar?.setOnSeekBarChangeListener(this)
    }

    private fun setAttributes(attrs: TypedArray) {
        val defaultColor = ResourcesCompat.getColor(resources, R.color.justell, null)

        txtCurrentMusic?.setTextColor(attrs.getColor(R.styleable.MKPlayerView_text_audio_title_color, defaultColor))
        txtCurrentDuration?.setTextColor(attrs.getColor(R.styleable.MKPlayerView_text_audio_current_duration_color, defaultColor))
        txtDuration?.setTextColor(attrs.getColor(R.styleable.MKPlayerView_text_audio_duration_color, defaultColor))

        progressBarPlayer?.indeterminateDrawable?.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_progress_color, defaultColor), PorterDuff.Mode.SRC_ATOP)
        seekBar?.progressDrawable?.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_seek_bar_color, defaultColor), PorterDuff.Mode.SRC_ATOP)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            seekBar?.thumb?.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_seek_bar_color, defaultColor), PorterDuff.Mode.SRC_ATOP)
            // TODO: change thumb color in older versions (14 and 15).
        }

        btnPlay.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_play_icon_color, defaultColor))
        btnPlay.setImageResource(attrs.getResourceId(R.styleable.MKPlayerView_play_icon, R.drawable.ic_play))

        btnPause.setImageResource(attrs.getResourceId(R.styleable.MKPlayerView_pause_icon, R.drawable.ic_pause))
        btnPause.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_pause_icon_color, defaultColor))

        btnNext?.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_next_icon_color, defaultColor))
        btnNext?.setImageResource(attrs.getResourceId(R.styleable.MKPlayerView_next_icon, R.drawable.ic_next))

        btnPrev?.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_previous_icon_color, defaultColor))
        btnPrev?.setImageResource(attrs.getResourceId(R.styleable.MKPlayerView_previous_icon, R.drawable.ic_previous))

        btnRandom?.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_random_icon_color, defaultColor))
        btnRandomIndicator?.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_random_icon_color, defaultColor))
        btnRandom?.setImageResource(attrs.getResourceId(R.styleable.MKPlayerView_random_icon, R.drawable.ic_shuffle))
        attrs.getBoolean(R.styleable.MKPlayerView_show_random_button, true).also { showButton ->
            if (showButton) {
                btnRandom?.visibility = View.GONE
            } else {
                btnRandom?.visibility = View.GONE
            }
        }

        btnRepeat?.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_repeat_icon_color, defaultColor))
        btnRepeatIndicator?.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_repeat_icon_color, defaultColor))
        btnRepeat?.setImageResource(attrs.getResourceId(R.styleable.MKPlayerView_repeat_icon, R.drawable.ic_repeat))
        attrs.getBoolean(R.styleable.MKPlayerView_show_repeat_button, true).also { showButton ->
            if (showButton) {
                btnRepeat?.visibility = View.GONE
            } else {
                btnRepeat?.visibility = View.GONE
            }
        }

        attrs.getBoolean(R.styleable.MKPlayerView_show_prev_button, true).also { showButton ->
            if (showButton) {
                btnPrev?.visibility = View.INVISIBLE
            } else {
                btnPrev?.visibility = View.INVISIBLE
            }
        }

        btnRepeatOne?.setColorFilter(attrs.getColor(R.styleable.MKPlayerView_repeat_one_icon_color, attrs.getColor(R.styleable.MKPlayerView_repeat_icon_color, defaultColor)))
        btnRepeatOne?.setImageResource(attrs.getResourceId(R.styleable.MKPlayerView_repeat_one_icon, R.drawable.ic_repeat_one))
    }

    /**
     * Initialize the playlist and controls.
     *
     * @param playlist List of MKAudio objects that you want play
     * @param MKPlayerManagerListener The view status MKPlayerManagerListener (optional)
     */
    fun initPlaylist(playlist: List<MKAudio>, MKPlayerManagerListener: MKPlayerManagerListener? = null) {
        /*Don't sort if the playlist have position number.
        We need to do this because there is a possibility that the user reload previous playlist
        from persistence storage like sharedPreference or SQLite.*/
        if (isAlreadySorted(playlist).not()) {
            sortPlaylist(playlist)
        }

        mkPlayerManager.playlist = playlist as ArrayList<MKAudio>
        mkPlayerManager.MKPlayerManagerListener = MKPlayerManagerListener
        mkPlayerManager.MKPlayerManagerListener = this
    }

    /**
     * Initialize an anonymous playlist with a default MKPlayer title for all audios
     *
     * @param playlist List of urls strings
     */
    fun initAnonPlaylist(playlist: List<MKAudio>) {
        generateTitleAudio(playlist, context.getString(R.string.track_number))
        initPlaylist(playlist)
    }

    /**
     * Initialize an anonymous playlist, but with a custom title for all audios
     *
     * @param playlist List of MKAudio files.
     * @param title    Default title for all audios
     */
    fun initWithTitlePlaylist(playlist: List<MKAudio>, title: String) {
        generateTitleAudio(playlist, title)
        initPlaylist(playlist)
    }

    /**
     * Add an audio for the playlist. We can track the MKAudio by
     * its id. So here we returning its id after adding to list.
     *
     * @param MKAudio audio file generated from [MKAudio]
     * @return MKAudio position.
     */
    fun addAudio(MKAudio: MKAudio): Int {
        mkPlayerManager.playlist.let {
            val lastPosition = it.size
            MKAudio.position = lastPosition + 1

            if (it.contains(MKAudio).not()) {
                it.add(lastPosition, MKAudio)
            }

            return MKAudio.position!!
        }
    }

    /**
     * Remove an audio for the playlist
     *
     * @param MKAudio MKAudio object
     */
    fun removeAudio(MKAudio: MKAudio) {
        mkPlayerManager.playlist.let {
            if (it.contains(MKAudio)) {
                if (it.size > 1) {
                    // play next audio when currently played audio is removed.
                    if (mkPlayerManager.isPlaying()) {
                        if (mkPlayerManager.currentAudio == MKAudio) {
                            it.remove(MKAudio)
                            pause()
                            resetPlayerInfo()
                        } else {
                            it.remove(MKAudio)
                        }
                    } else {
                        it.remove(MKAudio)
                    }
                } else {
                    //TODO: Maybe we need MKPlayerManager.stopPlay() for stopping the player
                    it.remove(MKAudio)
                    pause()
                    resetPlayerInfo()
                }
            }
        }
    }

    /**
     * Plays the give audio.
     * @param MKAudio The audio to be played.
     */
    fun playAudio(MKAudio: MKAudio) {
        showProgressBar()

        mkPlayerManager.playlist.let {
            if (it.contains(MKAudio).not()) {
                it.add(MKAudio)
            }


            mkPlayerManager.playAudio(MKAudio)
        }
    }

    /**
     * Shows the play button on player.
     */
    private fun showPlayButton() {
        btnPlay?.visibility = View.VISIBLE
        btnPause?.visibility = View.GONE
    }

    /**
     * Shows the pause button on player.
     */
    private fun showPauseButton() {
        btnPlay?.visibility = View.GONE
        btnPause?.visibility = View.VISIBLE
    }

    /**
     * Goes to next audio.
     */
    fun next() {
        mkPlayerManager.let { player ->
            player.currentAudio?.let {
                resetPlayerInfo()
                showProgressBar()

                try {
                    player.nextAudio()
                } catch (e: AudioListNullPointerException) {
                    dismissProgressBar()
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Continues the current audio.
     */
    fun continueAudio() {
        showProgressBar()

        try {
            mkPlayerManager.continueAudio()
        } catch (e: AudioListNullPointerException) {
            dismissProgressBar()
            e.printStackTrace()
        }
    }

    /**
     * Pauses the current audio.
     */
    fun pause() {
        mkPlayerManager.pauseAudio()
        showPlayButton()
    }


    /**
     * Goes to precious audio.
     */
    fun previous() {
        resetPlayerInfo()
        showProgressBar()

        try {
            mkPlayerManager.previousAudio()
        } catch (e: AudioListNullPointerException) {
            dismissProgressBar()
            e.printStackTrace()
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnPlay ->
                btnPlay?.let {
                    applyPulseAnimation(it)
                    continueAudio()
                }

            R.id.btnPause -> {
                btnPause?.let {
                    applyPulseAnimation(it)
                    pause()
                }
            }

            /*R.id.btnNext ->
                btnNext?.let {
                    applyPulseAnimation(it)
                    next()
                }
*/
            R.id.btnPrev ->
                btnPrev?.let {
                    applyPulseAnimation(it)
                    previous()
                }

           /* R.id.btnRandom -> {
                MKPlayerManager.onShuffleMode = MKPlayerManager.onShuffleMode.not()
                btnRandomIndicator.visibility = if (MKPlayerManager.onShuffleMode) View.VISIBLE else View.GONE
            }*/


            else -> { // Repeat case
                mkPlayerManager.activeRepeat()
                val active = mkPlayerManager.repeatPlaylist or mkPlayerManager.repeatCurrAudio

                btnRepeat?.visibility = View.VISIBLE
                btnRepeatOne?.visibility = View.GONE

                if (active) {
                    btnRepeatIndicator?.visibility = View.VISIBLE
                } else {
                    btnRepeatIndicator?.visibility = View.GONE
                }

                if (mkPlayerManager.repeatCurrAudio) {
                    btnRepeatOne?.visibility = View.VISIBLE
                    btnRepeat?.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Create a notification player with same playlist with a custom icon.
     *
     * @param iconResource icon path.
     */
    fun createNotification(iconResource: Int) {
        mkPlayerManager.createNewNotification(iconResource)
    }

    /**
     * Create a notification player with same playlist with a default icon
     */
    fun createNotification() {
        mkPlayerManager.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // For light theme
                it.createNewNotification(R.drawable.ic_notification_default_black)
            } else {
                // For dark theme
                it.createNewNotification(R.drawable.ic_notification_default_white)
            }
        }
    }

    override fun onPreparedAudio(status: MKStatus) {
        dismissProgressBar()
        resetPlayerInfo()
        onUpdateTitle(status.mkAudio.title)

        val duration = status.duration.toInt()
        seekBar?.post { seekBar?.max = duration }
        txtDuration?.post { txtDuration?.text = toTimeSongString(duration) }
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, fromUser: Boolean) {
        mkPlayerManager.let {
            if (fromUser) {
                it.seekTo(i)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        if (mkPlayerManager.currentAudio != null) {
            showProgressBar()
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        dismissProgressBar()

        if (mkPlayerManager.isPaused()) {
            showPlayButton()
        }
    }

    override fun onCompletedAudio() {
        resetPlayerInfo()

        try {
            mkPlayerManager.nextAudio()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onContinueAudio(status: MKStatus) {
        dismissProgressBar()
    }

    override fun onPlaying(status: MKStatus) {
        dismissProgressBar()
        showPauseButton()
    }

    override fun onTimeChanged(status: MKStatus) {
        val currentPosition = status.currentPosition.toInt()
        seekBar?.post { seekBar?.progress = currentPosition }
        txtCurrentDuration?.post { txtCurrentDuration?.text = toTimeSongString(currentPosition) }
    }

    override fun onPaused(status: MKStatus) {
    }

    override fun onStopped(status: MKStatus) {
    }

    override fun onMKpError(throwable: Throwable) {
        // TODO
//        MKPlayerManager.currentAudio?.let {
//            onInvalidPathListener?.onPathError(it)
//        }
    }

    private fun showProgressBar() {
        progressBarPlayer?.visibility = ProgressBar.VISIBLE
        btnPlay?.visibility = Button.GONE
        btnPause?.visibility = Button.GONE
    }

    private fun dismissProgressBar() {
        progressBarPlayer?.visibility = ProgressBar.GONE
        showPauseButton()
    }

    private fun onUpdateTitle(title: String) {
        txtCurrentMusic?.let {
            it.visibility = View.VISIBLE
            YoYo.with(Techniques.FadeInLeft)
                    .duration(TITLE_ANIMATION_DURATION.toLong())
                    .playOn(it)

            it.post { it.text = title }
        }
    }

    private fun resetPlayerInfo() {
        txtCurrentMusic?.post { txtCurrentMusic.text = "" }
        seekBar?.post { seekBar?.progress = 0 }
        txtDuration?.post { txtDuration.text = context.getString(R.string.play_initial_time) }
        txtCurrentDuration?.post { txtCurrentDuration.text = context.getString(R.string.play_initial_time) }
    }

    /**
     * Sorts the playlist.
     */
    private fun sortPlaylist(playlist: List<MKAudio>) {
        for (i in playlist.indices) {
            val MKAudio = playlist[i]
            MKAudio.position = i
        }
    }

    /**
     * Check if playlist already sorted or not.
     * We need to check because there is a possibility that the user reload previous playlist
     * from persistence storage like sharedPreference or SQLite.
     *
     * @param playlist list of MKAudio
     * @return true if sorted, false if not.
     */
    private fun isAlreadySorted(playlist: List<MKAudio>?): Boolean {
        // If there is position in the first audio, then playlist is already sorted.
        return playlist?.let { it[0].position != -1 } == true
    }

    /**
     * Generates a default audio title for each audio on list.
     * @param playlist The audio list.
     * @param title The default title.
     */
    private fun generateTitleAudio(playlist: List<MKAudio>, title: String) {
        for (i in playlist.indices) {
            if (title == context.getString(R.string.track_number)) {
                playlist[i].title = context.getString(R.string.track_number)
            } else {
                playlist[i].title = title
            }
        }
    }

    private fun applyPulseAnimation(view: View?) {
        view?.postDelayed({
            YoYo.with(Techniques.Pulse)
                    .duration(PULSE_ANIMATION_DURATION)
                    .playOn(view)
        }, PULSE_ANIMATION_DURATION)
    }

    /**
     * Kills the player
     */
    fun kill() {
        mkPlayerManager.kill()
    }
}
