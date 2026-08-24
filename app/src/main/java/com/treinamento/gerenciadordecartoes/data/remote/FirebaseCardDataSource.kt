package com.treinamento.gerenciadordecartoes.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.treinamento.gerenciadordecartoes.model.Card
import com.treinamento.gerenciadordecartoes.model.CardRequest
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus
import com.treinamento.gerenciadordecartoes.model.Purchase
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseCardDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    fun observeCards(): Flow<List<Card>> = callbackFlow {
        var cardListener: ListenerRegistration? = null
        val authListener = FirebaseAuth.AuthStateListener { currentAuth ->
            cardListener?.remove()
            val uid = currentAuth.currentUser?.uid
            if (uid == null) {
                trySend(emptyList())
                return@AuthStateListener
            }

            cardListener = cards(uid).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val result = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toObject(FirestoreCardEntity::class.java)?.toModel(document.id)
                }
                trySend(result)
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose {
            cardListener?.remove()
            auth.removeAuthStateListener(authListener)
        }
    }

    fun observePurchases(cardId: String): Flow<List<Purchase>> = callbackFlow {
        var purchaseListener: ListenerRegistration? = null
        val authListener = FirebaseAuth.AuthStateListener { currentAuth ->
            purchaseListener?.remove()
            val uid = currentAuth.currentUser?.uid
            if (uid == null) {
                trySend(emptyList())
                return@AuthStateListener
            }
            purchaseListener = cards(uid).document(cardId).collection(PURCHASES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val result = snapshot?.documents.orEmpty().mapNotNull { document ->
                        document.toObject(FirestorePurchaseEntity::class.java)
                            ?.toModel(document.id, cardId)
                    }
                    trySend(result)
                }
        }
        auth.addAuthStateListener(authListener)
        awaitClose {
            purchaseListener?.remove()
            auth.removeAuthStateListener(authListener)
        }
    }

    suspend fun requestCard(request: CardRequest) {
        val uid = requireUid()
        val document = cards(uid).document()
        val digits = request.cardNumber.filter(Char::isDigit)
        val entity = FirestoreCardEntity(
            holderName = request.holderName,
            lastFourDigits = digits.takeLast(4),
            brand = request.cardName,
            limit = request.requestedLimit,
            dueDay = 10,
            cardNumber = digits,
            securityCode = request.securityCode,
            expirationDate = request.expirationDate,
        )
        awaitTask { done -> document.set(entity).addOnCompleteListener { done(it.exception) } }
    }

    suspend fun upsertCard(card: Card) {
        val uid = requireUid()
        awaitTask { done ->
            cards(uid).document(card.id).set(FirestoreCardEntity.fromModel(card))
                .addOnCompleteListener { done(it.exception) }
        }
    }

    suspend fun upsertPurchase(purchase: Purchase) {
        val uid = requireUid()
        val document = cards(uid).document(purchase.cardId)
            .collection(PURCHASES).document(purchase.id)
        awaitTask { done ->
            document.set(FirestorePurchaseEntity.fromModel(purchase))
                .addOnCompleteListener { done(it.exception) }
        }
    }

    suspend fun setCardBlockStatus(cardId: String, status: CardBlockStatus) {
        val uid = requireUid()
        awaitTask { done ->
            cards(uid).document(cardId).update(
                mapOf(
                    "blockStatus" to status.firebaseValue,
                    "isBlocked" to status.isBlocked,
                ),
            )
                .addOnCompleteListener { done(it.exception) }
        }
    }

    suspend fun updateLimit(cardId: String, newLimit: Double) {
        require(newLimit > 0) { "O limite deve ser maior que zero." }
        val uid = requireUid()
        awaitTask { done ->
            cards(uid).document(cardId).update("limit", newLimit)
                .addOnCompleteListener { done(it.exception) }
        }
    }

    private fun cards(uid: String) = firestore.collection(USERS).document(uid).collection(CARDS)

    private fun requireUid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("Faça login para acessar os cartões.")

    private suspend fun awaitTask(start: ((Throwable?) -> Unit) -> Unit) =
        suspendCoroutine { continuation ->
            start { error ->
                if (error == null) continuation.resume(Unit)
                else continuation.resumeWithException(error)
            }
        }

    private companion object {
        const val USERS = "users"
        const val CARDS = "cards"
        const val PURCHASES = "purchases"
    }
}
