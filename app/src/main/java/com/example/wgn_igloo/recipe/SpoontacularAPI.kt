package com.example.wgn_igloo.recipe

import com.example.wgn_igloo.recipe.RecipeSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularAPI {
    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("includeIngredients") includeIngredients: String,
        @Query("instructionsRequired") instructions: Boolean, // Set to true to only return recipe's with instructions
        @Query("addRecipeInformation") addRecipeInformation: Boolean, // Set to true to enquire for more recipe details
        @Query("addRecipeInstructions") addRecipeInstructions: Boolean, // Set to true to include instructions in response
        @Query("fillIngredients") fillIngredients: Boolean, // Set to true return more information about ingredients
        @Query("number") number: Int,
        @Query("offset") offset: Int,
        @Query("apiKey") apiKey: String
    ): RecipeSearchResponse //
}