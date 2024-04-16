package com.example.wgn_igloo

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.text.ParseException
import com.example.wgn_igloo.databinding.FragmentNewItemsFormBinding

private const val TAG = "NewItemsForm"

class NewItemsFormFragment : Fragment() {
  
    private var _binding: FragmentNewItemsFormBinding? = null
    private val binding get() = _binding!!
  
    var message: String? = null

    companion object {
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"

        fun newInstance(message: String): NewItemsFormFragment {
            val fragment = NewItemsFormFragment()
            val args = Bundle()
            args.putString(EXTRA_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }


    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var submitButton: Button
    private lateinit var itemInput: EditText
    private lateinit var categoryInput: Spinner
    private lateinit var expirationDateInput: EditText
    private lateinit var quantityInput: EditText
    private lateinit var sharedWithInput: EditText

    private val categoryList = arrayOf("choose an option", "Meat", "Vegetable", "Dairy", "Fruits", "Carbohydrate")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestoreHelper = FirestoreHelper(requireContext())
        arguments?.let {
            message = it.getString(EXTRA_MESSAGE)
            Log.d(TAG, "Testing to see if the data went through: $message")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_items_form, container, false)
//        val navigateButton: Button = view.findViewById(R.id.submit_button) // Assuming you have a button to navigate
        // Initialize your views here, similar to how you've done before
        categoryInput = view.findViewById(R.id.category_input)
        submitButton = view.findViewById(R.id.submit_button)
        itemInput = view.findViewById(R.id.item_input)
        expirationDateInput = view.findViewById(R.id.expiration_input)
        setupDatePicker()

        // Set item input to what barcode scanned
        itemInput.setText(message)

        submitButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NewItemsFormFragment()) // Ensure you have a container in your activity's layout where fragments are swapped
                .addToBackStack(null) // This line ensures you can navigate back to InventoryDisplayFragment
                .commit()
            navigateBack()
        }

        // Setup the ArrayAdapter for the Spinner
        val categoryAdapter = ArrayAdapter(
            requireContext(), // Context
            android.R.layout.simple_spinner_item, // Layout for the spinner's row
            categoryList // Data array
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        categoryInput.adapter = categoryAdapter

        categoryInput.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Skip the default prompt
                if (position == 0) {
                    // "choose an option" is selected, do nothing or prompt the user to select a valid option
                    Toast.makeText(context, "Please select a valid option", Toast.LENGTH_SHORT).show()
                } else {
                    // A valid category is selected, proceed with your logic
                    val selectedCategory = categoryList[position]
                    Toast.makeText(context, "Selected: $selectedCategory", Toast.LENGTH_SHORT).show()
                    // Implement your logic here based on the selected category
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optionally handle the scenario when nothing is selected
                navigateBack()
            }
        }

        // Initialize other inputs (ownerInput, imageInput, etc.) here as well
        submitButton.setOnClickListener {
            // blocker in submitGroceryItem to add to the database - under review
//            submitGroceryItem()
            navigateBack()
        }
        return view
    }

    private fun submitGroceryItem() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid == null) {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val category = categoryInput.selectedItem.toString().takeIf { it != "choose an option" } ?: return
        val name = itemInput.text.toString()
        val quantity = quantityInput.text.toString().toIntOrNull() ?: return
        val expirationDate = convertStringToTimestamp(expirationDateInput.text.toString())
        // Collect other inputs similarly
        // Assume convertStringToTimestamp is a method you'll implement to parse the date string to Timestamp

        val groceryItem = GroceryItem(
            category = category,
            expirationDate = expirationDate,
            dateBought = Timestamp.now(), // Assuming the current timestamp as dateBought
            name = name,
            quantity = quantityInput.text.toString().toInt(),
            sharedWith = sharedWithInput.text.toString(),
            status = true // Assuming a new item is always active, adjust based on your logic
        )

        // Now push this groceryItem to Firestore
        addGroceryItemForUser(FirebaseAuth.getInstance().currentUser?.uid ?: return, groceryItem)
    }

    // You might already have this method in the InventoryDisplayFragment. You can move it to a common utility class or directly use it here.
    private fun addGroceryItemForUser(uid: String, groceryItem: GroceryItem) {
        firestoreHelper.addGroceryItem(uid, groceryItem, onSuccess = {
            // Navigate back to InventoryDisplayFragment upon success
            requireActivity().supportFragmentManager.popBackStack()
            navigateBack()
        }, onFailure = { e ->
            Log.e("NewItemsFormFragment", "Failed to add item: ", e)
            Toast.makeText(context, "Failed to add item", Toast.LENGTH_SHORT).show()
        })
    }

    private fun navigateBack() {
        if (isAdded) {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel(calendar)
        }

        // Use an onFocusChangeListener to show the DatePicker when the EditText gains focus
        expirationDateInput.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                DatePickerDialog(requireContext(), datePickerListener, calendar
                    .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).also {
                    it.show()
                    view.clearFocus() // Optional: Clear focus after showing the dialog to prevent it from showing again on back press or navigation
                }
            }
        }

        // It might still be useful to keep the OnClickListener for users who might re-click the EditText after losing focus
        expirationDateInput.setOnClickListener {
            DatePickerDialog(requireContext(), datePickerListener, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateLabel(calendar: Calendar) {
        // Define your desired date format
        val dateFormat = "MM/dd/yyyy" // For example, "MM/dd/yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.US)
        expirationDateInput.setText(sdf.format(calendar.time))
    }

    private fun convertStringToTimestamp(dateStr: String): Timestamp {
        val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        return try {
            val parsedDate = format.parse(dateStr) ?: Date()
            Timestamp(parsedDate)
        } catch (e: ParseException) {
            Log.e("NewItemsFormFragment", "Failed to parse date: ", e)
            Timestamp.now() // Return current time if parsing fails
        }
    }
}