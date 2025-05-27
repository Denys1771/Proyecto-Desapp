package com.guerrero.appt2.ui.guerrero.mascota

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guerrero.appt2.data.guerrero.model.Mascota
import com.guerrero.appt2.data.guerrero.repository.AuthRepository
import com.guerrero.appt2.data.guerrero.repository.MascotaRepository
import kotlinx.coroutines.launch
import java.io.File

class MascotaAddEditViewModel(
    private val petRepository: MascotaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _saveResult = MutableLiveData<Result<Boolean>>()
    val saveResult: LiveData<Result<Boolean>> = _saveResult

    fun savePet(pet: Mascota, imageFile: File? = null) {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null) {
            _saveResult.postValue(Result.failure(Exception("User not authenticated. Cannot save pet.")))
            return
        }

        val petToSave = pet.copy(ownerId = currentUserId)

        viewModelScope.launch {
            val result = if (petToSave.id.isEmpty()) {
                petRepository.addPet(petToSave, imageFile)
            } else {
                petRepository.updatePet(petToSave, imageFile, pet.deleteUrl)
            }
            _saveResult.postValue(result)
        }
    }
}