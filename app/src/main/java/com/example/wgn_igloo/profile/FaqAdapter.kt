package com.example.wgn_igloo.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R

class FaqAdapter(private val faqList: List<FaqItem>) :
    RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    class FaqViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionTextView: TextView = view.findViewById(R.id.question)
        val answerTextView: TextView = view.findViewById(R.id.answer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.faq_items_layout, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faq = faqList[position]
        holder.questionTextView.text = faq.question
        holder.answerTextView.text = faq.answer
        holder.answerTextView.visibility = View.GONE

        holder.questionTextView.setOnClickListener {
            holder.answerTextView.visibility =
                if (holder.answerTextView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount() = faqList.size
}