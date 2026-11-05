package com.example.foodlinksa.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.foodlinksa.R
import com.example.foodlinksa.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val languages = arrayOf("English", "isiZulu")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages)
        binding.spinnerLanguage.adapter = adapter

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (currentUser != null && userId != null) {
            binding.tvUserEmail.text = currentUser.email
            
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        binding.etDisplayName.setText(document.getString("display_name"))
                        binding.etCategory.setText(document.getString("preferred_category"))
                        val lang = document.getString("language") ?: "en"
                        if (lang == "zu") {
                            binding.spinnerLanguage.setSelection(1)
                        } else {
                            binding.spinnerLanguage.setSelection(0)
                        }
                    }
                }
        } else {
            binding.tvUserEmail.text = "Not logged in"
        }

        binding.btnSaveSettings.setOnClickListener {
            if (userId != null) {
                val newName = binding.etDisplayName.text.toString().trim()
                val newCategory = binding.etCategory.text.toString().trim()
                val selectedLangCode = if (binding.spinnerLanguage.selectedItemPosition == 1) "zu" else "en"

                val updates = hashMapOf(
                    "display_name" to newName,
                    "preferred_category" to newCategory,
                    "language" to selectedLangCode
                )

                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(selectedLangCode)
                )

                db.collection("users").document(userId).set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
