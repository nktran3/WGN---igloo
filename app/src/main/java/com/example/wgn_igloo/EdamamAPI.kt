package com.example.wgn_igloo

import retrofit2.http.GET
import retrofit2.http.Query

interface EdamamAPI {
    @GET("search")
    suspend fun searchRecipes(
        @Query("q") query: String,
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String
    ): RecipeSearchResponse // Define your RecipeSearchResponse data class based on the expected JSON response
}