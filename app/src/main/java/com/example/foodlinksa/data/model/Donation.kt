
package com.example.foodlinksa.data.model

data class Donation(
    val id: String = "",
    val donorId: String = "",
    val donorName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val quantity: Int = 0,
    val availableQuantity: Int = 0,
    val unit: String = "portions",
    val imageUrl: String = "",
    val pickupAddress: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val collectionDeadline: Long = 0L,
    val status: String = DonationStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object DonationStatus {
    const val AVAILABLE = "AVAILABLE"
    const val PARTIALLY_CLAIMED = "PARTIALLY_CLAIMED"
    const val FULLY_CLAIMED = "FULLY_CLAIMED"
    const val COLLECTED = "COLLECTED"
    const val CANCELLED = "CANCELLED"
    const val EXPIRED = "EXPIRED"
}
