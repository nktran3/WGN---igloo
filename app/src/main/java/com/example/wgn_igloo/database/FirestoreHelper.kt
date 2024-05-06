package com.example.wgn_igloo.database

import android.content.Context
import android.util.Log
import com.example.wgn_igloo.home.GroceryItem
import com.example.wgn_igloo.grocery.ShoppingListItem
import com.example.wgn_igloo.inbox.Notifications
//import com.example.wgn_igloo.home.GroceryIndividualItem
import com.example.wgn_igloo.recipe.SavedRecipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FirestoreHelper(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    companion object {
        private const val TAG = "FirestoreHelper"
    }


    fun addUser(user: User) {
        val userWithUsername = if (user.username.isEmpty()) user.copy(username = user.uid) else user
        db.collection("users").document(user.uid).set(userWithUsername)
            .addOnSuccessListener { Log.d(TAG, "User added successfully") }
            .addOnFailureListener { e -> Log.w(TAG, "Error adding user", e) }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

//    fun addGroceryItem(uid: String, item: GroceryItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        db.collection("users").document(uid).collection("groceryItems").add(item)
//            .addOnSuccessListener {
//                Log.d(TAG, "Item added successfully")
//                onSuccess()
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error adding item", e)
//                onFailure(e)
//            }
//    }
//    fun addGroceryItem(uid: String, item: GroceryItem, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
//        db.collection("users").document(uid).collection("groceryItems").add(item)
//            .addOnSuccessListener { documentReference ->
//                Log.d(TAG, "Item added successfully with ID: ${documentReference.id}")
//                onSuccess(documentReference.id)  // Pass the new document ID back
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error adding item", e)
//                onFailure(e)
//            }
//    }

    fun addGroceryItem(uid: String, item: GroceryItem, onSuccess: (GroceryItem) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(uid).collection("groceryItems").add(item)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Item added successfully with ID: ${documentReference.id}")
                val newItem = item.copy(documentId = documentReference.id)
                // Update the Firestore entry with the new documentId included
                documentReference.set(newItem)
                    .addOnSuccessListener {
                        onSuccess(newItem)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error updating item with documentId", e)
                        onFailure(e)
                    }
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


    fun addNotifications(uid: String, notif: Notifications) {
        db.collection("users").document(uid).collection("notificationItems").add(notif)
            .addOnSuccessListener {
                Log.d(TAG, "Notification added successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding notification", e)
            }
    }

    fun removeSavedRecipe(uid: String, recipe: SavedRecipe) {
        db.collection("users").document(uid)
            .collection("savedRecipes").document(recipe.recipeName).delete()
            .addOnSuccessListener {
                Log.d(TAG, "Saved recipe removed successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error removed saved recipe", e)
            }
    }

//    fun addShoppingListItem(uid: String, item: ShoppingListItem) {
//        db.collection("users").document(uid).collection("shoppingList").document(item.name).set(item)
//            .addOnSuccessListener {
//                Log.d(TAG, "Shopping list item added successfully")
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Error adding shopping list item", e)
//            }
//    }

    fun addShoppingListItem(uid: String, item: ShoppingListItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(uid).collection("shoppingList").document(item.name).set(item)
            .addOnSuccessListener {
                Log.d(TAG, "Shopping list item added successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding shopping list item", e)
                onFailure(e)
            }
    }

    fun moveItemToInventory(uid: String, item: ShoppingListItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // First, delete the item from the shopping list
        db.collection("users").document(uid).collection("shoppingList").document(item.name).delete()
            .addOnSuccessListener {
                // Create the corresponding GroceryItem
                val groceryItem = mapShoppingListItemToGroceryItem(item)
                // Add to grocery items and get the new document ID
                db.collection("users").document(uid).collection("groceryItems").add(groceryItem)
                    .addOnSuccessListener { documentReference ->
                        // Update the GroceryItem with the new documentId
                        val updatedGroceryItem = groceryItem.copy(documentId = documentReference.id)
                        documentReference.set(updatedGroceryItem)
                            .addOnSuccessListener {
                                Log.d(TAG, "Item moved to grocery items successfully with document ID: ${documentReference.id}")
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error updating item with documentId", e)
                                onFailure(e)
                            }
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


//    fun getGroceryItems(userId: String, onSuccess: (List<GroceryItem>) -> Unit, onFailure: (Exception) -> Unit) {
//        db.collection("users").document(userId).collection("groceryItems")
//            .get()
//            .addOnSuccessListener { snapshot ->
//                val items = snapshot.toObjects(GroceryItem::class.java)
//                onSuccess(items)
//            }
//            .addOnFailureListener { exception ->
//                onFailure(exception)
//            }
//    }

    fun getGroceryItems(userId: String, onSuccess: (List<GroceryItem>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId).collection("groceryItems")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.map { document ->
                    GroceryItem(
                        documentId = document.id,  // Store the document ID within each GroceryItem
                        category = document.getString("category") ?: "",
                        dateBought = document.getTimestamp("dateBought") ?: Timestamp.now(),
                        expirationDate = document.getTimestamp("expirationDate") ?: Timestamp.now(),
                        name = document.getString("name") ?: "",
                        quantity = (document.getLong("quantity") ?: 0L).toInt(),
                        sharedWith = document.getString("sharedWith") ?: "",
                        status = document.getBoolean("status") ?: false,
                        isOwnedByUser = document.getBoolean("isOwnedByUser") ?: true
                    )
                }
                onSuccess(items)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching grocery items: ", exception)
                onFailure(exception)
            }
    }

    fun fetchGroceryItemsForUser(userId: String, onSuccess: (List<GroceryItem>) -> Unit, onFailure: (Exception) -> Unit) {
        val userRef = db.collection("users").document(userId).collection("groceryItems")
        userRef.get().addOnSuccessListener { snapshot ->
            val ownedItems = snapshot.toObjects(GroceryItem::class.java)
            // Fetch items shared with this user
            db.collectionGroup("groceryItems")
                .whereEqualTo("sharedWith", userId)
                .get()
                .addOnSuccessListener { sharedSnapshot ->
                    val sharedItems = sharedSnapshot.toObjects(GroceryItem::class.java)
                    onSuccess(ownedItems + sharedItems)  // Combine owned and shared items
                }
                .addOnFailureListener(onFailure)
        }
            .addOnFailureListener(onFailure)
    }


    fun getSavedRecipe(userId: String, onSuccess: (List<SavedRecipe>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId).collection("savedRecipes")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.toObjects(SavedRecipe::class.java)
                onSuccess(items)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
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
            quantity = item.quantity,  // Default quantity
            sharedWith = "",  // No shared user by default
            status = true,  // Assuming the item is active/available
            isOwnedByUser = true
        )
    }

//    fun shareGroceryItem(userId: String, itemId: String, friendUserId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        val itemRef = db.collection("users").document(userId).collection("groceryItems").document(itemId)
//        itemRef.update("sharedWith", friendUserId)
//            .addOnSuccessListener {
//                Log.d(TAG, "Grocery item shared successfully")
//                onSuccess()
//            }
//            .addOnFailureListener { exception ->
//                Log.w(TAG, "Error sharing grocery item", exception)
//                onFailure(exception)
//            }
//    }

    fun shareGroceryItem(userId: String, itemId: String, friendUserId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val itemRef = db.collection("users").document(userId).collection("groceryItems").document(itemId)
        itemRef.update("sharedWith", friendUserId)
            .addOnSuccessListener {
                itemRef.get().addOnSuccessListener { documentSnapshot ->
                    val item = documentSnapshot.toObject(GroceryItem::class.java)
                    item?.let {
                        db.collection("users").document(friendUserId).collection("sharedItems").document(itemId).set(it)
                            .addOnSuccessListener {
                                Log.d(TAG, "Grocery item shared successfully and added to friend's shared items")
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error adding item to friend's shared items", e)
                                onFailure(e)
                            }
                    }
                }
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



//    fun updateUsername(uid: String, newUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        db.collection("users").document(uid).update("uid", newUsername)
//            .addOnSuccessListener {
//                Log.d(TAG, "Username updated successfully")
//                onSuccess()
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error updating username", e)
//                onFailure(e)
//            }
//    }

    fun getUserByUid(uid: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    onSuccess(documentSnapshot.toObject(User::class.java)!!)
                } else {
                    onFailure(Exception("No user found with UID: $uid"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun addFriend(currentUserId: String, friendUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").whereEqualTo("username", friendUsername).limit(1).get()
            .addOnSuccessListener { querySnapshot ->
                val friendUser = querySnapshot.documents.firstOrNull()?.toObject(User::class.java)
                if (friendUser != null) {
                    getCurrentUserEmail { currentUserEmail ->
                        getCurrentUsername { currentUsername ->
                            val friendDataForCurrentUser = mapOf(
                                "email" to friendUser.email,
                                "uid" to friendUser.uid,
                                "username" to friendUser.username,
                                "friendSince" to Timestamp.now()
                            )

                            val currentUserDataForFriend = mapOf(
                                "email" to currentUserEmail,
                                "uid" to currentUserId,
                                "username" to currentUsername,
                                "friendSince" to Timestamp.now(),

                            )

                            val batch = db.batch()
                            batch.set(db.collection("users").document(currentUserId).collection("friends").document(friendUser.uid), friendDataForCurrentUser)
                            batch.set(db.collection("users").document(friendUser.uid).collection("friends").document(currentUserId), currentUserDataForFriend)
                            getUser(currentUserId,
                                onSuccess = { user ->
                                    val notification = Notifications(
                                        title = "Friend Request",
                                        message = "You are now friends with ${friendUser.givenName}"
                                    )
                                    val friendNotification = Notifications(
                                        title = "Friend Request",
                                        message = "You are now friends with ${user.givenName}"
                                    )
                                    addNotifications(currentUserId, notification)
                                    addNotifications(friendUser.uid, friendNotification)
                                },
                                onFailure = { exception ->
                                    Log.d(TAG, exception.toString())
                                }
                                )


                            batch.commit()
                                .addOnSuccessListener {
                                    Log.d(TAG, "Friendship established successfully")
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error establishing friendship", e)
                                    onFailure(e)
                                }
                            }
                        }
                    } else {
                        onFailure(Exception("Friend not found with username: $friendUsername"))
                    }
                }
                    .addOnFailureListener { e ->
                        onFailure(e)
                    }
            }
        fun getCurrentUserEmail(onResult: (String) -> Unit) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        onResult(email)
    }

    fun getCurrentUsername(onResult: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: ""
                    onResult(username)
                }
                .addOnFailureListener {
                    Log.e(TAG, "Error fetching username")
                    onResult("")  // Handle the error case by returning an empty string
                }
        } else {
            onResult("")  // Return empty if uid is null
        }
    }

    fun updateUsername(uid: String, newUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(uid).update("username", newUsername)
            .addOnSuccessListener {
                Log.d(TAG, "User's username updated successfully.")
                updateFriendsWithNewUsername(uid, newUsername, onSuccess, onFailure)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating user's username: ", exception)
                if (exception is FirebaseFirestoreException && exception.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                    Log.e(TAG, "Missing index for query. Consider adding it via Firebase console.")
                    // Provide the direct link to create the index if available or suggest checking Firestore documentation
                }
                onFailure(exception)
            }
    }

    private fun updateFriendsWithNewUsername(userId: String, newUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collectionGroup("friends")
            .whereEqualTo("uid", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG, "No documents found to update.")
                    onSuccess()
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                documents.forEach { documentSnapshot ->
                    val friendRef = documentSnapshot.reference
                    batch.update(friendRef, "username", newUsername)
                }

                batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "All friend references updated successfully.")
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error updating friend references: ", exception)
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to find friends for username update: ", exception)
                onFailure(exception)
            }
    }

    fun parseTimestamp(dateStr: String): Timestamp {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return try {
            val date = dateFormat.parse(dateStr) ?: Date()
            Timestamp(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date string: $dateStr", e)
            Timestamp.now()  // Return current timestamp as fallback
        }
    }

//    fun updateGroceryItem(item: GroceryItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return onFailure(Exception("User not logged in"))
//
//        db.collection("users").document(userUid).collection("groceryItems").document(item.name)
//            .set(item)
//            .addOnSuccessListener {
//                Log.d(TAG, "Grocery item updated successfully")
//                onSuccess()
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error updating grocery item", e)
//                onFailure(e)
//            }
//    }
fun updateGroceryItem(userId: String, itemId: String, fieldsToUpdate: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    // Ensure both userId and itemId are valid
    if (userId.isBlank() || itemId.isBlank()) {
        onFailure(IllegalArgumentException("Invalid user ID or item ID"))
        return
    }

    db.collection("users").document(userId).collection("groceryItems").document(itemId)
        .update(fieldsToUpdate)
        .addOnSuccessListener {
            Log.d(TAG, "Grocery item updated successfully")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "Error updating grocery item: ${e.message}", e)
            onFailure(e)
        }
}


//    fun deleteGroceryItem(userId: String, itemName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        db.collection("users").document(userId).collection("groceryItems").document(itemName)
//            .delete()
//            .addOnSuccessListener {
//                Log.d(TAG, "Item deleted successfully")
//                onSuccess()
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error deleting item", e)
//                onFailure(e)
//            }
//    }
//    fun deleteGroceryItem(userId: String, itemName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        Log.d(TAG, "Attempting to delete item: $itemName for user: $userId")
////        val docRef = db.collection("users").document(userId).collection("groceryItems").whereEqualTo("name", itemName).get()
//        val docRef = db.collection("users").document(userId).collection("groceryItems").whereEqualTo("name", itemName).get()
//        docRef.addOnSuccessListener { documents ->
//            if (documents.isEmpty) {
//                Log.w(TAG, "No item found with name: $itemName for deletion")
//            } else {
//                for (document in documents) {
//                    db.collection("users").document(userId).collection("groceryItems").document(document.id).delete()
//                        .addOnSuccessListener {
//                            Log.d(TAG, "Item deleted successfully: ${document.id}")
//                            onSuccess()
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e(TAG, "Error deleting item: ${document.id}", e)
//                            onFailure(e)
//                        }
//                }
//            }
//        }.addOnFailureListener { e ->
//            Log.e(TAG, "Failed to retrieve item for deletion: $itemName", e)
//            onFailure(e)
//        }
//    }

//    fun deleteGroceryItem(userId: String, documentId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        db.collection("users").document(userId).collection("groceryItems").document(documentId)
//            .delete()
//            .addOnSuccessListener {
//                Log.d(TAG, "Item deleted successfully: $documentId")
//                onSuccess()
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error deleting item: $documentId", e)
//                onFailure(e)
//            }
//    }

    fun deleteGroceryItem(userId: String, documentId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (documentId.isBlank()) {
            Log.e(TAG, "Invalid document ID")
            onFailure(IllegalArgumentException("Invalid document ID"))
            return
        }

        db.collection("users").document(userId).collection("groceryItems").document(documentId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Item deleted successfully: $documentId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting item: $documentId", e)
                onFailure(e)
            }
    }

//    fun moveItemToShoppingList(userId: String, itemName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        val groceryItemRef = db.collection("users").document(userId).collection("groceryItems").whereEqualTo("name", itemName)
//
//        groceryItemRef.get().addOnSuccessListener { documents ->
//            if (documents.isEmpty) {
//                Log.w(TAG, "No item found with name: $itemName")
//                onFailure(Exception("No item found with name: $itemName"))
//            } else {
//                val document = documents.documents.first()  // Assuming name is unique, so we take the first document.
//                val item = document.toObject(GroceryItem::class.java)
//                if (item != null) {
//                    // Add to shopping list
//                    db.collection("users").document(userId).collection("shoppingList").document(itemName).set(item)
//                        .addOnSuccessListener {
//                            // Delete from grocery items
//                            db.collection("users").document(userId).collection("groceryItems").document(document.id).delete()
//                                .addOnSuccessListener {
//                                    Log.d(TAG, "Item moved to shopping list successfully and removed from grocery items")
//                                    onSuccess()
//                                }
//                                .addOnFailureListener { e ->
//                                    Log.e(TAG, "Error deleting item from grocery items", e)
//                                    onFailure(e)
//                                }
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e(TAG, "Error adding item to shopping list", e)
//                            onFailure(e)
//                        }
//                } else {
//                    Log.w(TAG, "Failed to parse the grocery item")
//                    onFailure(Exception("Failed to parse the grocery item"))
//                }
//            }
//        }.addOnFailureListener { e ->
//            Log.e(TAG, "Error retrieving grocery item", e)
//            onFailure(e)
//        }
//    }

    // Add this line
    var currentInventoryUserId: String? = null
    fun exampleMethod() {
        this.currentInventoryUserId = "exampleUserId"
    }
//    fun moveItemToShoppingList(userId: String, friendUserId: String, itemName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        // First, find the grocery item in the current user's grocery list.
//        val groceryItemRef = db.collection("users").document(userId).collection("groceryItems").whereEqualTo("name", itemName)
//
//        groceryItemRef.get().addOnSuccessListener { documents ->
//            if (documents.isEmpty) {
//                Log.w(TAG, "No item found with name: $itemName")
//                onFailure(Exception("No item found with name: $itemName"))
//                return@addOnSuccessListener
//            }
//
//            // Assuming the item name is unique, take the first document.
//            val document = documents.documents.first()
//            val item = document.toObject(GroceryItem::class.java)
//            if (item != null) {
//                // Create a batch to perform multiple writes atomically.
//                val batch = db.batch()
//
//                // Prepare the shopping list item for the current user.
//                val userShoppingListRef = db.collection("users").document(userId).collection("shoppingList").document(itemName)
//                batch.set(userShoppingListRef, item)
//
//                // Prepare the shopping list item for the friend user.
//                val friendShoppingListRef = db.collection("users").document(friendUserId).collection("shoppingList").document(itemName)
//                batch.set(friendShoppingListRef, item)
//
//                // Commit the batch.
//                batch.commit().addOnSuccessListener {
//                    Log.d(TAG, "Item successfully added to both shopping lists.")
//                    onSuccess()
//                }.addOnFailureListener { e ->
//                    Log.e(TAG, "Error adding item to shopping lists", e)
//                    onFailure(e)
//                }
//            } else {
//                Log.w(TAG, "Failed to parse the grocery item")
//                onFailure(Exception("Failed to parse the grocery item"))
//            }
//        }.addOnFailureListener { e ->
//            Log.e(TAG, "Error retrieving grocery item", e)
//            onFailure(e)
//        }
//    }
    fun moveItemToShoppingList(userId: String, friendUserId: String, itemName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        Log.d(TAG, "Attempting to move item: $itemName for user: $userId from user: $friendUserId's shopping list")
    //    val groceryItemRef = db.collection("users").document("M924sweDE1WZkYnTAF9MkpeTQX33").collection("groceryItems").whereEqualTo("name", itemName)
        val groceryItemRef = db.collection("users").document(friendUserId).collection("groceryItems").whereEqualTo("name", itemName)
        Log.d(TAG, "Querying for item: $itemName in /users/$friendUserId/groceryItems where name is equal to $itemName")

        groceryItemRef.get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                Log.w(TAG, "No item found with name: $itemName")
                onFailure(Exception("No item found with name: $itemName"))
                return@addOnSuccessListener
            }

            val item = documents.documents.firstOrNull()?.toObject(GroceryItem::class.java)
            if (item != null) {
                val batch = db.batch()

                // Log the document data for debugging
                Log.d(TAG, "Found item: ${item.name}, preparing to add to shopping lists.")

                // Add item to the current user's shopping list
                val userShoppingListRef = db.collection("users").document(userId).collection("shoppingList").document(itemName)
                batch.set(userShoppingListRef, item)

                // Add item to the friend's shopping list
                val friendShoppingListRef = db.collection("users").document(friendUserId).collection("shoppingList").document(itemName)
                batch.set(friendShoppingListRef, item)

                batch.commit().addOnSuccessListener {
                    Log.d(TAG, "Item successfully added to both shopping lists.")
                    onSuccess()
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error adding item to shopping lists", e)
                    onFailure(e)
                }
            } else {
                Log.w(TAG, "Failed to parse the grocery item")
                onFailure(Exception("Failed to parse the grocery item"))
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error retrieving grocery item", e)
            onFailure(e)
        }
    }


//    fun addShoppingItem(currentUserId: String, friendUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        db.collection("users").document(userId).collection("groceryItems").whereEqualTo("name", itemName)
//            .addOnSuccessListener { querySnapshot ->
//                val friendUser = querySnapshot.documents.firstOrNull()?.toObject(User::class.java)
//                if (friendUser != null) {
//                    getCurrentUserEmail { currentUserEmail ->
//                        getCurrentUsername { currentUsername ->
//                            val friendDataForCurrentUser = mapOf(
//                                "email" to friendUser.email,
//                                "uid" to friendUser.uid,
//                                "username" to friendUser.username,
//                                "friendSince" to Timestamp.now()
//                            )
//
//                            val currentUserDataForFriend = mapOf(
//                                "email" to currentUserEmail,
//                                "uid" to currentUserId,
//                                "username" to currentUsername,
//                                "friendSince" to Timestamp.now(),
//
//                                )
//
//                            val batch = db.batch()
//                            batch.set(db.collection("users").document(currentUserId).collection("friends").document(friendUser.uid), friendDataForCurrentUser)
//                            batch.set(db.collection("users").document(friendUser.uid).collection("friends").document(currentUserId), currentUserDataForFriend)
//                            getUser(currentUserId,
//                                onSuccess = { user ->
//                                    val notification = Notifications(
//                                        title = "Friend Request",
//                                        message = "You are now friends with ${friendUser.givenName}"
//                                    )
//                                    val friendNotification = Notifications(
//                                        title = "Friend Request",
//                                        message = "You are now friends with ${user.givenName}"
//                                    )
//                                    addNotifications(currentUserId, notification)
//                                    addNotifications(friendUser.uid, friendNotification)
//                                },
//                                onFailure = { exception ->
//                                    Log.d(TAG, exception.toString())
//                                }
//                            )
//
//
//                            batch.commit()
//                                .addOnSuccessListener {
//                                    Log.d(TAG, "Friendship established successfully")
//                                    onSuccess()
//                                }
//                                .addOnFailureListener { e ->
//                                    Log.w(TAG, "Error establishing friendship", e)
//                                    onFailure(e)
//                                }
//                        }
//                    }
//                } else {
//                    onFailure(Exception("Friend not found with username: $friendUsername"))
//                }
//            }
//            .addOnFailureListener { e ->
//                onFailure(e)
//            }
//    }
////    fun getCurrentUserEmail(onResult: (String) -> Unit) {
////        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
////        onResult(email)
////    }

}
