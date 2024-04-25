package com.example.wgn_igloo

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class InstructionsAdapter(private val instructions: List<String>) : RecyclerView.Adapter<InstructionsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.instruction_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.instructions_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = "${position + 1}. ${instructions[position]}"
    }

    override fun getItemCount() = instructions.size
}
