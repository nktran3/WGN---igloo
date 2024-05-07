package com.example.wgn_igloo.recipe

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R

// Adapter used to display Ingredients  in the RecipeDetailFrag
class IngredientsAdapter(private val ingredients: List<String>) : RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.ingredient_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.ingredients_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = "• ${ingredients[position]}"
    }

    override fun getItemCount() = ingredients.size
}