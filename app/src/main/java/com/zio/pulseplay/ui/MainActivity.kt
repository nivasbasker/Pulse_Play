package com.zio.pulseplay.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.zio.pulseplay.R
import com.zio.pulseplay.util.Helper
import com.zio.pulseplay.util.MainViewModel
import com.zio.pulseplay.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Integer.max


class MainActivity : AppCompatActivity() {


    var mySongs: List<Song> = listOf()
    var dummySong = Song(
        id = 0,
        songUrl = "",
        coverUrl = "",
        title = "Unknown Song",
        artist = "Unknown Artist"
    )
    lateinit var helper: Helper
    lateinit var fragments: Fragments

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind to the service
        helper = Helper(this)
        viewModel.bindService(this)
        viewModel.downloadSongList(this)
        fragments = Fragments(viewModel)


        setContent {
            MainScreen()
        }
    }

    @Composable
    fun MainScreen() {
        LaunchedEffect(helper) {
            viewModel.retrieve(baseContext)
        }
        val navController = rememberNavController()
        var activeFirst by remember { mutableStateOf(false) }

        Column {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->

                        if (dragAmount < -40.0 && activeFirst) navController.navigate("fragment2")
                        if (dragAmount > 40.0 && !activeFirst) navController.navigate("fragment1")

                    }
                }) {
                NavHost(navController = navController,
                    startDestination = "fragment2",
                    enterTransition = {
                        (slideInHorizontally(animationSpec = tween(1000),
                            initialOffsetX = { fullWidth ->
                                if (navController.currentDestination?.route == "fragment1") -fullWidth else fullWidth
                            }))
                    },
                    exitTransition = {
                        (slideOutHorizontally(animationSpec = tween(1000),
                            targetOffsetX = { fullWidth ->
                                if (navController.currentDestination?.route == "fragment1") fullWidth else -fullWidth
                            }))
                    }) {
                    composable("fragment1") {
                        fragments.ForYouFragment()
                        activeFirst = true
                    }
                    composable("fragment2") {
                        fragments.TopTracksFragment()
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
                    if (!activeFirst) navController.navigate("fragment1")
                }
                CustomIcon(!activeFirst, label = "Top tracks") {
                    if (activeFirst) navController.navigate("fragment2")
                }
            }
        }
        PlayerView()
    }

    @Composable
    fun PlayerView() {
        val currentSong: Song by viewModel.currentSong.observeAsState(initial = dummySong)
        val state: Int by viewModel.state.observeAsState(initial = 0)
        var mini by remember { mutableStateOf(true) }
        var startColor by remember { mutableStateOf(0) }

        Glide.with(baseContext).asBitmap().load(currentSong.coverUrl)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    val callback = object : Helper.PaletteCallback {
                        override fun onPaletteGenerated(color1: Int, color2: Int) {
                            startColor = color1
                        }
                    }
                    helper.createPaletteAsync(resource, callback)
                }
            })

        val fullScreenHeight = LocalView.current.height
        val offset by animateIntOffsetAsState(
            targetValue = if (mini) {
                IntOffset(0, max(2000, fullScreenHeight))
            } else {
                IntOffset.Zero
            },
            animationSpec = tween(durationMillis = 1000),
            label = "offset"
        )

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            if (state > 0 && state < 4)
                minPlayer(
                    playingSong = currentSong, playing = (state == 1), startColor = startColor
                ) {
                    mini = false
                }
            Spacer(
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth()
            )
        }
        FullPlayer(
            offset = offset,
            currentSong = currentSong,
            state = state,
            startColor = startColor
        ) {
            mini = true
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun minPlayer(playingSong: Song, playing: Boolean, startColor: Int, onRequest: () -> Unit) {

        LaunchedEffect(playingSong) {
            withContext(Dispatchers.IO) {
                viewModel.updateRecent(playingSong, baseContext)
            }
        }

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                //openPlayer(playingSong, if (playing) 1 else 2);
                onRequest()
            }
            .background(Color(startColor))
            .padding(horizontal = 20.dp, vertical = 10.dp),

            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            // Left Image
            GlideImage(
                model = playingSong.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            // Text in the middle
            Text(
                text = playingSong.title,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )

            // Right Image
            Icon(painter = if (playing) painterResource(id = R.drawable.icon_pause) else painterResource(
                id = R.drawable.icon_play
            ),
                contentDescription = "Play/Pause",
                tint = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp)
                    .clickable {
                        vibrator.cancel()
                        vibrator.vibrate(100)
                        onPlaynPause(!playing)
                    })
        }
    }

    @Composable
    fun FullPlayer(
        offset: IntOffset,
        currentSong: Song,
        state: Int,
        startColor: Int,
        onRequest: () -> Unit
    ) {

        Column(modifier = Modifier
            .offset { offset }
            .fillMaxSize()
            .animateContentSize()
            .background(
                Brush.linearGradient(
                    0.0f to Color(startColor),
                    500.0f to Color.Black,
                    start = androidx.compose.ui.geometry.Offset.Zero,
                    end = androidx.compose.ui.geometry.Offset.Infinite
                )
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount > 50.0) {
                        onRequest()
                    }

                }
            }, verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                DetailView(currentSong)
            }
            ControllerView(state == 1)
        }
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

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun DetailView(thisSong: Song) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                GlideImage(
                    model = thisSong.coverUrl,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 50.dp)
                )
            }
            Text(
                text = thisSong.title,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 25.sp,
                modifier = Modifier.padding(all = 5.dp),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = thisSong.artist,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            ProgressIndicator()
        }

    }

    @Composable
    fun ProgressIndicator() {
        val currentProgress: Int by viewModel.progress.observeAsState(initial = 0)
        val totalDuration: Int by viewModel.duration.observeAsState(initial = 0)

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            LinearProgressIndicator(
                progress = { currentProgress.toFloat() / totalDuration.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Helper.formatTime(currentProgress),
                    textAlign = TextAlign.Start,
                    color = Color.White
                )
                Text(
                    text = Helper.formatTime(totalDuration),
                    textAlign = TextAlign.End,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    fun ControllerView(isPlaying: Boolean) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous",
                tint = Color.Gray,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .clickable { })
            Spacer(modifier = Modifier.width(15.dp))
            Icon(painter = if (isPlaying) painterResource(id = R.drawable.icon_pause) else painterResource(
                id = R.drawable.icon_play
            ),
                contentDescription = "Play/Pause",
                tint = Color.White,
                modifier = Modifier
                    .size(60.dp)
                    .padding(8.dp)
                    .clickable { onPlaynPause(!isPlaying) })
            Spacer(modifier = Modifier.width(15.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
                tint = Color.Gray,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .clickable { })
        }
    }

    private fun onPlaynPause(play: Boolean) {
        if (play) viewModel.resume()
        else viewModel.pause()
    }

    override fun onDestroy() {
        // Unbind from the service when the activity is destroyed
        viewModel.unbindService(this)
        super.onDestroy()
    }

}