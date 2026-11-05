package com.example.foodlinksa.ui.donation

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodlinksa.data.model.Donation
import com.example.foodlinksa.databinding.ItemDonationBinding
import java.util.Date
import java.util.Locale

class DonationAdapter(
    private val donations: List<Donation>,
    private val onItemClick: (Donation) -> Unit
) : RecyclerView.Adapter<DonationAdapter.DonationViewHolder>() {

    inner class DonationViewHolder(private val binding: ItemDonationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(donation: Donation) {
            binding.tvTitle.text = donation.title
            binding.tvCategory.text = donation.category
            binding.tvDescription.text = donation.description.ifBlank { "No description provided." }
            binding.tvQuantity.text = "Available: ${donation.availableQuantity} ${donation.unit}"
            binding.tvPickupAddress.text = "📍 ${donation.pickupAddress}"
            
            val formattedDeadline = if (donation.collectionDeadline > 0) {
                DateFormat.format("MMM dd, yyyy HH:mm", Date(donation.collectionDeadline)).toString()
            } else {
                "No deadline"
            }
            binding.tvCollectionDeadline.text = "⏰ Collect by: $formattedDeadline"
            binding.chipStatus.text = donation.status.uppercase(Locale.ROOT)

            binding.root.setOnClickListener {
                onItemClick(donation)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val binding = ItemDonationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DonationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        holder.bind(donations[position])
    }

    override fun getItemCount(): Int = donations.size
}
