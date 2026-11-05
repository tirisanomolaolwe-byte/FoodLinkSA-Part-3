package com.example.foodlinksa.data.repository

import com.example.foodlinksa.data.model.Donation
import com.example.foodlinksa.data.model.DonationStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DonationDatabaseService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val donations = db.collection("donations")

    // CREATE DONATION
    suspend fun createDonation(donation: Donation): String {
        val user = auth.currentUser
            ?: throw IllegalStateException("Please log in first.")

        require(donation.title.isNotBlank()) {
            "Donation title is required."
        }

        require(donation.quantity > 0) {
            "Quantity must be greater than zero."
        }

        require(donation.pickupAddress.isNotBlank()) {
            "Collection address is required."
        }

        val document = donations.document()

        val donorName = donation.donorName.ifBlank {
            user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@")
                ?: "Donor"
        }

        val newDonation = donation.copy(
            id = document.id,
            donorId = user.uid,
            donorName = donorName,
            quantity = donation.quantity,
            availableQuantity = donation.quantity,
            status = DonationStatus.AVAILABLE,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        document.set(newDonation).await()

        return document.id
    }

    // RETRIEVE AVAILABLE DONATIONS
    suspend fun getAvailableDonations(): List<Donation> {
        val snapshot = donations
            .whereIn(
                "status",
                listOf(
                    DonationStatus.AVAILABLE,
                    DonationStatus.PARTIALLY_CLAIMED
                )
            )
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(Donation::class.java)
                ?.copy(id = document.id)
        }.sortedByDescending { it.createdAt }
    }

    // RETRIEVE DONATIONS CREATED BY CURRENT USER
    suspend fun getMyDonations(): List<Donation> {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("Please log in first.")

        val snapshot = donations
            .whereEqualTo("donorId", userId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(Donation::class.java)
                ?.copy(id = document.id)
        }.sortedByDescending { it.createdAt }
    }

    // RETRIEVE SINGLE DONATION BY ID
    suspend fun getDonationById(donationId: String): Donation? {
        if (donationId.isBlank()) return null
        val snapshot = donations.document(donationId).get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(Donation::class.java)?.copy(id = snapshot.id)
        } else {
            null
        }
    }

    // UPDATE DONATION

    suspend fun updateDonation(donation: Donation) {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("Please log in first.")

        require(donation.id.isNotBlank()) {
            "Invalid donation ID."
        }

        require(donation.title.isNotBlank()) {
            "Donation title is required."
        }

        require(donation.category.isNotBlank()) {
            "Food category is required."
        }

        require(donation.quantity > 0) {
            "Quantity must be greater than zero."
        }

        require(donation.pickupAddress.isNotBlank()) {
            "Collection address is required."
        }

        val document = donations.document(donation.id)

        db.runTransaction { transaction ->

            val snapshot = transaction.get(document)

            require(snapshot.exists()) {
                "Donation does not exist."
            }

            require(snapshot.getString("donorId") == userId) {
                "You cannot edit another user's donation."
            }

            val currentStatus = snapshot.getString("status")

            require(
                currentStatus == DonationStatus.AVAILABLE ||
                        currentStatus == DonationStatus.PARTIALLY_CLAIMED
            ) {
                "This donation can no longer be edited."
            }

            val oldQuantity = snapshot.getLong("quantity")?.toInt() ?: 0
            val oldAvailable = snapshot.getLong("availableQuantity")?.toInt() ?: 0

            val alreadyClaimed = oldQuantity - oldAvailable

            require(donation.quantity >= alreadyClaimed) {
                "Quantity cannot be less than the amount already claimed."
            }

            val newAvailable = donation.quantity - alreadyClaimed

            val newStatus = if (newAvailable == 0) {
                DonationStatus.FULLY_CLAIMED
            } else if (alreadyClaimed > 0) {
                DonationStatus.PARTIALLY_CLAIMED
            } else {
                DonationStatus.AVAILABLE
            }

            transaction.update(
                document,
                mapOf(
                    "title" to donation.title,
                    "description" to donation.description,
                    "category" to donation.category,
                    "quantity" to donation.quantity,
                    "availableQuantity" to newAvailable,
                    "unit" to donation.unit,
                    "pickupAddress" to donation.pickupAddress,
                    "collectionDeadline" to donation.collectionDeadline,
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
        }.await()
    }
    // CANCEL DONATION
    suspend fun cancelDonation(donationId: String) {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("Please log in first.")

        require(donationId.isNotBlank()) {
            "Invalid donation ID."
        }

        val document = donations.document(donationId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(document)

            require(snapshot.exists()) {
                "Donation does not exist."
            }

            require(snapshot.getString("donorId") == userId) {
                "You cannot cancel another user's donation."
            }

            val currentStatus = snapshot.getString("status")

            require(
                currentStatus == DonationStatus.AVAILABLE ||
                        currentStatus == DonationStatus.PARTIALLY_CLAIMED
            ) {
                "This donation can no longer be cancelled."
            }

            transaction.update(
                document,
                mapOf(
                    "status" to DonationStatus.CANCELLED,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
        }.await()
    }
}


