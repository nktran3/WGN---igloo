package com.example.wgn_igloo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class ShoppingListPage : Fragment() {
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var firestoreDb: FirebaseFirestore
    private var userUid: String? = null

    companion object {
        private const val TAG = "FirestoreHelper"
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
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

        val recyclerView: RecyclerView = view.findViewById(R.id.shopping_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ShoppingListAdapter(listOf("Yogurt", "Whole Milk", "Cheese", "Cream Cheese"))

//        userUid?.let { uid ->
//            moveGroceryItemsToShoppingList(uid)
//        } ?: Log.d(TAG, "User is not logged in")
        // Add a dummy shopping list item for testing purposes
        addDummyShoppingListItem()
    }

    private fun addDummyShoppingListItem() {
        userUid?.let { uid ->
            // Create a dummy ShoppingListItem
            val dummyItem = ShoppingListItem(
                category = "Meat",
                lastPurchased = Timestamp.now(), // Use a dummy timestamp
                name = "Chicken",
                purchasedBy = uid  // Use the UID of the current user
            )

            // Add the dummy item to Firestore using FirestoreHelper
            firestoreHelper.addShoppingListItem(uid, dummyItem)
        }
    }

    private fun moveGroceryItemsToShoppingList(uid: String) {
        firestoreDb.collection("users").document(uid)
            .collection("groceryItems").whereEqualTo("status", false).get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val dateBought = document.getTimestamp("dateBought") ?: Timestamp.now()
                    val groceryItemName = document.getString("name") ?: "Unknown"

                    val shoppingListItem = ShoppingListItem(
                        category = document.getString("category") ?: "Unknown",
                        lastPurchased = dateBought,
                        name = groceryItemName,
                        purchasedBy = uid
                    )

                    firestoreHelper.addShoppingListItem(uid, shoppingListItem)
                    // Additionally, consider removing the item from the groceryItems or updating its status
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting grocery items: ", exception)
            }
    }

}

class ShoppingListAdapter(private val items: List<String>) : RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>() {

    class ShoppingListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.shoppingItemCheckBox)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shopping_list_item_layout, parent, false)
        return ShoppingListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int) {
        holder.checkBox.text = items[position]
    }

    override fun getItemCount() = items.size
}


