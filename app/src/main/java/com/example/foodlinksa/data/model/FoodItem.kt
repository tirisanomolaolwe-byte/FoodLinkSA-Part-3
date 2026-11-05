package com.example.foodlinksa.data.model

data class FoodItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
