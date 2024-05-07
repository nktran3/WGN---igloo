package com.example.wgn_igloo.grocery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ItemViewModel: ViewModel() {
    private val _refreshItems = MutableLiveData<Boolean>()

    val refreshItems: LiveData<Boolean> get() = _refreshItems

    fun setRefreshItems(refresh: Boolean) {
        _refreshItems.value = refresh
    }
}