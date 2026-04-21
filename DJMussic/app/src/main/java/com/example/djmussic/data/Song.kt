package com.example.djmussic.data

data class Song(
    val id: Long = 0,
    val title: String,
    val artist: String,
    val uri: String,
    val duration: Long = 0
)