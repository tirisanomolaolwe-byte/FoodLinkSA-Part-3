package com.example.foodlinksa.data.model

data class Volunteer(
    val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val contactNumber: String = "",
    val skills: String = "",
    val availability: String = "",
    val additionalInformation: String = "",
    val status: String = VolunteerStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object VolunteerStatus {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
}