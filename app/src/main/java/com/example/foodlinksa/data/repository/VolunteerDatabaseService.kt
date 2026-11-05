package com.example.foodlinksa.data.repository

import com.example.foodlinksa.data.model.Volunteer
import com.example.foodlinksa.data.model.VolunteerStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VolunteerDatabaseService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val volunteers = db.collection("volunteers")

    // RETRIEVE THE CURRENT USER'S APPLICATION
    suspend fun getMyApplication(): Volunteer? {

        val user = auth.currentUser
            ?: throw IllegalStateException("Please log in first.")

        val document = volunteers
            .document(user.uid)
            .get()
            .await()

        if (!document.exists()) {
            return null
        }

        return document.toObject(Volunteer::class.java)
            ?.copy(id = document.id)
    }

    // CREATE OR UPDATE THE CURRENT USER'S APPLICATION
    suspend fun saveMyApplication(application: Volunteer): Volunteer {

        val user = auth.currentUser
            ?: throw IllegalStateException("Please log in first.")

        require(application.fullName.isNotBlank()) {
            "Full name is required."
        }

        require(application.contactNumber.isNotBlank()) {
            "Contact number is required."
        }

        require(application.skills.isNotBlank()) {
            "Please enter your skills."
        }

        require(application.availability.isNotBlank()) {
            "Please enter your availability."
        }

        // Use the authenticated user's UID as the document ID.
        // This prevents multiple application documents for one user.
        val document = volunteers.document(user.uid)

        val existingDocument = document.get().await()
        val now = System.currentTimeMillis()

        // Preserve the existing status and creation date when updating.
        val existingStatus = existingDocument.getString("status")
            ?: VolunteerStatus.PENDING

        val originalCreatedAt = existingDocument.getLong("createdAt")
            ?: now

        val savedApplication = Volunteer(
            id = user.uid,
            userId = user.uid,
            fullName = application.fullName.trim(),
            contactNumber = application.contactNumber.trim(),
            skills = application.skills.trim(),
            availability = application.availability.trim(),
            additionalInformation = application.additionalInformation.trim(),
            status = existingStatus,
            createdAt = originalCreatedAt,
            updatedAt = now
        )

        document.set(savedApplication).await()

        return savedApplication
    }
}