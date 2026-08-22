package com.treinamento.gerenciadordecartoes.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
}
