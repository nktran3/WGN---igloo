package com.example.wgn_igloo

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FoodResponse(
    @Json(name = "foods") val foods: List<FoodItem>
)

@JsonClass(generateAdapter = true)
data class FoodItem(
    @Json(name = "fdcId") val fdcId: Int,
    @Json(name = "description") val description: String,
    @Json(name = "brandOwner") val brandOwner: String?,
    @Json(name = "ingredients") val ingredients: String?,
    @Json(name = "foodNutrients") val foodNutrients: List<FoodNutrient>
)

@JsonClass(generateAdapter = true)
data class FoodNutrient(
    @Json(name = "nutrientId") val nutrientId: Int,
    @Json(name = "nutrientName") val nutrientName: String,
    @Json(name = "nutrientNumber") val nutrientNumber: String?,
    @Json(name = "unitName") val unitName: String,
    @Json(name = "value") val value: Double
)