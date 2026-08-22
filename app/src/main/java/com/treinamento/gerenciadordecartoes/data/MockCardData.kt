package com.treinamento.gerenciadordecartoes.data

import com.treinamento.gerenciadordecartoes.model.Card
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus
import com.treinamento.gerenciadordecartoes.model.Purchase

/** Carga inicial usada apenas quando o usuário ainda não possui cartões no Firestore. */
object MockCardData {
    val cards = listOf(
        Card("1", "Alex Silva", "4582", "Visa Platinum", 8_000.0, 2_340.75, 12),
        Card(
            "2", "Alex Silva", "9017", "Mastercard Gold", 4_500.0, 890.20, 5,
            CardBlockStatus.TEMPORARY_BLOCKED,
        ),
    )

    val purchases = listOf(
        Purchase("p1", "1", "Supermercado Central", "Hoje, 10:42", 186.90, "Alimentação"),
        Purchase("p2", "1", "StreamPlay", "15 ago", 39.90, "Assinaturas"),
        Purchase("p3", "1", "Posto Avenida", "13 ago", 250.00, "Transporte"),
        Purchase("p4", "2", "Livraria Horizonte", "10 ago", 74.50, "Compras"),
    )
}
