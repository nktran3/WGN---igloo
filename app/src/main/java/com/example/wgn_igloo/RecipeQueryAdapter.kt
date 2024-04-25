package com.example.wgn_igloo

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.databinding.RecipeItemLayoutBinding
import com.bumptech.glide.Glide

private const val TAG = "RecipeQueryAdapter"
class RecipeQueryAdapter(private var recipeList: List<RecipeSearch>) :
    RecyclerView.Adapter<RecipeQueryAdapter.RecipeViewHolder>() {

    // Updates the adapter's data with the parsed API response and prompts for a refresh of the RecyclerView
    fun updateData(newRecipes: List<RecipeSearch?>) {
        recipeList = newRecipes as List<RecipeSearch>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        Log.d(TAG, "${recipe.recipeName}")
        var cuisineTypeList = recipe.cuisineType?.joinToString(separator = ", ")
        var dietTypeList = recipe.dietType?.joinToString(separator = ", ")
        with(holder.binding) {
            Glide.with(holder.itemView.context).load(recipe.imageId).into(recipeImage)
            recipeTitle.text = recipe.recipeName
            totalTime.text = "Total Time: " + recipe.totalTime + "mins"
            cuisineType.text = "Cuisine: " + cuisineTypeList
            dietType.text = "Diet: " + dietTypeList
            servingSize.text = "Serving Size: " + recipe.servingSize
        }
    }
    override fun getItemCount() = recipeList.size
    class RecipeViewHolder(val binding: RecipeItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}