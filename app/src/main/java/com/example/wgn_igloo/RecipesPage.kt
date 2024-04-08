package com.example.wgn_igloo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import androidx.recyclerview.widget.*
import android.widget.*
import com.google.firebase.auth.FirebaseAuth


class RecipesPage : Fragment() {
//    private val adapter: RecipeAdapter
    private lateinit var firestoreHelper: FirestoreHelper
    private var userUid: String? = null

    companion object {
        private const val TAG = "FirestoreHelper"
    }

    private val recipeData = listOf(
        SavedRecipe(R.drawable.lobster, "Lobster Thermidor", "30 mins", "45 mins", "1 hr 15 mins", "2 servings"),
        SavedRecipe(R.drawable.salmon, "Garlic Butter Salmon", "20 mins", "30 mins", "50 mins", "4 servings"),
        SavedRecipe(R.drawable.salad, "Caesar Salad", "15 mins", "0 mins", "15 mins", "3 servings")
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestoreHelper = FirestoreHelper(requireContext())
        userUid = FirebaseAuth.getInstance().currentUser?.uid
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recipes_page, container, false)
    }

    private fun addDummyRecipeListItem() {
        userUid?.let { uid ->
            // Create a dummy SavedRecipe
            val dummyRecipe = SavedRecipe(
                imageId = R.drawable.lobster, // Assuming you have a drawable resource
                recipeName = "Dummy Recipe",
                preparationTime = "10 mins",
                cookTime = "20 mins",
                totalTime = "30 mins",
                servingSize = "2 servings"
            )

            // Add the dummy recipe to Firestore using FirestoreHelper
            firestoreHelper.addSavedRecipe(uid, dummyRecipe)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FirestoreHelper and get the user's UID
        firestoreHelper = FirestoreHelper(requireContext())
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Initialize your adapter with the necessary parameters
        val suggestedRecipesAdapter = RecipeAdapter(recipeData, firestoreHelper, userId)
        val savedRecipesAdapter = RecipeAdapter(recipeData, firestoreHelper, userId) // Assuming you want to use the same data for both for now

        // Setup the RecyclerView for suggested recipes
        val suggestedRecipeRecyclerView: RecyclerView = view.findViewById(R.id.suggested_recipes_recycler_view)
        suggestedRecipeRecyclerView.layoutManager = LinearLayoutManager(context)
        suggestedRecipeRecyclerView.adapter = suggestedRecipesAdapter // Use the adapter directly

        // Setup the RecyclerView for saved recipes
        val savedRecipeRecyclerView: RecyclerView = view.findViewById(R.id.saved_recipes_recycler_view)
        savedRecipeRecyclerView.layoutManager = LinearLayoutManager(context)
        savedRecipeRecyclerView.adapter = savedRecipesAdapter // Use another instance or the same adapter as needed

        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            addDummyRecipeListItem()
        } else {
            Log.d(InventoryDisplayFragment.TAG, "User is not logged in")
        }
    }
}

//class RecipeAdapter(recipeData: List<SavedRecipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {
class RecipeAdapter(private val recipeData: List<SavedRecipe>, private val firestoreHelper: FirestoreHelper, private val userId: String) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

//    val recipeData = listOf(
//        SavedRecipe(R.drawable.lobster, "Lobster Thermidor", "30 mins", "45 mins", "1 hr 15 mins", "2 servings"),
//        SavedRecipe(R.drawable.salmon, "Garlic Butter Salmon", "20 mins", "30 mins", "50 mins", "4 servings"),
//        SavedRecipe(R.drawable.salad, "Caesar Salad", "15 mins", "0 mins", "15 mins", "3 servings")
//    )
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item_layout, parent, false)
//        return RecipeViewHolder(view)
        return RecipeViewHolder(view, firestoreHelper, userId)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeData[position]
        holder.bind(recipe)
    }

    override fun getItemCount(): Int = recipeData.size

//    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    class RecipeViewHolder(itemView: View, private val firestoreHelper: FirestoreHelper, private val userId: String) : RecyclerView.ViewHolder(itemView) {
        private val recipeImage: ImageView = itemView.findViewById(R.id.recipe_image)
        private val recipeTitle: TextView = itemView.findViewById(R.id.recipe_title)
        private val prepTimeInfo: TextView = itemView.findViewById(R.id.prep_time_info)
        private val cookTimeInfo: TextView = itemView.findViewById(R.id.cook_time_info)
        private val totalTimeInfo: TextView = itemView.findViewById(R.id.total_time_info)
        private val servingSizeInfo: TextView = itemView.findViewById(R.id.serving_size_info)

        fun bind(recipe: SavedRecipe) {
            recipeImage.setImageResource(recipe.imageId)
            recipeTitle.text = recipe.recipeName
            prepTimeInfo.text = recipe.preparationTime
            cookTimeInfo.text = recipe.cookTime
            totalTimeInfo.text = recipe.totalTime
            servingSizeInfo.text = recipe.servingSize
        }
    }

}