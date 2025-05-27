package com.guerrero.appt2.data.guerrero.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.guerrero.appt2.data.guerrero.model.Mascota
import com.guerrero.appt2.data.guerrero.network.ImgBBService
import com.guerrero.appt2.data.guerrero.network.NetworkModule
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MascotaRepository(
    private val firestore: FirebaseFirestore,
    private val imgBBService: ImgBBService = NetworkModule.provideImgBBService()
) {

    private val petsCollection = firestore.collection("pets")

    suspend fun addPet(pet: Mascota, imageFile: File? = null): Result<Boolean> {
        return try {
            var petToSave = pet
            if (imageFile != null) {
                // Subir imagen a ImgBB
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
                val response = imgBBService.uploadImage(
                    apiKey = NetworkModule.getApiKey(),
                    image = imagePart,
                    expiration = 15552000 // 6 meses en segundos
                )
                if (response.success) {
                    petToSave = pet.copy(
                        imageUrl = response.data.url,
                        deleteUrl = response.data.delete_url
                    )
                } else {
                    throw Exception("Error uploading image to ImgBB")
                }
            }
            petsCollection.add(petToSave).await()
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun getPetsForOwner(ownerId: String): Flow<Result<List<Mascota>>> = callbackFlow {
        val subscription = petsCollection
            .whereEqualTo("ownerId", ownerId)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("MascotaRepository", "Error en SnapshotListener: ${e.message}", e)
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val pets = snapshot.documents.mapNotNull { document ->
                        document.toObject(Mascota::class.java)?.apply {
                            this.id = document.id
                        }
                    }
                    Log.d("MascotaRepository", "Mascotas obtenidas: ${pets.size}")
                    trySend(Result.success(pets))
                } else {
                    Log.w("MascotaRepository", "Snapshot es nulo")
                    trySend(Result.success(emptyList()))
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun updatePet(pet: Mascota, imageFile: File? = null, oldDeleteUrl: String? = null): Result<Boolean> {
        return try {
            var petToSave = pet
            if (imageFile != null) {
                // Subir nueva imagen a ImgBB
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
                val response = imgBBService.uploadImage(
                    apiKey = NetworkModule.getApiKey(),
                    image = imagePart,
                    expiration = 15552000
                )
                if (response.success) {
                    petToSave = pet.copy(
                        imageUrl = response.data.url,
                        deleteUrl = response.data.delete_url
                    )
                    // Eliminar la imagen antigua si existe
                    oldDeleteUrl?.let { deleteImage(it) }
                } else {
                    throw Exception("Error uploading image to ImgBB")
                }
            }
            petsCollection.document(pet.id).set(petToSave).await()
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deletePet(petId: String, deleteUrl: String? = null): Result<Boolean> {
        return try {
            // Eliminar la imagen de ImgBB si existe
            deleteUrl?.let { deleteImage(it) }
            petsCollection.document(petId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun deleteImage(deleteUrl: String) {
        // ImgBB usa una solicitud GET a la delete_url para eliminar la imagen
        // No se necesita una implementación compleja, ya que la URL ejecuta la eliminación
        try {
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder().url(deleteUrl).build()
            client.newCall(request).execute()
        } catch (e: Exception) {
            Log.e("MascotaRepository", "Error deleting image: ${e.message}", e)
        }
    }
}