package com.example.wgn_igloo.home

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.databinding.FragmentEditItemsFormBinding
import com.google.gson.Gson
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
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

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        _binding = FragmentEditItemsFormBinding.inflate(inflater, container, false)
//        firestore = FirebaseFirestore.getInstance()
//        return binding.root
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditItemsFormBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val itemJson = arguments?.getString(ARG_ITEM_JSON)
//        val item = Gson().fromJson(itemJson, GroceryItem::class.java)
//        setupViews(item)
//        loadSpinnerData(item)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemId = arguments?.getString("item_id") ?: return
        fetchItemAndSetupViews(itemId)
    }

    private fun fetchItemAndSetupViews(itemId: String) {
        val userId = FirestoreHelper(requireContext()).getCurrentUserId() ?: return
        firestore.document("/users/$userId/groceryItems/$itemId").get().addOnSuccessListener { documentSnapshot ->
            val item = documentSnapshot.toObject(GroceryItem::class.java) ?: return@addOnSuccessListener
            setupViews(item)
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to fetch item details", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun setupViews(item: GroceryItem?) {
////        item?.let {
////            binding.itemInput.setText(it.name)
////            binding.quantityInput.setText(it.quantity.toString())
//////            setupCategorySpinner(it.category)
////            setupDatePicker(it.expirationDate)
//////            setupSharedWithSpinner(it.sharedWith)
////        }
//        item?.let {
//            binding.itemInput.setText(it.name)
//            binding.quantityInput.setText(it.quantity.toString())
//        }
//        binding.submitButton.setOnClickListener {
//            updateItem()
//        }
//    }
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

//    private fun loadSpinnerData(item: GroceryItem?) {
//        firestore.collection("categories").get().addOnSuccessListener { snapshot ->
//            val categories = snapshot.documents.map { it.id }
//            setupCategorySpinner(categories, item?.category ?: "")
//        }
//
//        firestore.collection("shared_with_options").get().addOnSuccessListener { snapshot ->
//            val sharedWithOptions = snapshot.documents.map { it.id }
//            setupSharedWithSpinner(sharedWithOptions, item?.sharedWith ?: "")
//        }
//    }

//    private fun setupCategorySpinner(categories: List<String>, selectedCategory: String) {
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
//        binding.categoryInput.adapter = adapter
//        binding.categoryInput.setSelection(categories.indexOf(selectedCategory))
//    }

    private fun setupCategorySpinner(selectedCategory: String) {
        val categories = arrayOf("Drinks", "Dairy", "Produce") // Should be fetched from Firestore or defined statically
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        binding.categoryInput.adapter = adapter
        binding.categoryInput.setSelection(categories.indexOfFirst { it == selectedCategory })
    }

//    private fun setupSharedWithSpinner(sharedWithOptions: List<String>, selectedSharedWith: String) {
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sharedWithOptions)
//        binding.sharedWithInput.adapter = adapter
//        binding.sharedWithInput.setSelection(sharedWithOptions.indexOf(selectedSharedWith))
//    }
    private fun setupSharedWithSpinner(selectedSharedWith: String) {
        val sharedWithOptions = arrayOf("None", "Family", "Friends") // Should be fetched from Firestore or defined statically
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sharedWithOptions)
        binding.sharedWithInput.adapter = adapter
        binding.sharedWithInput.setSelection(sharedWithOptions.indexOfFirst { it == selectedSharedWith })
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

//    private fun updateItem() {
//        val updatedItem = GroceryItem(
//            name = binding.itemInput.text.toString(),
//            quantity = binding.quantityInput.text.toString().toInt(),
//            category = binding.categoryInput.selectedItem.toString(),
//            sharedWith = binding.sharedWithInput.selectedItem.toString(),
//            expirationDate = Timestamp(Date()),  // Replace with actual date parsing logic
//            dateBought = Timestamp.now(),
//            status = true,
//            isOwnedByUser = true
//        )
//
//        FirestoreHelper(requireContext()).updateGroceryItem(updatedItem, {
//            Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show()
//        }, {
//            Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show()
//        })
//    }
    private fun updateItem(item: GroceryItem) {
        val updatedItem = item.copy(
            name = binding.itemInput.text.toString(),
            quantity = binding.quantityInput.text.toString().toInt(),
            category = binding.categoryInput.selectedItem.toString(),
            sharedWith = binding.sharedWithInput.selectedItem.toString(),
            expirationDate = FirestoreHelper(requireContext()).parseTimestamp(binding.expirationInput.text.toString()),
            dateBought = item.dateBought,
            status = item.status,
            isOwnedByUser = item.isOwnedByUser
        )

        FirestoreHelper(requireContext()).updateGroceryItem(updatedItem, {
            Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show()
        }, {
            Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show()
        })
    }
}
