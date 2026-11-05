package com.example.foodlinksa.ui.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.foodlinksa.R
import com.example.foodlinksa.data.model.Volunteer
import com.example.foodlinksa.data.repository.VolunteerDatabaseService
import com.example.foodlinksa.databinding.FragmentVolunteerBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class VolunteerFragment : Fragment() {

    private var _binding: FragmentVolunteerBinding? = null
    private val binding get() = _binding!!

    private val volunteerService = VolunteerDatabaseService()
    private val auth = FirebaseAuth.getInstance()

    private var applicationExists = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVolunteerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveVolunteer.setOnClickListener {
            saveApplication()
        }

        binding.btnRetry.setOnClickListener {
            loadApplication()
        }

        loadApplication()
    }

    // LOAD THE CURRENT USER'S APPLICATION
    private fun loadApplication() {

        if (auth.currentUser == null) {
            showErrorState(getString(R.string.volunteer_sign_in_required))
            return
        }

        showLoadingState()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val application = volunteerService.getMyApplication()

                if (application != null) {
                    applicationExists = true
                    displayApplication(application)
                } else {
                    applicationExists = false

                    val user = auth.currentUser

                    binding.etFullName.setText(
                        user?.displayName.orEmpty()
                    )

                    binding.etContactNumber.setText(
                        user?.phoneNumber.orEmpty()
                    )

                    binding.tvApplicationStatus.visibility = View.GONE

                    binding.btnSaveVolunteer.text =
                        getString(R.string.volunteer_submit_application)
                }

                showContentState()

            } catch (e: Exception) {
                showErrorState(
                    e.localizedMessage
                        ?: getString(R.string.volunteer_load_failed)
                )
            }
        }
    }

    // DISPLAY AN EXISTING APPLICATION
    private fun displayApplication(application: Volunteer) {

        binding.etFullName.setText(application.fullName)
        binding.etContactNumber.setText(application.contactNumber)
        binding.etSkills.setText(application.skills)
        binding.etAvailability.setText(application.availability)
        binding.etAdditionalInformation.setText(
            application.additionalInformation
        )

        binding.tvApplicationStatus.text = getString(
            R.string.volunteer_status_format,
            application.status
        )

        binding.tvApplicationStatus.visibility = View.VISIBLE

        binding.btnSaveVolunteer.text =
            getString(R.string.volunteer_update_application)
    }

    // VALIDATE AND SAVE THE APPLICATION
    private fun saveApplication() {

        val fullName = binding.etFullName.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val contactNumber = binding.etContactNumber.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val skills = binding.etSkills.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val availability = binding.etAvailability.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val additionalInformation = binding.etAdditionalInformation.text
            ?.toString()
            ?.trim()
            .orEmpty()

        binding.tilFullName.error = null
        binding.tilContactNumber.error = null
        binding.tilSkills.error = null
        binding.tilAvailability.error = null

        var isValid = true

        if (fullName.isBlank()) {
            binding.tilFullName.error =
                getString(R.string.volunteer_full_name_required)
            isValid = false
        }

        if (contactNumber.isBlank()) {
            binding.tilContactNumber.error =
                getString(R.string.volunteer_contact_required)
            isValid = false
        }

        if (skills.isBlank()) {
            binding.tilSkills.error =
                getString(R.string.volunteer_skills_required)
            isValid = false
        }

        if (availability.isBlank()) {
            binding.tilAvailability.error =
                getString(R.string.volunteer_availability_required)
            isValid = false
        }

        if (!isValid) {
            return
        }

        if (auth.currentUser == null) {
            Toast.makeText(
                requireContext(),
                R.string.volunteer_sign_in_required,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val application = Volunteer(
            fullName = fullName,
            contactNumber = contactNumber,
            skills = skills,
            availability = availability,
            additionalInformation = additionalInformation
        )

        val isUpdate = applicationExists

        setSavingState(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val savedApplication =
                    volunteerService.saveMyApplication(application)

                applicationExists = true

                displayApplication(savedApplication)

                Toast.makeText(
                    requireContext(),
                    if (isUpdate) {
                        R.string.volunteer_application_updated
                    } else {
                        R.string.volunteer_application_saved
                    },
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.localizedMessage
                        ?: getString(R.string.volunteer_save_failed),
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                setSavingState(false)
            }
        }
    }

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollViewContent.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showContentState() {
        binding.progressBar.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.scrollViewContent.visibility = View.VISIBLE
    }

    private fun showErrorState(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.scrollViewContent.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    private fun setSavingState(isSaving: Boolean) {
        binding.btnSaveVolunteer.isEnabled = !isSaving
        binding.progressBar.visibility =
            if (isSaving) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}