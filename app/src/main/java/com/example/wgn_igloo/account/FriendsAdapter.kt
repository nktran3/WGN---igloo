package com.example.wgn_igloo.account

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R

class FriendsAdapter(private var friends: MutableList<Friend>) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    // ViewHolder class to hold the views for each Friend item
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fullNameTextView: TextView = view.findViewById(R.id.fridge_members)
        val usernameTextView: TextView = view.findViewById(R.id.fridge_members_username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friends_item_layout, parent, false)
        return ViewHolder(view)
    }

    // Bind data to the views in each ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        holder.fullNameTextView.text = "${friend.givenName} ${friend.familyName}"
        holder.usernameTextView.text = friend.username
        Log.d("MemberAdapter", "Binding view for: ${friend.givenName} ${friend.familyName}") // Debugging to see what's being bound
    }


    // Returns the total number of items in the friends list
    override fun getItemCount() = friends.size

    // Updates the friends list with new data
    fun updateFriends(newFriends: List<Friend>) {
        friends.clear()
        friends.addAll(newFriends)
        notifyDataSetChanged()
    }
}