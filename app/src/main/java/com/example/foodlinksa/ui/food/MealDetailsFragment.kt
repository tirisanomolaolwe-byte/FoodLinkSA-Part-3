package com.example.foodlinksa.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.foodlinksa.data.api.RetrofitClient
import com.example.foodlinksa.data.repository.FoodRepository
import com.example.foodlinksa.databinding.FragmentMealDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MealDetailsFragment : Fragment() {

    private var _binding: FragmentMealDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: FoodRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMealDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = FoodRepository(RetrofitClient.apiService, null)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val mealId = arguments?.getString("mealId") ?: return
        val mealName = arguments?.getString("mealName") ?: ""

        binding.tvMealTitle.text = mealName
        
        fetchMealDetails(mealId)
        setupFavoriteFab(mealId)
    }

    private fun fetchMealDetails(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val meal = repository.getMealDetails(id)
            meal?.let {
                binding.tvMealTitle.text = it.name
                binding.tvCategoryArea.text = "${it.category} | ${it.area}"
                binding.tvInstructions.text = it.instructions
                binding.tvIngredients.text = it.getIngredients().joinToString("\n")
                binding.ivMealLarge.load(it.imageUrl)
            }
        }
    }

    private fun setupFavoriteFab(id: String) {
        val userId = auth.currentUser?.uid ?: return
        
        binding.fabFavorite.setOnClickListener {
            db.collection("users").document(userId)
                .update("favorites", FieldValue.arrayUnion(id))
                .addOnSuccessListener {
                    Toast.makeText(context, "Added to favorites!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // If field doesn't exist, create it
                    db.collection("users").document(userId)
                        .set(mapOf("favorites" to listOf(id)), com.google.firebase.firestore.SetOptions.merge())
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
