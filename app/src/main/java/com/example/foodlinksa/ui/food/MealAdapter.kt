package com.example.foodlinksa.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.foodlinksa.data.model.Meal
import com.example.foodlinksa.databinding.ItemMealBinding

class MealAdapter(
    private val meals: List<Meal>,
    private val onItemClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    class MealViewHolder(val binding: ItemMealBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.binding.tvMealName.text = meal.name
        holder.binding.ivMealThumb.load(meal.imageUrl)
        
        holder.itemView.setOnClickListener { onItemClick(meal) }
    }

    override fun getItemCount(): Int = meals.size
}
