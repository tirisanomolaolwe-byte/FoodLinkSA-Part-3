package com.example.foodlinksa.data.model

data class DonationClaim(
    val id: String = "",
    val donationId: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val quantityClaimed: Int = 0,
    val unit: String = "portions",
    val status: String = ClaimStatus.RESERVED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object ClaimStatus {
    const val RESERVED = "RESERVED"
    const val CANCELLED = "CANCELLED"
    const val COLLECTED = "COLLECTED"
}