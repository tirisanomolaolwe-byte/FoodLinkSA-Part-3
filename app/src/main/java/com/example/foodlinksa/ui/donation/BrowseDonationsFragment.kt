package com.example.foodlinksa.ui.donation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodlinksa.R
import com.example.foodlinksa.data.repository.DonationDatabaseService
import com.example.foodlinksa.databinding.FragmentBrowseDonationsBinding
import kotlinx.coroutines.launch

class BrowseDonationsFragment : Fragment() {

    private var _binding: FragmentBrowseDonationsBinding? = null
    private val binding get() = _binding!!

    private val donationService = DonationDatabaseService()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseDonationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadDonations()
    }

    private fun setupRecyclerView() {
        binding.rvDonations.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.fabCreateDonation.setOnClickListener {
            findNavController().navigate(R.id.action_browseDonationsFragment_to_createDonationFragment)
        }

        binding.btnRetry.setOnClickListener {
            loadDonations()
        }
    }

    private fun loadDonations() {
        showLoadingState()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val donations = donationService.getAvailableDonations()
                if (donations.isEmpty()) {
                    showEmptyState()
                } else {
                    showContentState(donations)
                }
            } catch (e: Exception) {
                showErrorState(e.localizedMessage ?: "Failed to load donations.")
            }
        }
    }

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvDonations.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.rvDonations.visibility = View.GONE
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
    }

    private fun showContentState(donations: List<com.example.foodlinksa.data.model.Donation>) {
        binding.progressBar.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.rvDonations.visibility = View.VISIBLE

        binding.rvDonations.adapter = DonationAdapter(donations) { selectedDonation ->
            val bundle = bundleOf("donationId" to selectedDonation.id)
            findNavController().navigate(
                R.id.action_browseDonationsFragment_to_donationDetailsFragment,
                bundle
            )
        }
    }

    private fun showErrorState(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvDonations.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
