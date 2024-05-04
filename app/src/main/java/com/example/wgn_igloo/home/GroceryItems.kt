package com.example.wgn_igloo.home
import com.google.firebase.Timestamp
import android.os.Parcelable
import android.os.Parcel
import kotlinx.android.parcel.Parcelize
//
data class GroceryItem(
    val category: String = "",
    val expirationDate: Timestamp = Timestamp.now(),
    val dateBought: Timestamp = Timestamp.now(),
    val name: String = "",
    val quantity: Int = 0,
    val sharedWith: String = "",
    val status: Boolean = false,
    val isOwnedByUser: Boolean = true
)

//data class GroceryItem(
//    val category: String,
//    val expirationDate: Timestamp,
//    val dateBought: Timestamp,
//    val name: String,
//    val quantity: Int,
//    val sharedWith: String,
//    val status: Boolean,
//    val isOwnedByUser: Boolean
//) : Parcelable {
//    constructor(parcel: Parcel) : this(
//        parcel.readString() ?: "",
//        parcel.readParcelable(Timestamp::class.java.classLoader) ?: Timestamp.now(),
//        parcel.readParcelable(Timestamp::class.java.classLoader) ?: Timestamp.now(),
//        parcel.readString() ?: "",
//        parcel.readInt(),
//        parcel.readString() ?: "",
//        parcel.readByte() != 0.toByte(),
//        parcel.readByte() != 0.toByte()
//    )
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(category)
//        parcel.writeParcelable(expirationDate, flags)
//        parcel.writeParcelable(dateBought, flags)
//        parcel.writeString(name)
//        parcel.writeInt(quantity)
//        parcel.writeString(sharedWith)
//        parcel.writeByte(if (status) 1 else 0)
//        parcel.writeByte(if (isOwnedByUser) 1 else 0)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<GroceryItem> {
//        override fun createFromParcel(parcel: Parcel): GroceryItem {
//            return GroceryItem(parcel)
//        }
//
//        override fun newArray(size: Int): Array<GroceryItem?> {
//            return arrayOfNulls(size)
//        }
//    }
//}