package com.treinamento.gerenciadordecartoes.data

import com.treinamento.gerenciadordecartoes.model.Card
import com.treinamento.gerenciadordecartoes.model.CardRequest
import com.treinamento.gerenciadordecartoes.model.Purchase
import com.treinamento.gerenciadordecartoes.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Dados em memória. Troque esta classe por LocalCardRepository ao adicionar Room. */
class MockCardRepository : CardRepository {
    private val cards = MutableStateFlow(
        listOf(
            Card("1", "Alex Silva", "4582", "Visa Platinum", 8_000.0, 2_340.75, 12),
            Card("2", "Alex Silva", "9017", "Mastercard Gold", 4_500.0, 890.20, 5, true),
        )
    )
    private val purchases = MutableStateFlow(
        listOf(
            Purchase("p1", "1", "Supermercado Central", "Hoje, 10:42", 186.90, "Alimentação"),
            Purchase("p2", "1", "StreamPlay", "15 ago", 39.90, "Assinaturas"),
            Purchase("p3", "1", "Posto Avenida", "13 ago", 250.00, "Transporte"),
            Purchase("p4", "2", "Livraria Horizonte", "10 ago", 74.50, "Compras"),
        )
    )

    override fun observeCards(): Flow<List<Card>> = cards
    override fun observePurchases(cardId: String): Flow<List<Purchase>> =
        purchases.map { list -> list.filter { it.cardId == cardId } }

    override suspend fun requestCard(request: CardRequest): Result<Unit> = Result.success(Unit)

    override suspend fun setCardBlocked(cardId: String, blocked: Boolean): Result<Unit> {
        cards.value = cards.value.map { if (it.id == cardId) it.copy(isBlocked = blocked) else it }
        return Result.success(Unit)
    }

    override suspend fun updateLimit(cardId: String, newLimit: Double): Result<Unit> {
        if (newLimit <= 0) return Result.failure(IllegalArgumentException("O limite deve ser maior que zero."))
        cards.value = cards.value.map { if (it.id == cardId) it.copy(limit = newLimit) else it }
        return Result.success(Unit)
    }
}
