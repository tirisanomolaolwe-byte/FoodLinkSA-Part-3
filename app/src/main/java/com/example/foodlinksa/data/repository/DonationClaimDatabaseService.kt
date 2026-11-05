
package com.example.foodlinksa.data.repository

import com.example.foodlinksa.data.model.ClaimStatus
import com.example.foodlinksa.data.model.Donation
import com.example.foodlinksa.data.model.DonationClaim
import com.example.foodlinksa.data.model.DonationStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DonationClaimDatabaseService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val donations = db.collection("donations")
    private val claims = db.collection("donationClaims")

    suspend fun claimDonation(
        donationId: String,
        quantity: Int
    ): String {
        val user = auth.currentUser
            ?: throw IllegalStateException("You must be logged in to claim a donation.")

        if (donationId.isBlank()) {
            throw IllegalArgumentException("Invalid donation.")
        }

        if (quantity <= 0) {
            throw IllegalArgumentException("Please enter a valid quantity.")
        }

        val donationRef = donations.document(donationId)
        val claimRef = claims.document()
        val now = System.currentTimeMillis()

        val recipientName = user.displayName
            ?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "Recipient"

        return db.runTransaction { transaction ->
            val donationSnapshot = transaction.get(donationRef)

            if (!donationSnapshot.exists()) {
                throw IllegalStateException("This donation no longer exists.")
            }

            val donation = donationSnapshot
                .toObject(Donation::class.java)
                ?.copy(id = donationSnapshot.id)
                ?: throw IllegalStateException("Could not load donation details.")

            if (donation.donorId == user.uid) {
                throw IllegalStateException("You cannot claim your own donation.")
            }

            if (
                donation.status != DonationStatus.AVAILABLE &&
                donation.status != DonationStatus.PARTIALLY_CLAIMED
            ) {
                throw IllegalStateException("This donation is no longer available.")
            }

            if (
                donation.collectionDeadline > 0 &&
                donation.collectionDeadline <= now
            ) {
                throw IllegalStateException("The collection deadline has passed.")
            }

            val available = donation.availableQuantity

            if (available <= 0) {
                throw IllegalStateException("There is no quantity available.")
            }

            if (quantity > available) {
                throw IllegalArgumentException(
                    "Only $available ${donation.unit} available."
                )
            }

            val remainingQuantity = available - quantity

            val updatedDonationStatus =
                if (remainingQuantity == 0) {
                    DonationStatus.FULLY_CLAIMED
                } else {
                    DonationStatus.PARTIALLY_CLAIMED
                }

            val claim = DonationClaim(
                id = claimRef.id,
                donationId = donationId,
                recipientId = user.uid,
                recipientName = recipientName,
                quantityClaimed = quantity,
                unit = donation.unit,
                status = ClaimStatus.RESERVED,
                createdAt = now,
                updatedAt = now
            )

            transaction.update(
                donationRef,
                mapOf(
                    "availableQuantity" to remainingQuantity,
                    "status" to updatedDonationStatus,
                    "updatedAt" to now
                )
            )

            transaction.set(claimRef, claim)

            claimRef.id
        }.await()
    }

    suspend fun cancelClaim(claimId: String) {
        val user = auth.currentUser
            ?: throw IllegalStateException("You must be logged in.")

        if (claimId.isBlank()) {
            throw IllegalArgumentException("Invalid claim.")
        }

        val claimRef = claims.document(claimId)
        val now = System.currentTimeMillis()

        db.runTransaction { transaction ->
            val claimSnapshot = transaction.get(claimRef)

            if (!claimSnapshot.exists()) {
                throw IllegalStateException("This claim no longer exists.")
            }

            val claim = claimSnapshot
                .toObject(DonationClaim::class.java)
                ?.copy(id = claimSnapshot.id)
                ?: throw IllegalStateException("Could not load claim details.")

            if (claim.recipientId != user.uid) {
                throw IllegalStateException("You cannot cancel someone else's claim.")
            }

            if (claim.status != ClaimStatus.RESERVED) {
                throw IllegalStateException("Only reserved claims can be cancelled.")
            }

            val donationRef = donations.document(claim.donationId)
            val donationSnapshot = transaction.get(donationRef)

            if (!donationSnapshot.exists()) {
                throw IllegalStateException("The original donation no longer exists.")
            }

            val donation = donationSnapshot
                .toObject(Donation::class.java)
                ?.copy(id = donationSnapshot.id)
                ?: throw IllegalStateException("Could not load donation details.")

            if (
                donation.status == DonationStatus.CANCELLED ||
                donation.status == DonationStatus.EXPIRED ||
                donation.status == DonationStatus.COLLECTED
            ) {
                throw IllegalStateException(
                    "This donation can no longer accept returned quantities."
                )
            }

            val restoredQuantity =
                donation.availableQuantity + claim.quantityClaimed

            if (restoredQuantity > donation.quantity) {
                throw IllegalStateException(
                    "The returned quantity would exceed the original donation quantity."
                )
            }

            val updatedDonationStatus =
                if (restoredQuantity == donation.quantity) {
                    DonationStatus.AVAILABLE
                } else {
                    DonationStatus.PARTIALLY_CLAIMED
                }

            transaction.update(
                donationRef,
                mapOf(
                    "availableQuantity" to restoredQuantity,
                    "status" to updatedDonationStatus,
                    "updatedAt" to now
                )
            )

            transaction.update(
                claimRef,
                mapOf(
                    "status" to ClaimStatus.CANCELLED,
                    "updatedAt" to now
                )
            )

            Unit
        }.await()
    }

    suspend fun markClaimCollected(claimId: String) {
        val user = auth.currentUser
            ?: throw IllegalStateException("You must be logged in.")

        if (claimId.isBlank()) {
            throw IllegalArgumentException("Invalid claim.")
        }

        val claimRef = claims.document(claimId)
        val now = System.currentTimeMillis()

        db.runTransaction { transaction ->
            val claimSnapshot = transaction.get(claimRef)

            if (!claimSnapshot.exists()) {
                throw IllegalStateException("This claim no longer exists.")
            }

            val claim = claimSnapshot
                .toObject(DonationClaim::class.java)
                ?.copy(id = claimSnapshot.id)
                ?: throw IllegalStateException("Could not load claim details.")

            if (claim.status != ClaimStatus.RESERVED) {
                throw IllegalStateException(
                    "Only reserved claims can be marked as collected."
                )
            }

            val donationRef = donations.document(claim.donationId)
            val donationSnapshot = transaction.get(donationRef)

            if (!donationSnapshot.exists()) {
                throw IllegalStateException("The original donation no longer exists.")
            }

            val donation = donationSnapshot
                .toObject(Donation::class.java)
                ?.copy(id = donationSnapshot.id)
                ?: throw IllegalStateException("Could not load donation details.")

            if (donation.donorId != user.uid) {
                throw IllegalStateException(
                    "Only the donor can confirm this collection."
                )
            }

            if (
                donation.status == DonationStatus.CANCELLED ||
                donation.status == DonationStatus.EXPIRED
            ) {
                throw IllegalStateException("This donation is no longer active.")
            }

            transaction.update(
                claimRef,
                mapOf(
                    "status" to ClaimStatus.COLLECTED,
                    "updatedAt" to now
                )
            )

            transaction.update(
                donationRef,
                "updatedAt",
                now
            )

            Unit
        }.await()
    }

    suspend fun getClaimsForDonation(
        donationId: String
    ): List<DonationClaim> {
        val user = auth.currentUser
            ?: throw IllegalStateException("You must be logged in.")

        if (donationId.isBlank()) {
            throw IllegalArgumentException("Invalid donation.")
        }

        val donationSnapshot = donations.document(donationId)
            .get()
            .await()

        if (!donationSnapshot.exists()) {
            throw IllegalStateException("This donation no longer exists.")
        }

        val donation = donationSnapshot
            .toObject(Donation::class.java)
            ?: throw IllegalStateException("Could not load donation details.")

        if (donation.donorId != user.uid) {
            throw IllegalStateException(
                "Only the donor can view the claims for this donation."
            )
        }

        return claims
            .whereEqualTo("donationId", donationId)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(DonationClaim::class.java)
                    ?.copy(id = document.id)
            }
            .sortedByDescending { it.createdAt }
    }

    suspend fun getMyClaims(): List<DonationClaim> {
        val user = auth.currentUser
            ?: throw IllegalStateException("You must be logged in.")

        return claims
            .whereEqualTo("recipientId", user.uid)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(DonationClaim::class.java)
                    ?.copy(id = document.id)
            }
            .sortedByDescending { it.createdAt }
    }
}
