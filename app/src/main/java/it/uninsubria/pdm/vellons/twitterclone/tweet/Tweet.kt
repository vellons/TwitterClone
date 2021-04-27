package it.uninsubria.pdm.vellons.twitterclone.tweet

import it.uninsubria.pdm.vellons.twitterclone.user.User

data class Tweet(
    val position: Number,
    val id: String,
    val user: User,
    val displayDate: String,
    val text: String = "",
    val source: String = "",
    var commentCount: Number = 0,
    var retweetCount: Number = 0,
    var likeCount: Number = 0,
    var hasUserLike: Boolean = false,
)