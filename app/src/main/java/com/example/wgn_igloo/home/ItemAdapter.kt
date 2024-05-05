package com.example.wgn_igloo.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class ItemAdapter(private var items: List<GroceryItem>, private val firestoreHelper: FirestoreHelper) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    companion object {
        private const val TAG = "FirestoreHelper"
    }

    // State to track items moved to shopping list
    private var movedToShoppingList = mutableSetOf<Int>()

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
//    fun updateItems(newItems: List<GroceryItem>) {
//        items = newItems
//        notifyDataSetChanged()
//    }

    fun updateItems(newItems: List<GroceryItem>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                items[oldItemPosition].name == newItems[newItemPosition].name
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                items[oldItemPosition] == newItems[newItemPosition]
        })
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }


    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.inventory_item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
//        holder.itemTextView.text = "${item.name}"
//        holder.quantityValueTextView.text = "${item.quantity}"
//        holder.dateTextView.text = "${formatDate(item.expirationDate)}"
        holder.itemTextView.text = item.name
        holder.quantityValueTextView.text = item.quantity.toString()
        holder.dateTextView.text = formatDate(item.expirationDate)
        holder.quantityTextView.visibility = View.GONE
        holder.quantityValueTextView.visibility = View.GONE
        holder.expirationTextView.visibility = View.GONE
        holder.dateTextView.visibility = View.GONE
        holder.sharedWithTextView.visibility = View.GONE
        holder.sharedWithValueTextView.visibility = View.GONE
        holder.requestToBorrow.visibility = View.GONE
        holder.addToShoppingList.visibility = View.GONE
        holder.deleteButton.visibility = View.GONE

//        holder.deleteButton.setOnClickListener {
//            val userId = firestoreHelper.getCurrentUserId()
//            if (userId != null) {
//                firestoreHelper.deleteGroceryItem(userId, item.name,
//                    onSuccess = {
//                        // If deletion is successful, update the UI by removing the item from the list
//                        val newList = items.toMutableList()
//                        newList.removeAt(holder.adapterPosition)
//                        updateItems(newList)
//                    },
//                    onFailure = { exception ->
//                        // Handle failure, e.g., show an error message
//                        Log.e(TAG, "Error deleting item", exception)
//                    }
//                )
//            }
//        }

        holder.deleteButton.setOnClickListener {
            val userId = firestoreHelper.getCurrentUserId()
            val position = holder.adapterPosition
            if (userId != null && position != RecyclerView.NO_POSITION) {
                val documentId = items[position].documentId
                if (documentId.isNotEmpty()) {
                    firestoreHelper.deleteGroceryItem(userId, documentId,
                        onSuccess = {
                            val newList = items.toMutableList().apply {
                                removeAt(position)
                            }
                            updateItems(newList)
                        },
                        onFailure = { exception ->
                            Toast.makeText(holder.itemView.context, "Error deleting item: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    Toast.makeText(holder.itemView.context, "Invalid document ID", Toast.LENGTH_SHORT).show()
                }
            }
        }

        holder.addToShoppingList.setOnClickListener {
            val userId = firestoreHelper.getCurrentUserId()
            val position = holder.adapterPosition
            if (userId != null && position != RecyclerView.NO_POSITION) {
                val itemName = items[position].name
                firestoreHelper.moveItemToShoppingList(userId, itemName,
                    onSuccess = {
                        // Log success
                        Log.d(TAG, "Successfully moved item to shopping list")
                        // Assuming `context` is available or use holder.itemView.context
                        Toast.makeText(holder.itemView.context, "Item moved to shopping list", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        // Log error and notify user
                        Log.e(TAG, "Failed to move item to shopping list", exception)
                        // Assuming `context` is available or use holder.itemView.context
                        Toast.makeText(holder.itemView.context, "Error moving item to shopping list: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }

        if (movedToShoppingList.contains(position)) {
            holder.addToShoppingList.isEnabled = false
            holder.addToShoppingList.text = "Added"
        } else {
            holder.addToShoppingList.isEnabled = true
            holder.addToShoppingList.text = "Add to List"
        }

//        holder.addToShoppingList.setOnClickListener {
//            val userId = firestoreHelper.getCurrentUserId()
//            if (userId != null) {
//                firestoreHelper.moveItemToShoppingList(userId, item.name, onSuccess = {
//                    movedToShoppingList.add(position)
//                    notifyItemChanged(position)
//                    // Toast to show success
//                    Toast.makeText(holder.itemView.context, "Item moved to shopping list", Toast.LENGTH_SHORT).show()
//                }, onFailure = { exception ->
//                    Toast.makeText(holder.itemView.context, "Failed to move item: ${exception.message}", Toast.LENGTH_LONG).show()
//                })
//            }
//        }

        holder.editButton.visibility = View.GONE
        holder.editButton.setOnClickListener {
            it.context?.let { context ->
                if (context is AppCompatActivity) {
                    val fragment = EditItemsFormFragment.newInstance(item)
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
        holder.itemTextView.setOnClickListener {
            if (item.isOwnedByUser) {
                holder.editButton.visibility =
                    if (holder.editButton.visibility == View.VISIBLE) View.GONE else View.VISIBLE

                holder.deleteButton.visibility =
                    if (holder.deleteButton.visibility == View.VISIBLE) View.GONE else View.VISIBLE

                holder.addToShoppingList.visibility =
                    if (holder.addToShoppingList.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            } else {
                holder.requestToBorrow.visibility =
                    if (holder.requestToBorrow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                holder.addToShoppingList.visibility =
                    if (holder.addToShoppingList.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            holder.quantityTextView.visibility =
                if (holder.quantityTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            holder.quantityValueTextView.visibility =
                if (holder.quantityValueTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            holder.expirationTextView.visibility =
                if (holder.expirationTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            holder.dateTextView.visibility =
                if (holder.dateTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            if (item.sharedWith.isNotEmpty()) {
                holder.sharedWithValueTextView.text = "${item.sharedWith}"
                holder.sharedWithTextView.visibility =
                    if (holder.sharedWithTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                holder.sharedWithValueTextView.visibility =
                    if (holder.sharedWithValueTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }
    }

    private fun formatDate(timestamp: Timestamp): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return dateFormat.format(timestamp.toDate())
    }

}