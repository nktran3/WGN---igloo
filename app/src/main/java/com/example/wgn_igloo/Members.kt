package com.example.wgn_igloo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Members : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var memberAdapter: MemberAdapter
    private lateinit var memberList: List<Member>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_members, container, false)
        recyclerView = view.findViewById(R.id.fridge_members_recycler_view)

        // Initialize the member list with your custom Member class
        memberList = listOf(
            Member("Member 1"),
            Member("Member 2"),
            Member("Member 3")
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        memberAdapter = MemberAdapter(memberList)
        recyclerView.adapter = memberAdapter
        return view
    }
}

class MemberAdapter(private val members: List<Member>) :
    RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.fridge_members)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.members_item_layout, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.textView.text = member.name
    }

    override fun getItemCount() = members.size
}
