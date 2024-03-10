package com.zio.pulseplay.ui

import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.zio.pulseplay.data.Song
import com.zio.pulseplay.util.Helper
import com.zio.pulseplay.util.MainViewModel

class Fragments(val viewModel: MainViewModel) {

    var mySongs: List<Song> = listOf()

    @Composable
    fun TopTracksFragment() {

        val allSongs_: List<Song> by viewModel.allSongs.observeAsState(initial = mySongs)
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "All Tracks",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                modifier = Modifier.padding(all = 15.dp),
                fontStyle = FontStyle.Italic
            )
            LazyColumn {
                items(allSongs_) { song ->
                    SongCard(thisSong = song)
                }
            }

        }
    }

    @Composable
    fun ForYouFragment() {
        val allSongs_: List<Song> by viewModel.recentSongs.observeAsState(initial = mySongs)
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Your Recent Favourites",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                modifier = Modifier.padding(all = 15.dp),
                fontStyle = FontStyle.Italic
            )
            LazyColumn {
                items(allSongs_) { song ->
                    SongCard(thisSong = song)
                }
            }

        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun SongCard(thisSong: Song) {
        Row(modifier = Modifier
            .padding(all = 10.dp)
            .fillMaxSize()
            .clickable {
                viewModel.play(thisSong)

            }) {

            GlideImage(
                model = thisSong.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))

            Column() {
                Text(
                    text = thisSong.title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(all = 5.dp),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = thisSong.artist,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

            }
        }
    }

}