package com.example.wgn_igloo.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wgn_igloo.R

class AccountItemAdapter(private val data: List<AccountItem>, private val listener: (AccountItem) -> Unit) : RecyclerView.Adapter<AccountItemAdapter.ViewHolder>() {

    // Data model for each item in the RecyclerView
    data class AccountItem(val imageResId: Int, val text: String)

    // ViewHolder class to hold the views of each item
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.icon_image)
        val textView: TextView = view.findViewById(R.id.account_item_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.account_item, parent, false)
        return ViewHolder(view)
    }

    // Bind data to the views in each ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the data model based on position
        val accountItem = data[position]

        // Set item views based on your views and data model
        holder.textView.text = accountItem.text

        // Load image into ImageView using Glide library
        Glide.with(holder.imageView.context)
            .load(accountItem.imageResId)
            .into(holder.imageView)

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            listener(accountItem)
        }
    }

    // Return the total number of items in the data set
    override fun getItemCount(): Int = data.size
}
