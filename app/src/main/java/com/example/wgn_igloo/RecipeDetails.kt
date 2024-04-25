package com.example.wgn_igloo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class RecipeDetails : Fragment() {

    private lateinit var ingredientsRecyclerView: RecyclerView
    private lateinit var instructionsRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_details, container, false)

        // Initialize RecyclerViews
        ingredientsRecyclerView = view.findViewById(R.id.ingredients_list)
        instructionsRecyclerView = view.findViewById(R.id.instructions_list)

        // Setup the adapters and layout managers
        setupRecyclerViews()

        return view
    }
    private fun setupRecyclerViews() {
        val ingredients = listOf("Flour", "Sugar", "Butter", "Eggs")  // Example ingredients
        val instructions = listOf(
            "Mix all dry ingredients.",
            "Add eggs and butter.",
            "Bake at 350 degrees for 20 minutes."
        )  // Example instructions

        // Setting up the Ingredients RecyclerView
        ingredientsRecyclerView.adapter = IngredientsAdapter(ingredients)
        ingredientsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Setting up the Instructions RecyclerView
        instructionsRecyclerView.adapter = InstructionsAdapter(instructions)
        instructionsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

}