package it.uninsubria.pdm.vellons.twitterclone.tweet

import it.uninsubria.pdm.vellons.twitterclone.user.User

data class Tweet(
    val id: String,
    val userId: String,
    var user: User?,
    val displayDate: String,
    val text: String = "",
    val source: String = "",
    val photoLink: String? = null,
    var commentCount: Int = 0,
    var retweetCount: Int = 0,
    var likeCount: Int = 0,
    var hasUserLike: Boolean = false,
)