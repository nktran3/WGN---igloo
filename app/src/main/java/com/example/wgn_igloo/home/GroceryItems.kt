package com.example.wgn_igloo.home
import com.google.firebase.Timestamp
import android.os.Parcelable
import android.os.Parcel
import kotlinx.android.parcel.Parcelize
//
data class GroceryItem(
    val documentId: String = "",
    val category: String = "",
    val expirationDate: Timestamp = Timestamp.now(),
    val dateBought: Timestamp = Timestamp.now(),
    val name: String = "",
    val quantity: Int = 0,
    val sharedWith: String = "",
    val status: Boolean = false,
    val isOwnedByUser: Boolean = true
)