package com.example.wgn_igloo.inbox

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.grocery.GroceryItem
import com.example.wgn_igloo.home.InventoryDisplayFragment
import com.example.wgn_igloo.notifications.Notifications
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

private const val TAG = "InboxPage"
class InboxPage : Fragment() {
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationsAdapter
    private var notificationList: MutableList<Notifications> = mutableListOf(
        Notifications(title = "Item Request", message = "Gary borrowed coconut")
    ) // Ensure this is mutable

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inbox_page, container, false)
        recyclerView = view.findViewById(R.id.notifications_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        notificationAdapter = NotificationsAdapter(notificationList)
        recyclerView.adapter = notificationAdapter
        firestoreHelper = FirestoreHelper(requireContext())
        checkExpiring()
        fetchNotifications()
        return view
    }


    private fun checkExpiring() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users")
            .document(userUid).collection("groceryItems")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val today = Calendar.getInstance()
                    for (document in snapshot.documents) {
                        val item = document.toObject(GroceryItem::class.java)
                        val itemId = document.id  // Retrieving the document ID which serves as the itemID
                        if (item != null) {
                            val expirationCal: Calendar = Calendar.getInstance()
                            expirationCal.timeInMillis = item.expirationDate.seconds * 1000L
                            val date: String = DateFormat.format("dd-MM-yyyy", expirationCal).toString()
                            val diffTime = expirationCal.timeInMillis - today.timeInMillis
                            val diffDays = diffTime / (24 * 60 * 60 * 1000)
                            Log.d(TAG, "${item.name} expires on $date. Days until expiration: $diffDays")
                            // Check if notification needs to be sent
                            if (diffDays < 3 && !item.expireNotified) {
                                val notif = Notifications(
                                    title = "Item Expiring Soon",
                                    message = "$userUid's ${item.name} is expiring in $diffDays days"
                                )
                                if (userUid != null) {
                                    // Assuming firestoreHelper is already initialized and addNotifications method is appropriately defined
                                    firestoreHelper.addNotifications(userUid, notif)
                                    // Update expireNotified to true to avoid multiple notifications
                                    FirebaseFirestore.getInstance().collection("users")
                                        .document(userUid).collection("groceryItems").document(itemId)
                                        .update("expireNotified", true)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Notification flag updated for ${item.name}")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(TAG, "Error updating notification flag for ${item.name}", e)
                                        }
                                }
                            }
                        }
                    }
                    fetchNotifications()  // Assuming this method is defined elsewhere to handle notification fetching
                } else {
                    Log.d(TAG, "No grocery items found")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(InventoryDisplayFragment.TAG, "Error getting documents: ", exception)
            }
    }


    private fun fetchNotifications() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(userUid).collection("notificationItems")
            .get()
            .addOnSuccessListener { snapshot ->
                val notifs = snapshot.toObjects(Notifications::class.java)
                print("Notifications:" + notifs)
                notificationAdapter.updateItems(notifs)
            }
            .addOnFailureListener { exception ->
                Log.d(InventoryDisplayFragment.TAG, "Error getting documents: ", exception)
            }
    }


    class NotificationsAdapter(private var notifications: List<Notifications>) :
        RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_text_item, parent, false)
            return NotificationViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            val notification = notifications[position]
            holder.bind(notification)
        }

        fun updateItems(newNotifs: List<Notifications>) {
            notifications = newNotifs
            notifyDataSetChanged()
        }

        override fun getItemCount() = notifications.size

        class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(notification: Notifications) {
                itemView.findViewById<TextView>(R.id.notification_title).text = notification.title
                itemView.findViewById<TextView>(R.id.notification_body).text = notification.message
            }
        }
    }
}
