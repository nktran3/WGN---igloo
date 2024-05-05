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
//    private val db = FirebaseFirestore.getInstance()


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
        return inflater.inflate(R.layout.fragment_shopping_list_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.shopping_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ShoppingListAdapter(emptyList()) { item ->
            userUid?.let { uid ->
                firestoreHelper.moveItemToInventory(uid, item,
                    onSuccess = {
                        // Fetch updated items after successful inventory move
                        fetchShoppingListItems(uid, { items ->
                            (recyclerView.adapter as? ShoppingListAdapter)?.updateItems(items)
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
            setupShoppingListListener(uid) // Setup the real-time listener
        }

//        userUid?.let { uid ->
//            fetchShoppingListItems(uid,
//                onSuccess = { items ->
//                    (recyclerView.adapter as? ShoppingListAdapter)?.updateItems(items)
//                },
//                onFailure = { exception ->
//                    Log.w(TAG, "Error getting shopping list items", exception)
//                }
//            )
//        }
        // Add a dummy shopping list item for testing purposes
        // adding the item -- how to call to add the item
        // Set up the button (Replace 'R.id.add_button' with your actual button ID)
        view.findViewById<Button>(R.id.add_button)?.setOnClickListener {
            navigateToAddNewItemForm()
        }
//        addDummyShoppingListItem()
    }

    private fun navigateToAddNewItemForm() {
        // Ensure you are creating the correct fragment instance here.
        val newShoppingItemFormFragment = NewShoppingItemFormFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, newShoppingItemFormFragment)
            .addToBackStack(null) // This is crucial for the back navigation to work
            .commit()
    }


    private fun fetchShoppingListItems(
        uid: String,
        onSuccess: (List<ShoppingListItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestoreDb.collection("users").document(uid)
            .collection("shoppingList").get()
            .addOnSuccessListener { querySnapshot ->
                val shoppingListItems = querySnapshot.toObjects(ShoppingListItem::class.java)
                onSuccess(shoppingListItems)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    private fun addDummyShoppingListItem() {
        userUid?.let { uid ->
            // Create a dummy ShoppingListItem
            val dummyItem = ShoppingListItem(
                category = "Fruit",
                lastPurchased = Timestamp.now(), // Use a dummy timestamp
                name = "Dragon Fruit",
                quantity = 1,
                purchasedBy = uid  // Use the UID of the current user
            )

            // Add the dummy item to Firestore using FirestoreHelper
//            firestoreHelper.addShoppingListItem(uid, dummyItem)
        }
        // after add fetch again
        userUid?.let { uid ->
            fetchShoppingListItems(uid,
                onSuccess = { items ->
                    (recyclerView.adapter as? ShoppingListAdapter)?.updateItems(items)
                },
                onFailure = { exception ->
                    Log.w(TAG, "Error getting shopping list items: ", exception)
                }
            )
        }
    }
    private fun setupShoppingListListener(uid: String) {
        firestoreDb.collection("users").document(uid)
            .collection("shoppingList")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                val shoppingListItems = snapshot?.toObjects(ShoppingListItem::class.java) ?: emptyList()
                (recyclerView.adapter as? ShoppingListAdapter)?.updateItems(shoppingListItems)
            }
    }

}

class ShoppingListAdapter(
    private var items: List<ShoppingListItem>,
    private val onItemChecked: (ShoppingListItem) -> Unit
) : RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>() {

    class ShoppingListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.shoppingItemCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shopping_list_item_layout, parent, false)
        return ShoppingListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int) {
        val item = items[position]
        holder.checkBox.text = item.name
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onItemChecked(item)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<ShoppingListItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}