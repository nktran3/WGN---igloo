package com.example.wgn_igloo

import java.util.Date

data class Member(
    val username: String = "",
    val uid: String = "",
    val friendSince: Date? = null
)
