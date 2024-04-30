package com.example.wgn_igloo.recipe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import androidx.recyclerview.widget.*
import android.widget.*
import com.example.wgn_igloo.databinding.FragmentRecipesPageBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.home.InventoryDisplayFragment
import com.example.wgn_igloo.R
import com.google.firebase.auth.FirebaseAuth
import com.example.wgn_igloo.recipe.RecipeViewModel


private const val TAG = "RecipePage"

class RecipesPage : Fragment() {
    private lateinit var viewModel: RecipeViewModel // Used to hold the reference to a recipeSearch fragment
    private lateinit var firestoreHelper: FirestoreHelper
    private var userUid: String? = null
    private lateinit var binding: FragmentRecipesPageBinding
    private var query = ""

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
        viewModel = ViewModelProvider(requireActivity()).get(RecipeViewModel::class.java)
        firestoreHelper = FirestoreHelper(requireContext())
        userUid = FirebaseAuth.getInstance().currentUser?.uid
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipesPageBinding.inflate(inflater, container, false)

        binding.recipeSearchButton.setOnClickListener {
            query = binding.recipesSearchView.query.toString()
            val recipeSearchFragment = RecipeSearchFragment.newInstance(query)
            viewModel.currentFragment.value = recipeSearchFragment
            requireActivity().supportFragmentManager.beginTransaction()
                .hide(this@RecipesPage) // Hide the current instance of RecipesPage
                .add(R.id.fragment_container, recipeSearchFragment, "recipeSearchFragment")
                .addToBackStack(null)
                .show(recipeSearchFragment)
                .commit()
        }
        return binding.root
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

class RecipeAdapter(private val recipeData: List<SavedRecipe>, private val firestoreHelper: FirestoreHelper, private val userId: String) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item_layout, parent, false)
        return RecipeViewHolder(view, firestoreHelper, userId)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeData[position]
        holder.bind(recipe)
        holder.itemView.setOnClickListener {
            // Use the fragment manager to replace the container with the RecipeDetailsFragment
            val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
            val recipeDetailsFragment = RecipeDetailsFragment()
            fragmentManager.beginTransaction().replace(R.id.fragment_container, recipeDetailsFragment)
            .addToBackStack(null)
            .commit()
        }
    }

    override fun getItemCount(): Int = recipeData.size

//    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    class RecipeViewHolder(itemView: View, private val firestoreHelper: FirestoreHelper, private val userId: String) : RecyclerView.ViewHolder(itemView) {
        private val recipeImage: ImageView = itemView.findViewById(R.id.recipe_image)
        private val recipeTitle: TextView = itemView.findViewById(R.id.recipe_title)
        private val totalTimeInfo: TextView = itemView.findViewById(R.id.total_time)
        private val servingSizeInfo: TextView = itemView.findViewById(R.id.serving_size)

        fun bind(recipe: SavedRecipe) {
            recipeImage.setImageResource(recipe.imageId)
            recipeTitle.text = recipe.recipeName
            totalTimeInfo.text = recipe.totalTime
            servingSizeInfo.text = recipe.servingSize
        }
    }

}