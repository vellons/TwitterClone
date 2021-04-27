package it.uninsubria.pdm.vellons.twitterclone.user

data class User(
    val id: String,
    val name: String,
    val username: String,
    val userVerified: Boolean = false,
)