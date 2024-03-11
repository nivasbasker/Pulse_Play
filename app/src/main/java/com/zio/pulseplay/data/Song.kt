package com.zio.pulseplay.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "url")
    val songUrl: String,
    @ColumnInfo(name = "cover")
    val coverUrl: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "artist")
    var artist: String,
    @ColumnInfo(name = "accent")
    var accent: String

) {
    @ColumnInfo(name = "time")
    var lastAccess: Long = 0

    constructor(lastAccess: Long, song: Song) : this(
        song.id,
        song.songUrl,
        song.coverUrl,
        song.title,
        song.artist,
        song.accent,
    ) {
        this.lastAccess = lastAccess
    }

}