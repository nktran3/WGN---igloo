package com.example.wgn_igloo.recipe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wgn_igloo.databinding.FragmentRecipeDetailsBinding
import java.util.ArrayList

private const val TAG = "RecipeDetailsFragment"
class RecipeDetailsFragment : Fragment() {
    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!
    private var parsed_instructions: MutableList<String> = mutableListOf()
    private var parsed_ingredients: MutableList<String> = mutableListOf()

    companion object {
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        private const val EXTRA_MESSAGE_2 = "EXTRA_MESSAGE_2"
        private const val EXTRA_MESSAGE_3 = "EXTRA_MESSAGE_3"

        fun newInstance(message: List<String>?, message2: List<String>?): RecipeDetailsFragment {
            val fragment = RecipeDetailsFragment()
            val args = Bundle()

            args.putStringArrayList(EXTRA_MESSAGE, message as ArrayList<String>?)
            args.putStringArrayList(EXTRA_MESSAGE_2, message2 as ArrayList<String>?)

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

        val instructions = arguments?.getStringArrayList(EXTRA_MESSAGE)!!
        parsed_ingredients = arguments?.getStringArrayList(EXTRA_MESSAGE_2)!!

        Log.d(TAG, "$parsed_ingredients")
        for (lines in instructions){
            val steps = lines.split(".").filter{it.isNotEmpty()}
            for (step in steps){
                parsed_instructions.add("${step.trim()}.")
                Log.d(TAG, step)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup the adapters and layout managers
        setupRecyclerViews(parsed_instructions, parsed_ingredients)
    }

    private fun setupRecyclerViews(instructions: List<String>, ingredients: List<String>) {

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