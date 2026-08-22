package com.treinamento.gerenciadordecartoes.data

import com.treinamento.gerenciadordecartoes.model.Card
import com.treinamento.gerenciadordecartoes.model.CardRequest
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus
import com.treinamento.gerenciadordecartoes.model.Purchase
import com.treinamento.gerenciadordecartoes.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Dados em memória. Troque esta classe por LocalCardRepository ao adicionar Room. */
class MockCardRepository : CardRepository {
    private val cards = MutableStateFlow(MockCardData.cards)
    private val purchases = MutableStateFlow(MockCardData.purchases)

    override fun observeCards(): Flow<List<Card>> = cards
    override fun observePurchases(cardId: String): Flow<List<Purchase>> =
        purchases.map { list -> list.filter { it.cardId == cardId } }

    override suspend fun requestCard(request: CardRequest): Result<Unit> = Result.success(Unit)

    override suspend fun setCardBlockStatus(cardId: String, status: CardBlockStatus): Result<Unit> {
        cards.value = cards.value.map { if (it.id == cardId) it.copy(blockStatus = status) else it }
        return Result.success(Unit)
    }

    override suspend fun updateLimit(cardId: String, newLimit: Double): Result<Unit> {
        if (newLimit <= 0) return Result.failure(IllegalArgumentException("O limite deve ser maior que zero."))
        cards.value = cards.value.map { if (it.id == cardId) it.copy(limit = newLimit) else it }
        return Result.success(Unit)
    }
}
