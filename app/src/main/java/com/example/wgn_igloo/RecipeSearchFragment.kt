import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wgn_igloo.RecipeQueryAdapter
import com.example.wgn_igloo.databinding.FragmentRecipeSearchBinding
import com.example.wgn_igloo.RecipeSearch
import com.example.wgn_igloo.SpoonacularAPI
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val API_KEY = "54c26ca72c5c46f9ac43b5bee9886fca"
private const val TAG = "RecipeSearchPage"
class RecipeSearchFragment : Fragment() {

    private var _binding: FragmentRecipeSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipeQueryAdapter: RecipeQueryAdapter
    var query: String? = null

    private val recipeSearchList = listOf(
        RecipeSearch("", "Lobster Thermidor", "1 hr 15 mins", "2 servings"),
        RecipeSearch("", "Garlic Butter Salmon",  "50 mins", "4 servings"),
        RecipeSearch("", "Caesar Salad",  "15 mins", "3 servings")
    )

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
        query = arguments?.getString(EXTRA_MESSAGE)
        Log.d(TAG, "onCreate: Received message = $query")
        binding.recipeQuery.setText("Recipes containing: $query")
        query?.let { recipeSearch(it) }

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recipeQueryAdapter = RecipeQueryAdapter(recipeSearchList)
        binding.recipesQueryRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.recipesQueryRecyclerView.adapter = recipeQueryAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


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
                val response = spoonacularAPI.searchRecipes(query, true,true, true,10, 0, API_KEY)
                val newRecipes = response.results.map { recipe ->
                    RecipeSearch(
                        imageId = recipe.image, // Replace with actual image logic
                        recipeName = recipe.title,
                        totalTime = recipe.readyInMinutes.toString(),
                        servingSize = recipe.servings.toString() + " servings"
                    )
                }
                activity?.runOnUiThread {
                    recipeQueryAdapter.updateData(newRecipes)
                }
                Log.d(TAG, "Response: $response")

            } catch (ex: Exception) {
                Log.e(TAG, "Failed to fetch recipes: $ex")
            }
        }
    }
}