import com.google.firebase.Timestamp

data class GroceryItem(
    val category: String,
    val expirationDate: Timestamp,
    val dateBought: Timestamp,
    val name: String,
    val quantity: Int,
    val sharedWith: String,
    val status: Boolean
)