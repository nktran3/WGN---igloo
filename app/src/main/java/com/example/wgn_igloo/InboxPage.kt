package com.example.wgn_igloo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Notification(
    val notification: String
)
class InboxPage : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationsAdapter
    private lateinit var notificationList: List<Notification>

//    private val db = FirebaseFirestore.getInstance()
//
//    companion object {
//        const val TAG = "FirestoreHelper"
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_inbox_page, container, false)

        recyclerView = view.findViewById(R.id.notifications_recycler_view)

        notificationList = listOf(
             Notification("Roommate 1 requested to borrow eggs"),
            Notification("Roommate 2 requested to borrow milk"),
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        notificationAdapter = NotificationsAdapter(notificationList)
        recyclerView.adapter = notificationAdapter
        return view
    }

    class NotificationsAdapter(private val notifications: List<Notification>) :
        RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_action_item, parent, false)
            return NotificationViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            val notification = notifications[position]
            holder.bind(notification)
        }

        override fun getItemCount() = notifications.size

        class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(notification: Notification) {
                itemView.findViewById<TextView>(R.id.notification_request_text).text = notification.notification
            }
        }
    }





}