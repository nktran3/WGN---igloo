package com.example.wgn_igloo.grocery

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.Toolbar
import com.example.wgn_igloo.databinding.FragmentGroceryNewItemsFormBinding


class NewGroceryItemFormFragment : Fragment() {
    private var _binding: FragmentGroceryNewItemsFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var toolbarAddGrocery: Toolbar
    private lateinit var toolbarAddGroceryTitle: TextView

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    companion object {
        fun newInstance(): NewGroceryItemFormFragment {
            return NewGroceryItemFormFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestoreHelper = FirestoreHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroceryNewItemsFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupSubmitButton()
        toolbarAddGrocery = binding.toolbarAddGrocery
        toolbarAddGroceryTitle = binding.toolbarAddGroceryTitle
        updateToolbar()
    }

    private fun updateToolbar() {
        toolbarAddGrocery.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.back_icon)
        toolbarAddGrocery.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbarAddGroceryTitle.text = "Add New Item"
    }

    private fun setupSpinners() {
        val categories = arrayOf("Choose an option", "Condiments", "Dairy", "Drinks", "Freezer", "Meats", "Produce", "Other" )
        val defaultSharedWith = arrayOf("No one")

        val categoriesAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, categories)
        val sharedWithAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, defaultSharedWith)

        binding.categoryInput.adapter = categoriesAdapter
        binding.sharedWithInput.adapter = sharedWithAdapter
        // Fetch and update the sharedWith spinner with dynamic data
        fetchFriendsAndUpdateSpinner()

        binding.sharedWithInput.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                Log.d("SpinnerDebug", "Spinner item selected: $selected at position $position")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d("SpinnerDebug", "No spinner item selected")
            }
        }
    }

    private fun fetchFriendsAndUpdateSpinner() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    val friendsUsernames = mutableListOf("Choose an option", "None")
                    for (document in documents) {
                        document.getString("username")?.let { username ->
                            friendsUsernames.add(username)
                        }
                    }
                    val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, friendsUsernames)
                    binding.sharedWithInput.adapter = adapter
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to fetch friends: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener {
            if (validateInputs()) {
                submitGroceryItem()
            } else {
                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val isItemInputNotEmpty = binding.itemInput.text?.isNotEmpty() == true
        val isQuantityValid = binding.quantityInput.text?.toString()?.toIntOrNull() != null
        val isCategorySelected = binding.categoryInput.selectedItemPosition > 0
        val isSharedWithSelected = binding.sharedWithInput.selectedItemPosition > 0
        return isItemInputNotEmpty && isQuantityValid && isCategorySelected && isSharedWithSelected
    }


    private fun submitGroceryItem() {
        val userUid = auth.currentUser?.uid ?: run {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare the item data
        val groceryItemData = mapOf(
            "category" to binding.categoryInput.selectedItem.toString(),
            "lastPurchased" to Timestamp.now(),
            "name" to binding.itemInput.text.toString(),
            "quantity" to binding.quantityInput.text.toString().toInt(),
            "purchasedBy" to userUid,
            "sharedWith" to if (binding.sharedWithInput.selectedItemPosition > 1) binding.sharedWithInput.selectedItem.toString() else "",
            "expireNotified" to false,
            "status" to true,
            "ownedByUser" to true
        )

        // Add the item to the user's Firestore collection
        val userCollection = firestore.collection("/users/$userUid/groceryItems")
        userCollection.add(groceryItemData).addOnSuccessListener { userDocRef ->
            val userDocId = userDocRef.id
            userDocRef.update("documentId", userDocId).addOnSuccessListener {
                Toast.makeText(context, "Item added and document ID set for user.", Toast.LENGTH_SHORT).show()

                // Check if item should be shared and add to friend's collection
                if (binding.sharedWithInput.selectedItemPosition > 1) {
                    val sharedWithName = binding.sharedWithInput.selectedItem.toString()
                    firestore.collection("users").whereEqualTo("username", sharedWithName)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.documents.isNotEmpty()) {
                                val friendUid = querySnapshot.documents.first().id
                                val friendCollection = firestore.collection("/users/$friendUid/groceryItems")
                                friendCollection.add(groceryItemData).addOnSuccessListener { friendDocRef ->
                                    friendDocRef.update("documentId", friendDocRef.id).addOnSuccessListener {
                                        Toast.makeText(context, "Item also added and document ID set for friend.", Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener { e ->
                                        Log.e("FirestoreError", "Failed to set document ID for friend: ${e.message}")
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Friend not found", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to fetch friend's user ID: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update user's document ID: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to add item: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}