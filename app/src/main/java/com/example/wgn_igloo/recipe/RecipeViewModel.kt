package com.example.wgn_igloo.recipe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.fragment.app.Fragment

// ViewModel used to hold the state of RecipeSearchFragment and RecipeDetailFragment
class RecipeViewModel : ViewModel() {
    val currentRecipeSearchFragment = MutableLiveData<Fragment>()
    val currentRecipeDetailsFragment = MutableLiveData<Fragment>()
}