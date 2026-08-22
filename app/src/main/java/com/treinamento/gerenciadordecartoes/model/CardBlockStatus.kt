package com.treinamento.gerenciadordecartoes.model

enum class CardBlockStatus(
    val firebaseValue: String,
    val displayName: String,
    val shortDisplayName: String,
) {
    ACTIVE("ACTIVE", "Ativo", "Ativo"),
    TEMPORARY_BLOCKED("TEMPORARY_BLOCKED", "Bloqueio temporário", "Bloq. temporário"),
    PERMANENTLY_BLOCKED("PERMANENTLY_BLOCKED", "Bloqueio definitivo", "Bloq. definitivo");

    val isBlocked: Boolean get() = this != ACTIVE

    companion object {
        fun fromFirebase(value: String?, legacyIsBlocked: Boolean = false): CardBlockStatus =
            entries.firstOrNull { it.firebaseValue == value }
                ?: if (legacyIsBlocked) TEMPORARY_BLOCKED else ACTIVE
    }
}
