package com.treinamento.gerenciadordecartoes.data.remote

import com.treinamento.gerenciadordecartoes.model.Purchase

/** Formato persistido em users/{uid}/cards/{cardId}/purchases/{purchaseId}. */
data class FirestorePurchaseEntity(
    val merchant: String = "",
    val date: String = "",
    val amount: Double = 0.0,
    val category: String = "",
) {
    fun toModel(id: String, cardId: String) = Purchase(
        id = id,
        cardId = cardId,
        merchant = merchant,
        date = date,
        amount = amount,
        category = category,
    )

    companion object {
        fun fromModel(purchase: Purchase) = FirestorePurchaseEntity(
            merchant = purchase.merchant,
            date = purchase.date,
            amount = purchase.amount,
            category = purchase.category,
        )
    }
}
