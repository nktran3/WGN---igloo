package com.example.wgn_igloo.grocery
import com.google.firebase.Timestamp

data class GroceryListItem(
    val category: String = "",
    val lastPurchased: Timestamp = Timestamp.now(),
    val name: String = "",
    val quantity: Int = 0,
    val purchasedBy: String = "",
)
