package com.example.wgn_igloo.account

import java.util.Date

data class Friend(
    val username: String = "",
    val uid: String = "",
    val givenName: String = "",
    val familyName: String = "",
    val friendSince: Date? = null,
    val isCurrentUser: Boolean = false
)
