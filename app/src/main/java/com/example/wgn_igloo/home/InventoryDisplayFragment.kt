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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(NotificationsViewModel::class.java)

        viewPager = view.findViewById(R.id.view_pager)
        leftArrow = view.findViewById(R.id.left_arrow)
        rightArrow = view.findViewById(R.id.right_arrow)
        setupViewPagerAndArrows()

        val recyclerView: RecyclerView = view.findViewById(R.id.items_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
//        groceryItemAdapter = ItemAdapter(emptyList())
        groceryItemAdapter = ItemAdapter(emptyList(), firestoreHelper, viewModel)

        recyclerView.adapter = groceryItemAdapter
//        firestoreHelper = FirestoreHelper(requireContext())
        firestoreDb = FirebaseFirestore.getInstance()

        fetchCurrentUserAndFriends()

        adapter = ItemAdapter(emptyList(), firestoreHelper, viewModel)

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

    override fun onUserChanged(userId: String) {
        fetchGroceryItemsForUser(userId)
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