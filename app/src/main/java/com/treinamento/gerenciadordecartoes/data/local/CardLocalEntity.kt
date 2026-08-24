package com.treinamento.gerenciadordecartoes.data.local

import androidx.room.Entity
import com.treinamento.gerenciadordecartoes.model.Card
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus

@Entity(tableName = "cards", primaryKeys = ["userId", "id"])
data class CardLocalEntity(
    val userId: String,
    val id: String,
    val holderName: String,
    val lastFourDigits: String,
    val brand: String,
    val limit: Double,
    val usedLimit: Double,
    val dueDay: Int,
    val blockStatus: String,
    val cardNumber: String = lastFourDigits,
    val securityCode: String = "",
    val expirationDate: String = "",
) {
    fun toModel() = Card(
        id, holderName, lastFourDigits, brand, limit, usedLimit, dueDay,
        CardBlockStatus.fromFirebase(blockStatus),
        cardNumber,
        securityCode,
        expirationDate,
    )

    companion object {
        fun fromModel(userId: String, card: Card) = CardLocalEntity(
            userId, card.id, card.holderName, card.lastFourDigits, card.brand,
            card.limit, card.usedLimit, card.dueDay, card.blockStatus.firebaseValue,
            card.cardNumber, card.securityCode, card.expirationDate,
        )
    }
}
