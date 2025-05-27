package com.guerrero.appt2.ui.guerrero.mascota

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guerrero.appt2.data.guerrero.model.Mascota
import com.guerrero.appt2.data.guerrero.repository.AuthRepository
import com.guerrero.appt2.data.guerrero.repository.MascotaRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MascotaListViewModel(
    private val petRepository: MascotaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _pets = MutableLiveData<Result<List<Mascota>>>()
    val pets: LiveData<Result<List<Mascota>>> = _pets

    private val _deleteResult = MutableLiveData<Result<Boolean>>()
    val deleteResult: LiveData<Result<Boolean>> = _deleteResult

    private val _isUserNotAuthenticated = MutableLiveData<Boolean>()
    val isUserNotAuthenticated: LiveData<Boolean> = _isUserNotAuthenticated

    init {
        loadPets()
    }

    fun loadPets() {
        val currentUserId = authRepository.getCurrentUserId()
        Log.d("PetListViewModel", "Cargando mascotas para ownerId: $currentUserId")

        if (currentUserId == null) {
            _isUserNotAuthenticated.value = true
            _pets.value = Result.failure(Exception("User not authenticated"))
            Log.e("PetListViewModel", "Usuario no autenticado al cargar mascotas.")
            return
        }

        viewModelScope.launch {
            petRepository.getPetsForOwner(currentUserId).collectLatest { result ->
                result.onSuccess { pets ->
                    Log.d("PetListViewModel", "Flow emitió SUCCESS: ${pets.size} mascotas.")
                    _pets.postValue(Result.success(pets))
                }.onFailure { exception ->
                    Log.e("PetListViewModel", "Flow emitió FAILURE: ${exception.message}", exception)
                    _pets.postValue(Result.failure(exception))
                }
            }
        }
    }

    fun deletePet(petId: String, deleteUrl: String?) {
        viewModelScope.launch {
            val result = petRepository.deletePet(petId, deleteUrl)
            result.onSuccess {
                Log.d("PetListViewModel", "Mascota eliminada exitosamente: $petId")
            }.onFailure { exception ->
                Log.e("PetListViewModel", "Error al eliminar mascota: ${exception.message}", exception)
            }
            _deleteResult.postValue(result)
        }
    }

    fun refreshPets() {
        loadPets()
    }
}