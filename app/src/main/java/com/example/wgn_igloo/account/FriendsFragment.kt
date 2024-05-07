package com.example.wgn_igloo.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.databinding.FragmentFriendsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment() {
    private lateinit var memberAdapter: FriendsAdapter
    private var memberList: MutableList<Friend> = mutableListOf()
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var toolbarFriends: Toolbar
    private var _binding: FragmentFriendsBinding? = null
    private lateinit var toolbarFriendsTitle: TextView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarFriends = binding.toolbarFriends
        toolbarFriendsTitle = binding.toolbarFriendsTitle
        updateToolbar()

        binding.fridgeMembersRecyclerView.layoutManager = LinearLayoutManager(context)
        memberAdapter = FriendsAdapter(memberList)
        binding.fridgeMembersRecyclerView.adapter = memberAdapter

        firestoreHelper = FirestoreHelper(requireContext())  // Initialize FirestoreHelper

        binding.submitFriendRequestButton.setOnClickListener {
            val friendUid = binding.friendUidInput.text.toString()
            if (friendUid.isNotEmpty()) {
                handleFriendRequest(friendUid)
            } else {
                Toast.makeText(context, "Please enter a UID", Toast.LENGTH_SHORT).show()
            }
        }

        fetchMembers()
    }
    private fun updateToolbar() {
        toolbarFriends.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.back_icon)
        toolbarFriends.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbarFriendsTitle.text = "Friends"
    }
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
                val friendsList = mutableListOf<Friend>()
                snapshot?.documents?.forEach { document ->
                    val username = document.getString("username") ?: return@forEach
                    val uid = document.getString("uid") ?: return@forEach
                    val givenName = document.getString("givenName") ?: ""
                    val familyName = document.getString("familyName") ?: ""
                    val friendSince = document.getDate("friendSince")
                    friendsList.add(Friend(username, uid, givenName, familyName, friendSince))
                }
                memberAdapter.updateMembers(friendsList)
            }
    }

    private fun getCurrentUserId(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid  // This will return the user ID or null if no user is logged in
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





