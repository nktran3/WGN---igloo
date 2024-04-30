package com.example.wgn_igloo.recipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wgn_igloo.databinding.FragmentRecipeSearchBinding
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


private const val API_KEY = "c201d7750771474eac1437c392be2b1f" // We have to change the API key when we're completely done
private const val TAG = "RecipeSearchPage"
class RecipeSearchFragment : Fragment() {

    private var _binding: FragmentRecipeSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipeQueryAdapter: RecipeQueryAdapter
    var query: String? = null // Used to hold the user's query
    private lateinit var toolbarRecipeSearch: Toolbar


    // Static function used to create a new instance of fragment with bundled argument
    companion object {
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        fun newInstance(message: String): RecipeSearchFragment {
            val fragment = RecipeSearchFragment()
            val args = Bundle()
            args.putString(EXTRA_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recipeQueryAdapter = RecipeQueryAdapter(mutableListOf())
        query = arguments?.getString(EXTRA_MESSAGE) // Extract argument and set it to query
        Log.d(TAG, "onCreate: Received message = $query")
        query?.let { recipeSearch(it) } // Make a GET call using the query
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeSearchBinding.inflate(inflater, container, false)
        binding.recipeQuery.text = "Recipes Containing: $query"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recipesQueryRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.recipesQueryRecyclerView.adapter = recipeQueryAdapter

        toolbarRecipeSearch = binding.toolbarRecipeSearch
        updateToolbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateToolbar() {
        toolbarRecipeSearch.navigationIcon = ContextCompat.getDrawable(requireContext(), com.example.wgn_igloo.R.drawable.back_icon)
        toolbarRecipeSearch.setNavigationOnClickListener { activity?.onBackPressed() }

    }
    // Function used to make API call to Spoonacular
    private fun recipeSearch(query: String) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val spoonacularAPI: SpoonacularAPI = retrofit.create(SpoonacularAPI::class.java)
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Query: $query")
                val response = spoonacularAPI.searchRecipes(query, true,true, true,true, 10, 0, API_KEY)
                Log.d(TAG, "Response: ${response.results}")
                // Response is a list of recipes, so we need to map each recipe accordingly and pass to adapter
                val newRecipes = response.results.map { recipe ->
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
                    recipeQueryAdapter.updateData(newRecipes)
                }

            } catch (ex: Exception) {
                Log.e(TAG, "Failed to fetch recipes: $ex")
            }
        }
    }
}