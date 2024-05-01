package com.example.wgn_igloo.recipe

import com.example.wgn_igloo.recipe.RecipeSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularAPI {
    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("instructionsRequired") instructions: Boolean, // Set to true to only return recipe's with instructions
        @Query("addRecipeInformation") addRecipeInformation: Boolean, // Set to true to enquire for more recipe details
        @Query("addRecipeInstructions") addRecipeInstructions: Boolean, // Set to true to include instructions in response
        @Query("fillIngredients") fillIngredients: Boolean, // Set to true return more information about ingredients
        @Query("sort") sort: String, // Added to sort by max-used-ingredients
        @Query("number") number: Int,
        @Query("offset") offset: Int,
        @Query("apiKey") apiKey: String
    ): RecipeSearchResponse //

    @GET("recipes/complexSearch")
    suspend fun searchIngredients(
        @Query("query") query: String,
        @Query("includeIngredients") ingredients: String,
        @Query("instructionsRequired") instructions: Boolean, // Set to true to only return recipe's with instructions
        @Query("addRecipeInformation") addRecipeInformation: Boolean, // Set to true to enquire for more recipe details
        @Query("addRecipeInstructions") addRecipeInstructions: Boolean, // Set to true to include instructions in response
        @Query("fillIngredients") fillIngredients: Boolean, // Set to true return more information about ingredients
        @Query("sort") sort: String, // Added to sort by max-used-ingredients
        @Query("number") number: Int,
        @Query("offset") offset: Int,
        @Query("apiKey") apiKey: String
    ): RecipeSearchResponse //
}