package com.example.wgn_igloo.recipe

data class RecipeSearch (
    var imageId: String?,
    var recipeName: String,
    var totalTime: String,
    var cuisineType: List<String>?,
    var dietType: List<String>?,
    var servingSize: String
)