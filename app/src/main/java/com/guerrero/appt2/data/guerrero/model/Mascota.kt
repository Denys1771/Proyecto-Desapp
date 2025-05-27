package com.guerrero.appt2.data.guerrero.model

import com.google.firebase.firestore.DocumentId
data class Mascota (
    @DocumentId var id: String = "",
    var name: String = "",
    var type: String = "", // Ej: "Perro", "Gato"
    var age: Int = 0,
    var ownerId: String = "", // Para asociar la mascota con un usuario específico
    var imageUrl: String? = null, // URL de la imagen
    var deleteUrl: String? = null // URL para eliminar la imagen
)