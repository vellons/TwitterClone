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
    var commentCount: Number = 0,
    var retweetCount: Number = 0,
    var likeCount: Number = 0,
    var hasUserLike: Boolean = false,
)