package com.treinamento.gerenciadordecartoes.repository

import com.treinamento.gerenciadordecartoes.data.remote.FirebaseCardDataSource
import com.treinamento.gerenciadordecartoes.model.CardRequest
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus

class FirebaseCardRepository(
    private val remoteDataSource: FirebaseCardDataSource = FirebaseCardDataSource(),
) : CardRepository {
    override fun observeCards() = remoteDataSource.observeCards()

    override fun observePurchases(cardId: String) = remoteDataSource.observePurchases(cardId)

    override suspend fun requestCard(request: CardRequest) =
        runCatching { remoteDataSource.requestCard(request) }

    override suspend fun setCardBlockStatus(cardId: String, status: CardBlockStatus) =
        runCatching { remoteDataSource.setCardBlockStatus(cardId, status) }

    override suspend fun updateLimit(cardId: String, newLimit: Double) =
        runCatching { remoteDataSource.updateLimit(cardId, newLimit) }
}
