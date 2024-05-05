package com.example.wgn_igloo.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.inbox.NotificationsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InventoryDisplayFragment : Fragment(), OnUserChangeListener {

    private val firestoreHelper: FirestoreHelper by lazy {
        FirestoreHelper(requireContext())
    }
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var groceryItemAdapter: ItemAdapter
    private lateinit var adapter: ItemAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var leftArrow: ImageButton
    private lateinit var rightArrow: ImageButton
    private lateinit var userProfileAdapter: UserProfileAdapter

    private lateinit var viewModel: NotificationsViewModel

    companion object {
        const val TAG = "InventoryDisplayFragment"
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inventory_display, container, false)
    }

    override fun onResume() {
        super.onResume()
        // This method is called when the fragment is visible to the user and actively running.
        // Call your update method here to refresh the inventory every time the fragment comes back to the foreground.
        fetchCurrentUserAndFriends()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(NotificationsViewModel::class.java)
        viewPager = view.findViewById(R.id.view_pager)
        leftArrow = view.findViewById(R.id.left_arrow)
        rightArrow = view.findViewById(R.id.right_arrow)
        firestoreDb = FirebaseFirestore.getInstance()
        setupRecyclerView(view)
        setupViewPagerAndArrows()

        val recyclerView: RecyclerView = view.findViewById(R.id.items_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
//        groceryItemAdapter = ItemAdapter(emptyList())

        groceryItemAdapter = ItemAdapter(emptyList(), firestoreHelper)
//        firestoreHelper = FirestoreHelper(requireContext())
// New implementation - commented bellow to solve merge
//         groceryItemAdapter = ItemAdapter(emptyList(), firestoreHelper, viewModel)

        recyclerView.adapter = groceryItemAdapter
        firestoreDb = FirebaseFirestore.getInstance()

        fetchCurrentUserAndFriends()

        adapter = ItemAdapter(emptyList(), firestoreHelper, viewModel)

        view.findViewById<Button>(R.id.add_button)?.setOnClickListener {
            navigateToAddNewItemForm()
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.items_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        groceryItemAdapter = ItemAdapter(emptyList(), FirestoreHelper(requireContext()))
        recyclerView.adapter = groceryItemAdapter
    }

    private fun setupViewPagerAndArrows() {
        val initialUsers = listOf<User>()
        userProfileAdapter = UserProfileAdapter(initialUsers, this)
        viewPager.adapter = userProfileAdapter

        leftArrow.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem > 0) {
                viewPager.currentItem = currentItem - 1
            }
        }

        rightArrow.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < userProfileAdapter.count - 1) {
                viewPager.currentItem = currentItem + 1
            }
        }
    }

    override fun onUserChanged(userId: String) {
        fetchGroceryItemsForUser(userId)
    }

//    private fun fetchCurrentUserAndFriends() {
//        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
//        if (currentUserUid == null) {
//            Log.d(TAG, "No user logged in")
//            Toast.makeText(context, "Please log in to view profiles.", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserUid)
//        userRef.get().addOnSuccessListener { document ->
//            if (document.exists()) {
//                val currentUser = document.toObject(User::class.java)?.apply { this.uid = document.id }
//                val users = mutableListOf<User>()
//                currentUser?.let { users.add(it) }
//
//                userRef.collection("friends").get().addOnSuccessListener { friendsSnapshot ->
//                    val friendIds = friendsSnapshot.documents.map { it.id }
//                    var friendsFetched = 0
//                    for (friendId in friendIds) {
//                        FirebaseFirestore.getInstance().collection("users").document(friendId).get()
//                            .addOnSuccessListener { friendDoc ->
//                                if (friendDoc.exists()) {
//                                    friendDoc.toObject(User::class.java)?.apply {
//                                        this.uid = friendDoc.id
//                                        users.add(this)
//                                    }
//                                }
//                                friendsFetched++
//                                if (friendsFetched == friendIds.size) {
//                                    userProfileAdapter = UserProfileAdapter(users, this)
//                                    viewPager.adapter = userProfileAdapter
//                                }
//                            }
//                    }
//                }
//            } else {
//                Log.d(TAG, "User document does not exist")
//            }
//        }
//    }

    // Ensure the method is accessible
    fun onCategorySelected(category: String) {
        fetchGroceryItemsByCategory(category)
    }

//    fun fetchGroceryItemsByCategory(category: String) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        firestoreDb.collection("users/$userId/groceryItems")
//            .whereEqualTo("category", category)
//            .get()
//            .addOnSuccessListener { documents ->
//                val items = documents.toObjects(GroceryItem::class.java)
//                groceryItemAdapter.updateItems(items)
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Error getting grocery items by category: $exception")
//                Toast.makeText(context, "Error fetching items: ${exception.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

    fun fetchGroceryItemsByCategory(category: String) {
        Log.d(TAG, "FUNCTION CALED IN FetchGroceryItems")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.d(TAG, "User not logged in")
            return
        }

        firestoreDb.collection("users/$userId/groceryItems")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { documents ->
                val items = documents.toObjects(GroceryItem::class.java)
                groceryItemAdapter.updateItems(items)
                Log.d(TAG, "Number of items in category '$category': ${items.size}")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching grocery items by category: $category", exception)
                Toast.makeText(context, "Error fetching items: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun fetchCurrentUserAndFriends() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Log.d(TAG, "No user logged in")
            Toast.makeText(context, "Please log in to view profiles.", Toast.LENGTH_LONG).show()
            return
        }

        val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserUid)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentUser = document.toObject(User::class.java)?.apply { this.uid = document.id }
                val users = mutableListOf<User>()
                currentUser?.let { users.add(it) }

                userRef.collection("friends").get().addOnSuccessListener { friendsSnapshot ->
                    val friendIds = friendsSnapshot.documents.map { it.id }
                    Log.d(TAG, "$friendIds")
                    var friendsFetched = 0
                    if (friendIds.isEmpty()) {
                        // Update the user profile adapter if there are no friends
                        userProfileAdapter = UserProfileAdapter(users, this)
                        viewPager.adapter = userProfileAdapter
                        // Fetch grocery items for the current user
                        fetchGroceryItemsForUser(currentUserUid)
                    }
                    for (friendId in friendIds) {
                        FirebaseFirestore.getInstance().collection("users").document(friendId).get()
                            .addOnSuccessListener { friendDoc ->
                                if (friendDoc.exists()) {
                                    friendDoc.toObject(User::class.java)?.apply {
                                        this.uid = friendDoc.id
                                        users.add(this)
                                    }
                                }
                                friendsFetched++
                                if (friendsFetched == friendIds.size) {
                                    userProfileAdapter = UserProfileAdapter(users, this)
                                    viewPager.adapter = userProfileAdapter
                                    // Fetch grocery items for the current user after all friends are processed
                                    fetchGroceryItemsForUser(currentUserUid)
                                }
                            }
                    }
                }
            } else {
                Log.d(TAG, "User document does not exist")
            }
        }
    }


    private fun fetchGroceryItemsForUser(userId: String) {
        val isCurrentUser = userId == FirebaseAuth.getInstance().currentUser?.uid

        firestoreDb.collection("users").document(userId).collection("groceryItems")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.toObjects(GroceryItem::class.java).map { item ->
                    item.copy(isOwnedByUser = isCurrentUser)  // Set flag based on whether the item belongs to the current user
                }
                groceryItemAdapter.updateItems(items, userId)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting grocery items: ", exception)
            }
    }

    private fun navigateToAddNewItemForm() {
        val newItemsFormFragment = NewItemsFormFragment.newInstance("Your message here")
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, newItemsFormFragment)
            .addToBackStack(null)
            .commit()
    }
}

interface OnUserChangeListener {
    fun onUserChanged(userId: String)
}
