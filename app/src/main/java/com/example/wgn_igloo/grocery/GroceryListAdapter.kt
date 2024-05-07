package com.example.wgn_igloo.grocery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R

class GroceryListAdapter(private var items: List<GroceryListItem>, private val onItemChecked: (GroceryListItem) -> Unit) : RecyclerView.Adapter<GroceryListAdapter.GroceryListViewHolder>() {

    class GroceryListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.groceryItemCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grocery_list_item_layout, parent, false)
        return GroceryListViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroceryListViewHolder, position: Int) {
        val item = items[position]
        holder.checkBox.text = item.name
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onItemChecked(item)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<GroceryListItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}