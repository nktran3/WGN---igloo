package com.example.wgn_igloo

data class User(
    val email: String = "",  // Provide default empty value
    val uid: String = "",    // Provide default empty value
    val username: String = uid  // for username
)
