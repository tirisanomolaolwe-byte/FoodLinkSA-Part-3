
package com.example.foodlinksa.ui.donation

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodlinksa.R
import com.example.foodlinksa.data.model.Donation
import com.example.foodlinksa.data.model.DonationClaim
import com.example.foodlinksa.data.model.DonationStatus
import com.example.foodlinksa.data.repository.DonationClaimDatabaseService
import com.example.foodlinksa.data.repository.DonationDatabaseService
import com.example.foodlinksa.databinding.FragmentDonationDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class DonationDetailsFragment : Fragment() {

    private var _binding: FragmentDonationDetailsBinding? = null
    private val binding get() = _binding!!

    private val donationService = DonationDatabaseService()
    private val claimService = DonationClaimDatabaseService()
    private val auth = FirebaseAuth.getInstance()

    private var donationId: String? = null
    private var currentDonation: Donation? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonationDetailsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        donationId = arguments?.getString("donationId")

        binding.btnRetry.setOnClickListener {
            loadDonationDetails()
        }

        binding.btnEditDonation.setOnClickListener {
            val id = donationId ?: return@setOnClickListener
            val bundle = bundleOf("donationId" to id)

            findNavController().navigate(
                R.id.action_donationDetailsFragment_to_editDonationFragment,
                bundle
            )
        }

        binding.btnCancelDonation.setOnClickListener {
            confirmCancelDonation()
        }

        binding.btnClaimDonation.setOnClickListener {
            claimDonation()
        }
    }

    override fun onResume() {
        super.onResume()
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
                    currentDonation = donation
                    showContentState(donation)
                } else {
                    currentDonation = null
                    showErrorState("Donation not found.")
                }
            } catch (e: Exception) {
                showErrorState(
                    e.localizedMessage ?: "Failed to load donation details."
                )
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

        binding.tvDescription.text = donation.description.ifBlank {
            "No description provided."
        }

        binding.tvQuantityDetails.text =
            "${donation.availableQuantity} ${donation.unit} available " +
                    "(${donation.quantity} total original)"

        binding.tvPickupAddress.text = donation.pickupAddress

        val deadlineStr = if (donation.collectionDeadline > 0) {
            DateFormat.format(
                "MMM dd, yyyy HH:mm",
                Date(donation.collectionDeadline)
            ).toString()
        } else {
            "Not specified"
        }

        binding.tvCollectionDeadline.text = deadlineStr

        val donor = donation.donorName.ifBlank {
            "Anonymous Donor"
        }

        binding.tvDonorName.text = "Donated by: $donor"

        val createdStr = if (donation.createdAt > 0) {
            DateFormat.format(
                "MMM dd, yyyy HH:mm",
                Date(donation.createdAt)
            ).toString()
        } else {
            "N/A"
        }

        binding.tvCreatedAt.text = "Posted on: $createdStr"

        val currentUserId = auth.currentUser?.uid
        val isDonor = currentUserId != null &&
                currentUserId == donation.donorId

        if (isDonor) {
            binding.layoutDonorActions.visibility = View.VISIBLE
            binding.layoutDonorClaims.visibility = View.VISIBLE

            val isEditable =
                donation.status == DonationStatus.AVAILABLE ||
                        donation.status == DonationStatus.PARTIALLY_CLAIMED

            binding.btnEditDonation.isEnabled = isEditable
            binding.btnCancelDonation.isEnabled = isEditable

            loadDonorClaims(donation.id)
        } else {
            binding.layoutDonorActions.visibility = View.GONE
            binding.layoutDonorClaims.visibility = View.GONE
        }

        val isClaimableStatus =
            donation.status == DonationStatus.AVAILABLE ||
                    donation.status == DonationStatus.PARTIALLY_CLAIMED

        val deadlineHasPassed =
            donation.collectionDeadline > 0 &&
                    donation.collectionDeadline <= System.currentTimeMillis()

        val canClaim =
            currentUserId != null &&
                    !isDonor &&
                    isClaimableStatus &&
                    donation.availableQuantity > 0 &&
                    !deadlineHasPassed

        binding.layoutRecipientClaim.visibility =
            if (canClaim) View.VISIBLE else View.GONE

        binding.etClaimQuantity.text?.clear()
        binding.btnClaimDonation.isEnabled = canClaim

        if (canClaim) {
            binding.tvClaimAvailability.text =
                "You can claim up to ${donation.availableQuantity} ${donation.unit}."
        }
    }

    private fun loadDonorClaims(id: String) {
        binding.tvDonorClaimsEmpty.visibility = View.GONE
        binding.rvDonorClaims.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val claims = claimService.getClaimsForDonation(id)

                if (claims.isEmpty()) {
                    binding.tvDonorClaimsEmpty.text =
                        "No one has claimed this donation yet."
                    binding.tvDonorClaimsEmpty.visibility = View.VISIBLE
                    binding.rvDonorClaims.visibility = View.GONE
                } else {
                    binding.tvDonorClaimsEmpty.visibility = View.GONE
                    binding.rvDonorClaims.visibility = View.VISIBLE

                    binding.rvDonorClaims.layoutManager =
                        LinearLayoutManager(requireContext())

                    binding.rvDonorClaims.adapter =
                        DonorClaimAdapter(claims) { selectedClaim ->
                            confirmCollection(selectedClaim)
                        }
                }
            } catch (e: Exception) {
                binding.rvDonorClaims.visibility = View.GONE
                binding.tvDonorClaimsEmpty.text =
                    e.localizedMessage ?: "Unable to load donation claims."
                binding.tvDonorClaimsEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun confirmCollection(claim: DonationClaim) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Collection")
            .setMessage(
                "Confirm that ${claim.recipientName} has collected " +
                        "${claim.quantityClaimed} ${claim.unit}?"
            )
            .setPositiveButton("Confirm") { _, _ ->
                markCollectionComplete(claim)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markCollectionComplete(claim: DonationClaim) {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                claimService.markClaimCollected(claim.id)

                Toast.makeText(
                    requireContext(),
                    "Collection confirmed successfully.",
                    Toast.LENGTH_SHORT
                ).show()

                val id = donationId
                if (!id.isNullOrBlank()) {
                    loadDonorClaims(id)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Unable to confirm collection: ${
                        e.localizedMessage ?: "Please try again."
                    }",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun claimDonation() {
        val donation = currentDonation ?: return
        val id = donationId ?: return

        val quantityText = binding.etClaimQuantity.text
            ?.toString()
            ?.trim()

        val requestedQuantity = quantityText?.toIntOrNull()

        if (requestedQuantity == null || requestedQuantity <= 0) {
            binding.etClaimQuantity.error = "Enter a valid quantity."
            return
        }

        if (requestedQuantity > donation.availableQuantity) {
            binding.etClaimQuantity.error =
                "Only ${donation.availableQuantity} ${donation.unit} available."
            return
        }

        binding.btnClaimDonation.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollViewContent.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                claimService.claimDonation(
                    donationId = id,
                    quantity = requestedQuantity
                )

                Toast.makeText(
                    requireContext(),
                    "Donation claimed successfully.",
                    Toast.LENGTH_LONG
                ).show()

                loadDonationDetails()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Unable to claim donation: ${
                        e.localizedMessage ?: "Please try again."
                    }",
                    Toast.LENGTH_LONG
                ).show()

                loadDonationDetails()
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

                loadDonationDetails()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to cancel donation: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()

                loadDonationDetails()
            }
        }
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
