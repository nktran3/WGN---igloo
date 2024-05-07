package com.example.wgn_igloo.home

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.databinding.FragmentEditItemsFormBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import com.google.gson.Gson
import java.util.*
import java.text.ParseException

class EditItemsFormFragment : Fragment() {

    private var _binding: FragmentEditItemsFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var firestore: FirebaseFirestore

    // input item from the form
    private lateinit var itemInput: EditText
    private lateinit var quantityInput: EditText
    private lateinit var categoryInput: Spinner
    private lateinit var expirationDateInput: EditText
    private lateinit var sharedWithInput: Spinner
    // this is savebutton
    private lateinit var submitButton: Button

    // Hard coded list
    private val categoryList = arrayOf("Condiments", "Dairy", "Drinks", "Freezer", "Meats", "Produce", "Other" )
    // Default list with a placeholder for choosing an option
    private var sharedWithList = arrayOf("No one")


    companion object {
        private const val ARG_ITEM_JSON = "edit_item_json"

        fun newInstance(item: GroceryItem): EditItemsFormFragment {
            val fragment = EditItemsFormFragment()
            val args = Bundle()
            val itemJson = Gson().toJson(item)
            args.putString(ARG_ITEM_JSON, itemJson)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditItemsFormBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        // Initialize FirestoreHelper with the context
        firestoreHelper = FirestoreHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemJson = arguments?.getString(ARG_ITEM_JSON)
        val item = Gson().fromJson(itemJson, GroceryItem::class.java)
        if (item != null) {
            setupViews(item)
            fetchAndSetExpirationDate()
        }
    }

    private fun setupViews(item: GroceryItem) {
        //itemInput =
        binding.itemInput.setText(item.name)
        //quantityInput
        binding.quantityInput.setText(item.quantity.toString())
        //categoryInput
        setupCategorySpinner(item.category)
        //expirationDateInput
        setupDatePicker(item.expirationDate)
        //sharedWithInput
        setupSharedWithSpinner(item.sharedWith)
        //submitButton
        binding.submitButton.setOnClickListener {
            updateItem(item)
        }
    }

    private fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }


    private fun setupCategorySpinner(selectedCategory: String) {
        val userId = getCurrentUserId()
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("/users/$userId/groceryItems")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("SetupSpinner", "No categories found")
                    Toast.makeText(context, "No categories found", Toast.LENGTH_SHORT).show()
                } else {
                    // Extract unique categories using a set to avoid duplicates
                    val categories = documents.documents.mapNotNull { it.getString("category") }.toSet().toTypedArray()
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
                    binding.categoryInput.adapter = adapter
                    // Set the current selection based on fetched data
                    val selectedIndex = categories.indexOfFirst { it == selectedCategory }
                    binding.categoryInput.setSelection(if (selectedIndex >= 0) selectedIndex else 0)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SetupSpinner", "Failed to fetch categories: ${e.message}", e)
                Toast.makeText(context, "Failed to fetch categories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        setupSpinnerInteraction()
    }

    private fun setupSpinnerInteraction() {
        // Use a simple click listener to switch to the predefined list on any interaction
        binding.categoryInput.setOnTouchListener { v, event ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryList)
            binding.categoryInput.adapter = adapter
            binding.categoryInput.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    // No need to reset the adapter here since it's already set
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // No operation
                }
            }
            false // Return false to allow normal handling of the touch event after changing the adapter
        }
    }

    private fun setupSharedWithSpinner(selectedSharedWith: String) {
        val userId = getCurrentUserId()
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Assuming shared options are stored under a specific user's document
        firestore.collection("/users/$userId/groceryItems")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("SetupSpinner", "No shared options found")
                    Toast.makeText(context, "No shared options found", Toast.LENGTH_SHORT).show()
                } else {
//                    val sharedWithOptions = documents.mapNotNull { it.getString("sharedWith") }.toTypedArray()
//                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sharedWithOptions)
//                    binding.sharedWithInput.adapter = adapter
//                    val selectedIndex = sharedWithOptions.indexOfFirst { it == selectedSharedWith }
//                    binding.sharedWithInput.setSelection(if (selectedIndex >= 0) selectedIndex else 0)
                    val uids = documents.mapNotNull { it.getString("sharedWith") }
                    fetchUserDetails(uids, selectedSharedWith)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SetupSpinner", "Failed to fetch shared options: ${e.message}", e)
                Toast.makeText(context, "Failed to fetch shared options: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        setupSharedWithInteraction()
    }

    private fun fetchUserDetails(uids: List<String>, selectedSharedWith: String) {
        // Filter out invalid or empty UIDs
        val validUids = uids.filter { it.isNotBlank() }

        // Check if the list of valid UIDs is empty and handle it
        if (validUids.isEmpty()) {
            Toast.makeText(context, "No valid user IDs found.", Toast.LENGTH_SHORT).show()
            // Optionally, set the spinner to a default state if no valid UIDs are found
            updateSharedWithSpinner(listOf("No users found"), selectedSharedWith)
            return
        }

        val usernames = mutableListOf<String>()
        val expectedResponses = validUids.size
        var responsesReceived = 0

        for (uid in validUids) {
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "Unknown"
                    usernames.add(username)
                    responsesReceived++
                    if (responsesReceived == expectedResponses) {
                        updateSharedWithSpinner(usernames, selectedSharedWith)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FetchUserDetails", "Failed to fetch user details: ${e.message}", e)
                    usernames.add("Failed to fetch")
                    responsesReceived++
                    if (responsesReceived == expectedResponses) {
                        updateSharedWithSpinner(usernames, selectedSharedWith)
                    }
                }
        }
    }

    private fun updateSharedWithSpinner(usernames: List<String>, selectedSharedWith: String) {
        // Convert list to array and set up the adapter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, usernames)
        binding.sharedWithInput.adapter = adapter
        val selectedIndex = usernames.indexOfFirst { it == selectedSharedWith }
        binding.sharedWithInput.setSelection(if (selectedIndex >= 0) selectedIndex else 0)
    }

    private fun fetchFriendsAndUpdateSpinner() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    val friendsUsernames = mutableListOf("No one")
                    documents.forEach { document ->
                        document.getString("username")?.let { username ->
                            friendsUsernames.add(username)
                        }
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, friendsUsernames)
                    binding.sharedWithInput.adapter = adapter
                }
                .addOnFailureListener { exception ->
                    Log.e("SharedWithSpinner", "Error fetching friends", exception)
                }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSharedWithInteraction() {
        binding.sharedWithInput.setOnTouchListener { _, _ ->
            fetchFriendsAndUpdateSpinner()  // Fetch and update the spinner when touched
            false  // Allow the spinner to handle the touch event normally after setting up
        }
    }

    private fun fetchAndSetExpirationDate() {
        val userId = getCurrentUserId()
        firestore.collection("/users/$userId/groceryItems")
            .document("VEmOaU1vcZf6xk5cuVbg") // Specific document ID
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val expirationDate = document.getTimestamp("expirationDate")
                    expirationDate?.let {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        binding.expirationInput.setText(dateFormat.format(it.toDate()))
                    }
                } else {
                    Log.d("FetchExpiration", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FetchExpiration", "Error fetching document: ${e.message}", e)
            }
    }


    private fun setupDatePicker(selectedDate: Timestamp) {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate.toDate()
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            updateLabel(calendar) // This updates the TextView correctly
        }

        binding.expirationInput.setOnClickListener {
            DatePickerDialog(requireContext(), datePickerListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateLabel(calendar: Calendar) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        binding.expirationInput.setText(dateFormat.format(calendar.time))
    }

    private fun updateItem(item: GroceryItem) {
        val fieldsToUpdate = mapOf(
            "name" to binding.itemInput.text.toString(),
            "quantity" to binding.quantityInput.text.toString().toInt(),
            "category" to binding.categoryInput.selectedItem.toString(),
            "sharedWith" to binding.sharedWithInput.selectedItem.toString(),
            "expirationDate" to parseTimestamp(binding.expirationInput.text.toString())
        )

        val userId = getCurrentUserId()
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Make sure 'item.documentId' is not null or empty
        val itemId = item.documentId
        if (itemId.isBlank()) {
            Toast.makeText(context, "Invalid item ID", Toast.LENGTH_SHORT).show()
            return
        }

        firestoreHelper.updateGroceryItem(userId, itemId, fieldsToUpdate, {
            Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show()
        }, {
            Toast.makeText(context, "Failed to update item: ${it.message}", Toast.LENGTH_SHORT).show()
        })
    }



//    private fun parseTimestamp(dateStr: String): Timestamp {
//        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
//        return Timestamp(sdf.parse(dateStr) ?: Date())
//    }

    private val TAG = "EditItemsFormFragment"

    private fun parseTimestamp(dateStr: String): Timestamp {
        if (dateStr.isBlank()) { // Check if the date string is empty or only whitespace
            Log.d(TAG, "Received an empty or invalid date string.")
            return Timestamp(Date()) // Return the current date or handle it as you see fit
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return try {
            Timestamp(sdf.parse(dateStr)!!)
        } catch (e: ParseException) {
            Log.e(TAG, "Failed to parse date: $dateStr", e)
            Timestamp(Date()) // Return the current date as a fallback
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Clear the binding when the view is destroyed
    }

}
