package com.example.wgn_igloo.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import androidx.viewpager.widget.ViewPager
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.grocery.GroceryItem
import com.example.wgn_igloo.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InventoryDisplayFragment : Fragment() {

    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var adapter: MyItemAdapter

    private lateinit var viewPager: ViewPager
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var leftArrow: ImageButton
    private lateinit var rightArrow: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory_display, container, false)
    }

    companion object {
        const val TAG = "FirestoreHelper"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.view_pager)
        // Assume a list of user identifiers or some user-specific data
        val users = listOf("User1", "User2", "User3")
        viewPager.adapter = UserProfileAdapter(users)

        leftArrow = view.findViewById(R.id.left_arrow)
        rightArrow = view.findViewById(R.id.right_arrow)

        leftArrow.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem > 0) {
                viewPager.currentItem = currentItem - 1
            }
        }

        rightArrow.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < users.size - 1) {
                viewPager.currentItem = currentItem + 1
            }
        }

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
        val newItemsFormFragment =
            NewItemsFormFragment.newInstance("Your message here") // Use appropriate message or data
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

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemTextView: TextView = view.findViewById(R.id.itemTextView)
        val quantityTextView: TextView = view.findViewById(R.id.quantityTextView)
        val quantityValueTextView: TextView = view.findViewById(R.id.quantityValueTextView)
        val expirationTextView: TextView = view.findViewById(R.id.expirationDateTextView)
        val dateTextView: TextView = view.findViewById(R.id.expirationDateValueTextView)
        val sharedWithTextView: TextView = view.findViewById(R.id.sharedTextView)
        val sharedWithValueTextView: TextView = view.findViewById(R.id.sharedValueTextView)
        val requestToBorrow: Button = view.findViewById(R.id.request_button)
        val addToShoppingList: Button = view.findViewById(R.id.add_shopping_button)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
    }
    fun updateItems(newItems: List<GroceryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.inventory_item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.itemTextView.text = "${item.name}"
        holder.quantityTextView.visibility = View.GONE
        holder.quantityValueTextView.visibility = View.GONE
        holder.expirationTextView.visibility = View.GONE
        holder.dateTextView.visibility = View.GONE
        holder.sharedWithTextView.visibility = View.GONE
        holder.sharedWithValueTextView.visibility = View.GONE
        holder.requestToBorrow.visibility = View.GONE
        holder.addToShoppingList.visibility = View.GONE
        holder.editButton.visibility = View.GONE
        holder.deleteButton.visibility = View.GONE
        holder.itemTextView.setOnClickListener {
            holder.quantityTextView.visibility =
                if (holder.quantityTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            holder.quantityValueTextView.visibility =
                if (holder.quantityValueTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            holder.expirationTextView.visibility =
                if (holder.expirationTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            holder.dateTextView.visibility =
                if (holder.dateTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            holder.sharedWithTextView.visibility =
                if (holder.sharedWithTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            holder.sharedWithValueTextView.visibility =
                if (holder.sharedWithValueTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            holder.requestToBorrow.visibility =
                if (holder.requestToBorrow.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            holder.addToShoppingList.visibility =
                if (holder.addToShoppingList.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            holder.editButton.visibility =
                if (holder.editButton.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            holder.deleteButton.visibility =
                if (holder.deleteButton.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount() = items.size

}


