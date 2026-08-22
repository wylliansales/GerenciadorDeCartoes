package com.treinamento.gerenciadordecartoes.repository

import com.treinamento.gerenciadordecartoes.model.AuthenticatedUser

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    fun currentUser(): AuthenticatedUser?
    fun logout()
}
