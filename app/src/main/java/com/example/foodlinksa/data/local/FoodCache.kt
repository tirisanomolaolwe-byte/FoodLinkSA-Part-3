package com.example.foodlinksa.data.local

import android.content.Context
import com.example.foodlinksa.data.model.Meal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FoodCache(context: Context) {
    private val prefs = context.getSharedPreferences("food_link_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveMeals(category: String, meals: List<Meal>) {
        val json = gson.toJson(meals)
        prefs.edit().putString("category_$category", json).apply()
    }

    fun getMeals(category: String): List<Meal>? {
        val json = prefs.getString("category_$category", null) ?: return null
        val type = object : TypeToken<List<Meal>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (_: Exception) {
            null
        }
    }
}
