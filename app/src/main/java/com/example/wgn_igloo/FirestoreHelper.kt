package com.example.wgn_igloo

import com.example.wgn_igloo.GroceryItem
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.Timestamp

class FirestoreHelper(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "FirestoreHelper"
    }

    fun addUser(user: User) {
        db.collection("users").document(user.uid).set(user)
            .addOnSuccessListener { Log.d(TAG, "User added successfully") }
            .addOnFailureListener { e -> Log.w(TAG, "Error adding user", e) }
    }

    fun addGroceryItem(uid: String, item: GroceryItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(uid).collection("groceryItems").add(item)
            .addOnSuccessListener {
                Log.d(TAG, "Item added successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding item", e)
                onFailure(e)
            }
    }


    fun addSavedRecipe(uid: String, recipe: SavedRecipe) {
        db.collection("users").document(uid)
            .collection("savedRecipes").document(recipe.recipeName).set(recipe)
            .addOnSuccessListener {
                Log.d(TAG, "Saved recipe added successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding saved recipe", e)
            }
    }

//    fun addShoppingListItem(uid: String, item: ShoppingListItem) {
//        db.collection("users").document(uid).collection("shoppingList").add(item)
////        db.collection("users").document(uid)
////            .collection("shoppingList").document(item.name).add(item)
//            .addOnSuccessListener {
//                Log.d(TAG, "Shopping list item added successfully")
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Error adding shopping list item", e)
//            }
//    }

    fun addShoppingListItem(uid: String, item: ShoppingListItem) {
        db.collection("users").document(uid).collection("shoppingList").document(item.name).set(item)
            .addOnSuccessListener {
                Log.d(TAG, "Shopping list item added successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding shopping list item", e)
            }
    }

    fun moveItemToInventory(uid: String, item: ShoppingListItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Remove from shopping list
        db.collection("users").document(uid).collection("shoppingList").document(item.name).delete()
            .addOnSuccessListener {
                // Add to grocery items with a unique name
                val groceryItem = mapShoppingListItemToGroceryItem(item)
//                db.collection("users").document(uid).collection("groceryItems").add(item)
                db.collection("users").document(uid).collection("groceryItems").add(groceryItem)
//                db.collection("users").document(uid).collection("groceryItems").document(groceryItem.name).set(groceryItem)
                    .addOnSuccessListener {
                        Log.d(TAG, "Item moved to grocery items successfully")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding item to grocery items", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error removing item from shopping list", e)
                onFailure(e)
            }
    }

    private fun mapShoppingListItemToGroceryItem(item: ShoppingListItem): GroceryItem {
        // Generate a unique identifier for the grocery item - not needed
        val uniqueName = "${item.name}_${System.currentTimeMillis()}"
        return GroceryItem(
            category = item.category,
            expirationDate = Timestamp.now(),  // Assuming current time as expiration date
            dateBought = item.lastPurchased,
            name = item.name,  // Use the generated unique name
            quantity = 1,  // Default quantity
            sharedWith = "",  // No shared user by default
            status = true  // Assuming the item is active/available
        )
    }

    fun shareGroceryItem(userId: String, itemId: String, friendUserId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val itemRef = db.collection("users").document(userId).collection("groceryItems").document(itemId)
        itemRef.update("sharedWith", friendUserId)
            .addOnSuccessListener {
                Log.d(TAG, "Grocery item shared successfully")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error sharing grocery item", exception)
                onFailure(exception)
            }
    }

    fun getUser(userId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                user?.let { onSuccess(it) }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting user", e)
                onFailure(e)
            }
    }

    fun updateUsername(uid: String, newUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(uid).update("uid", newUsername)
            .addOnSuccessListener {
                Log.d(TAG, "Username updated successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating username", e)
                onFailure(e)
            }
    }


    fun addFriend(currentUserId: String, friendUserId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val friendRef = db.collection("users").document(currentUserId).collection("friendList").document(friendUserId)
        friendRef.set(mapOf("uid" to friendUserId))
            .addOnSuccessListener {
                Log.d(TAG, "Friend added successfully")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error adding friend", exception)
                onFailure(exception)
            }
    }

    fun fetchGroceryItems(uid: String, onSuccess: (List<GroceryItem>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(uid).collection("groceryItems")
            .get()
            .addOnSuccessListener { result ->
                val items = result.toObjects(GroceryItem::class.java)
                onSuccess(items)
            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.w(TAG, "Error fetching grocery items", e)
            }
    }

}
