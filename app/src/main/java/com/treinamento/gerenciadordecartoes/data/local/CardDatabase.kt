package com.treinamento.gerenciadordecartoes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CardLocalEntity::class, PurchaseLocalEntity::class, PendingOperationEntity::class],
    version = 2,
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
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cards ADD COLUMN cardNumber TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cards ADD COLUMN securityCode TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cards ADD COLUMN expirationDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("DROP INDEX IF EXISTS index_pending_operations_userId_type_cardId")
                db.execSQL("ALTER TABLE pending_operations ADD COLUMN resourceId TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pending_operations_userId_type_cardId_resourceId ON pending_operations(userId, type, cardId, resourceId)")
            }
        }
    }
}
