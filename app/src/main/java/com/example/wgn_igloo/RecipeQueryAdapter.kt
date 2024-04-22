package com.example.wgn_igloo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.databinding.RecipeItemLayoutBinding
import com.bumptech.glide.Glide

class RecipeQueryAdapter(private var recipeList: List<RecipeSearch>) :
    RecyclerView.Adapter<RecipeQueryAdapter.RecipeViewHolder>() {

    fun updateData(newRecipes: List<RecipeSearch>) {
        recipeList = newRecipes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        with(holder.binding) {
            Glide.with(holder.itemView.context).load(recipe.imageId).into(recipeImage)
            recipeTitle.text = recipe.recipeName
            totalTimeInfo.text = recipe.totalTime
            servingSizeInfo.text = recipe.servingSize
        }
    }

    override fun getItemCount() = recipeList.size

    class RecipeViewHolder(val binding: RecipeItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}