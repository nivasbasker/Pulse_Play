package com.zio.pulseplay.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zio.pulseplay.util.MusicService.MusicServiceCallback
import com.zio.pulseplay.data.DataBase
import com.zio.pulseplay.data.Song
import com.zio.pulseplay.util.Helper.Companion.LOGTAG
import kotlinx.coroutines.launch


class MainViewModel() : ViewModel() {

    val STATE_IDLE = 0
    val STATE_PLAYING = 1
    val STATE_PAUSED = 2
    val STATE_STOPPED = 3
    val STATE_ERROR = 4

    private var musicService: MusicService? = null
    private var isBound = false

    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> get() = _currentSong

    // state 0 for no playing, 1 for playing, 2 for paused
    private val _state = MutableLiveData<Int>()
    val state: LiveData<Int> get() = _state

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> get() = _progress

    private val _duration = MutableLiveData<Int>()
    val duration: LiveData<Int> get() = _duration

    private val _allSongs = MutableLiveData<List<Song>>()
    val allSongs: LiveData<List<Song>> get() = _allSongs

    private val _recentSongs = MutableLiveData<List<Song>>()
    val recentSongs: LiveData<List<Song>> get() = _recentSongs


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            settingCallBacks()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            _state.value = STATE_ERROR
        }
    }

    private fun settingCallBacks() {
        musicService?.setCallback(object : MusicServiceCallback {
            override fun onDurationChanged(duration: Int) {
                _duration.value = duration
            }

            override fun onPlaybackStopped() {
                _state.value = STATE_STOPPED
            }

            override fun onPlaybackError() {
                _state.value = STATE_ERROR
            }

            override fun onBuffering(isBuffering: Boolean) {
            }

            override fun onProgressChanged(progress: Int) {
                _progress.value = progress
            }

            override fun onPrepared() {
                _state.value = STATE_PLAYING

            }
        })
    }

    fun bindService(context: Context) {
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }

    fun play(song: Song) {
        _currentSong.value = song
        musicService?.startPlay(song)
        _state.value = STATE_PLAYING

    }

    suspend fun updateRecent(song: Song, context: Context) {
        val db = DataBase.getInstance(context)
        val songDao = db.yourEntityDao()

        if (song.id > 0) //avoid invalid songs
            songDao.insert(Song(System.currentTimeMillis(), song))
        val topFive = songDao.getTopFive()
        viewModelScope.launch {
            _recentSongs.value = topFive
        }

    }

    fun resume() {
        try {
            musicService?.resumePlay()
            _state.value = STATE_PLAYING
        } catch (e: Exception) {
            Log.d(LOGTAG, "Music service says : " + e.message)
        }
    }

    fun pause() {
        try {
            musicService?.pausePlay()
            _state.value = STATE_PAUSED;
        } catch (e: IllegalAccessError) {
            Log.d(LOGTAG, "Music service says : " + e.message)
        }
    }

    fun stop() {
        musicService?.stopPlay()
        _state.value = STATE_STOPPED;
    }

    fun downloadSongList(context: Context) {

        Helper(context).fetchSongs(
            onSuccess = { songs ->
                Log.d(LOGTAG, "Fetched ${songs.size} songs")
                _allSongs.value = songs;
            },
            onError = { errorMessage ->
                Log.e(LOGTAG, errorMessage)
            }
        )
    }

    suspend fun retrieve(context: Context) {
        val db = DataBase.getInstance(context)
        val songDao = db.yourEntityDao()
        val topf = songDao.getTopFive()

        viewModelScope.launch {
            _recentSongs.value = topf
        }
    }
}
