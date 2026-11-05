package com.example.foodlinksa.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.foodlinksa.R
import com.example.foodlinksa.data.api.RetrofitClient
import com.example.foodlinksa.data.repository.FoodRepository
import com.example.foodlinksa.databinding.FragmentFoodListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FoodListFragment : Fragment() {

    private var _binding: FragmentFoodListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FoodViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val foodCache = com.example.foodlinksa.data.local.FoodCache(
            requireContext()
        )

        val repository = FoodRepository(
            RetrofitClient.apiService,
            foodCache
        )

        val factory = object : ViewModelProvider.Factory {

            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>
            ): T {
                @Suppress("UNCHECKED_CAST")
                return FoodViewModel(repository) as T
            }
        }

        viewModel = ViewModelProvider(this, factory)[
            FoodViewModel::class.java
        ]

        setupMenu()
        setupUI()
        observeViewModel()
        loadMealsForUser()
    }

    private fun setupUI() {

        // Category filters
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->

            val chipId = checkedIds.firstOrNull()

            if (chipId != null) {

                val category = when (chipId) {
                    R.id.chipSeafood -> "Seafood"
                    R.id.chipBeef -> "Beef"
                    R.id.chipChicken -> "Chicken"
                    R.id.chipDessert -> "Dessert"
                    else -> "Seafood"
                }

                viewModel.fetchMeals(category)
            }
        }

        // Meal search
        binding.etSearch.doAfterTextChanged { text ->

            val query = text.toString().trim()

            if (query.length > 2) {
                viewModel.searchMeals(query)
            } else if (query.isEmpty()) {
                loadMealsForUser()
            }
        }
    }

    private fun loadMealsForUser() {

        val userId = auth.currentUser?.uid

        if (userId != null) {

            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    val preferredCategory =
                        document.getString("preferred_category")
                            ?: "Seafood"

                    viewModel.fetchMeals(preferredCategory)

                    when (preferredCategory) {
                        "Seafood" -> binding.chipSeafood.isChecked = true
                        "Beef" -> binding.chipBeef.isChecked = true
                        "Chicken" -> binding.chipChicken.isChecked = true
                        "Dessert" -> binding.chipDessert.isChecked = true
                    }
                }
                .addOnFailureListener {
                    viewModel.fetchMeals("Seafood")
                }

        } else {
            viewModel.fetchMeals("Seafood")
        }
    }

    private fun observeViewModel() {

        viewModel.meals.observe(viewLifecycleOwner) { meals ->

            meals?.let {

                binding.rvMeals.adapter = MealAdapter(it) { meal ->

                    val bundle = bundleOf(
                        "mealId" to meal.id,
                        "mealName" to meal.name
                    )

                    findNavController().navigate(
                        R.id.action_foodListFragment_to_mealDetailsFragment,
                        bundle
                    )
                }
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->

            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupMenu() {

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            object : MenuProvider {

                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.main_menu, menu)
                }

                override fun onMenuItemSelected(
                    menuItem: MenuItem
                ): Boolean {

                    return when (menuItem.itemId) {

                        R.id.action_favorites -> {
                            findNavController().navigate(
                                R.id.action_foodListFragment_to_favoritesFragment
                            )
                            true
                        }

                        R.id.action_volunteer -> {
                            findNavController().navigate(
                                R.id.volunteerFragment
                            )
                            true
                        }

                        R.id.action_settings -> {
                            findNavController().navigate(
                                R.id.action_foodListFragment_to_settingsFragment
                            )
                            true
                        }

                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}