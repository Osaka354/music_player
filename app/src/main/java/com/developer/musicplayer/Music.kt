package com.developer.musicplayer

import android.net.Uri

class Music(
    private val title: String,
    private val artist: String,
    private val duration: String,
    private val fileUri: Uri,
    var isPlaying: Boolean = false
) {
    val getTitle: String get() = title
    val getArtist: String get() = artist
    val getDuration: String get() = duration
    val getFileUrl: Uri get() = fileUri

    val titleAndName: String get() = "$title - $artist"
}