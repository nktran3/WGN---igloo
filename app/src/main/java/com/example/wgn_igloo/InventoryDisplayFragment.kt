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
import com.example.wgn_igloo.GroceryItem

class InventoryDisplayFragment : Fragment() {

    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var adapter: MyItemAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory_display, container, false)
    }

    companion object {
        const val TAG = "FirestoreHelper"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView and Adapter
        val recyclerView: RecyclerView = view.findViewById(R.id.items_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MyItemAdapter(emptyList())
        recyclerView.adapter = adapter

        // Initialize FirestoreHelper with the fragment's context
        firestoreHelper = FirestoreHelper(requireContext())
        firestoreDb = FirebaseFirestore.getInstance()

        // Fetch initial grocery items
        fetchGroceryItems()

        // Set up the button (Replace 'R.id.add_button' with your actual button ID)
        view.findViewById<Button>(R.id.add_button)?.setOnClickListener {
            navigateToAddNewItemForm()
        }

        // NEED TO SET UP BUTTON with OnClickListener
//        val addButton: Button = view.findViewById(R.id.add_button)
//        addButton.setOnClickListener {
            // Check for user's UID
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            // Add grocery item for user and then fetch updated list
//            addGroceryItemForUser(userUid)
//            onShareItemClicked("WZSXlJUk8jQTkoKi8bYG", "EVzFbTC1qhe8N1cG77DoAKv1s4s2")
        } else {
            Log.d(TAG, "User is not logged in")
            // Consider showing a Toast message or UI indication for login requirement
        }
//        }
    }

    private fun navigateToAddNewItemForm() {
        val newItemsFormFragment = NewItemsFormFragment.newInstance("Your message here") // Use appropriate message or data
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, newItemsFormFragment)
            .addToBackStack(null) // This is crucial for the back navigation to work
            .commit()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize FirestoreHelper with the fragment's context
        firestoreHelper = FirestoreHelper(requireContext())
        firestoreDb = FirebaseFirestore.getInstance()
    }

    private fun fetchGroceryItems() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(userUid).collection("groceryItems")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.toObjects(GroceryItem::class.java)
                print("this is the items:" + items)
                adapter.updateItems(items)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    // Example of a function within InventoryDisplayFragment to handle sharing an item
    fun onShareItemClicked(itemId: String, friendUserId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            firestoreHelper.shareGroceryItem(it, itemId, friendUserId, onSuccess = {
                Toast.makeText(context, "Item shared successfully", Toast.LENGTH_SHORT).show()
            }, onFailure = { exception ->
                Log.e(TAG, "Failed to share item: ${exception.message}", exception)
                Toast.makeText(context, "Failed to share item", Toast.LENGTH_SHORT).show()
            })
        }
    }


    //    }
    private fun addGroceryItemForUser(uid: String) {
        // test 1
//        val groceryItem = GroceryItem(
//            category = "vegetables",
//            expirationDate = Timestamp.now(),
//            dateBought = Timestamp.now(),
//            name = "spinach",
//            quantity = 1,
//            sharedWith = "U123456",
//            status = true
//        )
        // test 2
        val groceryItem = GroceryItem(
            category = "meat",
            expirationDate = Timestamp.now(),
            dateBought = Timestamp.now(),
            name = "snake",
            quantity = 1,
            sharedWith = "U123456",
            status = true
        )

        firestoreHelper.addGroceryItem(uid, groceryItem, onSuccess = {
            // Successfully added item, now fetch the updated list
            fetchGroceryItems()
        }, onFailure = { e ->
            Log.e(TAG, "Failed to add item: ", e)
            // Optionally, show a failure message to the user
        })
    }
}


class MyItemAdapter(private var items: List<GroceryItem>) : RecyclerView.Adapter<MyItemAdapter.ItemViewHolder>() {

    fun updateItems(newItems: List<GroceryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = "${item.name} - Qty: ${item.quantity}"
    }

    override fun getItemCount() = items.size

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemTextView)
    }
}


