package com.example.wgn_igloo

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

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

//    fun addGroceryItem(uid: String, item: GroceryItem) {
//        db.collection("User").document(uid)
//            .collection("groceryItems").document(item.Name).set(item)
//            .addOnSuccessListener { /* Handle success */ }
//            .addOnFailureListener { /* Handle error */ }
//    }

//    fun addSavedRecipe(uid: String, recipe: SavedRecipe) {
//        db.collection("User").document(uid)
//            .collection("savedRecipe").document(recipe.Name).set(recipe)
//            .addOnSuccessListener { /* Handle success */ }
//            .addOnFailureListener { /* Handle error */ }
//    }
//
//    fun addShoppingListItem(uid: String, item: ShoppingListItem) {
//        db.collection("User").document(uid)
//            .collection("shoppingList").document(item.Name).set(item)
//            .addOnSuccessListener { /* Handle success */ }
//            .addOnFailureListener { /* Handle error */ }
//    }

    fun getUser(userId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                user?.let { onSuccess(it) }
            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.w("FirestoreHelper", "Error getting user", e)
            }
    }

    // Add other Firestore operations here...
}
