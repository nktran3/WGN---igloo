package com.example.wgn_igloo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.recyclerview.widget.*
import android.widget.*


data class Recipe (
    val imageId: Int,
    val recipeName: String,
    val preparationTime: String,
    val cookTime: String,
    val totalTime: String,
    val servingSize: String
)


class RecipesPage : Fragment() {

    private val recipeData = listOf(
        Recipe(R.drawable.lobster, "Lobster Thermidor", "30 mins", "45 mins", "1 hr 15 mins", "2 servings"),
        Recipe(R.drawable.salmon, "Garlic Butter Salmon", "20 mins", "30 mins", "50 mins", "4 servings"),
        Recipe(R.drawable.salad, "Caesar Salad", "15 mins", "0 mins", "15 mins", "3 servings")
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipes_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val suggestedRecipeRecyclerView: RecyclerView = view.findViewById(R.id.suggested_recipes_recycler_view)
        suggestedRecipeRecyclerView.layoutManager = LinearLayoutManager(context)
        suggestedRecipeRecyclerView.adapter = RecipeAdapter(recipeData)

        val savedRecipeRecyclerView: RecyclerView = view.findViewById(R.id.saved_recipes_recycler_view)
        savedRecipeRecyclerView.layoutManager = LinearLayoutManager(context)
        savedRecipeRecyclerView.adapter = RecipeAdapter(recipeData)
    }
}

class RecipeAdapter(recipeData: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val recipeData = listOf(
        Recipe(R.drawable.lobster, "Lobster Thermidor", "30 mins", "45 mins", "1 hr 15 mins", "2 servings"),
        Recipe(R.drawable.salmon, "Garlic Butter Salmon", "20 mins", "30 mins", "50 mins", "4 servings"),
        Recipe(R.drawable.salad, "Caesar Salad", "15 mins", "0 mins", "15 mins", "3 servings")
    )
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item_layout, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeData[position]
        holder.bind(recipe)
    }

    override fun getItemCount(): Int = recipeData.size

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeImage: ImageView = itemView.findViewById(R.id.recipe_image)
        private val recipeTitle: TextView = itemView.findViewById(R.id.recipe_title)
        private val prepTimeInfo: TextView = itemView.findViewById(R.id.prep_time_info)
        private val cookTimeInfo: TextView = itemView.findViewById(R.id.cook_time_info)
        private val totalTimeInfo: TextView = itemView.findViewById(R.id.total_time_info)
        private val servingSizeInfo: TextView = itemView.findViewById(R.id.serving_size_info)

        fun bind(recipe: Recipe) {
            recipeImage.setImageResource(recipe.imageId)
            recipeTitle.text = recipe.recipeName
            prepTimeInfo.text = recipe.preparationTime
            cookTimeInfo.text = recipe.cookTime
            totalTimeInfo.text = recipe.totalTime
            servingSizeInfo.text = recipe.servingSize
        }
    }

}




