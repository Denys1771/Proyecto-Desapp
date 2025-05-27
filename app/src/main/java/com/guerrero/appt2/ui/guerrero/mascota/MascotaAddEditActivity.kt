package com.guerrero.appt2.ui.guerrero.mascota

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.guerrero.appt2.data.guerrero.model.Mascota
import com.guerrero.appt2.R
import com.guerrero.appt2.data.guerrero.repository.AuthRepository

import com.guerrero.appt2.data.guerrero.repository.MascotaRepository
import com.guerrero.appt2.databinding.ActivityMascotaAddEditBinding
import java.io.File


class MascotaAddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMascotaAddEditBinding
    private lateinit var addEditPetViewModel: MascotaAddEditViewModel
    private var petId: String? = null
    private var selectedImageUri: Uri? = null
    private var existingImageUrl: String? = null
    private var existingDeleteUrl: String? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Validar tamaño de la imagen (límite de ImgBB: 32 MB)
            val inputStream = contentResolver.openInputStream(it)
            val size = inputStream?.available() ?: 0
            inputStream?.close()
            if (size > 32 * 1024 * 1024) {
                Toast.makeText(this, "La imagen excede el límite de 32 MB", Toast.LENGTH_LONG).show()
                return@let
            }
            selectedImageUri = it
            binding.btnSelectImage.text = getString(R.string.pet_image_selected)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getContent.launch("image/*")
        } else {
            Toast.makeText(this, R.string.pet_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMascotaAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val authRepository = AuthRepository(firebaseAuth, firestore)
        val petRepository = MascotaRepository(firestore)

        addEditPetViewModel = ViewModelProvider(
            this,
            AddEditPetViewModelFactory(petRepository, authRepository)
        ).get(MascotaAddEditViewModel::class.java)

        petId = intent.getStringExtra("PET_ID")
        if (petId != null) {
            binding.tvTitle.setText(R.string.pet_edit_title)
            binding.etPetName.setText(intent.getStringExtra("PET_NAME"))
            binding.etPetType.setText(intent.getStringExtra("PET_TYPE"))
            binding.etPetAge.setText(intent.getIntExtra("PET_AGE", 0).toString())
            existingImageUrl = intent.getStringExtra("PET_IMAGE_URL")
            existingDeleteUrl = intent.getStringExtra("PET_DELETE_URL")
        } else {
            binding.tvTitle.setText(R.string.pet_add_title)
        }

        addEditPetViewModel.saveResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, R.string.pet_save_success, Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, getString(R.string.pet_save_failure, exception.message), Toast.LENGTH_LONG).show()
            }
        }

        binding.btnSelectImage.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                getContent.launch("image/*")
            }
        }

        binding.btnSavePet.setOnClickListener {
            savePet()
        }
    }

    private fun savePet() {
        val name = binding.etPetName.text.toString().trim()
        val type = binding.etPetType.text.toString().trim()
        val ageString = binding.etPetAge.text.toString().trim()

        if (name.isEmpty() || type.isEmpty() || ageString.isEmpty()) {
            Toast.makeText(this, R.string.pet_empty_fields_error, Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageString.toIntOrNull()
        if (age == null || age <= 0) {
            Toast.makeText(this, R.string.pet_invalid_age_error, Toast.LENGTH_SHORT).show()
            return
        }

        val pet = Mascota(
            id = petId ?: "",
            name = name,
            type = type,
            age = age,
            ownerId = "",
            imageUrl = existingImageUrl,
            deleteUrl = existingDeleteUrl
        )

        val imageFile = selectedImageUri?.let { uri ->
            val file = File(cacheDir, "pet_image_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        }

        addEditPetViewModel.savePet(pet, imageFile)
    }
}

class AddEditPetViewModelFactory(
    private val petRepository: MascotaRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MascotaAddEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MascotaAddEditViewModel(petRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}