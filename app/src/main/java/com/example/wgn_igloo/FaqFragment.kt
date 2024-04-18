package com.example.wgn_igloo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FaqFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var faqAdapter: FaqAdapter
    private lateinit var faqList: List<FaqItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_faq, container, false)
        recyclerView = view.findViewById(R.id.faq_recycler_view)

        faqList = listOf(
            FaqItem("How to use the app?", "Here's how you can use the app..."),
            FaqItem("Where to register?", "You can register on the login page..."),
            FaqItem("How do you add members to your fridge", "You can add members by...")
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        faqAdapter = FaqAdapter(faqList)
        recyclerView.adapter = faqAdapter
        return view
    }
}
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
