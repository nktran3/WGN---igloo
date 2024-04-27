package com.example.wgn_igloo.recipe

data class RecipeSearch (
    var imageId: String?,
    var recipeName: String,
    var totalTime: String,
    var dishType: List<String>?,
    var cuisineType: List<String>?,
    var dietType: List<String>?,
    var servingSize: String,
    var instructions: List<RecipeInstruction>?,
    var usedIngredients: List<Ingredient>?,
    var unusedIngredients: List<Ingredient>?,
    var missedIngredients: List<Ingredient>?
)