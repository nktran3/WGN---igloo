package com.example.wgn_igloo.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CarouselViewModel : ViewModel() {
    private val selectedCategory = MutableLiveData<String>()

    // Method to update the selected category
    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    // Method to retrieve the selected category as LiveData
    fun getSelectedCategory(): LiveData<String> = selectedCategory
}