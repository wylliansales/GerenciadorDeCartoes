package com.treinamento.gerenciadordecartoes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CardLocalEntity::class, PurchaseLocalEntity::class, PendingOperationEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile private var instance: CardDatabase? = null

        fun getInstance(context: Context): CardDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CardDatabase::class.java,
                "cardflow.db",
            ).build().also { instance = it }
        }
    }
}
