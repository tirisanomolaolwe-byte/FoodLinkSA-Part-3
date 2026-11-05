package com.example.foodlinksa.ui.donation

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodlinksa.R
import com.example.foodlinksa.data.model.ClaimStatus
import com.example.foodlinksa.data.model.DonationClaim
import com.google.android.material.button.MaterialButton
import java.util.Date

class DonorClaimAdapter(
    private val claims: List<DonationClaim>,
    private val onConfirmCollection: (DonationClaim) -> Unit
) : RecyclerView.Adapter<DonorClaimAdapter.DonorClaimViewHolder>() {

    class DonorClaimViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val recipientName: TextView =
            view.findViewById(R.id.tvRecipientName)

        val quantity: TextView =
            view.findViewById(R.id.tvClaimQuantity)

        val status: TextView =
            view.findViewById(R.id.tvClaimStatus)

        val date: TextView =
            view.findViewById(R.id.tvClaimDate)

        val confirmButton: MaterialButton =
            view.findViewById(R.id.btnConfirmCollection)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DonorClaimViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donor_claim, parent, false)

        return DonorClaimViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DonorClaimViewHolder,
        position: Int
    ) {
        val claim = claims[position]

        holder.recipientName.text =
            claim.recipientName.ifBlank { "Recipient" }

        holder.quantity.text =
            "${claim.quantityClaimed} ${claim.unit} claimed"

        holder.status.text = claim.status

        val date = if (claim.createdAt > 0) {
            DateFormat.format("MMM dd, yyyy HH:mm", Date(claim.createdAt))
                .toString()
        } else {
            "N/A"
        }

        holder.date.text = "Claimed on: $date"

        val canConfirm = claim.status == ClaimStatus.RESERVED

        holder.confirmButton.visibility =
            if (canConfirm) View.VISIBLE else View.GONE

        holder.confirmButton.setOnClickListener {
            onConfirmCollection(claim)
        }
    }

    override fun getItemCount(): Int = claims.size
}