package com.treinamento.gerenciadordecartoes.repository

import com.google.firebase.auth.FirebaseAuth
import com.treinamento.gerenciadordecartoes.data.local.PendingOperationEntity
import com.treinamento.gerenciadordecartoes.data.local.RoomCardDataSource
import com.treinamento.gerenciadordecartoes.data.remote.FirebaseCardDataSource
import com.treinamento.gerenciadordecartoes.model.Card
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus
import com.treinamento.gerenciadordecartoes.model.CardRequest
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/** Room é a fonte de verdade; Firestore sincroniza quando a rede está disponível. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class OfflineFirstCardRepository(
    private val local: RoomCardDataSource,
    private val remote: FirebaseCardDataSource = FirebaseCardDataSource(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : CardRepository {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun observeCards(): Flow<List<Card>> = userIdFlow().flatMapLatest { userId ->
        if (userId == null) return@flatMapLatest flowOf(emptyList())
        channelFlow {
            local.seedIfEmpty(userId)
            syncScope.launch { synchronize(userId) }
            launch {
                remote.observeCards()
                    .catch { /* Room continua disponível enquanto o Firebase está offline. */ }
                    .collect { cards ->
                        local.cacheRemoteCards(userId, cards)
                        synchronize(userId)
                    }
            }
            local.observeCards(userId).collect { send(it) }
        }
    }

    override fun observePurchases(cardId: String) = userIdFlow().flatMapLatest { userId ->
        if (userId == null) return@flatMapLatest flowOf(emptyList())
        channelFlow {
            launch {
                remote.observePurchases(cardId)
                    .catch { /* Mantém as compras locais em caso de falha de rede. */ }
                    .collect { purchases -> local.cacheRemotePurchases(userId, purchases) }
            }
            local.observePurchases(userId, cardId).collect { send(it) }
        }
    }

    override suspend fun requestCard(request: CardRequest): Result<Unit> = runCatching {
        val userId = requireUid()
        val card = Card(
            id = UUID.randomUUID().toString(),
            holderName = request.holderName,
            lastFourDigits = (1000..9999).random().toString(),
            brand = request.cardType,
            limit = request.requestedLimit,
            usedLimit = 0.0,
            dueDay = 10,
        )
        local.addCard(userId, card)
        local.enqueue(userId, PendingOperationEntity.UPSERT_CARD, card.id)
        syncScope.launch { synchronize(userId) }
    }

    override suspend fun setCardBlockStatus(
        cardId: String,
        status: CardBlockStatus,
    ): Result<Unit> = runCatching {
        val userId = requireUid()
        local.updateBlockStatus(userId, cardId, status)
        local.enqueue(userId, PendingOperationEntity.UPDATE_BLOCK_STATUS, cardId)
        syncScope.launch { synchronize(userId) }
    }

    override suspend fun updateLimit(cardId: String, newLimit: Double): Result<Unit> = runCatching {
        require(newLimit > 0) { "O limite deve ser maior que zero." }
        val userId = requireUid()
        local.updateLimit(userId, cardId, newLimit)
        local.enqueue(userId, PendingOperationEntity.UPDATE_LIMIT, cardId)
        syncScope.launch { synchronize(userId) }
    }

    private suspend fun synchronize(userId: String) {
        if (auth.currentUser?.uid != userId) return
        local.pending(userId).forEach { operation ->
            val card = local.card(userId, operation.cardId) ?: return@forEach
            val result = runCatching {
                when (operation.type) {
                    PendingOperationEntity.UPSERT_CARD -> remote.upsertCard(card)
                    PendingOperationEntity.UPDATE_LIMIT -> remote.updateLimit(card.id, card.limit)
                    PendingOperationEntity.UPDATE_BLOCK_STATUS ->
                        remote.setCardBlockStatus(card.id, card.blockStatus)
                }
            }
            if (result.isSuccess) local.markSynced(operation.operationId)
            else return
        }
    }

    private fun userIdFlow(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun requireUid() = auth.currentUser?.uid
        ?: throw IllegalStateException("Faça login para acessar os cartões.")
}
