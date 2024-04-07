package com.example.wgn_igloo
import com.google.firebase.Timestamp

data class ShoppingListItem(
    val category: String = "",
    val lastPurchased: Timestamp = Timestamp.now(),
    val name: String = "",
    val purchasedBy: String = ""
)
