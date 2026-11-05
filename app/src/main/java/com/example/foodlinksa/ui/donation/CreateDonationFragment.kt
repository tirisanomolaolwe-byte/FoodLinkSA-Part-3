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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodlinksa.data.model.Donation
import com.example.foodlinksa.data.repository.DonationDatabaseService
import com.example.foodlinksa.databinding.FragmentCreateDonationBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class CreateDonationFragment : Fragment() {

    private var _binding: FragmentCreateDonationBinding? = null
    private val binding get() = _binding!!

    private val donationService = DonationDatabaseService()
    private val auth = FirebaseAuth.getInstance()

    private var selectedDeadlineMillis: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateDonationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAuthentication()
        setupDropdowns()
        setupDateTimePicker()
        setupSubmitButton()
    }

    private fun checkAuthentication() {
        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "Please log in to create a donation.", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
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
        binding.actvUnit.setText("portions", false)
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

    private fun setupSubmitButton() {
        binding.btnSubmitDonation.setOnClickListener {
            if (validateInputs()) {
                submitDonation()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

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
        if (quantity == null || quantity <= 0) {
            binding.tilQuantity.error = "Enter a quantity greater than 0"
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

    private fun submitDonation() {
        setLoading(true)

        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        val quantity = binding.etQuantity.text.toString().trim().toInt()
        val unit = binding.actvUnit.text.toString().trim()
        val address = binding.etPickupAddress.text.toString().trim()

        val donation = Donation(
            title = title,
            description = description,
            category = category,
            quantity = quantity,
            availableQuantity = quantity,
            unit = unit,
            pickupAddress = address,
            collectionDeadline = selectedDeadlineMillis
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                donationService.createDonation(donation)
                Toast.makeText(requireContext(), "Donation created successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    requireContext(),
                    "Failed to create donation: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmitDonation.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
