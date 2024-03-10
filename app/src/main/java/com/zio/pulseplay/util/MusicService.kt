package com.zio.pulseplay.util

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.zio.pulseplay.data.Song

/**
 * Service wrapper with media player to control playback of songs from url and to listen for events
 * @see Song.class
 */
class MusicService : Service() {

    interface MusicServiceCallback {
        fun onDurationChanged(duration: Int)
        fun onPlaybackStopped()
        fun onPlaybackError()
        fun onBuffering(isBuffering: Boolean)
        fun onProgressChanged(progress: Int)
        fun onPrepared()
    }

    private lateinit var mediaPlayer: MediaPlayer
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
    }

    private var callback: MusicServiceCallback? = null

    val progressHandler = Handler()
    val progressRunnable = object : Runnable {
        override fun run() {
            callback?.onProgressChanged(mediaPlayer.currentPosition)
            progressHandler.postDelayed(this, 1000) // Update every second
        }
    }

    fun setCallback(callbackreq: MusicServiceCallback) {
        this.callback = callbackreq

        mediaPlayer.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            callback?.onPlaybackStopped()
        })
        mediaPlayer.setOnErrorListener(MediaPlayer.OnErrorListener { mediaPlayer, i, i2 ->
            callback?.onPlaybackError(); true
        })
        mediaPlayer.setOnPreparedListener(MediaPlayer.OnPreparedListener {
            callback?.onDurationChanged(it.duration)
            callback?.onPrepared()
        })

        setProgressor()
    }

    private fun setProgressor() {
        if (callback != null)
            progressRunnable.run()
    }

    private fun removeProgressor() {
        progressHandler.removeCallbacks(progressRunnable)
    }

    fun removeCallback() {
        this.callback = null
        removeProgressor()
    }

    fun startPlay(song: Song) {
        Log.e("TAG", "called play in service")
        try {
            mediaPlayer?.apply {
                reset()
                setDataSource(song.songUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    setProgressor()
                    callback?.onDurationChanged(it.duration)
                }
            }
        } catch (error: Exception) {
            callback?.onPlaybackError()
            Log.e("TAG", "unable to play")
        }
    }

    fun resumePlay() {
        mediaPlayer.start()
        setProgressor()
    }

    fun pausePlay() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            removeProgressor()
        }
    }

    fun stopPlay() {
        mediaPlayer.stop()
        mediaPlayer.prepareAsync()
        removeProgressor()
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.prepareAsync()
        mediaPlayer.release()
        removeCallback()
        super.onDestroy()
    }
}
