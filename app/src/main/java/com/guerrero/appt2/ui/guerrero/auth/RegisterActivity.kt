package com.guerrero.appt2.ui.guerrero.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.guerrero.appt2.R
import com.google.firebase.firestore.FirebaseFirestore
import com.guerrero.appt2.data.guerrero.repository.AuthRepository
import com.guerrero.appt2.databinding.ActivityRegisterBinding
import com.guerrero.appt2.ui.guerrero.mascota.MascotaListActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa Firebase Auth y Firestore
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // Inicializa el repositorio de autenticación
        val authRepository = AuthRepository(firebaseAuth, firestore)

        // Inicializa el ViewModel con una Factory
        registerViewModel = ViewModelProvider(this, RegisterViewModelFactory(authRepository))
            .get(RegisterViewModel::class.java)

        // Observa el resultado del intento de registro
        registerViewModel.registerResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show()
                navigateToPetList()
            }.onFailure { exception ->
                Toast.makeText(this, getString(R.string.register_failure, exception.message), Toast.LENGTH_LONG).show()
            }
        }

        // Configura el listener para el botón de registro
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString().trim()
            val confirmPassword = binding.etConfirmPasswordRegister.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, R.string.register_empty_fields_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, R.string.register_password_mismatch_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, R.string.register_password_length_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerViewModel.register(email, password)
        }

        // Configura el listener para el texto de inicio de sesión
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun navigateToPetList() {
        val intent = Intent(this, MascotaListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

class RegisterViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}