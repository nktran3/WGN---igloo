package com.example.wgn_igloo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class InboxPage : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "FirestoreHelper"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inbox_page, container, false)
    }

    fun grantAccessToItem(ownerId: String, itemId: String, requesterId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Fetch the item details
        val originalItemRef = db.collection("users").document(ownerId).collection("groceryItems").document(itemId)
        originalItemRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val itemData = document.data
                val newItemRef = db.collection("users").document(requesterId).collection("groceryItems").document()
                newItemRef.set(itemData!!)
                    .addOnSuccessListener {
                        Log.d(TAG, "Access granted successfully")
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error granting access", exception)
                        onFailure(exception)
                    }
            }
        }
    }

    fun listenForAccessRequests(ownerId: String, itemId: String, onNewRequest: (String) -> Unit) {
        db.collection("users").document(ownerId).collection("groceryItems").document(itemId).collection("accessRequests")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.forEach { doc ->
                    onNewRequest(doc.id)  // Assuming the document ID is the UID of the requester
                }
            }
    }



}