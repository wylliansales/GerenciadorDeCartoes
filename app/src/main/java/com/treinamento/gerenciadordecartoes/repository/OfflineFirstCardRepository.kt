package com.treinamento.gerenciadordecartoes.repository

import com.google.firebase.auth.FirebaseAuth
import com.treinamento.gerenciadordecartoes.data.local.PendingOperationEntity
import com.treinamento.gerenciadordecartoes.data.local.RoomCardDataSource
import com.treinamento.gerenciadordecartoes.data.remote.FirebaseCardDataSource
import com.treinamento.gerenciadordecartoes.model.Card
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus
import com.treinamento.gerenciadordecartoes.model.CardRequest
import com.treinamento.gerenciadordecartoes.model.Purchase
import com.treinamento.gerenciadordecartoes.model.PurchaseRequest
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
        val digits = request.cardNumber.filter(Char::isDigit)
        require(request.holderName.isNotBlank()) { "Informe o nome do cliente." }
        require(request.cardName.isNotBlank()) { "Informe o nome do cartão." }
        require(digits.length in 13..19) { "Informe um número de cartão fake válido." }
        require(request.securityCode.length in 3..4 && request.securityCode.all(Char::isDigit)) {
            "Informe um CVC fake de 3 ou 4 dígitos."
        }
        require(Regex("(0[1-9]|1[0-2])/\\d{2}").matches(request.expirationDate)) {
            "Informe o vencimento no formato MM/AA."
        }
        require(request.requestedLimit > 0) { "Informe um limite maior que zero." }
        val card = Card(
            id = UUID.randomUUID().toString(),
            holderName = request.holderName,
            lastFourDigits = digits.takeLast(4),
            brand = request.cardName,
            limit = request.requestedLimit,
            usedLimit = 0.0,
            dueDay = 10,
            cardNumber = digits,
            securityCode = request.securityCode,
            expirationDate = request.expirationDate,
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

    override suspend fun addPurchase(request: PurchaseRequest): Result<Unit> = runCatching {
        val userId = requireUid()
        require(request.merchant.isNotBlank()) { "Informe onde a compra foi realizada." }
        require(request.date.isNotBlank()) { "Informe a data da compra." }
        require(request.category.isNotBlank()) { "Informe a categoria." }
        require(request.amount > 0) { "Informe um valor maior que zero." }
        val card = local.card(userId, request.cardId)
            ?: throw IllegalArgumentException("Cartão não encontrado.")
        require(!card.isBlocked) { "Desbloqueie o cartão antes de lançar a compra." }
        require(request.amount <= card.availableLimit) { "Limite disponível insuficiente." }

        val purchase = Purchase(
            id = UUID.randomUUID().toString(),
            cardId = request.cardId,
            merchant = request.merchant,
            date = request.date,
            amount = request.amount,
            category = request.category,
        )
        local.addPurchase(userId, purchase)
        local.enqueue(
            userId, PendingOperationEntity.UPSERT_PURCHASE,
            request.cardId, purchase.id,
        )
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
                    PendingOperationEntity.UPSERT_PURCHASE -> {
                        val purchase = local.purchase(userId, operation.resourceId)
                            ?: error("Compra local não encontrada.")
                        remote.upsertPurchase(purchase)
                        remote.upsertCard(card)
                    }
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
