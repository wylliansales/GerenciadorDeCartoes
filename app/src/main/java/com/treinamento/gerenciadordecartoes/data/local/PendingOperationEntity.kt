package com.treinamento.gerenciadordecartoes.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_operations",
    indices = [Index(value = ["userId", "type", "cardId", "resourceId"], unique = true)],
)
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true) val operationId: Long = 0,
    val userId: String,
    val type: String,
    val cardId: String,
    val resourceId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val UPSERT_CARD = "UPSERT_CARD"
        const val UPDATE_LIMIT = "UPDATE_LIMIT"
        const val UPDATE_BLOCK_STATUS = "UPDATE_BLOCK_STATUS"
        const val UPSERT_PURCHASE = "UPSERT_PURCHASE"
    }
}
