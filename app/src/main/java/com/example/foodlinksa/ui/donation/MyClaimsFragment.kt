package com.example.foodlinksa.ui.donation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodlinksa.data.model.DonationClaim
import com.example.foodlinksa.data.repository.DonationClaimDatabaseService
import com.example.foodlinksa.databinding.FragmentMyClaimsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MyClaimsFragment : Fragment() {

    private var _binding: FragmentMyClaimsBinding? = null
    private val binding get() = _binding!!

    private val claimService = DonationClaimDatabaseService()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyClaimsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (auth.currentUser == null) {
            Toast.makeText(
                requireContext(),
                "Please log in to view your claims.",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigateUp()
            return
        }

        binding.rvMyClaims.layoutManager =
            LinearLayoutManager(requireContext())

        binding.btnRetry.setOnClickListener {
            loadMyClaims()
        }

        loadMyClaims()
    }

    private fun loadMyClaims() {
        showLoadingState()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val claims = claimService.getMyClaims()

                if (claims.isEmpty()) {
                    showEmptyState()
                } else {
                    showContentState(claims)
                }
            } catch (e: Exception) {
                showErrorState(
                    e.localizedMessage ?: "Failed to load your claims."
                )
            }
        }
    }

    private fun confirmCancelClaim(claim: DonationClaim) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Claim")
            .setMessage(
                "Are you sure you want to cancel this claim of " +
                        "${claim.quantityClaimed} ${claim.unit}?"
            )
            .setNegativeButton("Keep Claim", null)
            .setPositiveButton("Cancel Claim") { _, _ ->
                cancelClaim(claim)
            }
            .show()
    }

    private fun cancelClaim(claim: DonationClaim) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                claimService.cancelClaim(claim.id)

                Toast.makeText(
                    requireContext(),
                    "Claim cancelled successfully.",
                    Toast.LENGTH_SHORT
                ).show()

                loadMyClaims()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.localizedMessage ?: "Failed to cancel claim.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvMyClaims.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.rvMyClaims.visibility = View.GONE
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
    }

    private fun showContentState(claims: List<DonationClaim>) {
        binding.progressBar.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.rvMyClaims.visibility = View.VISIBLE

        binding.rvMyClaims.adapter = ClaimAdapter(claims) { selectedClaim ->
            confirmCancelClaim(selectedClaim)
        }
    }

    private fun showErrorState(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvMyClaims.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
