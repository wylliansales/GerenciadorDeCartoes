package com.treinamento.gerenciadordecartoes.data.local

import androidx.room.Entity
import com.treinamento.gerenciadordecartoes.model.Purchase

@Entity(tableName = "purchases", primaryKeys = ["userId", "id"])
data class PurchaseLocalEntity(
    val userId: String,
    val id: String,
    val cardId: String,
    val merchant: String,
    val date: String,
    val amount: Double,
    val category: String,
) {
    fun toModel() = Purchase(id, cardId, merchant, date, amount, category)

    companion object {
        fun fromModel(userId: String, purchase: Purchase) = PurchaseLocalEntity(
            userId, purchase.id, purchase.cardId, purchase.merchant,
            purchase.date, purchase.amount, purchase.category,
        )
    }
}
