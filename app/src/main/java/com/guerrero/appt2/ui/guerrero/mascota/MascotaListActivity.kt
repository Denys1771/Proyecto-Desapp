package com.guerrero.appt2.ui.guerrero.mascota

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.guerrero.appt2.R
import com.google.firebase.firestore.FirebaseFirestore
import com.guerrero.appt2.data.guerrero.repository.AuthRepository
import com.guerrero.appt2.data.guerrero.repository.MascotaRepository
import com.guerrero.appt2.databinding.ActivityMascotaListBinding
import com.guerrero.appt2.ui.guerrero.auth.LoginActivity


class MascotaListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMascotaListBinding
    private lateinit var petListViewModel: MascotaListViewModel
    private lateinit var adapter: MascotaAdapter // Tipo explícito añadido

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMascotaListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val authRepository = AuthRepository(firebaseAuth, firestore) // Corregido el nombre
        val petRepository = MascotaRepository(firestore)

        petListViewModel = ViewModelProvider(
            this,
            MascotaListViewModelFactory(petRepository, authRepository)
        ).get(MascotaListViewModel::class.java)

        setupRecyclerView()

        petListViewModel.pets.observe(this) { result ->
            result.onSuccess { pets ->
                adapter.submitList(pets)
                if (pets.isEmpty()) {
                    Toast.makeText(this, R.string.pet_list_empty, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { exception ->
                Toast.makeText(this, getString(R.string.pet_list_load_error, exception.message), Toast.LENGTH_LONG).show()
            }
        }

        petListViewModel.deleteResult.observe(this) { result ->
            result.onFailure { exception ->
                Toast.makeText(this, getString(R.string.pet_list_delete_error, exception.message), Toast.LENGTH_LONG).show()
            }
        }

        petListViewModel.isUserNotAuthenticated.observe(this) { isNotAuthenticated ->
            if (isNotAuthenticated) {
                navigateToLogin()
            }
        }

        binding.btnCerrarSesion.setOnClickListener {
            authRepository.logoutUser()
            Toast.makeText(this, R.string.pet_list_logout_success, Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }

        binding.fabAddPet.setOnClickListener {
            val intent = Intent(this, MascotaAddEditActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        petListViewModel.refreshPets()
    }

    private fun setupRecyclerView() {
        adapter = MascotaAdapter(
            onEditClick = { pet ->
                val intent = Intent(this, MascotaAddEditActivity::class.java).apply {
                    putExtra("PET_ID", pet.id)
                    putExtra("PET_NAME", pet.name)
                    putExtra("PET_TYPE", pet.type)
                    putExtra("PET_AGE", pet.age)
                    putExtra("PET_IMAGE_URL", pet.imageUrl)
                    putExtra("PET_DELETE_URL", pet.deleteUrl)
                }
                startActivity(intent)
            },
            onDeleteClick = { pet ->
                petListViewModel.deletePet(pet.id, pet.deleteUrl)
            }
        )
        binding.rvPets.layoutManager = LinearLayoutManager(this)
        binding.rvPets.adapter = adapter
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

class MascotaListViewModelFactory(
    private val petRepository: MascotaRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MascotaListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MascotaListViewModel(petRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}