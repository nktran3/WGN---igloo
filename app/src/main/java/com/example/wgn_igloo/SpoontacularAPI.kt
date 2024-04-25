package com.example.wgn_igloo

import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularAPI {
    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("instructionsRequired") instructions: Boolean,
        @Query("addRecipeInformation") addRecipeInformation: Boolean,
        @Query("addRecipeInstructions") addRecipeInstructions: Boolean,
        @Query("number") number: Int,
        @Query("offset") offset: Int,
        @Query("apiKey") apiKey: String
    ): RecipeSearchResponse //
}