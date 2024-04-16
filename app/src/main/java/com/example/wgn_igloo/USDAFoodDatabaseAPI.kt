package com.example.wgn_igloo

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface USDAFoodDatabaseAPI {
    @GET("/fdc/v1/foods/search")
    suspend fun fetchFoodInfoByUPC(
        @Query("api_key") apiKey: String,
        @Query("query") upc: String,
        @Query("dataType") dataType: String,

        ): FoodResponse

    @GET("/fdc/v1/food/{fdcId}")
    suspend fun fetchFoodInfoByFDCID(
        @Path("fdcId") fdcId: Int,
        @Query("api_key") apiKey: String
    ): FoodDetailResponse
}
