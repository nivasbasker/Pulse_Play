package com.zio.pulseplay.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.palette.graphics.Palette
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.zio.pulseplay.data.Song
import org.json.JSONObject

/**
 * Helper class for the following functions:
 *  * Get the prominent color of an image from its bitmap
 *  * Fetch all songs from the fixed API
 *  * Format duration in minutes to mm:ss form
 */
class Helper(context: Context) {


    private val queue = Volley.newRequestQueue(context)
    private val url = "https://cms.samespace.com/items/songs"

    fun fetchSongs(onSuccess: (List<Song>) -> Unit, onError: (String) -> Unit) {
        Log.d("TAG", "Fetching songs")

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // Parse the JSON response and create a list of Song objects
                val songs = parseJsonResponse(response.toString())
                onSuccess(songs)
            },
            { error ->
                onError("Error fetching songs: ${error.message}")
                error.printStackTrace()
            }
        )

        // Add the request to the RequestQueue
        queue.add(request)
    }

    private fun parseJsonResponse(response: String): List<Song> {
        val songs = mutableListOf<Song>()
        val rootJsonObject = JSONObject(response)

        // Extract the "data" array
        val dataArray = rootJsonObject.getJSONArray("data")
        for (i in 0 until dataArray.length()) {
            val songObject: JSONObject = dataArray.getJSONObject(i)
            // Extract relevant information from the JSON object
            val id = songObject.getInt("id")
            val songUrl = songObject.getString("url")
            val imageUrl = "https://cms.samespace.com/assets/${songObject.getString("cover")}"
            val title = songObject.getString("name")
            val artist = songObject.getString("artist")
            val accent = songObject.getString("accent")

            val song = Song(id, songUrl, imageUrl, title, artist, accent)
            songs.add(song)
        }

        return songs
    }

    interface PaletteCallback {
        fun onPaletteGenerated(color1: Int, color2: Int)
    }


    companion object {

        // all constants
        const val LOGTAG = "PP_LOG"
        val dummySongList: List<Song> = listOf()
        val dummySong = Song(
            id = 0,
            songUrl = "",
            coverUrl = "",
            title = "Unknown Song",
            artist = "Unknown Artist",
            accent = "#000000"
        )

        fun createPaletteAsync(bitmap: Bitmap, callback: PaletteCallback) {
            Palette.from(bitmap).generate(object : Palette.PaletteAsyncListener {
                override fun onGenerated(palette: Palette?) {
                    // Use the generated instance.
                    val color1 = palette?.getDarkMutedColor(Color.BLACK) ?: Color.BLACK
                    val color2 = palette?.getLightMutedColor(Color.BLACK) ?: Color.BLACK

                    // Call the callback function with the colors
                    callback.onPaletteGenerated(color1, color2)
                }
            })
        }

        fun formatTime(timeInMillis: Int): String {
            val totalSeconds = timeInMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60

            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}