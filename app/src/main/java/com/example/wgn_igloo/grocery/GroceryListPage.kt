package com.example.wgn_igloo.grocery

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.R
import com.example.wgn_igloo.inbox.NotificationsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class ShoppingListPage : Fragment() {
    // Firestore
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var firestoreDb: FirebaseFirestore
    private var userUid: String? = null
    private lateinit var itemViewModel: ItemViewModel

    // RecyclerView
    private lateinit var recyclerView: RecyclerView


    companion object {
        private const val TAG = "FirestoreHelper"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirestoreHelper and Firestore database instance
        firestoreHelper = FirestoreHelper(requireContext())
        firestoreDb = FirebaseFirestore.getInstance()

        // Get the current user's UID
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

        itemViewModel = ViewModelProvider(requireActivity()).get(ItemViewModel::class.java)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.shopping_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Setup Adapter
        recyclerView.adapter = GroceryListAdapter(emptyList()) { item ->
            userUid?.let { uid ->
                firestoreHelper.moveItemToInventory(uid, item,
                    onSuccess = {
                        firestoreHelper.deleteItemShoppingList(uid, item)
                        itemViewModel.setRefreshItems(true)
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
            setupGroceryListListener(uid) // Set up real-time listener for shopping list updates
        }

        // Set add button on click
        view.findViewById<Button>(R.id.add_button)?.setOnClickListener {
            navigateToAddNewItemForm()
        }

    }

    // Inflate the add new grocery item form
    private fun navigateToAddNewItemForm() {
        val newGroceryItemFormFragment = NewGroceryItemFormFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, newGroceryItemFormFragment)
            .addToBackStack(null)
            .commit()
    }



    // Fetch shopping list items from Firestore
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

    // Set up real-time listener for shopping list updates
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

