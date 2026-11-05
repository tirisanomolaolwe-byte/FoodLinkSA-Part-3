package com.example.foodlinksa.ui.donation

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.foodlinksa.data.model.Donation
import com.example.foodlinksa.data.repository.DonationDatabaseService
import com.example.foodlinksa.databinding.FragmentDonationDetailsBinding
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class DonationDetailsFragment : Fragment() {

    private var _binding: FragmentDonationDetailsBinding? = null
    private val binding get() = _binding!!

    private val donationService = DonationDatabaseService()
    private var donationId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonationDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        donationId = arguments?.getString("donationId")

        binding.btnRetry.setOnClickListener {
            loadDonationDetails()
        }

        loadDonationDetails()
    }

    private fun loadDonationDetails() {
        val id = donationId
        if (id.isNullOrBlank()) {
            showErrorState("Invalid donation ID.")
            return
        }

        showLoadingState()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val donation = donationService.getDonationById(id)
                if (donation != null) {
                    showContentState(donation)
                } else {
                    showErrorState("Donation not found.")
                }
            } catch (e: Exception) {
                showErrorState(e.localizedMessage ?: "Failed to load donation details.")
            }
        }
    }

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollViewContent.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showContentState(donation: Donation) {
        binding.progressBar.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.scrollViewContent.visibility = View.VISIBLE

        binding.tvTitle.text = donation.title
        binding.chipStatus.text = donation.status.uppercase(Locale.ROOT)
        binding.tvCategory.text = donation.category
        binding.tvDescription.text = donation.description.ifBlank { "No description provided." }
        binding.tvQuantityDetails.text = "${donation.availableQuantity} ${donation.unit} available (${donation.quantity} total original)"
        binding.tvPickupAddress.text = "📍 ${donation.pickupAddress}"

        val deadlineStr = if (donation.collectionDeadline > 0) {
            DateFormat.format("MMM dd, yyyy HH:mm", Date(donation.collectionDeadline)).toString()
        } else {
            "Not specified"
        }
        binding.tvCollectionDeadline.text = "⏰ $deadlineStr"

        val donor = donation.donorName.ifBlank { "Anonymous Donor" }
        binding.tvDonorName.text = "Donated by: $donor"

        val createdStr = if (donation.createdAt > 0) {
            DateFormat.format("MMM dd, yyyy HH:mm", Date(donation.createdAt)).toString()
        } else {
            "N/A"
        }
        binding.tvCreatedAt.text = "Posted on: $createdStr"
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
