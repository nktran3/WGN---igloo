package com.example.wgn_igloo

import GroceryItem
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

    fun addGroceryItem(uid: String, item: GroceryItem) {
        db.collection("users").document(uid)
            .collection("groceryItems").document(item.name) // Ensure item.name is capitalized correctly as in the GroceryItem class
            .set(item)
            .addOnSuccessListener {
                // Handle success, e.g., log a message or update UI
                Log.d(TAG, "Item added successfully to database")
            }
            .addOnFailureListener {
                // Handle error, e.g., log a message or show an error to the user
                e -> Log.w(TAG, "Error adding user: ", e)
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


    fun addShoppingListItem(uid: String, item: ShoppingListItem) {
        db.collection("users").document(uid)
            .collection("shoppingList").document(item.name).set(item)
            .addOnSuccessListener {
                Log.d(TAG, "Shopping list item added successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding shopping list item", e)
            }
    }

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
