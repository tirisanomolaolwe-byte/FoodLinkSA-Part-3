package com.example.foodlinksa.ui.donation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodlinksa.R
import com.example.foodlinksa.data.model.Donation
import com.example.foodlinksa.data.model.DonationStatus
import com.example.foodlinksa.data.repository.DonationDatabaseService
import com.example.foodlinksa.databinding.FragmentEditDonationBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class EditDonationFragment : Fragment() {

    private var _binding: FragmentEditDonationBinding? = null
    private val binding get() = _binding!!

    private val donationService = DonationDatabaseService()
    private val auth = FirebaseAuth.getInstance()

    private var donationId: String? = null
    private var existingDonation: Donation? = null
    private var selectedDeadlineMillis: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDonationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        donationId = arguments?.getString("donationId")

        setupDropdowns()
        setupDateTimePicker()
        setupListeners()

        loadDonation()
    }

    private fun setupDropdowns() {
        val categories = listOf(
            "Prepared Meals",
            "Fresh Produce",
            "Bakery",
            "Dairy",
            "Canned Goods",
            "Beverages",
            "Other"
        )
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        binding.actvCategory.setAdapter(categoryAdapter)

        val units = listOf("portions", "meals", "boxes", "kg", "items", "packs")
        val unitAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            units
        )
        binding.actvUnit.setAdapter(unitAdapter)
    }

    private fun setupDateTimePicker() {
        binding.etCollectionDeadline.setOnClickListener {
            showDatePicker()
        }
        binding.tilCollectionDeadline.setEndIconOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDeadlineMillis > 0) {
            calendar.timeInMillis = selectedDeadlineMillis
        }
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                showTimePicker(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePicker(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDeadlineMillis = selectedCalendar.timeInMillis
                val formatted = DateFormat.format("MMM dd, yyyy HH:mm", Date(selectedDeadlineMillis)).toString()
                binding.etCollectionDeadline.setText(formatted)
                binding.tilCollectionDeadline.error = null
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun setupListeners() {
        binding.btnUpdateDonation.setOnClickListener {
            if (validateInputs()) {
                updateDonation()
            }
        }

        binding.btnCancelDonation.setOnClickListener {
            confirmCancelDonation()
        }

        binding.btnRetry.setOnClickListener {
            loadDonation()
        }
    }

    private fun loadDonation() {
        val id = donationId
        if (id.isNullOrBlank()) {
            showErrorState("Invalid donation ID.")
            return
        }

        showLoadingState()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val donation = donationService.getDonationById(id)
                if (donation == null) {
                    showErrorState("Donation not found.")
                    return@launch
                }

                val currentUserId = auth.currentUser?.uid
                if (currentUserId != donation.donorId) {
                    showErrorState("You cannot edit another user's donation.")
                    return@launch
                }

                if (donation.status != DonationStatus.AVAILABLE && donation.status != DonationStatus.PARTIALLY_CLAIMED) {
                    showErrorState(getString(R.string.cannot_edit_status))
                    return@launch
                }

                existingDonation = donation
                populateFields(donation)
                showContentState()
            } catch (e: Exception) {
                showErrorState(e.localizedMessage ?: "Failed to load donation details.")
            }
        }
    }

    private fun populateFields(donation: Donation) {
        binding.etTitle.setText(donation.title)
        binding.etDescription.setText(donation.description)
        binding.actvCategory.setText(donation.category, false)
        binding.etQuantity.setText(donation.quantity.toString())
        binding.actvUnit.setText(donation.unit, false)
        binding.etPickupAddress.setText(donation.pickupAddress)

        selectedDeadlineMillis = donation.collectionDeadline
        if (selectedDeadlineMillis > 0) {
            val formatted = DateFormat.format("MMM dd, yyyy HH:mm", Date(selectedDeadlineMillis)).toString()
            binding.etCollectionDeadline.setText(formatted)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val currentDonation = existingDonation ?: return false

        val title = binding.etTitle.text.toString().trim()
        if (title.isBlank()) {
            binding.tilTitle.error = "Title is required"
            isValid = false
        } else {
            binding.tilTitle.error = null
        }

        val description = binding.etDescription.text.toString().trim()
        if (description.isBlank()) {
            binding.tilDescription.error = "Description is required"
            isValid = false
        } else {
            binding.tilDescription.error = null
        }

        val category = binding.actvCategory.text.toString().trim()
        if (category.isBlank()) {
            binding.tilCategory.error = "Please select a category"
            isValid = false
        } else {
            binding.tilCategory.error = null
        }

        val quantityStr = binding.etQuantity.text.toString().trim()
        val quantity = quantityStr.toIntOrNull()
        val alreadyClaimed = currentDonation.quantity - currentDonation.availableQuantity
        if (quantity == null || quantity <= 0) {
            binding.tilQuantity.error = "Enter a quantity greater than 0"
            isValid = false
        } else if (quantity < alreadyClaimed) {
            binding.tilQuantity.error = "Quantity cannot be less than claimed amount ($alreadyClaimed)"
            isValid = false
        } else {
            binding.tilQuantity.error = null
        }

        val unit = binding.actvUnit.text.toString().trim()
        if (unit.isBlank()) {
            binding.tilUnit.error = "Please select a unit"
            isValid = false
        } else {
            binding.tilUnit.error = null
        }

        val address = binding.etPickupAddress.text.toString().trim()
        if (address.isBlank()) {
            binding.tilPickupAddress.error = "Collection address is required"
            isValid = false
        } else {
            binding.tilPickupAddress.error = null
        }

        if (selectedDeadlineMillis <= System.currentTimeMillis()) {
            binding.tilCollectionDeadline.error = "Select a valid future collection deadline"
            isValid = false
        } else {
            binding.tilCollectionDeadline.error = null
        }

        return isValid
    }

    private fun updateDonation() {
        val currentDonation = existingDonation ?: return
        showLoadingState()

        val updatedDonation = currentDonation.copy(
            title = binding.etTitle.text.toString().trim(),
            description = binding.etDescription.text.toString().trim(),
            category = binding.actvCategory.text.toString().trim(),
            quantity = binding.etQuantity.text.toString().trim().toInt(),
            unit = binding.actvUnit.text.toString().trim(),
            pickupAddress = binding.etPickupAddress.text.toString().trim(),
            collectionDeadline = selectedDeadlineMillis
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                donationService.updateDonation(updatedDonation)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.donation_updated_success),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                showContentState()
                Toast.makeText(
                    requireContext(),
                    "Failed to update donation: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun confirmCancelDonation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.cancel_donation_confirm_title)
            .setMessage(R.string.cancel_donation_confirm_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                cancelDonation()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun cancelDonation() {
        val id = donationId ?: return
        showLoadingState()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                donationService.cancelDonation(id)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.donation_cancelled_success),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                showContentState()
                Toast.makeText(
                    requireContext(),
                    "Failed to cancel donation: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
