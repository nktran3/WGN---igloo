package com.example.wgn_igloo.database

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Members : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var memberAdapter: MemberAdapter
    private var memberList: MutableList<Member> = mutableListOf()
    private lateinit var friendUidInput: EditText
    private lateinit var submitButton: Button
    private lateinit var firestoreHelper: FirestoreHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_members, container, false)

        recyclerView = view.findViewById(R.id.fridge_members_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        memberAdapter = MemberAdapter(memberList)
        recyclerView.adapter = memberAdapter

        // Initialize EditText and Button
        friendUidInput = view.findViewById(R.id.friendUidInput)
        submitButton = view.findViewById(R.id.submitFriendRequestButton)

        // Initialize FirestoreHelper
        firestoreHelper = FirestoreHelper(requireContext())  // Ensure this line is added

        submitButton.setOnClickListener {
            val friendUid = friendUidInput.text.toString()
            if (friendUid.isNotEmpty()) {
                handleFriendRequest(friendUid)
            } else {
                Toast.makeText(context, "Please enter a UID", Toast.LENGTH_SHORT).show()
            }
        }

        fetchMembers()
        return view
    }

//
//    private fun handleFriendRequest(friendUid: String) {
//        val currentUserId = getCurrentUserId()
//        if (currentUserId == null) {
//            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        firestoreHelper.getUserByUid(friendUid,
//            onSuccess = { user ->
//                firestoreHelper.addFriend(currentUserId, friendUid,
//                    onSuccess = {
//                        Toast.makeText(context, "Friend added successfully", Toast.LENGTH_SHORT).show()
//                    },
//                    onFailure = { exception ->
//                        Toast.makeText(context, "Failed to add friend: ${exception.message}", Toast.LENGTH_SHORT).show()
//                    }
//                )
//            },
//            onFailure = { exception ->
//                Toast.makeText(context, "User not found: ${exception.message}", Toast.LENGTH_SHORT).show()
//            }
//        )
//    }
    private fun handleFriendRequest(friendUsername: String) {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        firestoreHelper.addFriend(currentUserId, friendUsername,
            onSuccess = {
                Toast.makeText(context, "Friend added successfully", Toast.LENGTH_SHORT).show()
            },
            onFailure = { exception ->
                Toast.makeText(context, "Failed to add friend: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun fetchMembers() {
        val currentUserId = getCurrentUserId() ?: return  // Return early if no user ID
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(currentUserId).collection("friends")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val friendsList = mutableListOf<Member>()
                snapshot?.documents?.forEach { document ->
                    val username = document.getString("username") ?: return@forEach
                    val uid = document.getString("uid") ?: return@forEach
                    val friendSince = document.getDate("friendSince")
                    friendsList.add(Member(username, uid, friendSince))
                }
                memberAdapter.updateMembers(friendsList)
            }
    }




    // Inside your Members fragment class
    private fun getCurrentUserId(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid  // This will return the user ID or null if no user is logged in
    }
}


class MemberAdapter(private var members: MutableList<Member>) :
    RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.fridge_members)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.members_item_layout, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.textView.text = member.username  // Use username to display in TextView
    }

    override fun getItemCount() = members.size

    fun updateMembers(newMembers: List<Member>) {
        members.clear()
        members.addAll(newMembers)
        notifyDataSetChanged()
    }
}


