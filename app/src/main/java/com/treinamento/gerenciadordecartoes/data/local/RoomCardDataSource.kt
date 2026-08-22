package com.treinamento.gerenciadordecartoes.data.local

import com.treinamento.gerenciadordecartoes.data.MockCardData
import com.treinamento.gerenciadordecartoes.model.Card
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus
import com.treinamento.gerenciadordecartoes.model.Purchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCardDataSource(private val dao: CardDao) {
    fun observeCards(userId: String): Flow<List<Card>> =
        dao.observeCards(userId).map { list -> list.map(CardLocalEntity::toModel) }

    fun observePurchases(userId: String, cardId: String): Flow<List<Purchase>> =
        dao.observePurchases(userId, cardId).map { list -> list.map(PurchaseLocalEntity::toModel) }

    suspend fun seedIfEmpty(userId: String) {
        if (dao.cardCount(userId) != 0) return
        dao.seed(
            MockCardData.cards.map { CardLocalEntity.fromModel(userId, it) },
            MockCardData.purchases.map { PurchaseLocalEntity.fromModel(userId, it) },
        )
    }

    suspend fun cacheRemoteCards(userId: String, cards: List<Card>) {
        cards.forEach { card ->
            if (dao.pendingCount(userId, card.id) == 0) {
                dao.upsertCard(CardLocalEntity.fromModel(userId, card))
            }
        }
    }

    suspend fun cacheRemotePurchases(userId: String, purchases: List<Purchase>) =
        dao.upsertPurchases(purchases.map { PurchaseLocalEntity.fromModel(userId, it) })

    suspend fun addCard(userId: String, card: Card) =
        dao.upsertCard(CardLocalEntity.fromModel(userId, card))

    suspend fun updateLimit(userId: String, cardId: String, limit: Double) =
        dao.updateLimit(userId, cardId, limit)

    suspend fun updateBlockStatus(userId: String, cardId: String, status: CardBlockStatus) =
        dao.updateBlockStatus(userId, cardId, status.firebaseValue)

    suspend fun enqueue(userId: String, type: String, cardId: String) =
        dao.enqueue(PendingOperationEntity(userId = userId, type = type, cardId = cardId))

    suspend fun pending(userId: String) = dao.pendingOperations(userId)
    suspend fun card(userId: String, cardId: String) = dao.getCard(userId, cardId)?.toModel()
    suspend fun markSynced(operationId: Long) = dao.deletePending(operationId)
}
