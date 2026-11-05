package com.example.foodlinksa.ui.food

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodlinksa.data.model.Meal
import com.example.foodlinksa.data.repository.FoodRepository
import kotlinx.coroutines.launch

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _meals = MutableLiveData<List<Meal>?>()
    val meals: LiveData<List<Meal>?> get() = _meals

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun fetchMeals(category: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.getMeals(category)
            _meals.value = result
            _loading.value = false
        }
    }

    fun searchMeals(query: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.searchMeals(query)
            _meals.value = result
            _loading.value = false
        }
    }
}
