package com.treinamento.gerenciadordecartoes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE userId = :userId ORDER BY id")
    fun observeCards(userId: String): Flow<List<CardLocalEntity>>

    @Query("SELECT * FROM purchases WHERE userId = :userId AND cardId = :cardId ORDER BY id")
    fun observePurchases(userId: String, cardId: String): Flow<List<PurchaseLocalEntity>>

    @Query("SELECT * FROM cards WHERE userId = :userId AND id = :cardId LIMIT 1")
    suspend fun getCard(userId: String, cardId: String): CardLocalEntity?

    @Query("SELECT * FROM purchases WHERE userId = :userId AND id = :purchaseId LIMIT 1")
    suspend fun getPurchase(userId: String, purchaseId: String): PurchaseLocalEntity?

    @Query("SELECT COUNT(*) FROM cards WHERE userId = :userId")
    suspend fun cardCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCards(cards: List<CardLocalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCard(card: CardLocalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPurchases(purchases: List<PurchaseLocalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPurchase(purchase: PurchaseLocalEntity)

    @Query("UPDATE cards SET `limit` = :newLimit WHERE userId = :userId AND id = :cardId")
    suspend fun updateLimit(userId: String, cardId: String, newLimit: Double)

    @Query("UPDATE cards SET blockStatus = :status WHERE userId = :userId AND id = :cardId")
    suspend fun updateBlockStatus(userId: String, cardId: String, status: String)

    @Query("UPDATE cards SET usedLimit = usedLimit + :amount WHERE userId = :userId AND id = :cardId AND blockStatus = 'ACTIVE' AND usedLimit + :amount <= `limit`")
    suspend fun reservePurchaseAmount(userId: String, cardId: String, amount: Double): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(operation: PendingOperationEntity)

    @Query("SELECT * FROM pending_operations WHERE userId = :userId ORDER BY createdAt")
    suspend fun pendingOperations(userId: String): List<PendingOperationEntity>

    @Query("SELECT COUNT(*) FROM pending_operations WHERE userId = :userId AND cardId = :cardId")
    suspend fun pendingCount(userId: String, cardId: String): Int

    @Query("DELETE FROM pending_operations WHERE operationId = :operationId")
    suspend fun deletePending(operationId: Long)

    @Transaction
    suspend fun addPurchase(userId: String, purchase: PurchaseLocalEntity) {
        check(reservePurchaseAmount(userId, purchase.cardId, purchase.amount) == 1) {
            "Compra não autorizada. Verifique o status e o limite disponível do cartão."
        }
        upsertPurchase(purchase)
    }

    @Transaction
    suspend fun seed(
        cards: List<CardLocalEntity>,
        purchases: List<PurchaseLocalEntity>,
    ) {
        upsertCards(cards)
        upsertPurchases(purchases)
    }
}
