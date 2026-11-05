package com.example.foodlinksa.data.model

import com.google.gson.annotations.SerializedName

data class MealDetailResponse(
    @SerializedName("meals")
    val meals: List<MealDetail>?
)

data class MealDetail(
    @SerializedName("idMeal") val id: String,
    @SerializedName("strMeal") val name: String,
    @SerializedName("strInstructions") val instructions: String,
    @SerializedName("strMealThumb") val imageUrl: String,
    @SerializedName("strCategory") val category: String,
    @SerializedName("strArea") val area: String,
    
    // Ingredients are individual fields in TheMealDB
    @SerializedName("strIngredient1") val ing1: String?,
    @SerializedName("strIngredient2") val ing2: String?,
    @SerializedName("strIngredient3") val ing3: String?,
    @SerializedName("strIngredient4") val ing4: String?,
    @SerializedName("strIngredient5") val ing5: String?,
    @SerializedName("strMeasure1") val m1: String?,
    @SerializedName("strMeasure2") val m2: String?,
    @SerializedName("strMeasure3") val m3: String?,
    @SerializedName("strMeasure4") val m4: String?,
    @SerializedName("strMeasure5") val m5: String?
) {
    fun getIngredients(): List<String> {
        val list = mutableListOf<String>()
        if (!ing1.isNullOrBlank()) list.add("${m1 ?: ""} $ing1")
        if (!ing2.isNullOrBlank()) list.add("${m2 ?: ""} $ing2")
        if (!ing3.isNullOrBlank()) list.add("${m3 ?: ""} $ing3")
        if (!ing4.isNullOrBlank()) list.add("${m4 ?: ""} $ing4")
        if (!ing5.isNullOrBlank()) list.add("${m5 ?: ""} $ing5")
        return list
    }
}
