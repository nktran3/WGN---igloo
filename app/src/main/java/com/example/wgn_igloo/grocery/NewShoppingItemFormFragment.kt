package com.example.wgn_igloo.grocery

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.databinding.FragmentShoppingNewItemsFormBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.appcompat.widget.Toolbar


class NewShoppingItemFormFragment : Fragment() {
    private var _binding: FragmentShoppingNewItemsFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var toolbarAddShopping: Toolbar
    private lateinit var toolbarAddShoppingTitle: TextView
//    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    companion object {
        fun newInstance(): NewShoppingItemFormFragment {
            return NewShoppingItemFormFragment()
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
        _binding = FragmentShoppingNewItemsFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupSubmitButton()
        toolbarAddShopping = binding.toolbarAddShopping
        toolbarAddShoppingTitle = binding.toolbarAddShoppingTitle
        updateToolbar()
    }

    private fun updateToolbar() {
        toolbarAddShopping.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.back_icon)
        toolbarAddShopping.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbarAddShoppingTitle.text = "Add New Item"
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
                submitShoppingItem()
            } else {
                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Use ?. to safely call methods on nullable receivers and ?: to provide a default value
        val isItemInputNotEmpty = binding.itemInput.text?.isNotEmpty() == true
        val isQuantityValid = binding.quantityInput.text?.toString()?.toIntOrNull() != null
        val isCategorySelected = binding.categoryInput.selectedItemPosition > 0
        val isSharedWithSelected = binding.sharedWithInput.selectedItemPosition > 0
        return isItemInputNotEmpty && isQuantityValid && isCategorySelected && isSharedWithSelected
    }


//    private fun submitShoppingItem() {
//        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
//            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val shoppingItem = ShoppingListItem(
//            category = binding.categoryInput.selectedItem.toString(),
//            lastPurchased = Timestamp(Date()), // Assuming you have a Timestamp constructor taking Date
//            name = binding.itemInput.text.toString(),
//            quantity = binding.quantityInput.text.toString().toInt(),
//            purchasedBy = userUid
//        )
//
//        firestoreHelper.addShoppingListItem(userUid, shoppingItem,
//            onSuccess = {
//                Toast.makeText(context, "${shoppingItem.name} added successfully", Toast.LENGTH_SHORT).show()
//            },
//            onFailure = {
//                Toast.makeText(context, "Failed to add item: ${it.message}", Toast.LENGTH_LONG).show()
//            }
//        )
//    }

//    private fun submitShoppingItem() {
//        Log.d("SubmitDebug", "submitShoppingItem called")  // This should always show up when the method is called
//        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
//            Log.d("SubmitDebug", "User is not logged in.")
//            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        Log.d("SubmitDebug", "User UID: $userUid")
//
//        val shoppingItem = ShoppingListItem(
//            category = binding.categoryInput.selectedItem.toString(),
//            lastPurchased = Timestamp(Date()),
//            name = binding.itemInput.text.toString(),
//            quantity = binding.quantityInput.text.toString().toInt(),
//            purchasedBy = userUid
//        )
//
//        // Check if "None" or "Choose an option" is not selected
//        val sharedWithPosition = binding.sharedWithInput.selectedItemPosition
//        val sharedWithName = binding.sharedWithInput.selectedItem as String
//        Log.d("SpinnerDebug", "Selected position: $sharedWithPosition")
//        Log.d("SpinnerDebug", "Selected name at position: $sharedWithName")
//
//        if ( binding.sharedWithInput.selectedItemPosition > 1) {
//            Log.d("SubmitDebug", "Valid friend selection, position: $sharedWithPosition")
//            // If a friend is selected, find their UID
//            firestore.collection("users").whereEqualTo("username", sharedWithName)
//                .get()
//                .addOnSuccessListener { documents ->
//                    if (!documents.isEmpty) {
//                        val friendUid = documents.documents.first().id
//                        firestoreHelper.addShoppingListItemToUserAndFriend(userUid, friendUid, shoppingItem, {
//                            Toast.makeText(context, "${shoppingItem.name} added successfully to both users", Toast.LENGTH_SHORT).show()
//                        }, { e ->
//                            Toast.makeText(context, "Failed to add item: ${e.message}", Toast.LENGTH_LONG).show()
//                        })
//                    } else {
//                        Toast.makeText(context, "No user found for username: $sharedWithName", Toast.LENGTH_LONG).show()
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(context, "Failed to fetch user: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        } else {
//            // If no friend is selected or "None" is selected, add only to user's list
//            firestoreHelper.addShoppingListItemToUserAndFriend(userUid, null, shoppingItem, {
//                Toast.makeText(context, "${shoppingItem.name} added successfully", Toast.LENGTH_SHORT).show()
//            }, { e ->
//                Toast.makeText(context, "Failed to add item: ${e.message}", Toast.LENGTH_LONG).show()
//            })
//        }
//    }

//    private fun submitShoppingItem() {
//        val userUid = auth.currentUser?.uid ?: run {
//            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val shoppingItem = mapOf(
//            "category" to binding.categoryInput.selectedItem.toString(),
//            "lastPurchased" to Timestamp.now(), // Assuming current timestamp as purchase date
//            "name" to binding.itemInput.text.toString(),
//            "quantity" to binding.quantityInput.text.toString().toInt(),
//            "purchasedBy" to userUid,
//            "sharedWith" to if (binding.sharedWithInput.selectedItemPosition > 1) binding.sharedWithInput.selectedItem.toString() else ""
//        )
//
//        // Add the item to Firestore
//        firestore.collection("/users/$userUid/groceryItems").add(shoppingItem)
//            .addOnSuccessListener { documentReference ->
//                // Successfully added, now set the documentId within the same document
//                val documentId = documentReference.id
//                documentReference.update("documentId", documentId).addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Toast.makeText(context, "Item added successfully and documentId set", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(context, "Failed to set documentId: ${task.exception?.message}", Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(context, "Failed to add item: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//    }

    private fun submitShoppingItem() {
        val userUid = auth.currentUser?.uid ?: run {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare the item data
        val shoppingItemData = mapOf(
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
        userCollection.add(shoppingItemData).addOnSuccessListener { userDocRef ->
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
                                friendCollection.add(shoppingItemData).addOnSuccessListener { friendDocRef ->
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