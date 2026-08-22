package com.treinamento.gerenciadordecartoes.repository

import com.treinamento.gerenciadordecartoes.model.Card
import com.treinamento.gerenciadordecartoes.model.CardRequest
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus
import com.treinamento.gerenciadordecartoes.model.Purchase
import kotlinx.coroutines.flow.Flow

/** Contrato pronto para receber uma implementação Room/SQLite ou remota. */
interface CardRepository {
    fun observeCards(): Flow<List<Card>>
    fun observePurchases(cardId: String): Flow<List<Purchase>>
    suspend fun requestCard(request: CardRequest): Result<Unit>
    suspend fun setCardBlockStatus(cardId: String, status: CardBlockStatus): Result<Unit>
    suspend fun updateLimit(cardId: String, newLimit: Double): Result<Unit>
}
