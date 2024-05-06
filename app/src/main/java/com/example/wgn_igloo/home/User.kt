package com.example.wgn_igloo.home

//data class User(
//    val email: String = "",
//    var uid: String = "",
//    val username: String = ""
//)

data class User(
    val givenName: String = "",
    val familyName: String = "",
    val email: String = "",  // Provide default empty value
    var uid: String = "",    // Provide default empty value
    val username: String = uid  // for username
)