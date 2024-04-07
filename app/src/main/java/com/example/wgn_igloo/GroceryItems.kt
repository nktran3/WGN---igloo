import com.google.firebase.Timestamp

//data class GroceryItem(
//    val category: String,
//    val expirationDate: Timestamp,
//    val dateBought: Timestamp,
//    val name: String,
//    val quantity: Int,
//    val sharedWith: String,
//    val status: Boolean
//)

data class GroceryItem(
    val category: String = "",
    val expirationDate: Timestamp = Timestamp.now(),
    val dateBought: Timestamp = Timestamp.now(),
    val name: String = "",
    val quantity: Int = 0,
    val sharedWith: String = "",
    val status: Boolean = false
)
