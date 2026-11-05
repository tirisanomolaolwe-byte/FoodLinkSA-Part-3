package com.example.foodlinksa.ui.donation

import android.os.Bundle
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
import com.example.foodlinksa.data.repository.DonationDatabaseService
import com.example.foodlinksa.databinding.FragmentMyDonationsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MyDonationsFragment : Fragment() {

    private var _binding: FragmentMyDonationsBinding? = null
    private val binding get() = _binding!!

    private val donationService = DonationDatabaseService()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDonationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "Please log in to view your donations.", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadMyDonations()
    }

    private fun setupRecyclerView() {
        binding.rvMyDonations.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.fabCreateDonation.setOnClickListener {
            findNavController().navigate(R.id.action_myDonationsFragment_to_createDonationFragment)
        }

        binding.btnRetry.setOnClickListener {
            loadMyDonations()
        }
    }

    private fun loadMyDonations() {
        showLoadingState()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val donations = donationService.getMyDonations()
                if (donations.isEmpty()) {
                    showEmptyState()
                } else {
                    showContentState(donations)
                }
            } catch (e: Exception) {
                showErrorState(e.localizedMessage ?: "Failed to load your donations.")
            }
        }
    }

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvMyDonations.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.rvMyDonations.visibility = View.GONE
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
    }

    private fun showContentState(donations: List<Donation>) {
        binding.progressBar.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.rvMyDonations.visibility = View.VISIBLE

        binding.rvMyDonations.adapter = DonationAdapter(
            donations = donations,
            onItemClick = { donation ->
                val bundle = bundleOf("donationId" to donation.id)
                findNavController().navigate(
                    R.id.action_myDonationsFragment_to_donationDetailsFragment,
                    bundle
                )
            },
            onEditClick = { donation ->
                val bundle = bundleOf("donationId" to donation.id)
                findNavController().navigate(
                    R.id.action_myDonationsFragment_to_editDonationFragment,
                    bundle
                )
            },
            onCancelClick = { donation ->
                confirmCancelDonation(donation)
            }
        )
    }

    private fun confirmCancelDonation(donation: Donation) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.cancel_donation_confirm_title)
            .setMessage(R.string.cancel_donation_confirm_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                cancelDonation(donation.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun cancelDonation(donationId: String) {
        showLoadingState()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                donationService.cancelDonation(donationId)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.donation_cancelled_success),
                    Toast.LENGTH_SHORT
                ).show()
                loadMyDonations()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to cancel donation: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                loadMyDonations()
            }
        }
    }

    private fun showErrorState(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvMyDonations.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
