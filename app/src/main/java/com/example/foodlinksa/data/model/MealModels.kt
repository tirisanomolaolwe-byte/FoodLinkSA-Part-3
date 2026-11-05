package com.example.foodlinksa.data.model

import com.google.gson.annotations.SerializedName

data class MealResponse(
    @SerializedName("meals")
    val meals: List<Meal>
)

data class Meal(
    @SerializedName("strMeal")
    val name: String,
    @SerializedName("strMealThumb")
    val imageUrl: String,
    @SerializedName("idMeal")
    val id: String
)
