package com.example.wgn_igloo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import GroceryItem

class InventoryDisplayFragment : Fragment() {

    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var firestoreDb: FirebaseFirestore

    companion object {
        const val TAG = "FirestoreHelper"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize FirestoreHelper with the fragment's context
        firestoreHelper = FirestoreHelper(requireContext())
        firestoreDb = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myItems = listOf("Yogurt", "Whole Milk", "Cheese", "Cream Cheese")
        val recyclerView: RecyclerView = view.findViewById(R.id.items_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = MyItemAdapter(myItems)

        // Fetching the user's email using their UID
        // Assuming you have the user's UID, replace "userUid" with the actual UID variable or method to retrieve it
//        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        fetchUserEmailByUid(userUid)
        // Fetching the user's UID and adding a grocery item
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            addGroceryItemForUser(userUid)
        } else {
            Log.d(TAG, "User is not logged in")
        }

        // You can call this method when you want to add an item to the grocery list
        // For demonstration purposes, let's assume you call it here directly
//        addGroceryItemToFirestore()
    }

    private fun addGroceryItemForUser(uid: String) {
        // Create a GroceryItem object with the necessary details
        val groceryItem = GroceryItem(
            category = "vegetables",
            expirationDate = Timestamp.now(),
            dateBought = Timestamp.now(),
            name = "spinach",
            quantity = 1,
            sharedWith = "U123456", // This should be a UID of the user with whom the item is shared
            status = true
        )

        // Use FirestoreHelper to add the item to the Firestore
        firestoreHelper.addGroceryItem(uid, groceryItem)
    }
}

class MyItemAdapter(private val items: List<String>) : RecyclerView.Adapter<MyItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.textView.text = items[position]
    }

    override fun getItemCount() = items.size
}

