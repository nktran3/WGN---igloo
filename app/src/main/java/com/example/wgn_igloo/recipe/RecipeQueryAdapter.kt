package com.example.wgn_igloo.recipe

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.databinding.RecipeItemLayoutBinding
import com.example.wgn_igloo.grocery.GroceryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "RecipeQueryAdapter"
class RecipeQueryAdapter(private var recipeList: List<RecipeSearch>) :
    RecyclerView.Adapter<RecipeQueryAdapter.RecipeViewHolder>() {

    // Updates the adapter's data with the parsed API response and prompts for a refresh of the RecyclerView
    fun updateData(newRecipes: List<RecipeSearch?>) {
        recipeList = newRecipes as List<RecipeSearch>
        notifyDataSetChanged()
        Log.d(TAG, "${recipeList.size}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        Log.d(TAG, "${recipe.recipeName}")
        var dishTypeList = recipe.dishType?.joinToString(separator =  ",")
        var cuisineTypeList = recipe.cuisineType?.joinToString(separator = ", ")
        var dietTypeList = recipe.dietType?.joinToString(separator = ", ")
        with(holder.binding) {
            Glide.with(holder.itemView.context).load(recipe.imageId).into(recipeImage)
            recipeTitle.text = recipe.recipeName
            totalTime.text = "Total Time: " + recipe.totalTime + " mins"
            cuisineType.text = "Cuisine: " + cuisineTypeList
            dietType.text = "Diet: " + dietTypeList
            servingSize.text = "Serving Size: " + recipe.servingSize
        }
        // Parse the recipe instructions
        var totalSteps: MutableList<String> = mutableListOf()
        for (instruction in recipe.instructions!!) {
           for (step in instruction.steps) {
               totalSteps.add(step.step)
           }
        }

        // Parse the recipe ingredients
        var totalIngredients: MutableList<String> = mutableListOf()
        for (ingredient in recipe.usedIngredients!!){
            totalIngredients.add(ingredient.name)
        }
        for (ingredient in recipe.unusedIngredients!!){
            totalIngredients.add(ingredient.name)
        }
        for (ingredient in recipe.missedIngredients!!){
            totalIngredients.add(ingredient.name)
        }

        holder.itemView.setOnClickListener {
            // Use the fragment manager to replace the container with the RecipeDetailsFragment
            val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
            val recipeDetailsFragment = RecipeDetailsFragment.newInstance(
                totalSteps, totalIngredients, recipe.recipeName,
                dishTypeList, cuisineTypeList, dietTypeList, recipe.totalTime, recipe.servingSize, recipe.imageId)
            fragmentManager.beginTransaction().replace(R.id.fragment_container, recipeDetailsFragment)
                .addToBackStack(null)
                .commit()
        }


    }



    override fun getItemCount() = recipeList.size

    class RecipeViewHolder(val binding: RecipeItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}