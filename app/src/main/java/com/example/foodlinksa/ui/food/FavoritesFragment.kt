package com.example.foodlinksa.ui.food

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
import com.example.foodlinksa.data.api.RetrofitClient
import com.example.foodlinksa.data.model.Meal
import com.example.foodlinksa.data.repository.FoodRepository
import com.example.foodlinksa.databinding.FragmentFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var repository: FoodRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        repository = FoodRepository(RetrofitClient.apiService, null)

        binding.rvFavorites.layoutManager = LinearLayoutManager(context)
        loadFavorites()
    }

    private fun loadFavorites() {
        val userId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val favoriteIds = document.get("favorites") as? List<String> ?: listOf()
                if (favoriteIds.isEmpty()) {
                    binding.progressBar.visibility = View.GONE
                    binding.rvFavorites.adapter = MealAdapter(listOf()) {}
                    return@addOnSuccessListener
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val meals = mutableListOf<Meal>()
                    for (id in favoriteIds) {
                        val detail = repository.getMealDetails(id)
                        detail?.let {
                            meals.add(Meal(it.name, it.imageUrl, it.id))
                        }
                    }
                    binding.progressBar.visibility = View.GONE
                    binding.rvFavorites.adapter = MealAdapter(meals) { meal ->
                        val bundle = bundleOf("mealId" to meal.id, "mealName" to meal.name)
                        findNavController().navigate(R.id.action_favoritesFragment_to_mealDetailsFragment, bundle)
                    }
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
