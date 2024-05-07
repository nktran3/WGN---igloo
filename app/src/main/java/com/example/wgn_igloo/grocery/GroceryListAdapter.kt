package com.example.wgn_igloo.grocery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R

class GroceryListAdapter(private var items: List<GroceryListItem>, private val onItemChecked: (GroceryListItem) -> Unit) : RecyclerView.Adapter<GroceryListAdapter.ViewHolder>() {

    // ViewHolder class to hold the views for each item in the RecyclerView
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.groceryItemCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grocery_list_item_layout, parent, false)
        return ViewHolder(view)
    }

    // Bind data to ViewHolder views
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.checkBox.text = item.name
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onItemChecked(item)
            }
        }
    }

    // Return the number of items in the list
    override fun getItemCount() = items.size

    // Update the list of items and notify the adapter about the change
    fun updateItems(newItems: List<GroceryListItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}