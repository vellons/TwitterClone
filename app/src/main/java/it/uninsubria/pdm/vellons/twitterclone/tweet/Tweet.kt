package it.uninsubria.pdm.vellons.twitterclone.tweet

data class Tweet(
    val position: Number,
    val id: String,
    val name: String,
    val username: String,
    val userVerified: Boolean = false,
    val displayDate: String,
    val text: String = "",
    val source: String = "",
    val commentCount: Number = 0,
    val retweetCount: Number = 0,
    val likeCount: Number = 0,
    val hasUserLike: Boolean = false,
)