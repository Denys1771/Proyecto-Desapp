package com.guerrero.appt2.utils.guerrero

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class AuthManager(private val firebaseAuth: FirebaseAuth) {

    /**
     * Obtiene el usuario de Firebase actualmente autenticado.
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Observa los cambios en el estado de autenticación.
     * @param listener Un callback que se invoca cada vez que el estado de autenticación cambia.
     * @return Un objeto AuthStateListener que puede ser usado para remover el listener.
     */
    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth.addAuthStateListener(listener)
    }

    /**
     * Remueve un listener de estado de autenticación.
     * @param listener El AuthStateListener a remover.
     */
    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth.removeAuthStateListener(listener)
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun signOut() {
        firebaseAuth.signOut()
    }
}