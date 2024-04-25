package com.example.wgn_igloo.grocery
import com.google.firebase.Timestamp

data class GroceryItem(
    val category: String = "",
    val expirationDate: Timestamp = Timestamp.now(),
    val dateBought: Timestamp = Timestamp.now(),
    val name: String = "",
    val quantity: Int = 0,
    val sharedWith: String = "",
    val status: Boolean = false
)
