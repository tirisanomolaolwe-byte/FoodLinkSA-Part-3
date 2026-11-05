package com.example.foodlinksa.data.repository

import com.example.foodlinksa.data.model.FoodItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FoodDatabaseService {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("food_items")

    // CREATE
    suspend fun createFoodItem(item: FoodItem): Boolean {
        return try {
            val docRef = collection.document()
            val newItem = item.copy(id = docRef.id)
            docRef.set(newItem).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    // RETRIEVE
    suspend fun getFoodItems(userId: String): List<FoodItem> {
        return try {
            val snapshot = collection.whereEqualTo("userId", userId).get().await()
            snapshot.toObjects(FoodItem::class.java)
        } catch (_: Exception) {
            emptyList()
        }
    }

    // UPDATE
    suspend fun updateFoodItem(item: FoodItem): Boolean {
        return try {
            if (item.id.isNotEmpty()) {
                collection.document(item.id).set(item).await()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    // DELETE
    suspend fun deleteFoodItem(itemId: String): Boolean {
        return try {
            collection.document(itemId).delete().await()
            true
        } catch (_: Exception) {
            false
        }
    }
}
