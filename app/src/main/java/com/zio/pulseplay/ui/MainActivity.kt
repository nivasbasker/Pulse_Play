package com.zio.pulseplay.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zio.pulseplay.util.Helper
import com.zio.pulseplay.util.MainViewModel
import com.zio.pulseplay.data.Song
import com.zio.pulseplay.util.Helper.Companion.dummySong
import com.zio.pulseplay.util.Helper.Companion.dummySongList


class MainActivity : AppCompatActivity() {

    private val TAB1 = "ForYou"
    private val TAB2 = "TopTracks"
    private val TABSWITCHVIBRATION: Long = 50

    private lateinit var helper: Helper
    private lateinit var fragments: Fragments
    private lateinit var players: Player

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind to the service
        helper = Helper(this)
        fragments = Fragments()
        players = Player(this)
        viewModel.bindService(this)
        viewModel.downloadSongList(this)

        setContent {
            MainScreen()
        }
    }

    @Composable
    fun MainScreen() {

        val navController = rememberNavController()
        var activeFirst by remember { mutableStateOf(false) }
        val vibrator = baseContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val currentSong: Song by viewModel.currentSong.observeAsState(initial = dummySong)
        val state: Int by viewModel.state.observeAsState(initial = 0)
        val currentProgress: Int by viewModel.progress.observeAsState(initial = 0)
        val totalDuration: Int by viewModel.duration.observeAsState(initial = 0)
        val allSongs: List<Song> by viewModel.allSongs.observeAsState(initial = dummySongList)
        val recentSongs: List<Song> by viewModel.recentSongs.observeAsState(initial = dummySongList)

        LaunchedEffect(currentSong) {
            viewModel.updateRecent(currentSong, baseContext)
        }

        Column {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->

                        if (dragAmount < -40.0 && activeFirst) navController.navigate(TAB2)
                        if (dragAmount > 40.0 && !activeFirst) navController.navigate(TAB1)

                    }
                }) {
                NavHost(navController = navController,
                    startDestination = TAB2,
                    enterTransition = {
                        (slideInHorizontally(animationSpec = tween(1000),
                            initialOffsetX = { fullWidth ->
                                if (activeFirst) -fullWidth else fullWidth
                            }))
                    },
                    exitTransition = {
                        (slideOutHorizontally(animationSpec = tween(1000),
                            targetOffsetX = { fullWidth ->
                                if (activeFirst) fullWidth else -fullWidth
                            }))
                    }) {
                    composable(TAB1) {
                        fragments.ForYouFragment(recentSongs) {
                            viewModel.play(it)
                        }
                        activeFirst = true
                    }
                    composable(TAB2) {
                        fragments.TopTracksFragment(allSongs) {
                            viewModel.play(it)
                        }
                        activeFirst = false
                    }
                }
            }
            // Bottom Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .height(70.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CustomIcon(activeFirst, label = "For You") {
                    vibrator.cancel()
                    vibrator.vibrate(TABSWITCHVIBRATION)
                    if (!activeFirst) navController.navigate(TAB1)
                }
                CustomIcon(!activeFirst, label = "Top tracks") {
                    vibrator.cancel()
                    vibrator.vibrate(TABSWITCHVIBRATION)
                    if (activeFirst) navController.navigate(TAB2)
                }
            }
        }

        players.PlayerView(
            currentSong = currentSong,
            state = state,
            currentProgress = currentProgress,
            totalDuration = totalDuration,
        ) {
            onPlaynPause(it)
        }
        //PlayerView()
    }

    @Composable
    fun CustomIcon(active: Boolean, label: String, onClick: () -> Unit) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .width(120.dp)
                .clickable { onClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary
            )
            if (active) Text(
                text = "•", fontSize = 30.sp, color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    private fun onPlaynPause(play: Boolean) {
        if (play) viewModel.resume()
        else viewModel.pause()
    }

    override fun onDestroy() {
        // Unbind from the service when the activity is destroyed
        viewModel.stop()
        viewModel.unbindService(this)
        super.onDestroy()
    }

}