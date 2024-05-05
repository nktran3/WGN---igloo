package com.example.wgn_igloo.database

import java.util.Date

data class Member(
    val username: String = "",
    val uid: String = "",
    val givenName: String = "",
    val familyName: String = "",
    val friendSince: Date? = null,
    val isCurrentUser: Boolean = false
)
