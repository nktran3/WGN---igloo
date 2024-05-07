package com.example.wgn_igloo.grocery

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class ShoppingListPage : Fragment() {
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var firestoreDb: FirebaseFirestore
    private var userUid: String? = null
    private lateinit var recyclerView: RecyclerView


    companion object {
        private const val TAG = "FirestoreHelper"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestoreHelper = FirestoreHelper(requireContext())
        firestoreDb = FirebaseFirestore.getInstance()
        userUid = FirebaseAuth.getInstance().currentUser?.uid
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_grocery_list_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.shopping_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = GroceryListAdapter(emptyList()) { item ->
            userUid?.let { uid ->
                firestoreHelper.moveItemToInventory(uid, item,
                    onSuccess = {
                        // Fetch updated items after successful inventory move
                        fetchGroceryListItems(uid, { items ->
                            (recyclerView.adapter as? GroceryListAdapter)?.updateItems(items)
                        }, { exception ->
                            Log.w(TAG, "Error refreshing shopping list after moving item", exception)
                        })
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to move item to inventory", exception)
                    }
                )
            }
        }

        userUid?.let { uid ->
            setupGroceryListListener(uid) // Setup the real-time listener
        }

        view.findViewById<Button>(R.id.add_button)?.setOnClickListener {
            navigateToAddNewItemForm()
        }

    }

    private fun navigateToAddNewItemForm() {
        val newGroceryItemFormFragment = NewGroceryItemFormFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, newGroceryItemFormFragment)
            .addToBackStack(null)
            .commit()
    }


    private fun fetchGroceryListItems(uid: String, onSuccess: (List<GroceryListItem>) -> Unit, onFailure: (Exception) -> Unit) {
        firestoreDb.collection("users").document(uid)
            .collection("shoppingList").get()
            .addOnSuccessListener { querySnapshot ->
                val shoppingListItems = querySnapshot.toObjects(GroceryListItem::class.java)
                onSuccess(shoppingListItems)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    private fun setupGroceryListListener(uid: String) {
        firestoreDb.collection("users").document(uid)
            .collection("shoppingList")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                val shoppingListItems = snapshot?.toObjects(GroceryListItem::class.java) ?: emptyList()
                (recyclerView.adapter as? GroceryListAdapter)?.updateItems(shoppingListItems)
            }
    }

}

