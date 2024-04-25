package com.example.wgn_igloo.home

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FoodDetailResponse(
    @Json(name = "fdcId") val fdcId: Int,
    @Json(name = "description") val description: String,
    @Json(name = "dataType") val dataType: String,
    @Json(name = "brandOwner") val brandOwner: String?,
    @Json(name = "ingredients") val ingredients: String?,
    @Json(name = "foodNutrients") val foodNutrients: List<NutrientDetail>?
)

@JsonClass(generateAdapter = true)
data class NutrientDetail(
    @Json(name = "nutrientId") val nutrientId: Int?,
    @Json(name = "nutrientName") val nutrientName: String?,
    @Json(name = "nutrientNumber") val nutrientNumber: String?,
    @Json(name = "unitName") val unitName: String?,
    @Json(name = "derivationCode") val derivationCode: String?,
    @Json(name = "derivationDescription") val derivationDescription: String?,
    @Json(name = "value") val value: Double?
)