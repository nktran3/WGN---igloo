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
import androidx.lifecycle.Observer
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
    // Track the current inventory user
    private var currentInventoryUserId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var carouselViewModel: CarouselViewModel

    private  var currentUserFridge = ""

    private var category = "all"
    private lateinit var viewModel: NotificationsViewModel

    companion object {
        const val TAG = "InventoryDisplayFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inventory_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(NotificationsViewModel::class.java)
        firestoreHelper.currentInventoryUserId = FirebaseAuth.getInstance().currentUser?.uid

        carouselViewModel = ViewModelProvider(requireActivity()).get(CarouselViewModel::class.java)

        carouselViewModel.getSelectedCategory().observe(viewLifecycleOwner, Observer { category ->
            // React to category change
            onCarouselChanged(category)
        })

        viewPager = view.findViewById(R.id.view_pager)
        leftArrow = view.findViewById(R.id.left_arrow)
        rightArrow = view.findViewById(R.id.right_arrow)
        setupViewPagerAndArrows()

        val recyclerView: RecyclerView = view.findViewById(R.id.items_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // Set the current user ID directly to the property
        firestoreHelper.currentInventoryUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Setup views and adapters...
        groceryItemAdapter = ItemAdapter(emptyList(), firestoreHelper, viewModel, firestoreHelper.currentInventoryUserId)

//        groceryItemAdapter = ItemAdapter(emptyList())
//        groceryItemAdapter = ItemAdapter(emptyList(), firestoreHelper, viewModel, currentInventoryUserId)
//        groceryItemAdapter = ItemAdapter(emptyList(), firestoreHelper, viewModel)

        recyclerView.adapter = groceryItemAdapter
//        firestoreHelper = FirestoreHelper(requireContext())
        firestoreDb = FirebaseFirestore.getInstance()

        fetchCurrentUserAndFriends()
//        currentInventoryUserId = FirebaseAuth.getInstance().currentUser?.uid

//        adapter = ItemAdapter(emptyList(), firestoreHelper, viewModel)
//        adapter = ItemAdapter(emptyList(), firestoreHelper, viewModel, currentInventoryUserId)


        view.findViewById<Button>(R.id.add_button)?.setOnClickListener {
            navigateToAddNewItemForm()
        }
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


    override fun onCategorySelected(category: String) {
        fetchGroceryItemsForUser(FirebaseAuth.getInstance().currentUser?.uid.orEmpty(), category)
    }

    private fun fetchGroceryItemsForUser(userId: String, category: String) {
        Log.d(TAG, "Fetching items for category: $category")  // Log the category being fetched
        currentInventoryUserId = userId  // Update the current user ID whenever items are fetched for a user
        val query = if (category == "All") {
            firestoreDb.collection("users").document(userId).collection("groceryItems")
        } else {
            firestoreDb.collection("users").document(userId).collection("groceryItems")
                .whereEqualTo("category", category)
        }

        query.get().addOnSuccessListener { snapshot ->
            val items = snapshot.toObjects(GroceryItem::class.java)
            Log.d(TAG, "Fetched ${items.size} items for category: $category")  // Log how many items were fetched
            groceryItemAdapter.updateItems(items, userId)
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error getting grocery items for category: $category", exception)  // Log error with specific category
        }
    }


    override fun onUserChanged(userId: String) {
        firestoreHelper.currentInventoryUserId = userId
//         fetchGroceryItemsForUser(userId, "All")
        currentUserFridge = userId
        fetchGroceryItemsForUser(userId)
    }

    private fun onCarouselChanged(newCategory: String) {
        this.category = newCategory
        fetchGroceryItemsForUser(currentUserFridge)
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
                                }
                            }
                    }
                }
            } else {
                Log.d(TAG, "User document does not exist")
            }
        }
    }


//    private fun fetchGroceryItemsForUser(userId: String) {
//        val isCurrentUser = userId == FirebaseAuth.getInstance().currentUser?.uid
//
//        firestoreDb.collection("users").document(userId).collection("groceryItems")
//            .get()
//            .addOnSuccessListener { snapshot ->
//                val items = snapshot.toObjects(GroceryItem::class.java).map { item ->
//                    item.copy(isOwnedByUser = isCurrentUser)  // Set flag based on whether the item belongs to the current user
//                }
//                groceryItemAdapter.updateItems(items, userId)
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Error getting grocery items: ", exception)
//            }
//    }

    // Originally done using an if, else if branch for each category and else was used for
    // all, but then later reduced and simplified using Chat-GPT4
    private fun fetchGroceryItemsForUser(userId: String?) {
        if (userId == null || userId.isBlank()) {
            Log.e(TAG, "Invalid user ID")
            return  // Exit the function if the userID is invalid
        }

        val isCurrentUser = userId == FirebaseAuth.getInstance().currentUser?.uid
        val categoryRef = firestoreDb.collection("users").document(userId).collection("groceryItems")

        val query = when (category) {
            "Condiments", "Dairy", "Drinks", "Freezer", "Meats", "Produce", "Other" -> {
                Log.d(TAG, "Returning $category items")
                categoryRef.whereEqualTo("category", category)
            }
            else -> {
                Log.d(TAG, "Returning all items")
                categoryRef  // If no specific category matches, fetch all items
            }
        }

        query.get()
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
    fun onCategorySelected(category: String)
}