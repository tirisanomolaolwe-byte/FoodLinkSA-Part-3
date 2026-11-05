package com.example.foodlinksa.data.api

import com.example.foodlinksa.data.model.MealDetailResponse
import com.example.foodlinksa.data.model.MealResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): Response<MealResponse>

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): Response<MealDetailResponse>

    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): Response<MealResponse>
}
