package com.example.wgn_igloo.profile

import java.util.Date

data class Member(
    val username: String = "",
    val uid: String = "",
    val givenName: String = "",
    val familyName: String = "",
    val friendSince: Date? = null,
    val isCurrentUser: Boolean = false
)
