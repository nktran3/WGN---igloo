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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.home.InventoryDisplayFragment
import com.example.wgn_igloo.R
import com.example.wgn_igloo.databinding.RecipeItemLayoutBinding
import com.example.wgn_igloo.home.GroceryItem
import com.google.firebase.auth.FirebaseAuth
import com.example.wgn_igloo.recipe.RecipeViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


private const val TAG = "RecipePage"
private const val API_KEY = "c201d7750771474eac1437c392be2b1f"


class RecipesPage : Fragment() {
    private lateinit var viewModel: RecipeViewModel // Used to hold the reference to a recipeSearch fragment
    private lateinit var firestoreHelper: FirestoreHelper
    private var userUid: String? = null
    private lateinit var binding: FragmentRecipesPageBinding
    private var query = ""
    private var groceryItems: List<GroceryItem> = emptyList()
    private var searchedRecipes: List<RecipeSearch> = mutableListOf()
    private lateinit var suggestedRecipesAdapter: RecipeAdapter
    private lateinit var savedRecipesAdapter:RecipeAdapter



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
        suggestedRecipesAdapter = RecipeAdapter(mutableListOf())
        savedRecipesAdapter = RecipeAdapter(mutableListOf())

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
        fetchGroceryItems()
        return binding.root
    }

    fun fetchGroceryItems() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            firestoreHelper.getGroceryItems(userUid, this::updateGroceryItems, this::handleFetchError)
        } else {
            Log.e(TAG, "No user UID found")
        }
    }

    private fun updateGroceryItems(items: List<GroceryItem>) {
        var ingredients: MutableList<String> = mutableListOf()
        groceryItems = items  // Update the local list with the fetched items
        Log.d(TAG, "Fetched and updated grocery items: ${groceryItems.map { it.name }}")
        for (item in groceryItems){
            ingredients.add(item.name)
        }
        recipeSearchByIngredients(ingredients.joinToString(separator =  ","))
    }

    private fun handleFetchError(exception: Exception) {
        Log.e(TAG, "Error fetching grocery items", exception)
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

    // Function used to make API call to Spoonacular
    private fun recipeSearchByIngredients(ingredientsQuery: String) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val spoonacularAPI: SpoonacularAPI = retrofit.create(SpoonacularAPI::class.java)
        Log.d(TAG,"Ingredient Query: $ingredientsQuery")
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Ingredients: $ingredientsQuery")
                val response = spoonacularAPI.searchIngredients(ingredientsQuery, ingredientsQuery,true,true, true,true, "max-used-ingredients",3, 0, API_KEY)
                Log.d(TAG, "Response: ${response.results}")
                // Response is a list of recipes, so we need to map each recipe accordingly and pass to adapter
                searchedRecipes = response.results.map { recipe ->
                    Log.d(TAG, "$recipe")
                    Log.d(TAG, "Ingredients:${recipe.missedIngredients}")
                    RecipeSearch(
                        imageId = recipe.image,
                        recipeName = recipe.title,
                        dishType = recipe.dishTypes,
                        cuisineType = recipe.cuisines,
                        dietType = recipe.diets,
                        totalTime = recipe.readyInMinutes.toString(),
                        servingSize = recipe.servings.toString() + " servings",
                        instructions = recipe.analyzedInstructions,
                        usedIngredients = recipe.usedIngredients,
                        unusedIngredients = recipe.unusedIngredients,
                        missedIngredients = recipe.missedIngredients
                    )
                }
                activity?.runOnUiThread {
                    suggestedRecipesAdapter.updateData(searchedRecipes)
                    savedRecipesAdapter.updateData(searchedRecipes)
                }


            } catch (ex: Exception) {
                Log.e(TAG, "Failed to fetch recipes: $ex")
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FirestoreHelper and get the user's UID
        firestoreHelper = FirestoreHelper(requireContext())
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Initialize your adapter with the necessary parameters
        suggestedRecipesAdapter = RecipeAdapter(searchedRecipes)
        savedRecipesAdapter = RecipeAdapter(searchedRecipes) // Assuming you want to use the same data for both for now

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

class RecipeAdapter(private var recipeData: List<RecipeSearch>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    fun updateData(newRecipes: List<RecipeSearch?>) {
        if (!newRecipes.isNullOrEmpty()) {
            recipeData = newRecipes as List<RecipeSearch>
            notifyDataSetChanged()
            Log.d(TAG, "Data updated with ${recipeData.size} recipes")
        } else {
            Log.d(TAG, "No recipes to update")
        }
    }


    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeData[position]
        Log.d(TAG, "${recipe.recipeName}")
        var dishTypeList = recipe.dishType?.joinToString(separator =  ",")
        var cuisineTypeList = recipe.cuisineType?.joinToString(separator = ", ")
        var dietTypeList = recipe.dietType?.joinToString(separator = ", ")
        with(holder.binding) {
            com.bumptech.glide.Glide.with(holder.itemView.context).load(recipe.imageId).into(recipeImage)
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


    override fun getItemCount(): Int = recipeData.size

    class RecipeViewHolder(val binding: RecipeItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)



}