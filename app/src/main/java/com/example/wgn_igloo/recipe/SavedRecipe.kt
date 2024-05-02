package com.example.wgn_igloo.recipe
data class SavedRecipe (
    var imageId: String = "",
    var recipeName: String = "",
    var dishType: String = "",
    var cuisineType: String = "",
    var dietType: String = "",
    var totalTime: String = "",
    var servingSize: String = "",
    var instructions: List<String> = listOf(),
    var ingredients: List<String>? = listOf()

)
