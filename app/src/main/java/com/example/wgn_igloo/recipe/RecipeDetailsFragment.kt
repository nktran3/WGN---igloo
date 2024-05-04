package com.example.wgn_igloo.recipe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.databinding.FragmentRecipeDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.ArrayList

private const val TAG = "RecipeDetailsFragment"
class RecipeDetailsFragment : Fragment() {
    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!
    private var parsed_instructions: MutableList<String> = mutableListOf()
    private var parsed_ingredients: MutableList<String> = mutableListOf()
    private var parsed_title: String = ""
    private var parsed_dish: String = ""
    private var parsed_cuisine: String = ""
    private var parsed_diet: String = ""
    private var parsed_time: String = ""
    private var parsed_servings: String = ""
    private var parsed_image: String = ""
    private var parsed_saved: Boolean = false

    private lateinit var toolbarRecipeDetails: Toolbar
    private lateinit var firestoreHelper: FirestoreHelper
    private var isRecipeSaved = false

    companion object {
        private const val INSTRUCTIONS = "INSTRUCTIONS"
        private const val INGREDIENTS = "INGREDIENTS"
        private const val TITLE = "TITLE"
        private const val DISH = "DISH"
        private const val CUISINE = "CUISINE"
        private const val DIET = "DIET"
        private const val TIME = "TIME"
        private const val SERVINGS = "SERVINGS"
        private const val IMAGE = "IMAGE"
        private const val SAVED = false

        fun newInstance(
            instructionsMessage: List<String>?, ingredientsMessage: List<String>?,
            titleMessage: String?, dishMessage: String?, cuisineMessage: String?,
            dietMessage: String?, timeMessage: String?, servingsMessage: String?,
            imageMessage: String?, savedMessage: Boolean): RecipeDetailsFragment {
            val fragment = RecipeDetailsFragment()
            val args = Bundle()


            args.putStringArrayList(INSTRUCTIONS, instructionsMessage as ArrayList<String>?)
            args.putStringArrayList(INGREDIENTS, ingredientsMessage as ArrayList<String>?)
            args.putString(TITLE, titleMessage)
            args.putString(DISH, dishMessage)
            args.putString(CUISINE, cuisineMessage)
            args.putString(DIET, dietMessage)
            args.putString(TIME, timeMessage)
            args.putString(SERVINGS, servingsMessage)
            args.putString(IMAGE, imageMessage)
            args.putBoolean(SAVED.toString(), savedMessage)

            // Set the Bundle as the fragment's arguments
            fragment.arguments = args

            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)

        val instructions = arguments?.getStringArrayList(INSTRUCTIONS)!!
        parsed_ingredients = arguments?.getStringArrayList(INGREDIENTS)!!
        parsed_title = arguments?.getString(TITLE)!!
        parsed_dish = arguments?.getString(DISH)!!
        parsed_cuisine = arguments?.getString(CUISINE)!!
        parsed_diet = arguments?.getString(DIET)!!
        parsed_time = arguments?.getString(TIME)!!
        parsed_servings = arguments?.getString(SERVINGS)!!
        parsed_image = arguments?.getString(IMAGE)!!
        parsed_saved = arguments?.getBoolean(SAVED.toString()) == true
        isRecipeSaved = parsed_saved




        Log.d(TAG, "$parsed_ingredients")
        for (lines in instructions){
            val steps = lines.split(".").filter{it.isNotEmpty()}
            for (step in steps){
                parsed_instructions.add("${step.trim()}.")
                Log.d(TAG, step)
            }
        }
        if (parsed_saved){

        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup the adapters and layout managers
        setupRecyclerViews(
            parsed_instructions, parsed_ingredients,
            parsed_title, parsed_dish, parsed_cuisine,
            parsed_diet, parsed_time, parsed_servings, parsed_image)

        toolbarRecipeDetails = binding.toolbarRecipeDetails
        (activity as AppCompatActivity).setSupportActionBar(toolbarRecipeDetails)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false) // Disable the title
        setHasOptionsMenu(true)
        updateToolbar()
        firestoreHelper = FirestoreHelper(requireContext())

        // Set the correct save icon based on the saved state
        toolbarRecipeDetails.post {
            activity?.invalidateOptionsMenu()  // Request to recreate the menu so it can reflect the updated icon
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                isRecipeSaved = !isRecipeSaved
                updateSaveIcon(item)
                val recipe = SavedRecipe(
                    imageId = parsed_image,
                    recipeName = parsed_title,
                    cuisineType = parsed_cuisine,
                    dietType = parsed_diet,
                    dishType = parsed_dish,
                    servingSize = parsed_servings,
                    totalTime = parsed_time,
                    ingredients = parsed_ingredients,
                    instructions = parsed_instructions,
                )
                if (isRecipeSaved) {
                    saveRecipe(recipe)
                } else {
                    unsaveRecipe(recipe)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_save)?.let {
            updateSaveIcon(it)
        }
    }

    private fun updateSaveIcon(item: MenuItem) {
        if (isRecipeSaved) {
            item.icon = ContextCompat.getDrawable(requireContext(), R.drawable.saved_clicked)
        } else {
            item.icon = ContextCompat.getDrawable(requireContext(), R.drawable.saved_icon)
        }
    }



    private fun saveRecipe(recipe: SavedRecipe) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            firestoreHelper.addSavedRecipe(uid,recipe )
        }
        Log.d(TAG, "Recipe saved")
    }

    private fun unsaveRecipe(recipe: SavedRecipe) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            firestoreHelper.removeSavedRecipe(uid, recipe)
        }
        Log.d(TAG, "Recipe unsaved")
    }


    private fun updateToolbar() {
        toolbarRecipeDetails.title = ""
        toolbarRecipeDetails.navigationIcon = ContextCompat.getDrawable(requireContext(), com.example.wgn_igloo.R.drawable.back_icon)
        toolbarRecipeDetails.setNavigationOnClickListener { activity?.onBackPressed() }

    }

    private fun setupRecyclerViews(
        instructions: List<String>, ingredients: List<String>, title: String,
        dish: String, cuisine: String, diet: String, time: String, servings: String,
        imageURL: String
    ) {
        binding.recipePageName.text = title
        binding.recipeDishType.text = dish.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        if (cuisine == "") {
            binding.recipeCuisine.text = "N/A"
        }
        else {
            binding.recipeCuisine.text = cuisine
        }

        if (diet == "") {
            binding.recipeDiet.text = "N/A"
        }
        else {
            binding.recipeDiet.text = diet
        }

        binding.recipeTotalTimeInfo.text = time + " mins"
        binding.recipeServingSize.text = servings
        Log.d(TAG,"Image: $imageURL")
        Glide.with(this)
            .load(imageURL)
            .placeholder(R.drawable.salmon)  // Shows a placeholder image while loading.
            .error(R.drawable.salad)        // Shows an error image if the URL load fails.
            .into(binding.recipePageImage)

        // Setting up the Ingredients RecyclerView
        binding.ingredientsList.adapter = IngredientsAdapter(ingredients)
        binding.ingredientsList.layoutManager = LinearLayoutManager(context)

        // Setting up the Instructions RecyclerView
        binding.instructionsList.adapter = InstructionsAdapter(instructions)
        binding.instructionsList.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Clean up binding reference
    }
}