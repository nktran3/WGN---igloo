package com.example.wgn_igloo.inbox

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.home.GroceryItem
import com.example.wgn_igloo.home.InventoryDisplayFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.ceil

private const val TAG = "InboxPage"
class InboxPage : Fragment() {

    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationsAdapter
    private lateinit var viewModel: NotificationsViewModel
    private var notificationList: MutableList<Notifications> = mutableListOf(
        Notifications(title = "", message = "")
    )

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
        viewModel = ViewModelProvider(requireActivity()).get(NotificationsViewModel::class.java)

        viewModel.refreshNotifications.observe(viewLifecycleOwner) { refresh ->
            if (refresh) {
                fetchNotifications()
                viewModel.setRefreshNotifications(false)
            }
        }
        return view
    }


    // Function used to scan the fridge items in the database and check to see if any items are expiring
    private fun checkExpiring() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users")
            .document(userUid).collection("groceryItems")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val today = Calendar.getInstance() // Get today's data
                    for (document in snapshot.documents) {
                        val item = document.toObject(GroceryItem::class.java)
                        val itemId = document.id  // Retrieving the document ID which serves as the itemID
                        if (item != null) {
                            val expirationCal: Calendar = Calendar.getInstance()
                            expirationCal.timeInMillis = item.expirationDate.seconds * 1000L // Get the time of expiration date
                            val date: String = DateFormat.format("dd-MM-yyyy", expirationCal).toString() // Get the time of today dates
                            val diffTime = expirationCal.timeInMillis - today.timeInMillis // Get the difference between the two
                            val diffDays = ceil(diffTime.toDouble() / (24 * 60 * 60 * 1000)).toInt()
                            Log.d(TAG, "${item.name} expires on $date. Days until expiration: $diffDays")
                            // Check if notification needs to be sent
                            if (diffDays < 3 && !item.expireNotified) {
                                var notif: Notifications
                                if (diffDays < 0){
                                    // Item is passed expired (1 day)
                                    if (diffDays == -1){
                                        notif = Notifications(
                                            title = "Item Expired",
                                            message = "${item.name} expired ${abs(diffDays)} day ago"
                                        )
                                    // Item is passed expired (more than 1 day)
                                    } else {
                                        notif = Notifications(
                                            title = "Item Expired",
                                            message = "${item.name} expired ${abs(diffDays)} days ago"
                                        )
                                    }
                                // Item is expiring today
                                } else if (diffDays == 0) {
                                    notif = Notifications(
                                        title = "Item Expiring Today",
                                        message = "${item.name} is expiring today!!!"
                                    )
                                // Item is expiring soon (1 day)
                                } else if (diffDays == 1){
                                    notif = Notifications(
                                        title = "Item Expiring Soon",
                                        message = "${item.name} is expiring in $diffDays day"
                                    )
                                // Item is expiring soon (more than 1 day)
                                } else {
                                    notif = Notifications(
                                        title = "Item Expiring Soon",
                                        message = "${item.name} is expiring in ${abs(diffDays)} days"
                                    )
                                }
                                // Send notification
                                if (userUid != null) {
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
                    // Update the UI
                    fetchNotifications()
                } else {
                    Log.d(TAG, "No grocery items found")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(InventoryDisplayFragment.TAG, "Error getting documents: ", exception)
            }
    }

    // Function used to fetch all new notification items in the database
    private fun fetchNotifications() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(userUid).collection("notificationItems").orderBy("timeCreated", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val notifs = snapshot.toObjects(Notifications::class.java)
                print("Notifications:$notifs")
                // Update the UI with new notification items
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
                val titleView = itemView.findViewById<TextView>(R.id.notification_title)
                val bodyView = itemView.findViewById<TextView>(R.id.notification_body)

                titleView.text = notification.title
                bodyView.text = notification.message

                // change text color for expired items
                if (notification.title.contains("Expiring Today") || notification.title.contains("Expired")) {
                    bodyView.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
                } else {
                    bodyView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                }
            }
        }
    }
}