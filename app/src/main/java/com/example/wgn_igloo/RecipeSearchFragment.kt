package com.example.wgn_igloo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipeSearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeQueryAdapter: RecipeQueryAdapter

    private val recipeSearchList = listOf(
        SearchRecipe(R.drawable.lobster, "Lobster Thermidor", "30 mins", "45 mins", "1 hr 15 mins", "2 servings"),
        SearchRecipe(R.drawable.salmon, "Garlic Butter Salmon", "20 mins", "30 mins", "50 mins", "4 servings"),
        SearchRecipe(R.drawable.salad, "Caesar Salad", "15 mins", "0 mins", "15 mins", "3 servings")
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recipe_search, container, false)

        recyclerView = view.findViewById(R.id.recipes_query_recycler_view)
        recipeQueryAdapter = RecipeQueryAdapter(recipeSearchList)
        recyclerView.adapter = recipeQueryAdapter
        return view
    }


}

class RecipeQueryAdapter(private val recipeList: List<SearchRecipe>) :
    RecyclerView.Adapter<RecipeQueryAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item_layout, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.itemView.findViewById<ImageView>(R.id.recipe_image).setImageResource(recipe.imageId)
        holder.itemView.findViewById<TextView>(R.id.recipe_title).text = recipe.recipeName
        holder.itemView.findViewById<TextView>(R.id.prep_time_info).text = recipe.preparationTime
        holder.itemView.findViewById<TextView>(R.id.cook_time_info).text = recipe.cookTime
        holder.itemView.findViewById<TextView>(R.id.total_time_info).text = recipe.totalTime
        holder.itemView.findViewById<TextView>(R.id.serving_size_info).text = recipe.servingSize
    }

    override fun getItemCount() = recipeList.size

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view)
}

