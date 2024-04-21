package com.example.wgn_igloo

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecipeSearchResponse(
    @Json(name = "q") val query: String,
    @Json(name = "from") val from: Int,
    @Json(name = "to") val to: Int,
    @Json(name = "more") val more: Boolean,
    @Json(name = "count") val count: Int,
    @Json(name = "hits") val hits: List<RecipeHit>
)

@JsonClass(generateAdapter = true)
data class RecipeHit(
    @Json(name = "recipe") val recipe: RecipeDetail
)

@JsonClass(generateAdapter = true)
data class RecipeDetail(
    @Json(name = "uri") val uri: String,
    @Json(name = "label") val label: String,
    @Json(name = "image") val image: String?,
    @Json(name = "source") val source: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "yield") val yield: Int?,
    @Json(name = "dietLabels") val dietLabels: List<String>?,
    @Json(name = "healthLabels") val healthLabels: List<String>?,
    @Json(name = "ingredientLines") val ingredientLines: List<String>?,
    @Json(name = "calories") val calories: Double?,
    @Json(name = "totalWeight") val totalWeight: Double?,
    @Json(name = "totalTime") val totalTime: Double?
)