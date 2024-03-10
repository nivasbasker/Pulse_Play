package com.zio.pulseplay.ui

import android.content.Context
import android.os.Vibrator
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.zio.pulseplay.data.Song
import com.zio.pulseplay.util.Helper
import com.zio.pulseplay.util.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Player(val viewModel: MainViewModel, val context: Context) {

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun minPlayer(playingSong: Song, playing: Boolean, startColor: Int, onRequest: () -> Unit) {

        LaunchedEffect(playingSong) {
            withContext(Dispatchers.IO) {
                viewModel.updateRecent(playingSong, context)
            }
        }

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

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
            Icon(imageVector = if (playing) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp)
                    .clickable {
                        vibrator.cancel()
                        vibrator.vibrate(100)
                        //onPlaynPause(!playing)
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
                    start = Offset.Zero,
                    end = Offset.Infinite
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
                tint = Color.White,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .clickable { })
            Spacer(modifier = Modifier.width(15.dp))
            Icon(imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = Color.White,
                modifier = Modifier
                    .size(60.dp)
                    .padding(8.dp)
                    .clickable { //onPlaynPause(!isPlaying)
                    })
            Spacer(modifier = Modifier.width(15.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .clickable { })
        }
    }
}