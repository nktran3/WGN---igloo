package com.example.wgn_igloo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class InventoryDisplayFragment : Fragment() {

    private lateinit var firestoreHelper: FirestoreHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize FirestoreHelper with the fragment's context
        firestoreHelper = FirestoreHelper(requireContext())
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
        // You can call this method when you want to add an item to the grocery list
        // For demonstration purposes, let's assume you call it here directly
        addGroceryItemToFirestore()
    }

    private fun addGroceryItemToFirestore() {
        // Example data - replace with actual data input from user
//        val groceryItem = GroceryItem(
//            Category = "vegetables",
//            expirationDate = Timestamp.now(), // Use current timestamp or get user input
//            Name = "spinach",
//            sharedWith = FirebaseFirestore.getInstance().document("User/U123456"), // Replace with actual user reference
//            Status = true
//        )
//
//        // Replace "U11894403" with the actual UID of the user
//        firestoreHelper.addGroceryItem("U11894403", groceryItem)
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
