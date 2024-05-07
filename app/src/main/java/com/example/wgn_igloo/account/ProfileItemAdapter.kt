package com.example.wgn_igloo.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wgn_igloo.R

class ProfileItemAdapter(private val myDataSet: List<ProfileItem>, private val listener: (ProfileItem) -> Unit) : RecyclerView.Adapter<ProfileItemAdapter.ViewHolder>() {

    data class ProfileItem(val imageResId: Int, val text: String)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.icon_image)
        val textView: TextView = view.findViewById(R.id.account_item_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the custom layout for each item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the data model based on position
        val profileItem = myDataSet[position]

        // Set item views based on your views and data model
        holder.textView.text = profileItem.text
        Glide.with(holder.imageView.context)
            .load(profileItem.imageResId) // Ensure this is a drawable or a correct image resource ID.
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            listener(profileItem)
        }
    }

    override fun getItemCount(): Int = myDataSet.size
}
