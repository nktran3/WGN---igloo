package com.example.wgn_igloo.recipe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.fragment.app.Fragment

class RecipeViewModel : ViewModel() {
    val currentFragment = MutableLiveData<Fragment>()
}