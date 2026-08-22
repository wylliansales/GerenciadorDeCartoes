package com.treinamento.gerenciadordecartoes.model

data class Card(
    val id: String,
    val holderName: String,
    val lastFourDigits: String,
    val brand: String,
    val limit: Double,
    val usedLimit: Double,
    val dueDay: Int,
    val blockStatus: CardBlockStatus = CardBlockStatus.ACTIVE,
) {
    val availableLimit: Double get() = limit - usedLimit
    val isBlocked: Boolean get() = blockStatus.isBlocked
}
