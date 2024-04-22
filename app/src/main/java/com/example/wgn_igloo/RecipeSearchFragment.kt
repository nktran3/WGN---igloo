import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wgn_igloo.EdamamAPI
import com.example.wgn_igloo.R
import com.example.wgn_igloo.RecipeQueryAdapter
import com.example.wgn_igloo.databinding.FragmentRecipeSearchBinding
import com.example.wgn_igloo.RecipeSearch
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val APP_ID = "7410b65a"
private const val API_KEY = "0699c8a3fc7e2db0b4607eb7dfacb5fc"
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
            .baseUrl("https://api.edamam.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val edamamRecipeApi: EdamamAPI = retrofit.create(EdamamAPI::class.java)
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Query: $query")
                val response = edamamRecipeApi.searchRecipes(query, APP_ID, API_KEY)
                val newRecipes = response.hits.map { hit ->
                    RecipeSearch(
                        imageId = hit.recipe.image, // Replace with actual image logic
                        recipeName = hit.recipe.label,
                        totalTime = hit.recipe.totalTime.toString(),
                        servingSize = hit.recipe.yield.toString() + " servings"
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