package com.example.wgn_igloo

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecipeSearchResponse(
    @Json(name = "offset") val offset: Int,
    @Json(name = "number") val number: Int,
    @Json(name = "totalResults") val totalResults: Int,
    @Json(name = "results") val results: List<RecipeResult>
)

@JsonClass(generateAdapter = true)
data class RecipeResult(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "image") val image: String,
    @Json(name = "imageType") val imageType: String,
    @Json(name = "servings") val servings: Int?,
    @Json(name = "readyInMinutes") val readyInMinutes: Int?,
    @Json(name = "license") val license: String?,
    @Json(name = "sourceName") val sourceName: String?,
    @Json(name = "sourceUrl") val sourceUrl: String?,
    @Json(name = "spoonacularScore") val spoonacularScore: Double?,
    @Json(name = "healthScore") val healthScore: Double?,
    @Json(name = "pricePerServing") val pricePerServing: Double?,
    @Json(name = "analyzedInstructions") val analyzedInstructions: List<RecipeInstruction>?,
    @Json(name = "ingredients") val ingredients: List<Ingredient>?,
    @Json(name = "diets") val diets: List<String>?,
    @Json(name = "dishTypes") val dishTypes: List<String>?,
    @Json(name = "cuisines") val cuisines: List<String>?,
    @Json(name = "nutrition") val nutrition: Nutrition?
)

@JsonClass(generateAdapter = true)
data class RecipeInstruction(
    @Json(name = "name") val name: String?,
    @Json(name = "steps") val steps: List<InstructionStep>
)

@JsonClass(generateAdapter = true)
data class InstructionStep(
    @Json(name = "number") val number: Int,
    @Json(name = "step") val step: String,
    @Json(name = "ingredients") val ingredients: List<Ingredient>,
    @Json(name = "equipment") val equipment: List<Equipment>,
    @Json(name = "length") val length: CookingLength?
)

@JsonClass(generateAdapter = true)
data class Ingredient(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String,
    @Json(name = "localizedName") val localizedName: String?,
    @Json(name = "image") val image: String?
)

@JsonClass(generateAdapter = true)
data class Equipment(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String,
    @Json(name = "image") val image: String?
)

@JsonClass(generateAdapter = true)
data class CookingLength(
    @Json(name = "number") val number: Int,
    @Json(name = "unit") val unit: String
)

@JsonClass(generateAdapter = true)
data class Nutrition(
    @Json(name = "nutrients") val nutrients: List<Nutrient>
)

@JsonClass(generateAdapter = true)
data class Nutrient(
    @Json(name = "name") val name: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "unit") val unit: String
)