package com.example.wgn_igloo.home

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.databinding.FragmentEditItemsFormBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import com.google.gson.Gson
import java.util.*

class EditItemsFormFragment : Fragment() {

    private var _binding: FragmentEditItemsFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var firestore: FirebaseFirestore

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemJson = arguments?.getString(ARG_ITEM_JSON)
        val item = Gson().fromJson(itemJson, GroceryItem::class.java)
        if (item != null) {
            setupViews(item)
        }
    }

    private fun setupViews(item: GroceryItem) {
        binding.itemInput.setText(item.name)
        binding.quantityInput.setText(item.quantity.toString())
        setupCategorySpinner(item.category)
        setupDatePicker(item.expirationDate)
        setupSharedWithSpinner(item.sharedWith)

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
                    val sharedWithOptions = documents.mapNotNull { it.getString("sharedWith") }.toTypedArray()
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sharedWithOptions)
                    binding.sharedWithInput.adapter = adapter
                    val selectedIndex = sharedWithOptions.indexOfFirst { it == selectedSharedWith }
                    binding.sharedWithInput.setSelection(if (selectedIndex >= 0) selectedIndex else 0)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SetupSpinner", "Failed to fetch shared options: ${e.message}", e)
                Toast.makeText(context, "Failed to fetch shared options: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }




    private fun setupDatePicker(selectedDate: Timestamp) {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate.toDate()
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            updateLabel(calendar)
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
        val updatedItem = item.copy(
            name = binding.itemInput.text.toString(),
            quantity = binding.quantityInput.text.toString().toInt(),
            category = binding.categoryInput.selectedItem.toString(),
            sharedWith = binding.sharedWithInput.selectedItem.toString(),
            expirationDate = parseTimestamp(binding.expirationInput.text.toString()),
            status = item.status, // assuming you want to keep the status unchanged
            isOwnedByUser = item.isOwnedByUser // assuming you want to keep the ownership status unchanged
        )

        firestoreHelper.updateGroceryItem(updatedItem, {
            Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show()
        }, {
            Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show()
        })
    }

    private fun parseTimestamp(dateStr: String): Timestamp {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return Timestamp(sdf.parse(dateStr) ?: Date())
    }
}
