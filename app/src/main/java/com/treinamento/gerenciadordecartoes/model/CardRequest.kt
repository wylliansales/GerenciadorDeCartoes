package com.treinamento.gerenciadordecartoes.model

data class CardRequest(
    val holderName: String,
    val cardName: String,
    val cardNumber: String,
    val securityCode: String,
    val expirationDate: String,
    val requestedLimit: Double,
)
