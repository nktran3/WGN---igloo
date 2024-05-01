package com.example.wgn_igloo.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class ItemAdapter(private var items: List<GroceryItem>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

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
    fun updateItems(newItems: List<GroceryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.inventory_item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.itemTextView.text = "${item.name}"
        holder.quantityValueTextView.text = "${item.quantity}"
        holder.dateTextView.text = "${formatDate(item.expirationDate)}"
        holder.quantityTextView.visibility = View.GONE
        holder.quantityValueTextView.visibility = View.GONE
        holder.expirationTextView.visibility = View.GONE
        holder.dateTextView.visibility = View.GONE
        holder.sharedWithTextView.visibility = View.GONE
        holder.sharedWithValueTextView.visibility = View.GONE
        holder.requestToBorrow.visibility = View.GONE
        holder.addToShoppingList.visibility = View.GONE
        holder.editButton.visibility = View.GONE
        holder.deleteButton.visibility = View.GONE
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