package com.example.foodlinksa.data.repository

import com.example.foodlinksa.data.api.ApiService
import com.example.foodlinksa.data.local.FoodCache
import com.example.foodlinksa.data.model.Meal
import com.example.foodlinksa.data.model.MealDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoodRepository(private val apiService: ApiService, private val foodCache: FoodCache?) {
    
    suspend fun getMeals(category: String): List<Meal>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMealsByCategory(category)
                if (response.isSuccessful && response.body()?.meals != null) {
                    val meals = response.body()!!.meals
                    foodCache?.saveMeals(category, meals)
                    meals
                } else {
                    foodCache?.getMeals(category)
                }
            } catch (_: Exception) {
                foodCache?.getMeals(category)
            }
        }
    }

    suspend fun getMealDetails(id: String): MealDetail? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMealById(id)
                if (response.isSuccessful) response.body()?.meals?.firstOrNull() else null
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun searchMeals(query: String): List<Meal>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchMeals(query)
                if (response.isSuccessful && response.body()?.meals != null) {
                    response.body()!!.meals
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
