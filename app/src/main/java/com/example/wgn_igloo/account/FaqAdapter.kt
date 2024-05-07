package com.example.wgn_igloo.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R

class FaqAdapter(private val faqList: List<FaqItem>) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    // ViewHolder class to hold the views for each FAQ item
    class FaqViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionTextView: TextView = view.findViewById(R.id.question)
        val answerTextView: TextView = view.findViewById(R.id.answer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.faq_items_layout, parent, false)
        return FaqViewHolder(view)
    }


    // Bind data to the views in each ViewHolder
    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faq = faqList[position]
        holder.questionTextView.text = faq.question
        holder.answerTextView.text = faq.answer
        holder.answerTextView.visibility = View.GONE // Hide answer initially

        // Reveal answer on click
        holder.questionTextView.setOnClickListener {
            holder.answerTextView.visibility =
                if (holder.answerTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    // Returns the total number of items in the faqList
    override fun getItemCount() = faqList.size
}