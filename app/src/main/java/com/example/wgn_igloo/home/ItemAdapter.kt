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
import com.example.wgn_igloo.inbox.Notifications
import com.example.wgn_igloo.inbox.NotificationsViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale


class ItemAdapter(private var items: List<GroceryItem>, private val firestoreHelper: FirestoreHelper, private val viewModel: NotificationsViewModel, private var currentInventoryUserId: String?) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    companion object {
        private const val TAG = "ItemAdapter"
    }

    private var friendsUID = ""

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

    fun updateItems(newItems: List<GroceryItem>, newUID: String?) {
        currentInventoryUserId = newUID  // Update the current inventory user ID
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                items[oldItemPosition].name == newItems[newItemPosition].name
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                items[oldItemPosition] == newItems[newItemPosition]
        })
        Log.d(TAG, "Friends UID: $newUID")
        if (newUID != null) {
            friendsUID = newUID
            Log.d(TAG, "Set Friends UID to $newUID")

        }
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

        holder.deleteButton.setOnClickListener {
            val userId = firestoreHelper.getCurrentUserId()
            val position = holder.adapterPosition
            if (userId != null && position != RecyclerView.NO_POSITION) {
                firestoreHelper.deleteGroceryItem(userId, items[position].name,
                    onSuccess = {
                        // If deletion is successful, remove the item from the list safely
                        val newList = items.toMutableList()
                        newList.removeAt(position)
                        updateItems(newList, null)
                    },
                    onFailure = { exception ->
                        // Handle failure, e.g., show an error message
                        Log.e(TAG, "Error deleting item", exception)
                    }
                )
            }
        }

        holder.addToShoppingList.setOnClickListener {
            val itemName = items[position].name
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId == null) {
                Toast.makeText(holder.itemView.context, "You must be logged in to modify the shopping list.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Define the friendUserId somehow, perhaps passed through the constructor or some other method
            val friendUserId = currentInventoryUserId ?: userId
            Log.e(TAG, "THIS IS THE CURRENT friendUserId: $friendUserId")


            firestoreHelper.moveItemToShoppingList(userId, friendUserId, itemName,
                onSuccess = {
                    Toast.makeText(holder.itemView.context, "Item added to both your and your friend's shopping list.", Toast.LENGTH_SHORT).show()
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to add item to shopping lists", exception)
                    Toast.makeText(holder.itemView.context, "Failed to add item: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            )
        }

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

                holder.requestToBorrow.setOnClickListener {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) {
                        Toast.makeText(holder.itemView.context, "User not logged in!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // Assuming friendsUID and uid are not empty
                    var friendName = ""
                    var userName = ""

                    firestoreHelper.getUser(uid,
                        onSuccess = { user ->
                            userName = user.givenName
                            // Nested call inside first onSuccess
                            firestoreHelper.getUser(friendsUID,
                                onSuccess = { friend ->
                                    friendName = friend.givenName
                                    sendNotification(friendName, userName, uid, friendsUID, item.name, holder)
                                },
                                onFailure = { exception ->
                                    Log.d(TAG, "Error getting friend", exception)
                                    Toast.makeText(holder.itemView.context, "Failed to find friend.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onFailure = { exception ->
                            Log.d(TAG, "Error getting user", exception)
                            Toast.makeText(holder.itemView.context, "Failed to notify user.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
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

    private fun sendNotification(friendName: String, userName: String, uid: String, friendsUID: String, itemName: String, holder: RecyclerView.ViewHolder) {
        if (friendName.isNotEmpty() && userName.isNotEmpty()) {
            val notif = Notifications(
                title = "Item Request",
                message = "You want to borrow $itemName from $friendName"
            )
            val notifToFriend = Notifications(
                title = "Item Request",
                message = "$userName wants to borrow $itemName from you"
            )
            firestoreHelper.addNotifications(uid, notif)
            firestoreHelper.addNotifications(friendsUID, notifToFriend)
            viewModel.setRefreshNotifications(true)
            Toast.makeText(holder.itemView.context, "Notification sent!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDate(timestamp: Timestamp): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return dateFormat.format(timestamp.toDate())
    }

}