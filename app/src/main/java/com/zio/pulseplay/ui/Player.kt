package com.zio.pulseplay.ui

import android.content.Context
import android.graphics.Color.parseColor
import android.os.Vibrator
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.zio.pulseplay.R
import com.zio.pulseplay.data.Song
import com.zio.pulseplay.util.Helper

class Player(val baseContext: Context) {

    private val PLAYVIBRATION: Long = 70
    private val PAUSEVIBRATION: Long = 30

    @Composable
    fun PlayerView(
        currentSong: Song,
        state: Int,
        currentProgress: Int,
        totalDuration: Int,
        onRequest: (play: Boolean) -> Unit
    ) {

        var isMinimized by remember { mutableStateOf(true) }

        /*var startColor by remember { mutableStateOf(0) }
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
                    Helper.createPaletteAsync(resource, callback)
                }
            })

         */

        val fullScreenHeight = LocalView.current.height
        val offset by animateIntOffsetAsState(
            targetValue = if (isMinimized) {
                IntOffset(0, Integer.max(2000, fullScreenHeight))
            } else {
                IntOffset.Zero
            }, animationSpec = tween(durationMillis = 1000), label = "offset"
        )

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            if (state > 0 && state < 4) minPlayer(playingSong = currentSong,
                playing = (state == 1),
                onExpandRequest = {
                    isMinimized = false
                },
                onRequest = {
                    onRequest(it)
                })
            Spacer(
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth()
            )
        }
        FullPlayer(offset = offset,
            currentSong = currentSong,
            state = state,
            currentProgress = currentProgress,
            totalDuration = totalDuration,
            onCollapseRequest = {
                isMinimized = true
            },
            onRequest = {
                onRequest(it)
            })
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun minPlayer(
        playingSong: Song,
        playing: Boolean,
        onExpandRequest: () -> Unit,
        onRequest: (play: Boolean) -> Unit,
    ) {

        val vibrator = baseContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                //openPlayer(playingSong, if (playing) 1 else 2);
                onExpandRequest()
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount > -50.0) {
                        onExpandRequest()
                    }
                }
            }
            .background(Color(parseColor(playingSong.accent)))
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
            Icon(painter = if (playing) painterResource(id = R.drawable.icon_pause)
            else painterResource(id = R.drawable.icon_play),
                contentDescription = "Play/Pause",
                tint = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp)
                    .clickable {
                        vibrator.cancel()
                        if (playing)
                            vibrator.vibrate(PAUSEVIBRATION)
                        else vibrator.vibrate(PLAYVIBRATION)
                        //onPlaynPause(!playing)
                        onRequest(!playing)
                    })
        }
    }

    @Composable
    fun FullPlayer(
        offset: IntOffset,
        currentSong: Song,
        state: Int,
        currentProgress: Int,
        totalDuration: Int,
        onCollapseRequest: () -> Unit,
        onRequest: (play: Boolean) -> Unit
    ) {

        Column(modifier = Modifier
            .offset { offset }
            .fillMaxSize()
            .animateContentSize()
            .background(
                Brush.linearGradient(
                    0.0f to Color(parseColor(currentSong.accent)),
                    500.0f to Color.Black,
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount > 50.0) {
                        onCollapseRequest()
                    }

                }
            }, verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                DetailView(currentSong, currentProgress, totalDuration)
            }
            ControllerView(state == 1) {
                onRequest(it)
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun DetailView(thisSong: Song, currentProgress: Int, totalDuration: Int) {
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
            ProgressIndicator(currentProgress, totalDuration)
        }

    }

    @Composable
    fun ProgressIndicator(currentProgress: Int, totalDuration: Int) {

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
    fun ControllerView(isPlaying: Boolean, onRequest: (play: Boolean) -> Unit) {
        val vibrator = baseContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .clickable { //TODO : prev jump
                    })
            Spacer(modifier = Modifier.width(15.dp))
            Icon(painter = if (isPlaying) painterResource(id = R.drawable.icon_pause)
            else painterResource(id = R.drawable.icon_play),
                contentDescription = "Play/Pause",
                tint = Color.White,
                modifier = Modifier
                    .size(60.dp)
                    .padding(8.dp)
                    .clickable { //onPlaynPause(!isPlaying)
                        vibrator.cancel()
                        if (isPlaying)
                            vibrator.vibrate(PAUSEVIBRATION)
                        else vibrator.vibrate(PLAYVIBRATION)
                        onRequest(!isPlaying)
                    })
            Spacer(modifier = Modifier.width(15.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .clickable { //TODO : next jump
                    })
        }
    }
}