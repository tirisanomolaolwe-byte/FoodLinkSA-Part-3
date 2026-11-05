
package com.example.foodlinksa.ui.donation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodlinksa.data.model.DonationClaim
import com.example.foodlinksa.data.model.ClaimStatus
import com.example.foodlinksa.databinding.ItemClaimBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClaimAdapter(
    private val claims: List<DonationClaim>,
    private val onCancelClick: (DonationClaim) -> Unit
) : RecyclerView.Adapter<ClaimAdapter.ClaimViewHolder>() {

    class ClaimViewHolder(
        val binding: ItemClaimBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClaimViewHolder {
        val binding = ItemClaimBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClaimViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ClaimViewHolder,
        position: Int
    ) {
        val claim = claims[position]
        val binding = holder.binding

        binding.tvClaimDonationId.text =
            "Donation reference: ${claim.donationId}"

        binding.tvClaimQuantity.text =
            "Quantity claimed: ${claim.quantityClaimed} ${claim.unit}"

        binding.tvClaimStatus.text =
            "Status: ${claim.status.replace('_', ' ')}"

        val formattedDate = if (claim.createdAt > 0) {
            SimpleDateFormat(
                "dd MMM yyyy, HH:mm",
                Locale.getDefault()
            ).format(Date(claim.createdAt))
        } else {
            "Date unavailable"
        }

        binding.tvClaimDate.text = "Claimed on: $formattedDate"

        binding.btnCancelClaim.visibility =
            if (claim.status == ClaimStatus.RESERVED) {
                View.VISIBLE
            } else {
                View.GONE
            }

        binding.btnCancelClaim.setOnClickListener {
            onCancelClick(claim)
        }
    }

    override fun getItemCount(): Int = claims.size
}
