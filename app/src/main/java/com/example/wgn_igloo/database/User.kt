package com.example.wgn_igloo.database

// database structure
data class User(
    val givenName: String = "",
    val familyName: String = "",
    val email: String = "",  // Provide default empty value
    val uid: String = "",    // Provide default empty value
    val username: String = uid  // for username
)

